package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyRecord
import com.example.data.Prayer
import com.example.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.last30DailyRecords.collectAsState()
    val allPrayers by viewModel.allPrayers.collectAsState()
    val nawafil by viewModel.currentNawafil.collectAsState()

    // Base variables
    val activeDate by viewModel.selectedDate.collectAsState()
    val activeRecord = records.find { it.date == activeDate }
    val maxNaflPoints = nawafil.sumOf { it.pointWeight }
    val maxPossibleToday = 50 + maxNaflPoints

    val streak = viewModel.getStreak(records)
    val bestDayPoints = viewModel.getBestDayPoints(records)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "الإحصائيات",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Two Top Cards (Row of 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Today's Points circular chart
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .testTag("stats_points_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(90.dp)
                        ) {
                            val currentTodayPoints = activeRecord?.totalPoints ?: 0
                            val fraction = if (maxPossibleToday > 0) {
                                (currentTodayPoints.toFloat() / maxPossibleToday.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            CircularProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentTodayPoints",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 22.sp
                                    )
                                )
                                Text(
                                    text = "من $maxPossibleToday",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "نقاط اليوم",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Card 2: Streak / Best Day
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .testTag("stats_streak_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        // Diamond style icon or flame
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔶",
                                fontSize = 18.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$streak يوم متتالي",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "الأفضل: $streak",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Text(
                            text = "أعلى يوم: $bestDayPoints نقطة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Weekly Bar Chart (Canvas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_weekly_chart_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "نقاط الأسبوع (٪)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyBarChart(records = records, maxPoints = maxPossibleToday)
                }
            }
        }

        // Prayer Breakdown over Last 30 Days
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_prayer_breakdown_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "تفصيل الصلوات (آخر ٣٠ يوم)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    prayerNames.forEach { pArabic ->
                        val pData = allPrayers.filter { it.prayerName.equals(pArabic, ignoreCase = true) }
                        PrayerBreakdownRow(
                            prayerName = pArabic,
                            prayers = pData
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // mini legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF34D399), label = "جماعة")
                        Spacer(modifier = Modifier.width(12.dp))
                        LegendItem(color = Color(0xFF10B981), label = "حاضر")
                        Spacer(modifier = Modifier.width(12.dp))
                        LegendItem(color = Color(0xFFF59E0B), label = "قضاء")
                        Spacer(modifier = Modifier.width(12.dp))
                        LegendItem(color = Color(0xFFEF4444), label = "لم أصلِ")
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyBarChart(records: List<DailyRecord>, maxPoints: Int) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    // Generate last 7 days keys
    val last7Days = rememberLast7DaysKeys()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        last7Days.forEach { (dateKey, labelName) ->
            val matchingRecord = records.find { it.date == dateKey }
            val totalPts = matchingRecord?.totalPoints ?: 0
            val fraction = if (maxPoints > 0) (totalPts.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f) else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Bar container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(trackColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(10.dp))
                            .background(barColor)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Short Arabic weekday
                Text(
                    text = labelName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PrayerBreakdownRow(prayerName: String, prayers: List<Prayer>) {
    val prayerArabic = getPrayerArabicName(prayerName)
    val totalCount = prayers.size.coerceAtLeast(1)

    val groupCount = prayers.count { it.status == 4 }
    val presentCount = prayers.count { it.status == 3 }
    val qadaaCount = prayers.count { it.status == 2 }
    val missedCount = prayers.count { it.status == 1 }

    val groupPct = (groupCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val presentPct = (presentCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val qadaaPct = (qadaaCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val missedPct = (missedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val emptyPct = (1f - (groupPct + presentPct + qadaaPct + missedPct)).coerceAtLeast(0f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Label (Fajr, etc.)
        Text(
            text = prayerArabic,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.width(60.dp)
        )

        // Progress breakdown bar (Custom Canvas or segmented Row)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (groupPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(groupPct)
                        .background(Color(0xFF34D399))
                )
            }
            if (presentPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(presentPct)
                        .background(Color(0xFF10B981))
                )
            }
            if (qadaaPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(qadaaPct)
                        .background(Color(0xFFF59E0B))
                )
            }
            if (missedPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(missedPct)
                        .background(Color(0xFFEF4444))
                )
            }
            if (emptyPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(emptyPct)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Success rate percentage (Present + Group)
        val successRate = (((groupCount + presentCount).toFloat() / totalCount.toFloat()) * 100).toInt()
        Text(
            text = "+$successRate٪",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

// Helper to calculate last 7 days logical date keys and Arabic short labels
@Composable
fun rememberLast7DaysKeys(): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfDay = SimpleDateFormat("EEE", Locale("ar"))

    val cal = Calendar.getInstance()
    // Align with Islamic 3 AM logical reset window:
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    if (hour < 3) {
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }

    // Go back 6 days + today
    cal.add(Calendar.DAY_OF_YEAR, -6)

    for (i in 0..6) {
        val key = sdfKey.format(cal.time)
        val label = sdfDay.format(cal.time)
        // Clean trailing dots if any, or map cleanly
        val formattedLabel = label.replace(".", "").take(5)
        list.add(Pair(key, formattedLabel))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}
