package com.bimo.kuetradisionalapp.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.MappedByteBuffer
import java.util.*

abstract class Classifier protected constructor(
    activity: Activity?,
    device: Device?,
    numThreads: Int
) {

    enum class Device {
        CPU, GPU
    }

    private var tfliteModel: MappedByteBuffer?
    val imageSizeX: Int
    val imageSizeY: Int

    private var gpuDelegate: GpuDelegate? = null

    protected var tflite: Interpreter?
    private val tfliteOptions = Interpreter.Options()
    private val labels: List<String>
    private var inputImageBuffer: TensorImage
    private val outputProbabilityBuffer: TensorBuffer
    private val probabilityProcessor: TensorProcessor

    class Recognition(
        val id: String?,
        val title: String?,
        val confidence: Float?,
        private var location: RectF?
    ) {

        fun getLocation(): RectF {
            return RectF(location)
        }

        fun setLocation(location: RectF?) {
            this.location = location
        }

        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }
            if (title != null) {
                resultString += "$title "
            }
            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f)
            }
            if (location != null) {
                resultString += location.toString() + " "
            }
            return resultString.trim { it <= ' ' }
        }
    }

    fun recognizeImage(bitmap: Bitmap, sensorOrientation: Int): List<Recognition> {
        Trace.beginSection("recognizeImage")
        Trace.beginSection("loadImage")
        val startTimeForLoadImage = SystemClock.uptimeMillis()
        inputImageBuffer = loadImage(bitmap, sensorOrientation)
        val endTimeForLoadImage = SystemClock.uptimeMillis()
        Trace.endSection()

        Trace.beginSection("runInference")
        val startTimeForReference = SystemClock.uptimeMillis()
        tflite!!.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())
        val endTimeForReference = SystemClock.uptimeMillis()
        Trace.endSection()

        val labeledProbability =
            TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                .mapWithFloatValue
        Trace.endSection()
        return getTopKProbability(labeledProbability)
    }

    fun close() {
        if (tflite != null) {
            tflite!!.close()
            tflite = null
        }
        if (gpuDelegate != null) {
            gpuDelegate!!.close()
            gpuDelegate = null
        }
        tfliteModel = null
    }

    private fun loadImage(bitmap: Bitmap, sensorOrientation: Int): TensorImage {
        inputImageBuffer.load(bitmap)
        val cropSize = Math.min(bitmap.width, bitmap.height)
        val numRoration = sensorOrientation / 90
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeMethod.NEAREST_NEIGHBOR))
            .add(Rot90Op(numRoration))
            .add(preprocessNormalizeOp)
            .build()
        return imageProcessor.process(inputImageBuffer)
    }

    protected abstract val modelPath: String?
    protected abstract val labelPath: String?
    protected abstract val preprocessNormalizeOp: TensorOperator?
    protected abstract val postprocessNormalizeOp: TensorOperator?

    companion object {
        private const val MAX_RESULTS = 3
        @Throws(IOException::class)
        fun create(
            activity: Activity?,
            device: Device?,
            numThreads: Int
        ): Classifier {
            return ClassifierFloatMobileNet(activity, device, numThreads)
        }

        private fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition> {
            val pq = PriorityQueue(
                MAX_RESULTS,
                object : Comparator<Recognition?> {
                    override fun compare(lhs: Recognition?, rhs: Recognition?): Int {
                        if (rhs != null && lhs != null) {
                            return (rhs.confidence!!).compareTo(lhs.confidence!!)
                        }
                        return 0
                    }
                })
            for ((key, value) in labelProb) {
                pq.add(Recognition("" + key, key, value, null))
            }
            val recognitions = ArrayList<Recognition>()
            val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
            for (i in 0 until recognitionsSize) {
                pq.poll()?.let { recognitions.add(it) }
            }
            return recognitions
        }
    }

    init {
        tfliteModel = FileUtil.loadMappedFile(activity!!, modelPath!!)
        when (device) {
            Device.GPU -> {
                gpuDelegate = GpuDelegate()
                tfliteOptions.addDelegate(gpuDelegate)
            }
            Device.CPU -> {
            }
        }
        tfliteOptions.setNumThreads(numThreads)
        tflite = Interpreter(tfliteModel!!, tfliteOptions)
        labels = FileUtil.loadLabels(activity, labelPath!!)
        val imageTensorIndex = 0
        val imageShape = tflite!!.getInputTensor(imageTensorIndex).shape() // {1, height, width, 3}
        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType = tflite!!.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape =
            tflite!!.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}
        val probabilityDataType = tflite!!.getOutputTensor(probabilityTensorIndex).dataType()
        inputImageBuffer = TensorImage(imageDataType)
        outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        probabilityProcessor = TensorProcessor.Builder().add(postprocessNormalizeOp).build()
    }
}