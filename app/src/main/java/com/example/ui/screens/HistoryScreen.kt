package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyRecord
import com.example.data.Prayer
import com.example.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HabitViewModel,
    onDaySelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val records by viewModel.last30DailyRecords.collectAsState()
    val allPrayers by viewModel.allPrayers.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "السجل",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "آخر ٣٠ يوماً",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Legend at the top (RTL friendly)
        LegendSection()

        Spacer(modifier = Modifier.height(16.dp))

        // List of days
        if (records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم تسجيل أي نشاط بعد. ابدأ اليوم بتسجيل صلواتك!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(records) { record ->
                    val dayPrayers = allPrayers.filter { it.date == record.date }
                    HistoryDayCard(
                        record = record,
                        prayers = dayPrayers,
                        onClick = {
                            viewModel.selectDate(record.date)
                            onDaySelected()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LegendSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_legend_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFF10B981), label = "حاضر/جماعة")
            LegendItem(color = Color(0xFFF59E0B), label = "قضاء")
            LegendItem(color = Color(0xFFEF4444), label = "لم أصلِ")
            LegendItem(color = Color(0xFFEAB308), label = "نوافل")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun HistoryDayCard(
    record: DailyRecord,
    prayers: List<Prayer>,
    onClick: () -> Unit
) {
    val (dayName, formattedDate) = getArabicDate(record.date)
    val isToday = record.date == SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_day_card_${record.date}")
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right: Date & Day
            Column {
                if (isToday) {
                    Text(
                        text = "اليوم",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Left: dots and points
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 5 horizontal dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sorting standard prayer order: Fajr, Dhuhr, Asr, Maghrib, Isha
                    val prayerOrder = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    prayerOrder.forEach { prayerName ->
                        val matchingPrayer = prayers.find { it.prayerName.equals(prayerName, ignoreCase = true) }
                        val dotColor = when (matchingPrayer?.status) {
                            1 -> Color(0xFFEF4444) // Missed
                            2 -> Color(0xFFF59E0B) // Qadaa
                            3 -> Color(0xFF10B981) // Present
                            4 -> Color(0xFF34D399) // Jamaah
                            else -> MaterialTheme.colorScheme.surfaceVariant // None / Not logged
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                // Points Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${record.totalPoints} ن",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Open day details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
