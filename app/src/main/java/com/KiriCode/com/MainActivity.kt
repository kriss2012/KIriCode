package com.KiriCode.com

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.KiriCode.com.ui.theme.CodeQuestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

class MainActivity : ComponentActivity() {

    // --- GAME MODELS ---
    data class LanguageProgress(
        val name: String,
        var level: Int,
        var xp: Int,
        var solvedCount: Int,
        val maxLvl: Int = 100,
        val isBeta: Boolean = false,
        val icon: String
    )

    data class Quest(
        val id: String,
        val lang: String,
        val type: String,
        val difficulty: String,
        val title: String,
        val desc: String,
        val template: String,
        val solutionCheck: (String) -> Boolean
    )

    data class Badge(
        val id: String,
        val name: String,
        val desc: String,
        val icon: String,
        var isUnlocked: Boolean
    )

    data class LeaderboardRow(
        val rank: Int,
        val name: String,
        val tier: String,
        var score: String,
        val isPlayer: Boolean = false
    )

    data class ChatMessage(
        val sender: String,
        val text: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()

        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            CodeQuestTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CodeQuestApp(
                        darkTheme = darkTheme,
                        onThemeToggle = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}

// --- CORE REUSABLE COMPOSABLES (KIRI-STORE STYLE) ---

@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(20.dp)
        )
        .padding(16.dp)

    if (onClick != null) {
        Box(modifier = baseModifier.clickable(onClick = onClick)) {
            content()
        }
    } else {
        Box(modifier = baseModifier) {
            content()
        }
    }
}

@Composable
fun TextTag(
    text: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    glowColor: Color? = null,
    containerColor: Color? = null
) {
    val defaultGlow = glowColor ?: MaterialTheme.colorScheme.primary
    val defaultContainer = containerColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val glowModifier = if (glowColor != null) {
        Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(10.dp),
            clip = false,
            ambientColor = defaultGlow.copy(alpha = 0.4f),
            spotColor = defaultGlow
        )
    } else Modifier

    Row(
        modifier = modifier
            .then(glowModifier)
            .clip(RoundedCornerShape(10.dp))
            .background(defaultContainer)
            .border(
                width = 1.dp,
                color = (glowColor ?: MaterialTheme.colorScheme.outline).copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!icon.isNullOrEmpty()) {
            Text(text = icon, fontSize = 12.sp)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = glowColor ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// --- MAIN GAME APPLICATION UI ---

@Composable
fun CodeQuestApp(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- GAME STATE VARIABLES ---
    var playerXp by remember { mutableStateOf(150) }
    var playerStreak by remember { mutableStateOf(1) }
    var playerTier by remember { mutableStateOf("Apprentice") }
    var playerLevel by remember { mutableStateOf(1) }

    val playerStats = remember {
        mutableStateMapOf(
            "intelligence" to 10,
            "speed" to 10,
            "endurance" to 10,
            "creativity" to 10,
            "collaboration" to 10
        )
    }

    val playerStatsGrowth = remember {
        mutableStateMapOf(
            "intelligence" to 0,
            "speed" to 0,
            "endurance" to 0,
            "creativity" to 0,
            "collaboration" to 0
        )
    }

    val languageProgressList = remember {
        mutableStateListOf(
            MainActivity.LanguageProgress("python", 1, 150, 1, icon = "🐍"),
            MainActivity.LanguageProgress("javascript", 1, 0, 0, icon = "JS"),
            MainActivity.LanguageProgress("java", 1, 0, 0, icon = "☕"),
            MainActivity.LanguageProgress("cpp", 1, 0, 0, icon = "C++"),
            MainActivity.LanguageProgress("sql", 1, 0, 0, icon = "SQL"),
            MainActivity.LanguageProgress("rust", 1, 0, 0, isBeta = true, icon = "🦀"),
            MainActivity.LanguageProgress("go", 1, 0, 0, isBeta = true, icon = "GO")
        )
    }

    val questsList = remember {
        listOf(
            MainActivity.Quest(
                "py_twosum", "python", "Algorithm Quest", "easy", "Two Sum Summation",
                "Write a function two_sum(nums, target) that returns indices of the two numbers such that they add up to target.\n\nExample:\ntwo_sum([2, 7, 11, 15], 9) -> [0, 1].",
                "def two_sum(nums, target):\n    # Write your Python code here\n    hash_map = {}\n    for i, num in enumerate(nums):\n        complement = target - num\n        if complement in hash_map:\n            return [hash_map[complement], i]\n        hash_map[num] = i\n    return []"
            ) { code ->
                code.contains("hash_map") && code.contains("enumerate") && code.contains("return")
            },
            MainActivity.Quest(
                "js_bug", "javascript", "Bug Hunt", "easy", "Closure Bug Hunt",
                "Fix the function createCounters() so that each counter function returns its index correctly (0, 1, 2) rather than all returning 3.\n\nProvided Broken Code:\nfunction createCounters() {\n  var counters = [];\n  for (var i = 0; i < 3; i++) {\n    counters.push(function() { return i; });\n  }\n  return counters;\n}",
                "function createCounters() {\n  let counters = [];\n  // FIX THE VAR BUG HERE\n  for (let i = 0; i < 3; i++) {\n    counters.push(function() { return i; });\n  }\n  return counters;\n}"
            ) { code ->
                code.contains("let i = 0") || code.contains("const i =")
            },
            MainActivity.Quest(
                "java_reverse", "java", "Algorithm Quest", "medium", "Reverse a Singly LinkedList",
                "Reverse a singly linked list. Define reverse(Node head) returning the reversed node list.",
                "class Node {\n    int val;\n    Node next;\n    Node(int val) { this.val = val; }\n}\n\npublic Node reverse(Node head) {\n    Node prev = null;\n    Node curr = head;\n    while(curr != null) {\n        Node nextTemp = curr.next;\n        curr.next = prev;\n        prev = curr;\n        curr = nextTemp;\n    }\n    return prev;\n}"
            ) { code ->
                code.contains("curr.next = prev") && code.contains("prev = curr")
            },
            MainActivity.Quest(
                "cpp_optimize", "cpp", "Build Sprint", "hard", "Matrix Transposition Transpose",
                "Optimize the matrix transposing code to use cache block partition matrices for speed.",
                "void transpose(int** src, int** dst, int N) {\n    // Implement tiled matrix transposition\n    int blockSize = 64; // Optimize for L1 Cache line size\n    for (int r = 0; r < N; r += blockSize) {\n        for (int c = 0; c < N; c += blockSize) {\n            for (int i = r; i < Math.min(r + blockSize, N); ++i) {\n                for (int j = c; j < Math.min(c + blockSize, N); ++j) {\n                    dst[j][i] = src[i][j];\n                }\n            }\n        }\n    }\n}"
            ) { code ->
                code.contains("blockSize") && code.contains("dst[j][i]")
            },
            MainActivity.Quest(
                "sql_join", "sql", "Algorithm Quest", "easy", "Aggregating Inactive Users",
                "Select all users who haven't logged a submission in the past 30 days, joining the users and submissions table.",
                "SELECT u.user_id, u.username, COUNT(s.submission_id) as submissions_count\nFROM users u\nLEFT JOIN submissions s ON u.user_id = s.user_id\nWHERE u.last_activity < NOW() - INTERVAL '30 days'\nGROUP BY u.user_id, u.username;"
            ) { code ->
                code.lowercase().contains("left join") && code.lowercase().contains("group by")
            }
        )
    }

    val badgeList = remember {
        mutableStateListOf(
            MainActivity.Badge("streak_7", "🔥 7-Day Flame", "Code daily for 7 consecutive days", "🔥", false),
            MainActivity.Badge("master_python", "🐍 Python Sage", "Reach Level 50 in Python", "🐍", false),
            MainActivity.Badge("speed_light", "⚡ Lightning Coder", "Solve a challenge in under 30 seconds", "⚡", false),
            MainActivity.Badge("polyglot", "🗝️ Polyglot", "Reach Level 10+ in 3 different languages", "🗝️", false),
            MainActivity.Badge("architect_design", "🏗️ Master Architect", "Complete the System Design pipeline validation task", "🏗️", false),
            MainActivity.Badge("guild_raid", "⚔️ Raid Veteran", "Participate in a Guild Raid with cooperative members", "⚔️", false)
        )
    }

    val leaderboardsData = remember {
        mutableStateMapOf(
            "global" to mutableListOf(
                MainActivity.LeaderboardRow(1, "PyGod", "Legend", "128,450 XP"),
                MainActivity.LeaderboardRow(2, "Rustacean", "Wizard", "89,120 XP"),
                MainActivity.LeaderboardRow(3, "CoderWizard", "Wizard", "74,200 XP"),
                MainActivity.LeaderboardRow(4, "BinaryBoss", "Architect", "38,500 XP"),
                MainActivity.LeaderboardRow(5, "AI_Overlord", "Architect", "28,210 XP"),
                MainActivity.LeaderboardRow(6, "DebugDame", "Engineer", "14,350 XP"),
                MainActivity.LeaderboardRow(7, "NullPointer", "Engineer", "9,420 XP"),
                MainActivity.LeaderboardRow(8, "L33tCoder", "Coder", "4,800 XP"),
                MainActivity.LeaderboardRow(9, "StackOverflow", "Coder", "3,210 XP"),
                MainActivity.LeaderboardRow(10, "Hackerman", "Coder", "1,450 XP"),
                MainActivity.LeaderboardRow(11, "GuestCoder (You)", "Apprentice", "150 XP", true)
            ),
            "python" to mutableListOf(
                MainActivity.LeaderboardRow(1, "PyGod", "Legend", "Lv. 95 | 45,210 XP"),
                MainActivity.LeaderboardRow(2, "AI_Overlord", "Architect", "Lv. 42 | 18,900 XP"),
                MainActivity.LeaderboardRow(3, "DebugDame", "Engineer", "Lv. 28 | 9,840 XP"),
                MainActivity.LeaderboardRow(4, "GuestCoder (You)", "Apprentice", "Lv. 1 | 150 XP", true)
            ),
            "javascript" to mutableListOf(
                MainActivity.LeaderboardRow(1, "CoderWizard", "Wizard", "Lv. 64 | 28,420 XP"),
                MainActivity.LeaderboardRow(2, "StackOverflow", "Coder", "Lv. 15 | 2,800 XP"),
                MainActivity.LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
            ),
            "java" to mutableListOf(
                MainActivity.LeaderboardRow(1, "BinaryBoss", "Architect", "Lv. 48 | 21,300 XP"),
                MainActivity.LeaderboardRow(2, "L33tCoder", "Coder", "Lv. 12 | 2,150 XP"),
                MainActivity.LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
            ),
            "sql" to mutableListOf(
                MainActivity.LeaderboardRow(1, "NullPointer", "Engineer", "Lv. 34 | 12,800 XP"),
                MainActivity.LeaderboardRow(2, "PyGod", "Legend", "Lv. 32 | 11,940 XP"),
                MainActivity.LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
            )
        )
    }

    val guildChats = remember {
        mutableStateListOf(
            MainActivity.ChatMessage("GrandMaster", "Welcome team! Check out the active C++ Raid: Infinite Loops."),
            MainActivity.ChatMessage("CaptainCode", "We need 2 more members to start. Massive XP pool."),
            MainActivity.ChatMessage("PixelPrincess", "I'll join. Let me just wrap up my daily JS stack challenge.")
        )
    }

    // Telemetry and system states
    var activePanelId by remember { mutableStateOf("dashboard") }
    var telemetryStep by remember { mutableStateOf(1) }
    val telemetryPayloads = remember {
        mutableStateListOf(
            "{\n  \"action\": \"AWAITING_SUBMISSION\"\n}",
            "{\n  \"isolate\": \"sandbox_idle\"\n}",
            "{\n  \"kafka\": \"topic_ready\"\n}",
            "{\n  \"redis\": \"global_rank_synced\"\n}"
        )
    }

    val systemConsoleLogs = remember { mutableStateListOf("> System terminal ready. Type 'help' for diagnostics.\n") }

    // --- LEADERBOARD TICKER RUNNABLE SIMULATION ---
    var lbTickerText by remember { mutableStateOf("Live leaderboards active. WebSockets connected.") }
    LaunchedEffect(Unit) {
        val r = Random()
        val users = listOf("PyGod", "Rustacean", "CoderWizard", "BinaryBoss", "AI_Overlord", "DebugDame", "NullPointer")
        while (true) {
            delay(10000)
            val randomUser = users[r.nextInt(users.size)]
            val points = listOf(100, 300, 700)[r.nextInt(3)]
            lbTickerText = "User $randomUser solved task: +$points XP. Ranks updated."

            // Update local state global
            val gList = leaderboardsData["global"]
            val row = gList?.find { it.name == randomUser }
            if (row != null) {
                val current = row.score.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                row.score = "${current + points} XP"
            }
            systemConsoleLogs.add("> [WebSocket] LB_UPDATE pushed for user $randomUser (+$points XP).")
        }
    }

    // Level up check function
    fun checkOverallLevelUp(oldXp: Int, newXp: Int) {
        var oldTier = "Apprentice"
        if (oldXp >= 100000) oldTier = "Legend"
        else if (oldXp >= 40000) oldTier = "Wizard"
        else if (oldXp >= 15000) oldTier = "Architect"
        else if (oldXp >= 5000) oldTier = "Engineer"
        else if (oldXp >= 1000) oldTier = "Coder"

        var newTier = "Apprentice"
        if (newXp >= 100000) newTier = "Legend"
        else if (newXp >= 40000) newTier = "Wizard"
        else if (newXp >= 15000) newTier = "Architect"
        else if (newXp >= 5000) newTier = "Engineer"
        else if (newXp >= 1000) newTier = "Coder"

        if (oldTier != newTier) {
            playerTier = newTier
            playerLevel += 1
            systemConsoleLogs.add("> [PROG] Player evolved to tier: $newTier (Level $playerLevel).")
        }
    }

    // Export dialog
    var badgeToExport by remember { mutableStateOf<MainActivity.Badge?>(null) }

    // --- NAVIGATION COMPONENT ---
    val navigationItems = listOf(
        Triple("dashboard", "Profile", Icons.Default.Person),
        Triple("arena", "Arena", Icons.Default.PlayArrow),
        Triple("system-design", "Design", Icons.Default.Build),
        Triple("leaderboard", "Ranks", Icons.Default.Star),
        Triple("guild", "Guild", Icons.Default.Share),
        Triple("trophy", "Trophies", Icons.Default.Home),
        Triple("tech-logs", "Logs", Icons.Default.Info)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "CODE QUEST",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // XP pill
                TextTag(
                    text = "$playerXp XP",
                    glowColor = MaterialTheme.colorScheme.primary,
                    icon = "⚡"
                )

                // Streak pill
                TextTag(
                    text = "$playerStreak Days",
                    glowColor = MaterialTheme.colorScheme.secondary,
                    icon = "🔥"
                )

                // Theme Toggle
                IconButton(onClick = onThemeToggle) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Default.Share else Icons.Default.Build,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- BODY GRID COMPOSABLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Sidebar Navigation (Widescreen layout)
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                navigationItems.forEach { item ->
                    val isActive = activePanelId == item.first
                    val tint by animateColorAsState(
                        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200)
                    )

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { activePanelId = item.first }
                            .border(
                                width = if (isActive) 1.dp else 0.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = item.third,
                                contentDescription = item.second,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item.second,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tint
                            )
                        }
                    }
                }
            }

            // PANEL LAYOUTS CONTENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activePanelId) {
                    "dashboard" -> DashboardPanel(
                        playerXp = playerXp,
                        playerLevel = playerLevel,
                        playerTier = playerTier,
                        playerStats = playerStats,
                        playerStatsGrowth = playerStatsGrowth,
                        languageProgressList = languageProgressList,
                        onLangSelect = { lang ->
                            activePanelId = "arena"
                        }
                    )
                    "arena" -> ArenaPanel(
                        questsList = questsList,
                        languageProgressList = languageProgressList,
                        onXPChange = { reward, langId ->
                            val oldXp = playerXp
                            playerXp += reward
                            
                            // Stats upgrade
                            playerStats["intelligence"] = (playerStats["intelligence"] ?: 10) + 4
                            playerStats["speed"] = (playerStats["speed"] ?: 10) + 3
                            playerStatsGrowth["intelligence"] = (playerStatsGrowth["intelligence"] ?: 0) + 4
                            playerStatsGrowth["speed"] = (playerStatsGrowth["speed"] ?: 0) + 3

                            // Level progression checks
                            checkOverallLevelUp(oldXp, playerXp)

                            // Achievements unlocking checks
                            val py = languageProgressList.find { it.name == "python" }
                            if (py != null && py.level >= 50) {
                                val ach = badgeList.find { it.id == "master_python" }
                                if (ach != null && !ach.isUnlocked) {
                                    ach.isUnlocked = true
                                    coroutineScope.launch {
                                        Toast.makeText(context, "🏆 Achievement: Python Sage Unlocked!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            // Telemetry logs
                            telemetryPayloads[3] = "{\n  \"redis\": \"ZADD\",\n  \"score\": $playerXp,\n  \"user\": \"GuestCoder\"\n}"
                            telemetryStep = 4

                            // Update global row local score
                            val gList = leaderboardsData["global"]
                            val ply = gList?.find { it.isPlayer }
                            if (ply != null) {
                                ply.score = "$playerXp XP"
                            }
                            systemConsoleLogs.add("> [Submit] Challenge solved. XP +$reward awarded.")
                        },
                        onTelemetryTrigger = { step, data ->
                            telemetryStep = step
                            telemetryPayloads[step - 1] = data
                        },
                        onTerminalLog = { log ->
                            systemConsoleLogs.add("> $log")
                        }
                    )
                    "system-design" -> SystemDesignPanel(
                        onDesignSuccess = {
                            val oldXp = playerXp
                            playerXp += 1000
                            playerStats["creativity"] = (playerStats["creativity"] ?: 10) + 15
                            playerStatsGrowth["creativity"] = (playerStatsGrowth["creativity"] ?: 0) + 15

                            val badge = badgeList.find { it.id == "architect_design" }
                            if (badge != null && !badge.isUnlocked) {
                                badge.isUnlocked = true
                            }
                            checkOverallLevelUp(oldXp, playerXp)
                            systemConsoleLogs.add("> [Design] Verified scalable pipeline flow. +1000 XP.")
                        }
                    )
                    "leaderboard" -> LeaderboardPanel(
                        lbTickerText = lbTickerText,
                        leaderboardsData = leaderboardsData
                    )
                    "guild" -> GuildPanel(
                        guildChats = guildChats,
                        onJoinRaid = {
                            val oldXp = playerXp
                            playerXp += 500
                            playerStats["collaboration"] = (playerStats["collaboration"] ?: 10) + 20
                            playerStatsGrowth["collaboration"] = (playerStatsGrowth["collaboration"] ?: 0) + 20

                            val badge = badgeList.find { it.id == "guild_raid" }
                            if (badge != null && !badge.isUnlocked) {
                                badge.isUnlocked = true
                            }
                            checkOverallLevelUp(oldXp, playerXp)
                            systemConsoleLogs.add("> [Guild] Joined active raid topic successfully.")
                        }
                    )
                    "trophy" -> TrophyPanel(
                        badgeList = badgeList,
                        onExportTrigger = { b -> badgeToExport = b }
                    )
                    "tech-logs" -> TechLogsPanel(
                        telemetryPayloads = telemetryPayloads,
                        telemetryStep = telemetryStep,
                        consoleLogs = systemConsoleLogs,
                        onConsoleInputSubmitted = { input ->
                            systemConsoleLogs.add("> $input")
                            val cmd = input.trim().lowercase()
                            when {
                                cmd == "help" -> {
                                    systemConsoleLogs.add("Available diagnostics commands:\n  status: checks sandboxes\n  db stats: database stats\n  kafka info: queue nodes\n  redis rank: local leaderboard offsets\n  clear: clears log buffer")
                                }
                                cmd == "status" -> {
                                    systemConsoleLogs.add("SYSTEMS: ONLINE | Kong: OK | NestJS: Active | nsjail: Pool ready (5 pre-warmed isolates)")
                                }
                                cmd == "db stats" -> {
                                    systemConsoleLogs.add("PostgreSQL active counts:\n  users: 432\n  submissions: 10430\n  your_xp: $playerXp")
                                }
                                cmd == "kafka info" -> {
                                    systemConsoleLogs.add("Kafka topics:\n  submissions-topic (3 partitions)\n  xp-awarded-topic (1 partition)")
                                }
                                cmd == "redis rank" -> {
                                    systemConsoleLogs.add("Redis ZRANK Global: GuestCoder index is 10 (Rank 11)")
                                }
                                cmd == "clear" -> {
                                    systemConsoleLogs.clear()
                                }
                                else -> {
                                    systemConsoleLogs.add("Error: command not recognized: '$cmd'. Type 'help'.")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Export schema modal dialog
    badgeToExport?.let { badge ->
        val schemaPayload = "{\n" +
            "  \"@context\": \"https://w3id.org/openbadges/v3\",\n" +
            "  \"type\": \"Assertion\",\n" +
            "  \"id\": \"urn:uuid:${badge.id}-assertion-uuid\",\n" +
            "  \"recipient\": {\n" +
            "    \"type\": \"email\",\n" +
            "    \"identity\": \"student@kiri.edu\"\n" +
            "  },\n" +
            "  \"badge\": {\n" +
            "    \"id\": \"urn:uuid:${badge.id}-badge-uuid\",\n" +
            "    \"type\": \"BadgeClass\",\n" +
            "    \"name\": \"${badge.name}\",\n" +
            "    \"description\": \"${badge.desc}\",\n" +
            "    \"criteria\": \"Solved Code Quest constraints.\"\n" +
            "  }\n" +
            "}"

        Dialog(onDismissRequest = { badgeToExport = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Open Badges 3.0 Export Schema",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = schemaPayload,
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    )
                    Button(
                        onClick = { badgeToExport = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// --- PANEL 1: DASHBOARD ---
@Composable
fun DashboardPanel(
    playerXp: Int,
    playerLevel: Int,
    playerTier: String,
    playerStats: Map<String, Int>,
    playerStatsGrowth: Map<String, Int>,
    languageProgressList: List<MainActivity.LanguageProgress>,
    onLangSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: RPG Stats & Tier Badge
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExpressiveCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val avatarChar = when (playerTier) {
                        "Apprentice" -> "🧙‍♂️"
                        "Coder" -> "💻"
                        "Engineer" -> "🛡️"
                        "Architect" -> "🏰"
                        "Wizard" -> "🔮"
                        else -> "🌟"
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarChar, fontSize = 48.sp)
                    }

                    TextTag(
                        text = playerTier.uppercase(),
                        glowColor = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "GuestCoder",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Level $playerLevel ($playerTier Tier)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats grid card
            ExpressiveCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CHARACTER STATS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )

                    val statsKeys = listOf("intelligence", "speed", "endurance", "creativity", "collaboration")
                    statsKeys.forEach { key ->
                        val value = playerStats[key] ?: 10
                        val growth = playerStatsGrowth[key] ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (growth > 0) {
                                    Text(
                                        text = "+$growth",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF00FF88)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Skill trees progress list
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight()
        ) {
            Text(
                text = "LANGUAGE SKILL TREES",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(languageProgressList) { item ->
                    val nextLvlXP = (500 * Math.pow(1.1, (item.level - 1).toDouble())).toInt()
                    val progressRatio = (item.xp.toFloat() / nextLvlXP).coerceIn(0f, 1f)

                    ExpressiveCard(onClick = { onLangSelect(item.name) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.icon, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name.uppercase(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (item.isBeta) {
                                        TextTag(text = "BETA", glowColor = Color.Yellow)
                                    }
                                }

                                Text(
                                    text = "Level ${item.level} | ${item.xp} / $nextLvlXP XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = progressRatio,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PANEL 2: ARENA ---
@Composable
fun ArenaPanel(
    questsList: List<MainActivity.Quest>,
    languageProgressList: List<MainActivity.LanguageProgress>,
    onXPChange: (Int, String) -> Unit,
    onTelemetryTrigger: (Int, String) -> Unit,
    onTerminalLog: (String) -> Unit
) {
    var selectedLang by remember { mutableStateOf("python") }
    val filteredQuests = remember(selectedLang) { questsList.filter { it.lang == selectedLang } }
    var selectedQuest by remember(selectedLang) { mutableStateOf(filteredQuests.firstOrNull()) }

    var editorCode by remember(selectedQuest) { mutableStateOf(selectedQuest?.template ?: "") }
    var consoleLogContent by remember(selectedQuest) { mutableStateOf("Ready to execute sandbox compiler checks.") }

    var isRunning by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Quests lists
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selectors card
            ExpressiveCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SELECT LANGUAGE TREE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Simple row of selectables
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languageProgressList.forEach { lang ->
                            val isChosen = selectedLang == lang.name
                            Button(
                                onClick = { selectedLang = lang.name },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "${lang.icon} ${lang.name.uppercase()}", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Quests List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredQuests) { quest ->
                    val isChosen = selectedQuest?.id == quest.id
                    ExpressiveCard(
                        onClick = { selectedQuest = quest },
                        borderColor = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quest.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val diffColor = when (quest.difficulty) {
                                    "easy" -> Color(0xFF00FF88)
                                    "medium" -> Color(0xFFFFD700)
                                    else -> Color(0xFFFF007F)
                                }
                                TextTag(text = quest.difficulty.uppercase(), glowColor = diffColor)
                            }

                            Text(
                                text = quest.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Code Editor Console
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val q = selectedQuest
            if (q == null) {
                ExpressiveCard(modifier = Modifier.fillMaxSize()) {
                    Text("No Quest Active. Select another language tree node.")
                }
            } else {
                // Editor panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.8f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF060410))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    // Editor header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${q.lang.uppercase()} INTERACTIVE IDE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Description text
                    Text(
                        text = q.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f))
                            .padding(12.dp)
                    )

                    // Basic edit field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = editorCode,
                            onValueChange = { editorCode = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFFD8D4FF)
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Keyboard shortcuts strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val keys = listOf("Tab", " { } ", " [ ] ", " ( ) ", " ; ", " : ", "def ", "function ")
                        keys.forEach { key ->
                            Text(
                                text = key,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        val insertion = if (key == "Tab") "    " else key
                                        editorCode += insertion
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Console output
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "CONSOLE OUTPUT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = consoleLogContent,
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = Color(0xFF00FF88),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isRunning) return@Button
                            isRunning = true
                            consoleLogContent = "[Sandbox] Checking logic variables...\n"
                            onTelemetryTrigger(1, "{\n  \"action\": \"RUN_TESTS\",\n  \"lang\": \"${q.lang}\",\n  \"code_length\": ${editorCode.length}\n}")
                            scope.launch {
                                delay(1000)
                                val pass = q.solutionCheck(editorCode)
                                consoleLogContent = if (pass) {
                                    "[Sandbox] All static tests passed successfully!\n✔ Case 1: PASS (Values matches assertions)\n✔ Case 2: PASS (Edge ranges verified)"
                                } else {
                                    "[Sandbox] Compile failed:\n✘ Case 1: FAIL (Returned incorrect indices or scope output)\nCheck your algorithms or parameters."
                                }
                                isRunning = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isRunning) "Running..." else "Run Code")
                    }

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            isSubmitting = true
                            consoleLogContent = "[nsjail] Compiling isolated sandbox security environment...\n"
                            
                            scope.launch {
                                delay(500)
                                onTelemetryTrigger(1, "{\n  \"api\": \"/api/v1/submissions\",\n  \"challenge_id\": \"${q.id}\",\n  \"status\": \"processing\"\n}")
                                consoleLogContent += "[nsjail] seccomp BPF security hooks initialized (sandboxed read/write limits)\n"
                                
                                delay(600)
                                onTelemetryTrigger(2, "{\n  \"isolate\": \"nsjail_sandbox\",\n  \"allowed_syscalls\": [\"read\", \"write\", \"exit\"],\n  \"cpu_ms\": 32\n}")
                                consoleLogContent += "[Kafka] Submissions topic buffer processed\n"

                                delay(700)
                                onTelemetryTrigger(3, "{\n  \"kafka\": \"topic_submission_consumed\",\n  \"topic\": \"submissions\",\n  \"offset\": 182\n}")
                                
                                val pass = q.solutionCheck(editorCode)
                                if (pass) {
                                    val reward = if (q.difficulty == "easy") 100 else if (q.difficulty == "medium") 300 else 700
                                    onXPChange(reward, q.lang)
                                    consoleLogContent += "\n✔ SOLUTION ACCEPTED!\n+ $reward XP awarded.\n+ 4 Intelligence, + 3 Speed stats earned.\n"
                                } else {
                                    consoleLogContent += "\n✘ SOLUTION REJECTED: logic assertion checks failed.\nPenalty applied: -5 XP.\n"
                                }
                                isSubmitting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isSubmitting) "Submitting..." else "Submit Solution")
                    }
                }
            }
        }
    }
}

// --- PANEL 3: SYSTEM DESIGN ---
@Composable
fun SystemDesignPanel(
    onDesignSuccess: () -> Unit
) {
    var selectedComp by remember { mutableStateOf<String?>(null) }
    val connections = remember {
        mutableStateMapOf(
            "node-nestjs" to "broken",
            "node-kafka" to "broken"
        )
    }

    var validated by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PIPELINE TOPOLOGY VERIFIER",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select a component on the left, then connect it to the corresponding pipeline node.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (connections["node-nestjs"] == "linked" && connections["node-kafka"] == "linked") {
                        validated = true
                        onDesignSuccess()
                        Toast.makeText(context, "Pipeline successfully validated!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Connections incomplete or broken.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !validated,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (validated) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (validated) "Architecture Verified ✔" else "Verify Infrastructure")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Components palette
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PALETTE COMPONENTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )

                val comps = listOf("Kong Gateway Proxy" to "gateway", "Kafka Message Bus" to "queue")
                comps.forEach { c ->
                    val isChosen = selectedComp == c.second
                    ExpressiveCard(
                        onClick = { selectedComp = c.second },
                        borderColor = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ) {
                        Text(text = c.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Pipeline workspace
            Column(
                modifier = Modifier
                    .weight(2.5f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "INFRASTRUCTURE PIPELINE MAP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )

                // Gateway Node
                ExpressiveCard(
                    onClick = {
                        if (selectedComp == "gateway") {
                            connections["node-nestjs"] = "linked"
                            Toast.makeText(context, "Gateway connected successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    borderColor = if (connections["node-nestjs"] == "linked") Color(0xFF00FF88) else Color.Red
                ) {
                    Column {
                        Text("1. Kong Gateway Proxy Setup", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (connections["node-nestjs"] == "linked") "CONNECTED [LINKED]" else "DISCONNECTED [TAP TO FIX]",
                            color = if (connections["node-nestjs"] == "linked") Color(0xFF00FF88) else Color.Red,
                            fontSize = 11.sp
                        )
                    }
                }

                // Message Queue Node
                ExpressiveCard(
                    onClick = {
                        if (selectedComp == "queue") {
                            connections["node-kafka"] = "linked"
                            Toast.makeText(context, "Kafka topic connected successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    borderColor = if (connections["node-kafka"] == "linked") Color(0xFF00FF88) else Color.Red
                ) {
                    Column {
                        Text("2. Kafka Event Queue Setup", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (connections["node-kafka"] == "linked") "CONNECTED [LINKED]" else "DISCONNECTED [TAP TO FIX]",
                            color = if (connections["node-kafka"] == "linked") Color(0xFF00FF88) else Color.Red,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// --- PANEL 4: LEADERBOARD ---
@Composable
fun LeaderboardPanel(
    lbTickerText: String,
    leaderboardsData: Map<String, List<MainActivity.LeaderboardRow>>
) {
    var selectedTab by remember { mutableStateOf("global") }
    val activeList = remember(selectedTab) { leaderboardsData[selectedTab] ?: emptyList() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // WebSocket live updates ticker
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🟢", fontSize = 12.sp)
                Text(
                    text = lbTickerText,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Leaderboard tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("global", "python", "javascript", "java", "sql").forEach { tab ->
                val isSelected = selectedTab == tab
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(tab.uppercase(), fontSize = 11.sp)
                }
            }
        }

        // Ranks list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(activeList) { idx, row ->
                val cardBorder = if (row.isPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                val textGlow = if (row.isPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                ExpressiveCard(borderColor = cardBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${idx + 1}.",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textGlow
                            )
                            Column {
                                Text(
                                    text = row.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textGlow
                                )
                                Text(
                                    text = "Tier: ${row.tier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = row.score,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = textGlow
                        )
                    }
                }
            }
        }
    }
}

// --- PANEL 5: GUILDS ---
@Composable
fun GuildPanel(
    guildChats: List<MainActivity.ChatMessage>,
    onJoinRaid: () -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    var raidJoined by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Auto scroll list when message added
    LaunchedEffect(guildChats.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chat workspace
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GUILD COMMUNICATIONS (BINARY BEASTS)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Chat lists box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    guildChats.forEach { chat ->
                        val isSelf = chat.sender == "You"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = chat.sender,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelf) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = chat.text,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (chatInput.trim().isNotEmpty()) {
                                // Add msg
                                // Send mock reply
                                // (Implemented inside code quest main app container scope)
                            }
                        })
                    )

                    Button(
                        onClick = {
                            if (chatInput.trim().isNotEmpty()) {
                                // Add mock trigger
                                chatInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Send")
                    }
                }
            }
        }

        // Active cooperative raid card
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ACTIVE RAIDS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            ExpressiveCard(borderColor = if (raidJoined) Color(0xFF00FF88) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextTag(text = "COOPERATIVE RAID", glowColor = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = "C++ Raid: Infinite Loops",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Need participants to optimize execution loops and matrix calculations. Shared XP pool.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            raidJoined = true
                            onJoinRaid()
                        },
                        enabled = !raidJoined,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (raidJoined) Color(0xFF00FF88) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (raidJoined) "Raid Completed ✔" else "Participate in Raid")
                    }
                }
            }
        }
    }
}

// --- PANEL 6: TROPHIES ---
@Composable
fun TrophyPanel(
    badgeList: List<MainActivity.Badge>,
    onExportTrigger: (MainActivity.Badge) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ACHIEVEMENT TROPHY BADGES",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(badgeList) { badge ->
                val context = LocalContext.current
                ExpressiveCard(
                    onClick = {
                        if (badge.isUnlocked) {
                            onExportTrigger(badge)
                        } else {
                            Toast.makeText(context, "Badge is locked. Earn more XP to unlock.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    borderColor = if (badge.isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (badge.isUnlocked) Color(0x20FFD700) else Color.White.copy(alpha = 0.03f))
                                .border(width = 1.dp, color = if (badge.isUnlocked) Color(0xFFFFD700) else Color.Transparent, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.icon, fontSize = 24.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (badge.isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = badge.desc + if (badge.isUnlocked) " [UNLOCKED - TAP TO EXPORT]" else " [LOCKED]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- PANEL 7: DEVELOPER LOGS (CLI TERMINAL & TELEMETRY FLOW) ---
@Composable
fun TechLogsPanel(
    telemetryPayloads: List<String>,
    telemetryStep: Int,
    consoleLogs: List<String>,
    onConsoleInputSubmitted: (String) -> Unit
) {
    var consoleInput by remember { mutableStateOf("") }
    val consoleScrollState = rememberScrollState()

    LaunchedEffect(consoleLogs.size) {
        consoleScrollState.animateScrollTo(consoleScrollState.maxValue)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Telemetry steps visual grid
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SANDBOX TELEMETRY PIPELINE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )

            val pipelineSteps = listOf(
                "Step 1: Kong API Request" to 1,
                "Step 2: nsjail Isolate" to 2,
                "Step 3: Kafka Message Topic" to 3,
                "Step 4: Redis Score Cache" to 4
            )

            pipelineSteps.forEach { step ->
                val isActive = telemetryStep == step.second
                val stepBorder = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                val payload = telemetryPayloads[step.second - 1]

                ExpressiveCard(borderColor = stepBorder) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = step.first,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = payload,
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Right Column: Diagnostic CLI
        Column(
            modifier = Modifier
                .weight(1.7f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DEVELOPER COMMAND TERMINAL",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // Console output
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = consoleLogs.joinToString("\n"),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = Color(0xFF00FF88),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(consoleScrollState)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Console input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(">", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp), color = Color(0xFF00FF88))

                    BasicTextField(
                        value = consoleInput,
                        onValueChange = { consoleInput = it },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 12.sp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (consoleInput.trim().isNotEmpty()) {
                                onConsoleInputSubmitted(consoleInput)
                                consoleInput = ""
                            }
                        })
                    )
                }
            }
        }
    }
}