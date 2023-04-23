package com.iomt.android.compose.components.charts

import android.content.Context
import android.util.Log

import com.iomt.android.entities.Characteristic
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.beforeNow
import com.iomt.android.utils.toNumberOrNull

import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime

/**
 * Data loader for [LineChart]
 */
class ChartDataLoader(context: Context) {
    private val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)
    private val recordRepository = RecordRepository(context)

    /**
     * @param deviceMac MAC address of Bluetooth LE device
     * @param characteristic [Characteristic] to display
     * @param periodDuration data periodicity
     * @return [ChartEntryModelProducer] with [characteristic] values of device with [deviceMac] and periodicity of [periodDuration]
     */
    suspend fun loadData(
        deviceMac: String,
        characteristic: Characteristic,
        periodDuration: Duration = 1.minutes,
    ): ChartEntryModelProducer {
        val linkId = requireNotNull(
            deviceCharacteristicLinkRepository
                .getLinkByDeviceMac(deviceMac)
                ?.getLinkIdByCharacteristicName(characteristic.name),
        )
        return recordRepository
            .getPeriodicalRecordsByLinkIdNotOlderThen(linkId, LocalDateTime.beforeNow(24.hours), periodDuration)
            .also { Log.d(loggerTag, "Fetched ${it.count()} records") }
            .mapNotNull { record -> record.value.toNumberOrNull()?.let { it to record.timestamp.time } }
            .mapIndexed { index, (number, time) -> Entry(time, index.toFloat(), number.toFloat()) }
            .let { ChartEntryModelProducer(it) }
    }

    /**
     * @param periodDuration data periodicity
     * @return [AxisValueFormatter] for dates for bottom axis
     */
    fun axisValueFormatterForDate(periodDuration: Duration = 1.minutes): AxisValueFormatter<AxisPosition.Horizontal.Bottom> = AxisValueFormatter { value, chartValues ->
        (chartValues.chartEntryModel.entries.firstOrNull()?.getOrNull(value.toInt()) as? Entry)
            ?.localTime
            ?.run {
                if (periodDuration.inWholeMinutes == 0L) {
                    "$hour:$minute:$second"
                } else if (periodDuration.inWholeHours == 0L) {
                    "$hour:$minute"
                } else {
                    hour.toString()
                }
            }
            .orEmpty()
    }
    companion object {
        private val loggerTag = ChartDataLoader::class.java.simpleName
    }
}
