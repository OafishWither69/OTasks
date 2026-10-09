package com.bulktracker.app.domain

data class BusyBlock(
    val startMold: Int,
    val endMold: Int,
    val label: String,
    val kind: String // "CLASS" | "EVENT" | "GYM"
)
