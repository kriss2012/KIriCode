package com.KiriCode.com

import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- DESIGN SYSTEM COLORS (Brutalist Modernist Palette) ---
val BrutalistBackgroundColor = Color(0xFFFFFFFF)     // 80% Pure White
val BrutalistAccentColor = Color(0xFFFFE680)         // 18% Accent Yellow (Highlights, Active States)
val BrutalistAccentSoftColor = Color(0xFFFFF4B8)     // Soft Accent Yellow (Cards, Input focus background)
val BrutalistBlackColor = Color(0xFF000000)          // 2% Black (Typography, Borders, Shadows)
val BrutalistDarkGrayColor = Color(0xFF333333)       // Dark Gray for secondary typography
val BrutalistLightGrayColor = Color(0xFFEAEAEA)      // Divider & Border borders
val BrutalistDangerColor = Color(0xFFFF6B6B)         // Danger Red color
val BrutalistSuccessColor = Color(0xFF2D8A4E)        // Accent green for achievements

@Composable
fun BrutalistTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = BrutalistAccentColor,
        onPrimary = BrutalistBlackColor,
        background = BrutalistBackgroundColor,
        onBackground = BrutalistBlackColor,
        surface = BrutalistBackgroundColor,
        onSurface = BrutalistBlackColor,
        outline = BrutalistBlackColor,
        surfaceVariant = BrutalistAccentSoftColor,
        onSurfaceVariant = BrutalistBlackColor
    )
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

// --- CUSTOM MODIFIERS FOR NEO-BRUTALISM ---
fun Modifier.brutalistShadow(
    color: Color = BrutalistBlackColor,
    offset: Dp = 5.dp,
    cornerRadius: Dp = 8.dp
) = this.drawBehind {
    val offsetPx = offset.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(offsetPx, offsetPx),
        size = size,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

fun Modifier.bottomBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

fun Modifier.topBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val y = strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

fun Modifier.rightBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val x = size.width - strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = strokeWidth
    )
}

// --- DATA CLASS MODELS ---
data class BrutalistTask(
    val id: Long,
    val title: String,
    val priority: String,
    val completed: Boolean
)

data class AlertMessage(
    val id: Long,
    val type: String, // "success", "warning"
    val text: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BrutalistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BrutalistBackgroundColor
                ) {
                    KiriFlowApp()
                }
            }
        }
    }
}

// --- CUSTOM REUSABLE COMPOSABLES ---

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = BrutalistBlackColor,
    borderWidth: Dp = 4.dp,
    shadowOffset: Dp = 5.dp,
    cornerRadius: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .brutalistShadow(offset = shadowOffset, cornerRadius = cornerRadius)
        .background(color = backgroundColor, shape = RoundedCornerShape(cornerRadius))
        .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(16.dp)

    Column(
        modifier = baseModifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Snappy physical translation offset when pressed
    val translationOffset by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 50),
        label = "pressTranslation"
    )
    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = tween(durationMillis = 50),
        label = "shadowTranslation"
    )

    Box(
        modifier = modifier
            .offset(x = translationOffset, y = translationOffset)
            .brutalistShadow(offset = shadowOffset, cornerRadius = 8.dp)
            .background(
                color = if (isPrimary) BrutalistAccentColor else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(4.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }
    val background = if (isFocused) BrutalistAccentSoftColor else Color.White
    val borderStrokeWidth = if (isFocused) 4.dp else 3.dp
    val shadowColor = if (isFocused) BrutalistAccentColor else BrutalistBlackColor

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            color = BrutalistBlackColor,
            letterSpacing = 0.5.sp
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalistShadow(color = shadowColor, offset = 4.dp, cornerRadius = 8.dp)
                .background(background, RoundedCornerShape(8.dp))
                .border(borderStrokeWidth, BrutalistBlackColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = BrutalistBlackColor,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = BrutalistDarkGrayColor.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (isError) {
            Text(
                text = errorMessage,
                color = BrutalistDangerColor,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun BrutalistSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val offset by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "switchOffset"
    )
    val background = if (checked) BrutalistAccentColor else Color.White

    Box(
        modifier = Modifier
            .size(54.dp, 30.dp)
            .background(background, RoundedCornerShape(4.dp))
            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = offset)
                .size(20.dp)
                .background(BrutalistBlackColor, RoundedCornerShape(2.dp))
                .border(1.dp, BrutalistBlackColor, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun BrutalistCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(if (checked) BrutalistAccentColor else Color.White, RoundedCornerShape(4.dp))
            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = BrutalistBlackColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun BrutalistBadge(
    text: String,
    backgroundColor: Color = BrutalistAccentColor,
    contentColor: Color = BrutalistBlackColor
) {
    Text(
        text = text.uppercase(),
        fontWeight = FontWeight.Black,
        fontSize = 9.sp,
        color = contentColor,
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(2.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun BrutalistCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "circularProgress"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = this.size.width - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            val arcSize = Size(diameter, diameter)
            
            // White inner circle
            drawCircle(
                color = Color.White,
                radius = diameter / 2,
                center = center
            )
            // Black outline
            drawCircle(
                color = BrutalistBlackColor,
                radius = diameter / 2,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )
            
            // Progress arc
            drawArc(
                color = BrutalistAccentColor,
                startAngle = -90f,
                sweepAngle = animProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
            )
        }
        
        Text(
            text = "${(progress * 100).toInt()}%",
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            color = BrutalistBlackColor
        )
    }
}

@Composable
fun AvatarUploadBox(
    onUploadSuccess: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .brutalistShadow(offset = 3.dp, cornerRadius = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
            .clickable { onUploadSuccess() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Upload Icon",
                tint = BrutalistBlackColor,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "UPLOAD PROFILE AVATAR",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = BrutalistBlackColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Drag & drop files or tap here (Max 5MB)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalistDarkGrayColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// --- MAIN APPLICATION CORE ---

@Composable
fun KiriFlowApp() {
    val coroutineScope = rememberCoroutineScope()
    
    // --- LOCAL STATES ---
    var currentTab by remember { mutableStateOf("dashboard") }
    var taskTitleInput by remember { mutableStateOf("") }
    var taskPriorityInput by remember { mutableStateOf("Medium") }
    var taskFilter by remember { mutableStateOf("all") }
    var chartRange by remember { mutableStateOf("week") } // "week" or "month"
    
    // Settings States
    var settingsName by remember { mutableStateOf("Krishna Prasad") }
    var settingsEmail by remember { mutableStateOf("krishna@kiricode.com") }
    var settingsWorkspace by remember { mutableStateOf("kiricode") }
    var settingsNotificationsEnabled by remember { mutableStateOf(true) }
    var settingsCompactMode by remember { mutableStateOf(false) }

    // Alert List State
    val alerts = remember { mutableStateListOf<AlertMessage>() }
    
    // Initial Task DB
    val tasks = remember {
        mutableStateListOf(
            BrutalistTask(1, "Configure Vercel Deployments", "High", true),
            BrutalistTask(2, "Debug Android release keystore signing", "High", false),
            BrutalistTask(3, "Optimize DoseFlow compositions", "Medium", false),
            BrutalistTask(4, "Integrate native notification dialogs", "Low", true),
            BrutalistTask(5, "Review pull requests for KiriCode v1.2", "Medium", false)
        )
    }

    // Helper: Trigger Alert banner
    fun triggerAlert(type: String, text: String) {
        val newAlert = AlertMessage(System.currentTimeMillis(), type, text)
        alerts.add(newAlert)
        coroutineScope.launch {
            delay(3500)
            alerts.remove(newAlert)
        }
    }

    // Calculate dynamic stats
    val completedCount = tasks.count { it.completed }
    val totalCount = tasks.size
    val efficiencyRate = if (totalCount > 0) Math.round((completedCount.toFloat() / totalCount.toFloat()) * 100) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistBackgroundColor)
    ) {
        // --- STICKY TOP NAVIGATION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .bottomBorder(4.dp, BrutalistBlackColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bold square brand logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrutalistAccentColor, RoundedCornerShape(6.dp))
                        .border(3.5.dp, BrutalistBlackColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KF",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BrutalistBlackColor
                    )
                }
                
                Text(
                    text = "KiriFlow",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.8).sp,
                    color = BrutalistBlackColor
                )
            }

            // Notification action icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
                    .clickable {
                        triggerAlert("success", "Synchronizing cloud systems! Done.")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = BrutalistBlackColor,
                    modifier = Modifier.size(22.dp)
                )
                
                // Red badge marker
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(9.dp)
                        .background(BrutalistDangerColor, CircleShape)
                        .border(1.5.dp, BrutalistBlackColor, CircleShape)
                )
            }
        }

        // --- ALERTS FLOATING BANNER CONTAINER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                alerts.forEach { alert ->
                    val isSuccess = alert.type == "success"
                    val bannerBg = if (isSuccess) BrutalistAccentSoftColor else BrutalistDangerColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brutalistShadow(offset = 4.dp, cornerRadius = 8.dp)
                            .background(bannerBg, RoundedCornerShape(8.dp))
                            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Alert icon",
                            tint = BrutalistBlackColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = alert.text,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = BrutalistBlackColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✕",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = BrutalistBlackColor,
                            modifier = Modifier.clickable { alerts.remove(alert) }
                        )
                    }
                }
            }
        }

        // --- CORE VIEWPORT CONTENT ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (currentTab) {
                "dashboard" -> {
                    DashboardScreen(
                        completedCount = completedCount,
                        efficiencyRate = efficiencyRate,
                        tasks = tasks,
                        onCompletedChange = { taskId, completed ->
                            val idx = tasks.indexOfFirst { it.id == taskId }
                            if (idx != -1) {
                                tasks[idx] = tasks[idx].copy(completed = completed)
                                triggerAlert("success", if (completed) "Task completed!" else "Task active.")
                            }
                        },
                        onNavigateToTasks = { currentTab = "tasks" }
                    )
                }
                "tasks" -> {
                    TasksScreen(
                        tasks = tasks,
                        taskTitleInput = taskTitleInput,
                        taskPriorityInput = taskPriorityInput,
                        taskFilter = taskFilter,
                        onTitleChange = { taskTitleInput = it },
                        onPriorityChange = { taskPriorityInput = it },
                        onFilterChange = { taskFilter = it },
                        onAddTaskSubmit = {
                            if (taskTitleInput.trim().isNotEmpty()) {
                                tasks.add(BrutalistTask(System.currentTimeMillis(), taskTitleInput.trim(), taskPriorityInput, false))
                                taskTitleInput = ""
                                triggerAlert("success", "Task added successfully!")
                            } else {
                                triggerAlert("warning", "Task name cannot be empty.")
                            }
                        },
                        onCompletedChange = { taskId, completed ->
                            val idx = tasks.indexOfFirst { it.id == taskId }
                            if (idx != -1) {
                                tasks[idx] = tasks[idx].copy(completed = completed)
                                triggerAlert("success", if (completed) "Task completed!" else "Task active.")
                            }
                        },
                        onDeleteTask = { taskId ->
                            tasks.removeAll { it.id == taskId }
                            triggerAlert("warning", "Task removed from backlog.")
                        }
                    )
                }
                "analytics" -> {
                    AnalyticsScreen(
                        chartRange = chartRange,
                        onRangeChange = {
                            chartRange = it
                            triggerAlert("success", "Analytics data range changed.")
                        }
                    )
                }
                "settings" -> {
                    SettingsScreen(
                        name = settingsName,
                        email = settingsEmail,
                        workspace = settingsWorkspace,
                        notificationsEnabled = settingsNotificationsEnabled,
                        compactMode = settingsCompactMode,
                        onNameChange = { settingsName = it },
                        onEmailChange = { settingsEmail = it },
                        onWorkspaceChange = { settingsWorkspace = it },
                        onNotificationsChange = { settingsNotificationsEnabled = it },
                        onCompactChange = { settingsCompactMode = it },
                        onSave = {
                            triggerAlert("success", "Configuration saved!")
                        },
                        onReset = {
                            tasks.clear()
                            tasks.add(BrutalistTask(1, "Configure Vercel Deployments", "High", true))
                            tasks.add(BrutalistTask(2, "Debug Android release keystore signing", "High", false))
                            triggerAlert("warning", "Workspace stats and tasks reset.")
                        }
                    )
                }
            }
        }

        // --- STICKY BOTTOM NAVIGATION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .topBorder(4.dp, BrutalistBlackColor)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val navItems = listOf(
                Triple("dashboard", Icons.Default.Home, "Home"),
                Triple("tasks", Icons.Default.List, "Tasks"),
                Triple("analytics", Icons.Default.Info, "Stats"),
                Triple("settings", Icons.Default.Settings, "Config")
            )

            navItems.forEach { item ->
                val isActive = currentTab == item.first
                val labelColor = BrutalistBlackColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { currentTab = item.first }
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isActive) BrutalistAccentColor else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .then(
                                    if (isActive) Modifier.border(2.5.dp, BrutalistBlackColor, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.third,
                                tint = BrutalistBlackColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.third,
                            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 1: DASHBOARD PANEL ---
@Composable
fun DashboardScreen(
    completedCount: Int,
    efficiencyRate: Int,
    tasks: List<BrutalistTask>,
    onCompletedChange: (Long, Boolean) -> Unit,
    onNavigateToTasks: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header Box
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hey, Partner! 👋",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-0.5).sp,
                            color = BrutalistBlackColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Here's your startup flow overview today.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BrutalistDarkGrayColor
                        )
                    }
                    
                    // Avatar Box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrutalistAccentSoftColor, CircleShape)
                            .border(3.dp, BrutalistBlackColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☕", fontSize = 20.sp)
                    }
                }
            }
        }

        // Stats Row Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completed Card
                BrutalistCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = BrutalistAccentSoftColor,
                    shadowOffset = 4.dp
                ) {
                    Text(
                        text = "COMPLETED",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistDarkGrayColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = completedCount.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp,
                        color = BrutalistBlackColor
                    )
                    Text(
                        text = "+4 this week",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = BrutalistSuccessColor
                    )
                }

                // Efficiency Card
                BrutalistCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White,
                    shadowOffset = 4.dp
                ) {
                    Text(
                        text = "EFFICIENCY",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistDarkGrayColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$efficiencyRate%",
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp,
                        color = BrutalistBlackColor
                    )
                    Text(
                        text = "Target: 90%",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = BrutalistDarkGrayColor
                    )
                }
            }
        }

        // Featured Promo Callout
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BrutalistAccentSoftColor,
                shadowOffset = 5.dp
            ) {
                BrutalistBadge("FEATURED TASK", BrutalistBlackColor, Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scale Server Resources",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp,
                    color = BrutalistBlackColor
                )
                Text(
                    text = "Ensure database performance is stable ahead of tomorrow's product launch campaign.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BrutalistDarkGrayColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrutalistButton(onClick = onNavigateToTasks, isPrimary = true) {
                        Text("View Backlog", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        // Active items checklist preview header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Highlights",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp,
                    color = BrutalistBlackColor
                )
                
                Text(
                    text = "View all",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }
        }

        // Display top 3 active tasks
        val activeTasks = tasks.filter { !it.completed }.take(3)
        if (activeTasks.isEmpty()) {
            item {
                BrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BrutalistAccentSoftColor
                ) {
                    Text(
                        text = "All caught up! 🎉",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(activeTasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brutalistShadow(offset = 3.dp, cornerRadius = 8.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BrutalistCheckbox(
                        checked = task.completed,
                        onCheckedChange = { onCompletedChange(task.id, it) }
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrutalistBlackColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val badgeBg = when(task.priority) {
                            "High" -> BrutalistDangerColor
                            "Low" -> BrutalistBlackColor
                            else -> BrutalistAccentColor
                        }
                        val badgeTextCol = if (task.priority == "Low") Color.White else BrutalistBlackColor
                        
                        BrutalistBadge(task.priority, badgeBg, badgeTextCol)
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 2: WORKSPACE TASKS ---
@Composable
fun TasksScreen(
    tasks: List<BrutalistTask>,
    taskTitleInput: String,
    taskPriorityInput: String,
    taskFilter: String,
    onTitleChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onAddTaskSubmit: () -> Unit,
    onCompletedChange: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Workspace Tasks",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.8).sp,
                    color = BrutalistBlackColor
                )
                Text(
                    text = "Manage, prioritize and execute tasks in real-time.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BrutalistDarkGrayColor
                )
            }
        }

        // Add task card form
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ADD NEW TASK",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Form input name
                BrutalistTextField(
                    value = taskTitleInput,
                    onValueChange = onTitleChange,
                    placeholder = "e.g. Write V2 API specs",
                    label = "Task Name"
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                // Custom priority segmented group block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRIORITY",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistBlackColor,
                        letterSpacing = 0.5.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val priorities = listOf("High", "Medium", "Low")
                        priorities.forEachIndexed { idx, prio ->
                            val isSelected = taskPriorityInput == prio
                            val bg = if (isSelected) {
                                when (prio) {
                                    "High" -> BrutalistDangerColor
                                    "Medium" -> BrutalistAccentColor
                                    else -> BrutalistLightGrayColor
                                }
                            } else Color.White
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg)
                                    .clickable { onPriorityChange(prio) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = BrutalistBlackColor
                                )
                            }
                            
                            if (idx < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(38.dp)
                                        .background(BrutalistBlackColor)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Create task submit button
                BrutalistButton(
                    onClick = onAddTaskSubmit,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Task", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create")
                }
            }
        }

        // Filter tabs bar row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalistShadow(offset = 3.dp, cornerRadius = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("all" to "All", "active" to "Active", "completed" to "Done")
                filters.forEachIndexed { index, pair ->
                    val isSelected = taskFilter == pair.first
                    val bg = if (isSelected) BrutalistAccentColor else Color.White
                    val count = when(pair.first) {
                        "active" -> tasks.count { !it.completed }
                        "completed" -> tasks.count { it.completed }
                        else -> tasks.size
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(bg)
                            .clickable { onFilterChange(pair.first) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pair.second} ($count)",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = BrutalistBlackColor
                        )
                    }

                    // Divider lines between cells
                    if (index < 2) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(38.dp)
                                .background(BrutalistBlackColor)
                        )
                    }
                }
            }
        }

        // Main tasks list filtered
        val filtered = tasks.filter {
            when (taskFilter) {
                "active" -> !it.completed
                "completed" -> it.completed
                else -> true
            }
        }

        if (filtered.isEmpty()) {
            item {
                BrutalistCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No tasks found in workspace backlog.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BrutalistDarkGrayColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(filtered) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brutalistShadow(offset = 3.dp, cornerRadius = 8.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BrutalistCheckbox(
                        checked = task.completed,
                        onCheckedChange = { onCompletedChange(task.id, it) }
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrutalistBlackColor,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val badgeBg = when(task.priority) {
                            "High" -> BrutalistDangerColor
                            "Low" -> BrutalistBlackColor
                            else -> BrutalistAccentColor
                        }
                        val badgeTextCol = if (task.priority == "Low") Color.White else BrutalistBlackColor
                        
                        BrutalistBadge(task.priority, badgeBg, badgeTextCol)
                    }

                    // Delete Action
                    IconButton(
                        onClick = { onDeleteTask(task.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(2.dp, BrutalistBlackColor, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = BrutalistBlackColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 3: PERFORMANCE METRICS ---
@Composable
fun AnalyticsScreen(
    chartRange: String,
    onRangeChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Performance Analytics",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.8).sp,
                    color = BrutalistBlackColor
                )
                Text(
                    text = "Visual statistics generated from live workspace events.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BrutalistDarkGrayColor
                )
            }
        }

        // Score highlights
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BrutalistAccentSoftColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL WORKSPACE SCORE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = BrutalistDarkGrayColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "8,420",
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1).sp,
                            color = BrutalistBlackColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BrutalistBadge("+14.2% Sprint Gain", BrutalistBlackColor, Color.White)
                    }
                    
                    BrutalistCircularProgress(
                        progress = 0.78f,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Brutalist progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(3.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.78f)
                            .background(BrutalistAccentColor)
                            .rightBorder(2.5.dp, BrutalistBlackColor)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Weekly Goal: 10,000 pts", fontWeight = FontWeight.Black, fontSize = 10.sp)
                    Text("78% achieved", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
        }

        // Bar Chart Box
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header of chart card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Velocity Metric",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    // Toggle controls (Figma Segmented style)
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(2.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
                    ) {
                        val isWeek = chartRange == "week"
                        
                        Box(
                            modifier = Modifier
                                .background(if (isWeek) BrutalistAccentColor else Color.White)
                                .clickable { onRangeChange("week") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Weekly", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(BrutalistBlackColor)
                        )

                        Box(
                            modifier = Modifier
                                .background(if (!isWeek) BrutalistAccentColor else Color.White)
                                .clickable { onRangeChange("month") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Monthly", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Chart Drawing - 6 vertical bars
                val heights = if (chartRange == "week") {
                    listOf(0.7f, 0.4f, 0.9f, 0.35f, 0.65f, 0.8f)
                } else {
                    listOf(0.45f, 0.85f, 0.25f, 0.95f, 0.5f, 0.7f)
                }
                
                val labels = listOf("M", "T", "W", "T", "F", "S")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(3.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    // Dotted background lines
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BrutalistLightGrayColor)
                            )
                        }
                    }
                    
                    // The actual bars
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        heights.forEachIndexed { idx, pct ->
                            val animPct by animateFloatAsState(
                                targetValue = pct,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "chartBar_$idx"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "${(pct * 100).toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = BrutalistDarkGrayColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(animPct)
                                        .background(BrutalistAccentColor, RoundedCornerShape(4.dp))
                                        .border(2.5.dp, BrutalistBlackColor, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = labels[idx],
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Additional stats table
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalistShadow(offset = 3.dp, cornerRadius = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val rows = listOf(
                    "Cycle Time Average" to "1.4 Days",
                    "Bottlenecks Detected" to "None",
                    "Resource Utilization" to "88.5%"
                )

                rows.forEachIndexed { index, pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pair.first,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = BrutalistBlackColor
                        )
                        
                        BrutalistBadge(
                            text = pair.second,
                            backgroundColor = if (index % 2 == 0) BrutalistAccentColor else BrutalistBlackColor,
                            contentColor = if (index % 2 == 0) BrutalistBlackColor else Color.White
                        )
                    }

                    if (index < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(BrutalistBlackColor)
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 4: APP CONFIG / SETTINGS ---
@Composable
fun SettingsScreen(
    name: String,
    email: String,
    workspace: String,
    notificationsEnabled: Boolean,
    compactMode: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onWorkspaceChange: (String) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onCompactChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Application Settings",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.8).sp,
                    color = BrutalistBlackColor
                )
                Text(
                    text = "Modify environment settings and preferences.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BrutalistDarkGrayColor
                )
            }
        }

        // Form settings configuration card
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PROFILE CONFIGURATIONS",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                AvatarUploadBox(
                    onUploadSuccess = {
                        Toast.makeText(context, "Avatar uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                BrutalistTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "Display name",
                    label = "Display Name"
                )

                Spacer(modifier = Modifier.height(4.dp))

                BrutalistTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "Email Address",
                    label = "Email Address"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Segmented Workspace Select
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRIMARY WORKSPACE",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistBlackColor,
                        letterSpacing = 0.5.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(3.dp, BrutalistBlackColor, RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val options = listOf("kiricode" to "Sandbox", "production" to "Prod V1", "staging" to "Staging")
                        options.forEachIndexed { index, pair ->
                            val isSelected = workspace == pair.first
                            val bg = if (isSelected) BrutalistAccentColor else Color.White
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg)
                                    .clickable { onWorkspaceChange(pair.first) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.second,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = BrutalistBlackColor
                                )
                            }
                            
                            if (index < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(38.dp)
                                        .background(BrutalistBlackColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(BrutalistLightGrayColor)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Switch 1: Push notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Push Notifications", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(
                            "Receive real-time system warnings and deployments",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalistDarkGrayColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    BrutalistSwitch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Switch 2: Compact board layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Compact Board Mode", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(
                            "Reduce typography padding and borders on tables",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalistDarkGrayColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    BrutalistSwitch(
                        checked = compactMode,
                        onCheckedChange = onCompactChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Form
                BrutalistButton(
                    onClick = onSave,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Configuration", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Danger zone
        item {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFF5F5),
                borderColor = BrutalistDangerColor,
                shadowOffset = 4.dp
            ) {
                Text(
                    text = "DANGER ZONE",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = BrutalistDangerColor,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Actions performed here cannot be easily undone.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalistDarkGrayColor
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalistButton(
                        onClick = { onReset() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Workspace Data", fontWeight = FontWeight.Black, fontSize = 12.sp, color = BrutalistDangerColor)
                    }
                }
            }
        }
    }
}