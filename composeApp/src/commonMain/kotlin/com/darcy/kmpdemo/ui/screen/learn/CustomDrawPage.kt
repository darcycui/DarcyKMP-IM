package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darcy.kmpdemo.bean.ui.HistogramBean
import com.darcy.kmpdemo.bean.ui.PieChartBean
import com.darcy.kmpdemo.ui.screen.learn.repository.ChartsRepository

@Preview (
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ShowCustomDrawPage(
    histogramBean: HistogramBean = ChartsRepository().getHistogramBean(),
    pieChartBean: PieChartBean = ChartsRepository().getPieChartBean(),
) {
    Column {
//    DrawFront()
//    DrawBehind()
//    DrawWithCache()
        DrawHistogram(histogramBean, Modifier.weight(2f))
        DrawPieChart(pieChartBean, Modifier.weight(1f))
    }
}

@Composable
fun DrawPieChart(
    pieChartBean: PieChartBean,
    modifier: Modifier
) {
    Column(modifier.padding(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            pieChartBean.items.forEach { item ->
                drawArc(
                    color = item.color,
                    topLeft = item.topLeft,
                    size = Size(width = item.width.toPx(), height = item.height.toPx()),
                    useCenter = true,
                    startAngle = item.startAngle,
                    sweepAngle = item.sweepAngle,
                )
            }
            drawCircle(
                color = Color.White,
                center = Offset(x = 254.dp.toPx() / 2, y = 254.dp.toPx() / 2),
                radius = 47.dp.toPx(),
            )
        }
    }
}

@Composable
fun DrawHistogram(
    histogramBean: HistogramBean,
    modifier: Modifier
) {
    val nameMeasurer = rememberTextMeasurer()
    val numberMeasurer = rememberTextMeasurer()
    val xTextMeasurer = rememberTextMeasurer()

    Column(modifier.padding(8.dp)) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height - 40.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(10.dp.toPx(), 8.dp.toPx()),
                phase = 0f
            )
            // 横坐标轴
            drawLine(
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                color = Color(0xFFDBDBDB),
                strokeWidth = 1.dp.toPx(),
            )
            // 纵坐标轴
            drawLine(
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = height),
                color = Color(0xFFDBDBDB),
                strokeWidth = 1.dp.toPx(),
            )
            // 横坐标轴刻值
            val xDivisionCount = histogramBean.xAxisBean.divisionCount
            val xDivisionPeriod = histogramBean.xAxisBean.divisionPeriod
            val innerWidth = width - 30
            repeat(xDivisionCount + 1) { index ->
                val x = innerWidth / xDivisionCount * index - 4.dp.toPx()
                val y = height + 4.dp.toPx()
                val text = index * xDivisionPeriod
                // 画文本
                drawText(
                    textMeasurer = xTextMeasurer,
                    text = if (histogramBean.xAxisBean.showDivisionTextFloatDot) text.toString() else text.toInt()
                        .toString(),
                    style = TextStyle(
                        color = Color(0xFF2E3344),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    topLeft = Offset(x = x, y = y),
                )
            }
            // 横坐标轴刻度 虚线
            repeat(xDivisionCount) {
                val x = innerWidth / xDivisionCount * (it + 1)
                val y = height

                drawLine(
                    start = Offset(x = x, y = 0f),
                    end = Offset(x = x, y = y),
                    color = Color(0xFFDBDBDB),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            val yDivisionCount = histogramBean.yAxisBean.divisionCount
            histogramBean.items.forEachIndexed { index, item ->
                val itemWidth = innerWidth * item.count / xDivisionPeriod / xDivisionCount
                // 画矩形
                drawRect(
                    brush = Brush.linearGradient(item.colors),
                    topLeft = Offset(x = 0f, y = height * item.yPercent),
                    size = Size(width = itemWidth - 25.dp.toPx(), height = 50.dp.toPx())
                )
                // 画半圆
                val arcLeft = itemWidth - 50.dp.toPx()
                drawArc(
                    brush = Brush.linearGradient(item.colors),
                    // 计算横坐标 x = 400 - 50/2
                    topLeft = Offset(x = arcLeft - 1, y = height * item.yPercent),
                    size = Size(width = 50.dp.toPx(), height = 50.dp.toPx()),
                    useCenter = false,
                    startAngle = -90f,
                    sweepAngle = 180f
                )

                // 画名称
                drawText(
                    textMeasurer = nameMeasurer,
                    text = item.text,
                    style = TextStyle(
                        color = Color(0xFF333333),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    topLeft = Offset(x = 4.dp.toPx(), y = height * item.yPercent - 30.dp.toPx()),
                )

                // 画数字
                val numberLeft = itemWidth + 8.dp.toPx()
                drawText(
                    textMeasurer = numberMeasurer,
                    text = if (item.showNumberFloatDot) item.count.toString() else item.count.toInt()
                        .toString(),
                    style = TextStyle(
                        color = Color(0xFF333333),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    topLeft = Offset(x = numberLeft, y = height * item.yPercent),
                )
            }
        }
    }
}

@Composable
fun DrawWithCache() {
    Text(
        text = "DrawWithCache CustomBrush",
        modifier = Modifier
            .drawWithCache {
                val brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF9E82F0),
                        Color(0xFF42A5F5)
                    )
                )
                onDrawBehind {
                    drawRoundRect(
                        brush = brush,
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }
            .padding(6.dp)
    )
}

@Composable
fun DrawBehind() {
    Text(
        text = "DrawBehind DefaultBrush",
        modifier = Modifier.drawBehind {
            drawRoundRect(
                Color(0xFFBBAAEE),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
        }.padding(6.dp)
    )
}

@Composable
fun DrawFront() {
    var pointerOffset by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput("dragging") {
                detectDragGestures { change, dragAmount ->
                    pointerOffset += dragAmount
                }
            }
            .onSizeChanged {
                pointerOffset = Offset(it.width / 2f, it.height / 2f)
            }
            .drawWithContent {
                drawContent()
                // draws a fully black area with a small keyhole at pointerOffset that’ll show part of the UI.
                drawRect(
                    Brush.radialGradient(
                        listOf(Color.Transparent, Color.Black),
                        center = pointerOffset,
                        radius = 100.dp.toPx(),
                    )
                )
            }
    ) {
        LazyColumn {
            items(100) {
                Text("Hello everyone! This is a item of list. This is a item of list, the index is:$it")
            }
        }
    }
}