package com.bimo.kuetradisionalapp.model

data class KueTradisionalResponse(
    val `data`: KueTradisionalData,
    val meta: Meta,
    val pagination: Pagination,
    val version: Version
)