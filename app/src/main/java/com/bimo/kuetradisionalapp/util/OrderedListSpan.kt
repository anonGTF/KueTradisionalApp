package com.bimo.kuetradisionalapp.util

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class OrderedListSpan(
    private val width: Int,
    private val leadingText: String
) : LeadingMarginSpan {

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout
    ) {
        // Check if we're at the start of the span
        val spanStart = (text as Spanned).getSpanStart(this)
        val isFirstCharacter = spanStart == start

        // If so, draw the text in the leading span
        if (isFirstCharacter) {
            canvas.drawText(leadingText, x.toFloat(), baseline.toFloat(), paint)
        }
    }

    override fun getLeadingMargin(first: Boolean): Int = width
}