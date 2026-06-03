package com.darcy.kmpdemo.ui.screen.learn.repository

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.darcy.kmpdemo.bean.ui.ArcRegion
import com.darcy.kmpdemo.bean.ui.ArcRegionInfo
import com.darcy.kmpdemo.bean.ui.AxisBean
import com.darcy.kmpdemo.bean.ui.HistogramBean
import com.darcy.kmpdemo.bean.ui.HistogramInfo
import com.darcy.kmpdemo.bean.ui.HistogramItemBean
import com.darcy.kmpdemo.bean.ui.PieChartBean
import com.darcy.kmpdemo.repository.IRepository
import com.darcy.kmpdemo.ui.colors.AppColors

class ChartsRepository : IRepository {
    val histogramColorList = listOf(
        listOf(AppColors.color_109865, AppColors.color_36cb93),
        listOf(AppColors.color_fb784a, AppColors.color_ffcb5f),
    )
    val histogramYPercentList = listOf(0.3f, 0.7f)
    val histogramTestList = listOf(
        HistogramInfo(
            text = "应用市场访问量",
            count = 82
        ),
        HistogramInfo(
            text = "下载数量",
            count = 34
        )
    ).mapIndexed { index, item ->
        HistogramInfo(
            text = item.text,
            count = item.count,
            colors = histogramColorList[index % histogramColorList.size],
            yPercent = histogramYPercentList[index % histogramYPercentList.size]
        )
    }

    fun getHistogramBean(histogramInfoList: List<HistogramInfo> = histogramTestList): HistogramBean {
        val histogramBean = HistogramBean(
            items = histogramInfoList.map { item ->
                HistogramItemBean(
                    text = item.text,
                    count = item.count,
                    colors = item.colors,
                    yPercent = item.yPercent
                )
            },
            xAxisBean = AxisBean(
                divisionCount = 12,
                divisionPeriod = 10f,
                showDivisionLine = true,
                showDivisionText = true,
            ),

            yAxisBean = AxisBean(
                divisionCount = 5,
                divisionPeriod = 10f,
                showDivisionLine = false,
                showDivisionText = false,
            )
        )
        return histogramBean
    }

    val arcRegionColorList = listOf(
        AppColors.color_f9e363,
        AppColors.color_8cd37a,
        AppColors.color_3b6f68,
        AppColors.color_efab4a,
        AppColors.color_3ac889,
        AppColors.color_9e82f0,
        AppColors.color_42a5f5,
        AppColors.color_f06292,
        AppColors.color_f9a825,
        AppColors.color_4caf50
    )

    val arcRegionTestList = listOf(
        ArcRegionInfo(
            text = "分类1",
            count = 1,
        ),
        ArcRegionInfo(
            text = "分类2",
            count = 2
        ),
        ArcRegionInfo(
            text = "分类3",
            count = 3
        ),
        ArcRegionInfo(
            text = "分类4",
            count = 4
        ),
        ArcRegionInfo(
            text = "分类5",
            count = 5
        )
    ).mapIndexed { index, item ->
        ArcRegionInfo(
            text = item.text,
            count = item.count,
            color = arcRegionColorList[index % arcRegionColorList.size]
        )

    }

    fun getPieChartBean(arcRegionInfoList: List<ArcRegionInfo> = arcRegionTestList): PieChartBean {
        // 计算总数
        val totalCount = arcRegionInfoList.sumOf { it.count.toDouble() }
        // 按 count 从大到小排序
        val sortedList = arcRegionInfoList.sortedByDescending { it.count }
        val startDegreeOffset = 120f
        var currentStartAngle = 0f
        val pieChartBean = PieChartBean(
            items = sortedList.map { info ->
                // 计算占比对应的 sweepAngle (360度)
                val percentage = if (totalCount > 0) info.count.toDouble() / totalCount else 0.0
                val sweepAngle = (percentage * 360f).toFloat()
                ArcRegion(
                    startAngle = currentStartAngle - startDegreeOffset,
                    sweepAngle = sweepAngle,
                    color = info.color,
                    topLeft = Offset(x = 0f, y = 0f),
                    width = 254.dp,
                    height = 254.dp,
                    text = info.text,
                    count = info.count
                ).also {
                    currentStartAngle += sweepAngle
                }
            }
        )
        return pieChartBean
    }
}