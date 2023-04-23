/**
 * File containing line chart
 */

package com.iomt.android.compose.components.charts

import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.entities.Characteristic

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

import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val valuePeriodicityMapper = listOf(
    1.seconds, 10.seconds, 15.seconds, 30.seconds,
    1.minutes,
    10.minutes, 15.minutes, 30.minutes, 1.hours,
)

private val valueLabelMapper = valuePeriodicityMapper.map { it.toString() }

/**
 * @param deviceMac MAC address of Bluetooth LE device
 * @param characteristic [Characteristic] to display
 * @param expanded flag that defines if the chart is shown or not
 */
@Composable
fun LineChart(deviceMac: String, characteristic: Characteristic, expanded: Boolean) {
    val chartDataLoader = ChartDataLoader(LocalContext.current)
    var chartEntryModelProducer by remember { mutableStateOf<ChartEntryModelProducer?>(null) }
    var axisValueFormatter by remember { mutableStateOf<AxisValueFormatter<AxisPosition.Horizontal.Bottom>?>(null) }
    var sliderValue by remember { mutableStateOf(5f) }

    val updateData: suspend () -> Unit = {
        if (expanded) {
            withContext(Dispatchers.IO) {
                chartEntryModelProducer = chartDataLoader.loadData(
                    deviceMac,
                    characteristic,
                    valuePeriodicityMapper[sliderValue.roundToInt()],
                )
                axisValueFormatter = chartDataLoader.axisValueFormatterForDate()
            }
        } else {
            chartEntryModelProducer = null
            axisValueFormatter = null
        }
    }

    LaunchedEffect(expanded) { updateData() }

    val fallback: @Composable () -> Unit = { Text("No information") }
    val marker = rememberMarker()
    val startAxisLabelComponent = textComponent(
        background = shapeComponent(Shapes.pillShape, color = colorScheme.surfaceVariant),
        color = colorScheme.onSurfaceVariant,
        lineCount = 1,
        typeface = Typeface.MONOSPACE,
        margins = MutableDimensions(5f, 5f),
    )
    val scope = rememberCoroutineScope()
    Column(
        Modifier.padding(bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                Divider()
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { scope.launch { updateData() } },
                    steps = valuePeriodicityMapper.size,
                    valueRange = 0f..valuePeriodicityMapper.size.toFloat() - 1,
                )
                Text(valueLabelMapper[sliderValue.roundToInt()])
            } ?: fallback
        } ?: fallback
    }
}
