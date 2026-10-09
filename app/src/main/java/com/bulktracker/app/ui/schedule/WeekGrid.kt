package com.bulktracker.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktracker.app.data.db.ClassEntity
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.SansFamily
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils
import kotlin.math.roundToInt

data class LayoutClass(
    val entity: ClassEntity,
    val laneIndex: Int,
    val totalLanes: Int
)

data class WeekGridClassLayout(
    val entity: ClassEntity,
    val weekday: Int,
    val laneIndex: Int,
    val totalLanes: Int,
    val topYDp: Dp,
    val blockHeightDp: Dp
)

fun layoutDayClasses(classes: List<ClassEntity>): List<LayoutClass> {
    if (classes.isEmpty()) return emptyList()
    val sorted = classes.sortedWith(compareBy({ it.startMold }, { it.endMold }))

    val components = mutableListOf<MutableList<ClassEntity>>()
    for (c in sorted) {
        if (components.isEmpty()) {
            components.add(mutableListOf(c))
        } else {
            val currentComp = components.last()
            val compMaxEnd = currentComp.maxOf { it.endMold }
            // Overlapping if c.startMold < compMaxEnd (touching end == start does NOT overlap)
            if (c.startMold < compMaxEnd) {
                currentComp.add(c)
            } else {
                components.add(mutableListOf(c))
            }
        }
    }

    val result = mutableListOf<LayoutClass>()
    for (comp in components) {
        val laneEndTimes = mutableListOf<Int>()
        val assigned = mutableListOf<Pair<ClassEntity, Int>>()
        for (c in comp) {
            var assignedLane = -1
            for (lane in laneEndTimes.indices) {
                if (laneEndTimes[lane] <= c.startMold) {
                    assignedLane = lane
                    laneEndTimes[lane] = c.endMold
                    break
                }
            }
            if (assignedLane == -1) {
                assignedLane = laneEndTimes.size
                laneEndTimes.add(c.endMold)
            }
            assigned.add(c to assignedLane)
        }
        val totalLanes = maxOf(1, laneEndTimes.size)
        for ((c, lane) in assigned) {
            result.add(LayoutClass(c, lane, totalLanes))
        }
    }
    return result
}

@Composable
fun WeekGrid(
    classes: List<ClassEntity>,
    onClassClick: (ClassEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BulkTrackerTheme.colors

    val gutterWidth = 40.dp
    val hourHeight = 48.dp
    val totalHours = 16 // 7:00 AM (420) to 11:00 PM (1380)
    val totalGridHeight = hourHeight * totalHours // 768.dp

    val hourLabels = remember {
        listOf(
            "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "12 PM",
            "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM",
            "7 PM", "8 PM", "9 PM", "10 PM", "11 PM"
        )
    }
    val dayNames = remember {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    val layoutClasses = remember(classes) {
        val list = mutableListOf<WeekGridClassLayout>()
        for (weekday in 1..7) {
            val dayClasses = classes.filter { it.weekday == weekday }
            val dayLayout = layoutDayClasses(dayClasses)
            for (lc in dayLayout) {
                val c = lc.entity
                val startM = c.startMold.coerceIn(420, 1380)
                val endM = c.endMold.coerceIn(420, 1380)
                if (endM <= startM) continue

                val topY = ((startM - 420) * 48f / 60f).dp
                val blockHeight = ((endM - startM) * 48f / 60f).dp

                list.add(
                    WeekGridClassLayout(
                        entity = c,
                        weekday = weekday,
                        laneIndex = lc.laneIndex,
                        totalLanes = lc.totalLanes,
                        topYDp = topY,
                        blockHeightDp = blockHeight
                    )
                )
            }
        }
        list
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header Row: Mon-Sun
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(gutterWidth))
            dayNames.forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary,
                        fontFamily = SansFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.SpaceXs))

        // Grid Area with static grid cached in drawWithCache
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalGridHeight + 16.dp)
                .drawWithCache {
                    val gutterPx = gutterWidth.toPx()
                    val hourHeightPx = hourHeight.toPx()
                    val lineStrokePx = 1.dp.toPx()
                    val totalGridHeightPx = totalGridHeight.toPx()
                    val borderColor = colors.border
                    val verticalLineColor = colors.border.copy(alpha = 0.5f)

                    onDrawBehind {
                        val columnsAreaWidthPx = size.width - gutterPx
                        val columnWidthPx = columnsAreaWidthPx / 7f

                        // 1dp hour line in border colour across the 7 columns
                        for (i in 0..totalHours) {
                            val y = i * hourHeightPx
                            drawRect(
                                color = borderColor,
                                topLeft = Offset(gutterPx, y),
                                size = Size(columnsAreaWidthPx, lineStrokePx)
                            )
                        }

                        // Vertical subtle lines between columns
                        for (dayIndex in 1..6) {
                            val x = gutterPx + columnWidthPx * dayIndex
                            drawRect(
                                color = verticalLineColor,
                                topLeft = Offset(x, 0f),
                                size = Size(lineStrokePx, totalGridHeightPx)
                            )
                        }
                    }
                }
        ) {
            val availableWidth = maxWidth
            val columnsAreaWidth = availableWidth - gutterWidth
            val columnWidth = columnsAreaWidth / 7

            // Hour labels in left gutter
            for (i in 0..totalHours) {
                if (i < hourLabels.size) {
                    val yOffset = hourHeight * i
                    Text(
                        text = hourLabels[i],
                        fontSize = 12.sp,
                        color = colors.textTertiary,
                        fontFamily = SansFamily,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .offset(x = 0.dp, y = yOffset - 7.dp)
                            .width(gutterWidth - 6.dp)
                    )
                }
            }

            // Render Class blocks
            for (item in layoutClasses) {
                key(item.entity.id) {
                    val c = item.entity
                    val laneWidth = columnWidth / item.totalLanes
                    val blockWidth = (laneWidth - 2.dp).coerceAtLeast(1.dp)
                    val blockHeight = (item.blockHeightDp - 2.dp).coerceAtLeast(18.dp)

                    Box(
                        modifier = Modifier
                            .offset {
                                val colXPx = (gutterWidth + columnWidth * (item.weekday - 1)).roundToPx()
                                val laneWidthPx = (columnWidth / item.totalLanes).toPx()
                                val blockXPx = (colXPx + laneWidthPx * item.laneIndex + 1.dp.toPx()).roundToInt()
                                val topYPx = (item.topYDp + 1.dp).roundToPx()
                                IntOffset(blockXPx, topYPx)
                            }
                            .width(blockWidth)
                            .height(blockHeight)
                            .clip(RoundedCornerShape(Tokens.RadiusSm))
                            .background(colors.border)
                            .clickable { onClassClick(c) }
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    ) {
                        Column {
                            Text(
                                text = c.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = colors.textPrimary,
                                fontFamily = SansFamily
                            )
                            if (item.blockHeightDp >= 28.dp) {
                                Text(
                                    text = DateUtils.formatMold(c.startMold),
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp,
                                    color = colors.textSecondary,
                                    fontFamily = SansFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
