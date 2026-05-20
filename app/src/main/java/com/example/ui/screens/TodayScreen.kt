package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyRecord
import com.example.data.Nafl
import com.example.data.Prayer
import com.example.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val record by viewModel.currentDailyRecord.collectAsState()
    val prayers by viewModel.currentPrayers.collectAsState()
    val nawafil by viewModel.currentNawafil.collectAsState()

    val haptic = LocalHapticFeedback.current

    val (arabicDay, arabicFullDate) = getArabicDate(selectedDate)

    // Calculate maximum points and stats
    val completedFaraid = prayers.count { it.status != 0 }
    val completedNawafil = nawafil.count { it.isCompleted }
    val maxNaflPoints = nawafil.sumOf { it.pointWeight }
    val maxPossiblePoints = 50 + maxNaflPoints

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = arabicDay,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = arabicFullDate,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // 2. Main Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_progress_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Points summary
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "من $maxPossiblePoints نقطة",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "Points",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${record?.totalPoints ?: 0} نقطة",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Right: Faraid / Nawafil numbers
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "فريضة $completedFaraid/٥",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "نافلة $completedNawafil/${nawafil.size}",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    val progressFraction = if (maxPossiblePoints > 0) {
                        ((record?.totalPoints ?: 0).toFloat() / maxPossiblePoints.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ابدأ يومك بذكر الله وتتبع صلواتك وسننك للحصول على النقاط والارتقاء في الطاعات.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        // 3. Umar رضي الله عنه Quote Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "« حاسبوا أنفسكم قبل أن تُحاسبوا »",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "عمر بن الخطاب رضي الله عنه",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            }
        }

        // 4. Faraid (Mandatory Prayers) Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الفرائض",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$completedFaraid/٥",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 5. Prayers Cards List
        items(prayers) { prayer ->
            PrayerCard(
                prayer = prayer,
                onStatusSelected = { status ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updatePrayer(prayer, status)
                }
            )
        }

        // 6. Nawafil Section Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "السنن والنوافل",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "$completedNawafil/${nawafil.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 7. Nawafil Items
        if (nawafil.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "لا توجد سنن نشطة حالياً. يمكنك تفعيلها وإضافتها من شاشة الإعدادات.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(nawafil) { nafl ->
                NaflRow(
                    nafl = nafl,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleNafl(nafl)
                    }
                )
            }
        }
    }
}

@Composable
fun PrayerCard(
    prayer: Prayer,
    onStatusSelected: (Int) -> Unit
) {
    val prayerArabicName = getPrayerArabicName(prayer.prayerName)
    val prayerIcon = getPrayerIcon(prayer.prayerName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.prayerName.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = prayerIcon,
                            contentDescription = prayerArabicName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = prayerArabicName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Current indicator text
                val currentStatusText = when (prayer.status) {
                    1 -> "لم يُصلَّ"
                    2 -> "قضاء"
                    3 -> "حاضر"
                    4 -> "جماعة"
                    else -> "لم يُسجّل"
                }

                val indicatorColor = when (prayer.status) {
                    1 -> Color(0xFFEF4444) // Missed
                    2 -> Color(0xFFF59E0B) // Qadaa
                    3 -> Color(0xFF10B981) // Present
                    4 -> Color(0xFF34D399) // Jamaah
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                Text(
                    text = currentStatusText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = indicatorColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    Triple(1, "لم أصلّ\n-2", Color(0xFF331414)),
                    Triple(2, "قضاء\n+2", Color(0xFF332110)),
                    Triple(3, "حاضر\n+5", Color(0xFF102e21)),
                    Triple(4, "جماعة\n+10", Color(0xFF143e2f))
                )

                statuses.forEach { (statusCode, label, defaultBg) ->
                    val isSelected = prayer.status == statusCode
                    val activeBg = when (statusCode) {
                        1 -> Color(0xFFEF4444)
                        2 -> Color(0xFFF59E0B)
                        3 -> Color(0xFF10B981)
                        4 -> Color(0xFF34D399)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeBg else defaultBg.copy(alpha = 0.6f))
                            .clickable { onStatusSelected(statusCode) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                lineHeight = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NaflRow(
    nafl: Nafl,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nafl_row_${nafl.naflName}")
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = nafl.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = nafl.naflName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (nafl.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (nafl.isCompleted) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }

            // Point Weight Label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (nafl.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+${nafl.pointWeight}",
                    color = if (nafl.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helpers
fun getPrayerArabicName(name: String): String {
    return when (name.lowercase()) {
        "fajr" -> "الفجر"
        "dhuhr" -> "الظهر"
        "asr" -> "العصر"
        "maghrib" -> "المغرب"
        "isha" -> "العشاء"
        else -> name
    }
}

fun getPrayerIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "fajr" -> Icons.Default.WbTwilight
        "dhuhr" -> Icons.Default.WbSunny
        "asr" -> Icons.Default.WbCloudy
        "maghrib" -> Icons.Default.WbTwilight
        "isha" -> Icons.Default.Bedtime
        else -> Icons.Default.Nightlight
    }
}

fun getArabicDate(dateStr: String): Pair<String, String> {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateStr) ?: Date()

        val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        Pair(dayFormat.format(date), dateFormat.format(date))
    } catch (e: Exception) {
        Pair("اليوم", dateStr)
    }
}
