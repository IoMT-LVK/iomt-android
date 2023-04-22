package com.iomt.android.compose.components.charts

import com.patrykandpatrick.vico.core.entry.ChartEntry
import kotlinx.datetime.LocalTime

/**
 * @property localTime
 * @property x
 * @property y
 */
class Entry(
    val localTime: LocalTime,
    override val x: Float,
    override val y: Float,
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = Entry(localTime, x, y)
}
