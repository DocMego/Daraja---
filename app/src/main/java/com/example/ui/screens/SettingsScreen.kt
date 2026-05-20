package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NaflMetadata
import com.example.viewmodel.HabitViewModel

@Composable
fun SettingsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val metadataList by viewModel.naflMetadata.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var pointsExpanded by remember { mutableStateOf(true) }
    var prayerPointsExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header ---
        item {
            Column {
                Text(
                    text = "الإعدادات",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // --- Theme Selector Card (New UI Component) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_selector_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مظهر التطبيق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختر مظهر التطبيق المفضل لديك (الوضع الداكن، الوضع الفاتح، أو تلقائي حسب النظام)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentTheme by viewModel.appTheme.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "داكن 🌙",
                            isSelected = currentTheme == "dark",
                            onClick = { viewModel.updateAppTheme("dark") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "فاتح ☀️",
                            isSelected = currentTheme == "light",
                            onClick = { viewModel.updateAppTheme("light") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "تلقائي ⚙️",
                            isSelected = currentTheme == "system",
                            onClick = { viewModel.updateAppTheme("system") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 2. Nawafil Management Segment ---
        item {
            Column {
                Text(
                    text = "السنن والنوافل",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "المفتاح للإظهار/الإخفاء • سلة الحذف لإزالة النافلة",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        items(metadataList) { meta ->
            SettingsNaflItemRow(
                metadata = meta,
                onVisibilityChanged = { isVisible ->
                    viewModel.updateNaflMetadataVisibility(meta, isVisible)
                },
                onDelete = {
                    viewModel.deleteNaflMetadata(meta)
                }
            )
        }

        // --- 3. Dashed Outline triggers Add Nafl Dialog ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showAddDialog = true }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Nafl",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "إضافة نافلة جديدة",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // --- 4. Prayer Points Weight Settings (Collapsible) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_points_collapsible"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { prayerPointsExpanded = !prayerPointsExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نقاط الصلوات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Icon(
                            imageVector = if (prayerPointsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val ptsMissed by viewModel.ptsMissed.collectAsState()
                    val ptsQadaa by viewModel.ptsQadaa.collectAsState()
                    val ptsPresent by viewModel.ptsPresent.collectAsState()
                    val ptsJamaah by viewModel.ptsJamaah.collectAsState()

                    AnimatedVisibility(visible = prayerPointsExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تعديل نقاط صلوات الفرائض يعيد احتساب جميع نقاط الأيام والتواريخ فورياً تلقائياً:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            PrayerWeightAdjustmentRow(
                                label = "صلاة في جماعة (المسجد)",
                                weight = ptsJamaah,
                                onIncrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, ptsQadaa, ptsPresent, ptsJamaah + 1)
                                },
                                onDecrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, ptsQadaa, ptsPresent, (ptsJamaah - 1).coerceAtLeast(1))
                                }
                            )

                            PrayerWeightAdjustmentRow(
                                label = "أداء الصلاة حاضراً في وقتها",
                                weight = ptsPresent,
                                onIncrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, ptsQadaa, ptsPresent + 1, ptsJamaah)
                                },
                                onDecrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, ptsQadaa, (ptsPresent - 1).coerceAtLeast(1), ptsJamaah)
                                }
                            )

                            PrayerWeightAdjustmentRow(
                                label = "قضاء الصلاة الفائتة لاحقاً",
                                weight = ptsQadaa,
                                onIncrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, ptsQadaa + 1, ptsPresent, ptsJamaah)
                                },
                                onDecrease = {
                                    viewModel.updatePrayerPoints(ptsMissed, (ptsQadaa - 1).coerceAtLeast(1), ptsPresent, ptsJamaah)
                                }
                            )

                            PrayerWeightAdjustmentRow(
                                label = "تفويت الصلاة أو تركها",
                                weight = ptsMissed,
                                onIncrease = {
                                    viewModel.updatePrayerPoints((ptsMissed + 1).coerceAtMost(0), ptsQadaa, ptsPresent, ptsJamaah)
                                },
                                onDecrease = {
                                    viewModel.updatePrayerPoints(ptsMissed - 1, ptsQadaa, ptsPresent, ptsJamaah)
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 5. Points Weight Settings (Collapsible) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nawafil_points_collapsible"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pointsExpanded = !pointsExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نقاط النوافل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Icon(
                            imageVector = if (pointsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = pointsExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = "حدّد عدد النقاط لكل نافلة بشكل مستقل",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                metadataList.filter { it.isVisible }.forEach { meta ->
                                    WeightAdjustmentRow(
                                        metadata = meta,
                                        onIncrease = {
                                            viewModel.updateNaflMetadataWeight(meta, meta.pointWeight + 1)
                                        },
                                        onDecrease = {
                                            viewModel.updateNaflMetadataWeight(meta, meta.pointWeight - 1)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Warning / Notice Banner ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_warning_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "إخفاء الصلوات أو النوافل لا يمس سجلاتها السابقة. تعديل النقاط يُطبّق فورياً على جميع الشاشات.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }

    // --- Dialog: Add New Nafl ---
    if (showAddDialog) {
        var habitName by remember { mutableStateOf("") }
        var habitWeight by remember { mutableStateOf(1) }
        var selectedIcon by remember { mutableStateOf("star") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "إضافة نافلة جديدة",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("اسم العبادة أو النافلة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Weight Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نقاط العبادة:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { if (habitWeight > 1) habitWeight-- }) {
                                Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "+$habitWeight",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { habitWeight++ }) {
                                Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Icon Selector
                    Text(
                        text = "اختر رمز العبادة:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val icons = listOf(
                        Pair("star", "⭐"),
                        Pair("wind", "💨"),
                        Pair("leaf", "🍃"),
                        Pair("cloud", "☁️"),
                        Pair("heart", "💚"),
                        Pair("book", "📖"),
                        Pair("read", "📚"),
                        Pair("flash", "⚡"),
                        Pair("gift", "🎁")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        icons.take(5).forEach { (type, emoji) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedIcon == type) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedIcon = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        icons.drop(5).forEach { (type, emoji) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedIcon == type) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedIcon = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            viewModel.addNaflMetadata(habitName, habitWeight, selectedIcon)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsNaflItemRow(
    metadata: NaflMetadata,
    onVisibilityChanged: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val habitIcon = getMetadataIcon(metadata.iconType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_nafl_row_${metadata.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right part in RTL: icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habitIcon,
                        contentDescription = metadata.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = metadata.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (metadata.isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // Left part in RTL: Switch and delete button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = metadata.isVisible,
                    onCheckedChange = onVisibilityChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun WeightAdjustmentRow(
    metadata: NaflMetadata,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val habitIcon = getMetadataIcon(metadata.iconType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stepper_nafl_row_${metadata.name}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = habitIcon,
                    contentDescription = metadata.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = metadata.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Stepper buttons with value display in the middle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${metadata.pointWeight}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PointRuleRow(label: String, pts: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (pts.startsWith("-")) Color(0xFFEF4444).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = pts,
                color = if (pts.startsWith("-")) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getMetadataIcon(type: String): ImageVector {
    return when (type) {
        "star" -> Icons.Default.StarBorder
        "wind" -> Icons.Default.Air
        "leaf" -> Icons.Default.Eco
        "cloud" -> Icons.Default.CloudQueue
        "heart" -> Icons.Default.FavoriteBorder
        "book" -> Icons.Default.Book
        "read" -> Icons.Default.AutoStories
        "flash" -> Icons.Default.FlashOn
        "gift" -> Icons.Default.CardGiftcard
        else -> Icons.Default.StarBorder
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.height(44.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PrayerWeightAdjustmentRow(
    label: String,
    weight: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_points_row_${label}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val sign = if (weight > 0) "+" else ""
                Text(
                    text = "$sign$weight",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (weight < 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
