/**
 * File containing line chart
 */

package com.iomt.android.compose.components.charts

import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.entities.Characteristic
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.beforeNow
import com.iomt.android.utils.toNumberOrNull

import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.LocalDateTime

/**
 * @param deviceMac MAC address of Bluetooth LE device
 * @param characteristic [Characteristic] to display
 * @param expanded flag that defines if the chart is shown or not
 */
@Composable
fun LineChart(deviceMac: String, characteristic: Characteristic, expanded: Boolean) {
    val context = LocalContext.current
    val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)
    val recordRepository = RecordRepository(context)
    var chartEntryModelProducer by remember { mutableStateOf<ChartEntryModelProducer?>(null) }
    var axisValueFormatter by remember { mutableStateOf<AxisValueFormatter<AxisPosition.Horizontal.Bottom>?>(null) }

    LaunchedEffect(expanded) {
        if (expanded) {
            val linkId = requireNotNull(
                deviceCharacteristicLinkRepository
                    .getLinkByDeviceMac(deviceMac)
                    ?.getLinkIdByCharacteristicName(characteristic.name),
            )
            chartEntryModelProducer = recordRepository.getRecordsByLinkIdNotOlderThen(linkId, LocalDateTime.beforeNow(24.hours))
                .mapNotNull { record -> record.value.toNumberOrNull()?.let { it to record.timestamp.time } }
                .mapIndexed { index, (number, time) -> Entry(time, index.toFloat(), number.toFloat()) }
                .let { ChartEntryModelProducer(it) }
            axisValueFormatter = AxisValueFormatter { value, chartValues ->
                (chartValues.chartEntryModel.entries.firstOrNull()?.getOrNull(value.toInt()) as? Entry)
                    ?.localTime
                    ?.run { " $hour:$minute:$second" }
                    .orEmpty()
            }
        } else {
            chartEntryModelProducer = null
        }
    }

    val fallback: @Composable () -> Unit = { Text("No information") }
    val marker = rememberMarker()
    val startAxisLabelComponent = textComponent(
        background = shapeComponent(Shapes.pillShape, color = colorScheme.surfaceVariant),
        color = colorScheme.onSurfaceVariant,
        lineCount = 1,
        typeface = Typeface.MONOSPACE,
        margins = MutableDimensions(5f, 5f),
    )
    Column(Modifier.padding(bottom = 5.dp)) {
        chartEntryModelProducer?.let { modelProducer ->
            axisValueFormatter?.let { valueFormatter ->
                Chart(
                    chart = lineChart(),
                    chartModelProducer = modelProducer,
                    startAxis = startAxis(maxLabelCount = 20, title = characteristic.prettyName, titleComponent = startAxisLabelComponent),
                    bottomAxis = bottomAxis(valueFormatter = valueFormatter, labelRotationDegrees = 270f),
                    isZoomEnabled = true,
                    marker = marker,
                )
            } ?: fallback
        } ?: fallback
    }
}
