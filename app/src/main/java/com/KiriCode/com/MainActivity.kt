package com.KiriCode.com

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    // --- GAME DATA MODEL ---
    private var playerXp = 150
    private var playerStreak = 1
    private var playerTier = "Apprentice"
    private var playerLevel = 1
    
    private val playerStats = mutableMapOf(
        "intelligence" to 10,
        "speed" to 10,
        "endurance" to 10,
        "creativity" to 10,
        "collaboration" to 10
    )

    private val playerStatsGrowth = mutableMapOf(
        "intelligence" to 0,
        "speed" to 0,
        "endurance" to 0,
        "creativity" to 0,
        "collaboration" to 0
    )

    class LanguageProgress(var level: Int, var xp: Int, var solvedCount: Int, val maxLvl: Int, val isBeta: Boolean, val icon: String)

    private val languageProgressList = mutableMapOf(
        "python" to LanguageProgress(1, 150, 1, 100, false, "🐍"),
        "javascript" to LanguageProgress(1, 0, 0, 100, false, "JS"),
        "java" to LanguageProgress(1, 0, 0, 100, false, "☕"),
        "cpp" to LanguageProgress(1, 0, 0, 100, false, "C++"),
        "sql" to LanguageProgress(1, 0, 0, 100, false, "SQL"),
        "rust" to LanguageProgress(1, 0, 0, 100, true, "🦀"),
        "go" to LanguageProgress(1, 0, 0, 100, true, "GO")
    )

    class Quest(val id: String, val lang: String, val type: String, val difficulty: String, val title: String, val desc: String, val template: String, val solutionCheck: (String) -> Boolean)

    private val questsList = listOf(
        Quest("py_twosum", "python", "Algorithm Quest", "easy", "Two Sum Summation", 
            "Write a function two_sum(nums, target) that returns indices of the two numbers such that they add up to target.\n\nExample:\ntwo_sum([2, 7, 11, 15], 9) -> [0, 1].",
            "def two_sum(nums, target):\n    # Write your Python code here\n    hash_map = {}\n    for i, num in enumerate(nums):\n        complement = target - num\n        if complement in hash_map:\n            return [hash_map[complement], i]\n        hash_map[num] = i\n    return []") { code ->
            code.contains("hash_map") && code.contains("enumerate") && code.contains("return")
        },
        Quest("js_bug", "javascript", "Bug Hunt", "easy", "Closure Bug Hunt",
            "Fix the function createCounters() so that each counter function returns its index correctly (0, 1, 2) rather than all returning 3.\n\nProvided Broken Code:\nfunction createCounters() {\n  var counters = [];\n  for (var i = 0; i < 3; i++) {\n    counters.push(function() { return i; });\n  }\n  return counters;\n}",
            "function createCounters() {\n  let counters = [];\n  // FIX THE VAR BUG HERE\n  for (let i = 0; i < 3; i++) {\n    counters.push(function() { return i; });\n  }\n  return counters;\n}") { code ->
            code.contains("let i = 0") || code.contains("const i =")
        },
        Quest("java_reverse", "java", "Algorithm Quest", "medium", "Reverse a Singly LinkedList",
            "Reverse a singly linked list. Define reverse(Node head) returning the reversed node list.",
            "class Node {\n    int val;\n    Node next;\n    Node(int val) { this.val = val; }\n}\n\npublic Node reverse(Node head) {\n    Node prev = null;\n    Node curr = head;\n    while(curr != null) {\n        Node nextTemp = curr.next;\n        curr.next = prev;\n        prev = curr;\n        curr = nextTemp;\n    }\n    return prev;\n}") { code ->
            code.contains("curr.next = prev") && code.contains("prev = curr")
        },
        Quest("cpp_optimize", "cpp", "Build Sprint", "hard", "Matrix Transposition Transpose",
            "Optimize the matrix transposing code to use cache block partition matrices for speed.",
            "void transpose(int** src, int** dst, int N) {\n    // Implement tiled matrix transposition\n    int blockSize = 64; // Optimize for L1 Cache line size\n    for (int r = 0; r < N; r += blockSize) {\n        for (int c = 0; c < N; c += blockSize) {\n            for (int i = r; i < Math.min(r + blockSize, N); ++i) {\n                for (int j = c; j < Math.min(c + blockSize, N); ++j) {\n                    dst[j][i] = src[i][j];\n                }\n            }\n        }\n    }\n}") { code ->
            code.contains("blockSize") && code.contains("dst[j][i]")
        },
        Quest("sql_join", "sql", "Algorithm Quest", "easy", "Aggregating Inactive Users",
            "Select all users who haven't logged a submission in the past 30 days, joining the users and submissions table.",
            "SELECT u.user_id, u.username, COUNT(s.submission_id) as submissions_count\nFROM users u\nLEFT JOIN submissions s ON u.user_id = s.user_id\nWHERE u.last_activity < NOW() - INTERVAL '30 days'\nGROUP BY u.user_id, u.username;") { code ->
            code.lowercase().contains("left join") && code.lowercase().contains("group by")
        }
    )

    class Badge(val id: String, val name: String, val desc: String, val icon: String, var isUnlocked: Boolean)

    private val badgeList = listOf(
        Badge("streak_7", "🔥 7-Day Flame", "Code daily for 7 consecutive days", "🔥", false),
        Badge("master_python", "🐍 Python Sage", "Reach Level 50 in Python", "🐍", false),
        Badge("speed_light", "⚡ Lightning Coder", "Solve a challenge in under 30 seconds", "⚡", false),
        Badge("polyglot", "🗝️ Polyglot", "Reach Level 10+ in 3 different languages", "🗝️", false),
        Badge("architect_design", "🏗️ Master Architect", "Complete the System Design pipeline validation task", "🏗️", false),
        Badge("guild_raid", "⚔️ Raid Veteran", "Participate in a Guild Raid with cooperative members", "⚔️", false)
    )

    class LeaderboardRow(val rank: Int, val name: String, val tier: String, var score: String, val isPlayer: Boolean = false)

    private val leaderboardsData = mapOf(
        "global" to mutableListOf(
            LeaderboardRow(1, "PyGod", "Legend", "128,450 XP"),
            LeaderboardRow(2, "Rustacean", "Wizard", "89,120 XP"),
            LeaderboardRow(3, "CoderWizard", "Wizard", "74,200 XP"),
            LeaderboardRow(4, "BinaryBoss", "Architect", "38,500 XP"),
            LeaderboardRow(5, "AI_Overlord", "Architect", "28,210 XP"),
            LeaderboardRow(6, "DebugDame", "Engineer", "14,350 XP"),
            LeaderboardRow(7, "NullPointer", "Engineer", "9,420 XP"),
            LeaderboardRow(8, "L33tCoder", "Coder", "4,800 XP"),
            LeaderboardRow(9, "StackOverflow", "Coder", "3,210 XP"),
            LeaderboardRow(10, "Hackerman", "Coder", "1,450 XP"),
            LeaderboardRow(11, "GuestCoder (You)", "Apprentice", "150 XP", true)
        ),
        "python" to mutableListOf(
            LeaderboardRow(1, "PyGod", "Legend", "Lv. 95 | 45,210 XP"),
            LeaderboardRow(2, "AI_Overlord", "Architect", "Lv. 42 | 18,900 XP"),
            LeaderboardRow(3, "DebugDame", "Engineer", "Lv. 28 | 9,840 XP"),
            LeaderboardRow(4, "GuestCoder (You)", "Apprentice", "Lv. 1 | 150 XP", true)
        ),
        "javascript" to mutableListOf(
            LeaderboardRow(1, "CoderWizard", "Wizard", "Lv. 64 | 28,420 XP"),
            LeaderboardRow(2, "StackOverflow", "Coder", "Lv. 15 | 2,800 XP"),
            LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
        ),
        "java" to mutableListOf(
            LeaderboardRow(1, "BinaryBoss", "Architect", "Lv. 48 | 21,300 XP"),
            LeaderboardRow(2, "L33tCoder", "Coder", "Lv. 12 | 2,150 XP"),
            LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
        ),
        "sql" to mutableListOf(
            LeaderboardRow(1, "NullPointer", "Engineer", "Lv. 34 | 12,800 XP"),
            LeaderboardRow(2, "PyGod", "Legend", "Lv. 32 | 11,940 XP"),
            LeaderboardRow(3, "GuestCoder (You)", "Apprentice", "Lv. 1 | 0 XP", true)
        )
    )

    class ChatMessage(val sender: String, val text: String)

    private val guildChats = mutableListOf(
        ChatMessage("GrandMaster", "Welcome team! Check out the active C++ Raid: Infinite Loops."),
        ChatMessage("CaptainCode", "We need 2 more members to start. Massive XP pool."),
        ChatMessage("PixelPrincess", "I'll join. Let me just wrap up my daily JS stack challenge.")
    )

    // System Design components & state
    private val designComponents = listOf(
        Pair("Kong Gateway Proxy", "gateway"),
        Pair("Kafka Message Bus", "queue"),
        Pair("nsjail Sandbox Node", "sandbox")
    )
    private var selectedDesignComponentType: String? = null
    private val designConnections = mutableMapOf(
        "node-kong" to "linked",
        "node-nestjs" to "broken",
        "node-nsjail" to "linked",
        "node-kafka" to "broken",
        "node-redis" to "linked",
        "node-postgres" to "linked"
    )

    // --- VIEW BINDINGS & VIEWS ---
    private lateinit var contentContainer: FrameLayout
    private val panels = mutableMapOf<String, View>()
    private var activePanelId = "dashboard"

    // Header views
    private lateinit var headerXpText: TextView
    private lateinit var headerStreakText: TextView

    // Bottom Navigation labels
    private val navLabels = mutableMapOf<String, TextView>()

    // Handler for simulating real-time rankings updates
    private val simulationHandler = Handler(Looper.getMainLooper())
    private val tickerRandom = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUi()

        // Init Header
        headerXpText = findViewById(R.id.header_xp_text)
        headerStreakText = findViewById(R.id.header_streak_text)

        // Init Frame layout container
        contentContainer = findViewById(R.id.content_container)

        // Inflate all sub panels
        val inflater = LayoutInflater.from(this)
        panels["dashboard"] = inflater.inflate(R.layout.panel_dashboard, contentContainer, false)
        panels["arena"] = inflater.inflate(R.layout.panel_arena, contentContainer, false)
        panels["system-design"] = inflater.inflate(R.layout.panel_system_design, contentContainer, false)
        panels["leaderboard"] = inflater.inflate(R.layout.panel_leaderboard, contentContainer, false)
        panels["guild"] = inflater.inflate(R.layout.panel_guild, contentContainer, false)
        panels["trophy"] = inflater.inflate(R.layout.panel_trophy, contentContainer, false)
        panels["tech-logs"] = inflater.inflate(R.layout.panel_tech_logs, contentContainer, false)

        // Add all to container and hide non-active ones
        for ((_, view) in panels) {
            contentContainer.addView(view)
            view.visibility = View.GONE
        }

        // Setup bottom navigation buttons
        setupBottomNavigation()

        // Switch to default dashboard
        switchPanel("dashboard")

        // Load specific panel details
        initDashboardPanel()
        initArenaPanel()
        initSystemDesignPanel()
        initLeaderboardPanel()
        initGuildPanel()
        initTrophyPanel()
        initTechLogsPanel()

        // Start real-time updates simulation loop
        startLeaderboardSimulation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // --- BOTTOM NAVIGATION LOGIC ---
    private fun setupBottomNavigation() {
        // Collect navigation labels for coloring active tab
        navLabels["dashboard"] = findViewById(R.id.nav_lbl_profile)
        navLabels["arena"] = findViewById(R.id.nav_lbl_arena)
        navLabels["system-design"] = findViewById(R.id.nav_lbl_design)
        navLabels["leaderboard"] = findViewById(R.id.nav_lbl_leaderboard)
        navLabels["guild"] = findViewById(R.id.nav_lbl_guild)
        navLabels["trophy"] = findViewById(R.id.nav_lbl_trophy)
        navLabels["tech-logs"] = findViewById(R.id.nav_lbl_logs)

        // Bind clicks
        findViewById<View>(R.id.nav_btn_profile).setOnClickListener { switchPanel("dashboard") }
        findViewById<View>(R.id.nav_btn_arena).setOnClickListener { switchPanel("arena") }
        findViewById<View>(R.id.nav_btn_design).setOnClickListener { switchPanel("system-design") }
        findViewById<View>(R.id.nav_btn_leaderboard).setOnClickListener { switchPanel("leaderboard") }
        findViewById<View>(R.id.nav_btn_guild).setOnClickListener { switchPanel("guild") }
        findViewById<View>(R.id.nav_btn_trophy).setOnClickListener { switchPanel("trophy") }
        findViewById<View>(R.id.nav_btn_logs).setOnClickListener { switchPanel("tech-logs") }
    }

    private fun switchPanel(panelId: String) {
        activePanelId = panelId
        
        // Hide all views, show selected
        for ((id, view) in panels) {
            if (id == panelId) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        // Color navigation labels
        val textMutedColor = ContextCompat.getColor(this, R.color.text_muted)
        val textCyanColor = ContextCompat.getColor(this, R.color.neon_cyan)

        for ((id, lbl) in navLabels) {
            if (id == panelId) {
                lbl.setTextColor(textCyanColor)
            } else {
                lbl.setTextColor(textMutedColor)
            }
        }

        // Refresh dynamic UI elements on switch
        if (panelId == "dashboard") updateDashboardUI()
        if (panelId == "leaderboard") updateLeaderboardUI()
        if (panelId == "guild") updateGuildUI()
        if (panelId == "trophy") updateTrophyUI()
    }

    // --- PANEL 1: DASHBOARD ---
    private lateinit var dashAvatarGraphic: TextView
    private lateinit var dashTierBadge: TextView
    private lateinit var dashUsername: TextView
    private lateinit var dashLevelLabel: TextView
    private lateinit var dashStatInt: TextView
    private lateinit var dashStatIntGrowth: TextView
    private lateinit var dashStatSpd: TextView
    private lateinit var dashStatSpdGrowth: TextView
    private lateinit var dashStatEnd: TextView
    private lateinit var dashStatEndGrowth: TextView
    private lateinit var dashStatCrt: TextView
    private lateinit var dashStatCrtGrowth: TextView
    private lateinit var dashLanguagesContainer: LinearLayout

    private fun initDashboardPanel() {
        val root = panels["dashboard"]!!
        dashAvatarGraphic = root.findViewById(R.id.dash_avatar_graphic)
        dashTierBadge = root.findViewById(R.id.dash_tier_badge)
        dashUsername = root.findViewById(R.id.dash_username)
        dashLevelLabel = root.findViewById(R.id.dash_level_label)
        dashStatInt = root.findViewById(R.id.dash_stat_int)
        dashStatIntGrowth = root.findViewById(R.id.dash_stat_int_growth)
        dashStatSpd = root.findViewById(R.id.dash_stat_spd)
        dashStatSpdGrowth = root.findViewById(R.id.dash_stat_spd_growth)
        dashStatEnd = root.findViewById(R.id.dash_stat_end)
        dashStatEndGrowth = root.findViewById(R.id.dash_stat_end_growth)
        dashStatCrt = root.findViewById(R.id.dash_stat_crt)
        dashStatCrtGrowth = root.findViewById(R.id.dash_stat_crt_growth)
        dashLanguagesContainer = root.findViewById(R.id.dash_languages_container)

        updateDashboardUI()
    }

    private fun updateDashboardUI() {
        // Update header XP/streak text
        headerXpText.text = "$playerXp XP"
        headerStreakText.text = "$playerStreak Day" + (if (playerStreak > 1) "s" else "")

        // Character profile values
        dashUsername.text = "GuestCoder"
        dashTierBadge.text = playerTier.uppercase()
        dashLevelLabel.text = "Level $playerLevel ($playerTier Tier)"

        // Stats
        dashStatInt.text = playerStats["intelligence"].toString()
        dashStatIntGrowth.text = "+" + playerStatsGrowth["intelligence"].toString()
        
        dashStatSpd.text = playerStats["speed"].toString()
        dashStatSpdGrowth.text = "+" + playerStatsGrowth["speed"].toString()
        
        dashStatEnd.text = playerStats["endurance"].toString()
        dashStatEndGrowth.text = "+" + playerStatsGrowth["endurance"].toString()
        
        dashStatCrt.text = playerStats["creativity"].toString()
        dashStatCrtGrowth.text = "+" + playerStatsGrowth["creativity"].toString()

        // Avatar graphics by Tier
        when (playerTier) {
            "Apprentice" -> {
                dashAvatarGraphic.text = "🧙‍♂️"
                dashTierBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            }
            "Coder" -> {
                dashAvatarGraphic.text = "💻"
                dashTierBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
            }
            "Engineer" -> {
                dashAvatarGraphic.text = "🛡️"
                dashTierBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            }
            "Architect" -> {
                dashAvatarGraphic.text = "🏰"
                dashTierBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_yellow))
            }
            "Wizard" -> {
                dashAvatarGraphic.text = "🔮"
                dashTierBadge.setTextColor(Color.parseColor("#B700FF"))
            }
            else -> {
                dashAvatarGraphic.text = "🌟"
                dashTierBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_magenta))
            }
        }

        // Draw Language skill tree items
        dashLanguagesContainer.removeAllViews()
        for ((lang, data) in languageProgressList) {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, dashLanguagesContainer, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            text1.text = "${data.icon}  ${lang.uppercase()}"
            text1.setTextColor(ContextCompat.getColor(this, R.color.white))
            text1.textSize = 14f

            val nextLvlXP = (500 * Math.pow(1.1, (data.level - 1).toDouble())).toInt()
            text2.text = "Level ${data.level}  |  ${data.xp} / $nextLvlXP XP" + (if (data.isBeta) " (Beta)" else "")
            text2.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            text2.textSize = 11f

            view.setPadding(0, 16, 0, 16)
            view.setOnClickListener {
                // select language and navigate to Arena
                selectedArenaLanguage = lang
                switchPanel("arena")
                refreshArenaQuestSpinner()
            }
            dashLanguagesContainer.addView(view)
        }
    }

    private fun checkOverallLevelUp() {
        val oldTier = playerTier
        var newTier = "Apprentice"
        
        if (playerXp >= 100000) newTier = "Legend"
        else if (playerXp >= 40000) newTier = "Wizard"
        else if (playerXp >= 15000) newTier = "Architect"
        else if (playerXp >= 5000) newTier = "Engineer"
        else if (playerXp >= 1000) newTier = "Coder"

        if (oldTier != newTier) {
            playerTier = newTier
            playerLevel += 1
            triggerLevelUpDialog(playerLevel - 1, playerLevel, newTier)
        }
    }

    private fun triggerLevelUpDialog(oldLvl: Int, newLvl: Int, tierName: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_level_up, null)
        builder.setView(view)
        
        view.findViewById<TextView>(R.id.dialog_old_lvl).text = oldLvl.toString()
        view.findViewById<TextView>(R.id.dialog_new_lvl).text = newLvl.toString()
        
        val msg = view.findViewById<TextView>(R.id.dialog_msg)
        msg.text = "Congratulations! You have evolved into an $tierName.\nNew challenges, features, and streak bonuses have been unlocked."
        
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        view.findViewById<Button>(R.id.dialog_btn_continue).setOnClickListener {
            dialog.dismiss()
            updateDashboardUI()
        }
    }

    // --- PANEL 2: QUEST ARENA & CODE EDITOR ---
    private lateinit var arenaLangSpinner: Spinner
    private lateinit var arenaQuestSpinner: Spinner
    private lateinit var arenaQuestTitle: TextView
    private lateinit var arenaQuestDifficulty: TextView
    private lateinit var arenaQuestDesc: TextView
    private lateinit var arenaEditorLangLbl: TextView
    private lateinit var arenaCodeInput: EditText
    private lateinit var arenaConsoleLog: TextView
    private lateinit var arenaShortcutsContainer: LinearLayout

    private var selectedArenaLanguage = "python"
    private var selectedQuest: Quest? = null

    private fun initArenaPanel() {
        val root = panels["arena"]!!
        arenaLangSpinner = root.findViewById(R.id.arena_language_spinner)
        arenaQuestSpinner = root.findViewById(R.id.arena_quest_spinner)
        arenaQuestTitle = root.findViewById(R.id.arena_quest_title)
        arenaQuestDifficulty = root.findViewById(R.id.arena_quest_difficulty)
        arenaQuestDesc = root.findViewById(R.id.arena_quest_desc)
        arenaEditorLangLbl = root.findViewById(R.id.arena_editor_lang_lbl)
        arenaCodeInput = root.findViewById(R.id.arena_code_input)
        arenaConsoleLog = root.findViewById(R.id.arena_console_log)
        arenaShortcutsContainer = root.findViewById(R.id.arena_shortcuts_container)

        // Setup language spinner adapter
        val langAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageProgressList.keys.toList())
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        arenaLangSpinner.adapter = langAdapter

        arenaLangSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedArenaLanguage = languageProgressList.keys.toList()[position]
                refreshArenaQuestSpinner()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup shortcut keyboard keys
        val shortcuts = listOf("    ", "{}", "[]", "()", ";", ":", "def ", "function ")
        for (item in shortcuts) {
            val btn = Button(this)
            btn.text = if (item == "    ") "Tab" else item
            btn.background = ContextCompat.getDrawable(this, R.drawable.shortcut_key_bg)
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            btn.textSize = 12f
            
            // Set margins
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            params.setMargins(0, 0, 12, 0)
            btn.layoutParams = params
            
            btn.setOnClickListener {
                val start = arenaCodeInput.selectionStart
                val end = arenaCodeInput.selectionEnd
                val text = arenaCodeInput.text
                text.replace(Math.min(start, end), Math.max(start, end), item)
                arenaCodeInput.requestFocus()
                arenaCodeInput.setSelection(start + item.length)
            }
            arenaShortcutsContainer.addView(btn)
        }

        // Action Buttons
        root.findViewById<Button>(R.id.arena_btn_run).setOnClickListener { runArenaCode() }
        root.findViewById<Button>(R.id.arena_btn_submit).setOnClickListener { submitArenaCode() }
    }

    private fun refreshArenaQuestSpinner() {
        val list = questsList.filter { it.lang == selectedArenaLanguage }
        if (list.isEmpty()) {
            arenaQuestTitle.text = "No Quests Found"
            arenaQuestDifficulty.text = "N/A"
            arenaQuestDesc.text = "Select another programming language tree to access quests."
            arenaCodeInput.setText("")
            selectedQuest = null
            return
        }

        val titles = list.map { it.title }
        val questAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
        questAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        arenaQuestSpinner.adapter = questAdapter

        arenaQuestSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedQuest = list[position]
                loadSelectedQuestDetails()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadSelectedQuestDetails() {
        val q = selectedQuest ?: return
        arenaQuestTitle.text = q.title
        arenaQuestDifficulty.text = q.difficulty.uppercase()
        arenaQuestDesc.text = q.desc
        arenaCodeInput.setText(q.template)

        arenaEditorLangLbl.text = "${q.lang.uppercase()} INTERACTIVE IDE"
        arenaConsoleLog.text = "Sandbox console loaded for quest: ${q.title}."

        // Update difficulty tag color
        when (q.difficulty) {
            "easy" -> {
                arenaQuestDifficulty.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
                arenaQuestDifficulty.setBackgroundColor(Color.parseColor("#2000FF88"))
            }
            "medium" -> {
                arenaQuestDifficulty.setTextColor(ContextCompat.getColor(this, R.color.neon_yellow))
                arenaQuestDifficulty.setBackgroundColor(Color.parseColor("#20FFD700"))
            }
            else -> {
                arenaQuestDifficulty.setTextColor(ContextCompat.getColor(this, R.color.neon_magenta))
                arenaQuestDifficulty.setBackgroundColor(Color.parseColor("#20FF007F"))
            }
        }
    }

    private fun runArenaCode() {
        val q = selectedQuest ?: return
        val code = arenaCodeInput.text.toString()
        arenaConsoleLog.text = "[Sandbox] Loading virtual compiler checks...\n"

        // Simulate API logs in telemetry step
        updateTelemetryLog(1, "{\n  \"action\": \"RUN_TESTS\",\n  \"lang\": \"${q.lang}\",\n  \"code_length\": ${code.length}\n}")

        Handler(Looper.getMainLooper()).postDelayed({
            if (q.solutionCheck(code)) {
                arenaConsoleLog.text = "[Sandbox] All static tests passed successfully!\n" +
                        "✔ Case 1: PASS (Matches target parameter logic)\n" +
                        "✔ Case 2: PASS (Edge case limits parsed)"
            } else {
                arenaConsoleLog.text = "[Sandbox] Execution failed logic assertions:\n" +
                        "✘ Case 1: FAIL (Incorrect output returned)\n" +
                        "Check your variable scopes, logic conditions, or output structure."
            }
        }, 1000)
    }

    private fun submitArenaCode() {
        val q = selectedQuest ?: return
        val code = arenaCodeInput.text.toString()
        arenaConsoleLog.text = "[nsjail] Restricting sandbox resources...\n"

        // Telemetry pipeline step animations
        Handler(Looper.getMainLooper()).postDelayed({
            updateTelemetryLog(1, "{\n  \"api\": \"/api/v1/submissions\",\n  \"method\": \"POST\",\n  \"status\": 202,\n  \"body\": {\n    \"challenge_id\": \"${q.id}\",\n    \"code\": \"...\"\n  }\n}")
            arenaConsoleLog.append("[nsjail] seccomp BPF filters enabled (allowing standard read/write/exit).\n")
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            updateTelemetryLog(2, "{\n  \"isolate\": \"nsjail_active\",\n  \"allowed_syscalls\": [\"read\", \"write\", \"exit_group\"],\n  \"cpu_time_ms\": 45\n}")
            arenaConsoleLog.append("[Kafka] Event published to result topics queue.\n")
        }, 1100)

        Handler(Looper.getMainLooper()).postDelayed({
            updateTelemetryLog(3, "{\n  \"kafka\": \"message_processed\",\n  \"topic\": \"submissions-topic\",\n  \"offset\": 2390\n}")
            
            if (q.solutionCheck(code)) {
                val reward = if (q.difficulty == "easy") 100 else if (q.difficulty == "medium") 300 else 700
                playerXp += reward
                
                // upgrade stats
                playerStats["intelligence"] = playerStats["intelligence"]!! + 4
                playerStats["speed"] = playerStats["speed"]!! + 3
                playerStatsGrowth["intelligence"] = playerStatsGrowth["intelligence"]!! + 4
                playerStatsGrowth["speed"] = playerStatsGrowth["speed"]!! + 3

                // language levels progress
                val data = languageProgressList[q.lang]!!
                data.xp += reward
                data.solvedCount += 1
                
                val nextLvlXP = (500 * Math.pow(1.1, (data.level - 1).toDouble())).toInt()
                if (data.xp >= nextLvlXP) {
                    data.level += 1
                    data.xp = 0
                    Toast.makeText(this, "Language level up: ${q.lang.uppercase()} Level ${data.level}!", Toast.LENGTH_LONG).show()
                }

                // Check achievements & level ups
                checkOverallLevelUp()
                checkAchievementsUnlocks()

                // Redis telemetry
                updateTelemetryLog(4, "{\n  \"redis\": \"ZADD\",\n  \"set\": \"leaderboards:global\",\n  \"score\": $playerXp,\n  \"user\": \"GuestCoder\"\n}")
                
                // Update player entry in leaderboard
                val ply = leaderboardsData["global"]?.find { it.isPlayer }
                if (ply != null) {
                    ply.score = "$playerXp XP"
                }

                val plyLang = leaderboardsData[q.lang]?.find { it.isPlayer }
                if (plyLang != null) {
                    plyLang.score = "Lv. ${data.level} | ${data.xp} XP"
                }

                arenaConsoleLog.append("\n✔ SOLUTION ACCEPTED!\n+ $reward XP awarded.\n+ 4 Intelligence, + 3 Speed stats earned.\n")
                Toast.makeText(this, "Solved! +$reward XP earned.", Toast.LENGTH_SHORT).show()

                // Update dev console CLI logs
                logToSystemConsole("Submission accepted for challenge: ${q.id}. XP awarded: $reward.")
            } else {
                arenaConsoleLog.append("\n✘ SOLUTION REJECTED: Logic assertion failure.\nRetry penalty applied: -5 XP.\n")
                playerXp = Math.max(0, playerXp - 5)
                Toast.makeText(this, "Logic error. -5 XP penalty.", Toast.LENGTH_SHORT).show()
            }
            updateDashboardUI()
        }, 1800)
    }

    // --- PANEL 3: SYSTEM DESIGN ---
    private lateinit var designCompSelectorLayout: LinearLayout
    private lateinit var nodeNestjs: TextView
    private lateinit var nodeKafka: TextView
    private lateinit var btnValidateDesign: Button

    private fun initSystemDesignPanel() {
        val root = panels["system-design"]!!
        designCompSelectorLayout = root.findViewById(R.id.design_comp_selector_layout)
        nodeNestjs = root.findViewById(R.id.node_nestjs)
        nodeKafka = root.findViewById(R.id.node_kafka)
        btnValidateDesign = root.findViewById(R.id.design_btn_validate)

        // Draw component selector buttons
        for (item in designComponents) {
            val btn = Button(this)
            btn.text = "${item.first}"
            btn.background = ContextCompat.getDrawable(this, R.drawable.shortcut_key_bg)
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            btn.textSize = 11f
            btn.isAllCaps = false
            
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 16, 0)
            btn.layoutParams = params

            btn.setOnClickListener {
                selectedDesignComponentType = item.second
                // Highlight select
                for (i in 0 until designCompSelectorLayout.childCount) {
                    val child = designCompSelectorLayout.getChildAt(i) as Button
                    child.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
                }
                btn.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
                Toast.makeText(this, "Component selected. Tap a broken topology node in the diagram to link.", Toast.LENGTH_SHORT).show()
            }
            designCompSelectorLayout.addView(btn)
        }

        // Tap nodes bindings
        nodeNestjs.setOnClickListener {
            if (selectedDesignComponentType == "gateway") {
                designConnections["node-nestjs"] = "linked"
                nodeNestjs.text = "2. NestJS Game Svc\n[Linked]"
                nodeNestjs.setBackgroundColor(Color.parseColor("#2000FF88"))
                Toast.makeText(this, "Connected Gateway to NestJS successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect component. Link the Gateway proxy to NestJS.", Toast.LENGTH_SHORT).show()
            }
        }

        nodeKafka.setOnClickListener {
            if (selectedDesignComponentType == "queue") {
                designConnections["node-kafka"] = "linked"
                nodeKafka.text = "4. Kafka Event Queue\n[Linked]"
                nodeKafka.setBackgroundColor(Color.parseColor("#2000FF88"))
                Toast.makeText(this, "Connected Kafka event distribution successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect component. Link the Kafka message bus here.", Toast.LENGTH_SHORT).show()
            }
        }

        btnValidateDesign.setOnClickListener {
            if (designConnections["node-nestjs"] == "linked" && designConnections["node-kafka"] == "linked") {
                playerXp += 1000
                playerStats["creativity"] = playerStats["creativity"]!! + 15
                playerStatsGrowth["creativity"] = playerStatsGrowth["creativity"]!! + 15
                
                unlockSpecificBadge("architect_design")
                btnValidateDesign.text = "Architecture Verified ✔"
                btnValidateDesign.isEnabled = false
                
                Toast.makeText(this, "System design success! +1000 XP awarded.", Toast.LENGTH_SHORT).show()
                updateDashboardUI()
                logToSystemConsole("System Design Challenge validated successfully: scaled leaderboard pipeline is verified.")
            } else {
                Toast.makeText(this, "Pipeline validation failed. Resolve all broken nodes in the topology flow first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- PANEL 4: LEADERBOARD ---
    private lateinit var lbTickerText: TextView
    private lateinit var lbTabsContainer: LinearLayout
    private lateinit var lbRanksContainer: LinearLayout
    private var selectedLbTab = "global"

    private fun initLeaderboardPanel() {
        val root = panels["leaderboard"]!!
        lbTickerText = root.findViewById(R.id.lb_ticker_text)
        lbTabsContainer = root.findViewById(R.id.lb_tabs_container)
        lbRanksContainer = root.findViewById(R.id.lb_ranks_container)

        // Draw sub tabs
        val tabs = listOf("global", "python", "javascript", "java", "sql")
        for (tab in tabs) {
            val btn = Button(this)
            btn.text = tab.uppercase()
            btn.background = null
            btn.setTextColor(ContextCompat.getColor(this, if (tab == selectedLbTab) R.color.neon_cyan else R.color.text_muted))
            btn.textSize = 12f
            
            btn.setOnClickListener {
                selectedLbTab = tab
                for (i in 0 until lbTabsContainer.childCount) {
                    val child = lbTabsContainer.getChildAt(i) as Button
                    child.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
                }
                btn.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
                updateLeaderboardUI()
            }
            lbTabsContainer.addView(btn)
        }
        updateLeaderboardUI()
    }

    private fun updateLeaderboardUI() {
        lbRanksContainer.removeAllViews()
        val list = leaderboardsData[selectedLbTab] ?: return
        
        // Sort list by score values (numeric)
        val sortedList = list.sortedByDescending { 
            val numStr = it.score.replace(Regex("[^0-9]"), "")
            if (numStr.isEmpty()) 0 else numStr.toInt()
        }

        for ((idx, row) in sortedList.withIndex()) {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, lbRanksContainer, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            text1.text = "${idx + 1}.  ${row.name}"
            text1.setTextColor(ContextCompat.getColor(this, if (row.isPlayer) R.color.neon_cyan else R.color.white))
            text1.textSize = 14f

            text2.text = "Tier: ${row.tier}  |  Score: ${row.score}"
            text2.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            text2.textSize = 11f

            view.setPadding(16, 12, 16, 12)
            lbRanksContainer.addView(view)
        }
    }

    private fun startLeaderboardSimulation() {
        val simulateTask = object : Runnable {
            override fun run() {
                val users = listOf("PyGod", "Rustacean", "CoderWizard", "BinaryBoss", "AI_Overlord", "DebugDame", "NullPointer")
                val randomUser = users[tickerRandom.nextInt(users.size)]
                val points = listOf(100, 300, 700)[tickerRandom.nextInt(3)]

                // Update ticker
                lbTickerText.text = "User $randomUser solved task: +$points XP. Ranks updated."

                // Append score to user
                val list = leaderboardsData["global"]!!
                val userRow = list.find { it.name == randomUser }
                if (userRow != null) {
                    val currentXP = userRow.score.replace(Regex("[^0-9]"), "").toInt()
                    userRow.score = "${currentXP + points} XP"
                }

                // Push message to dev telemetry logs
                logToSystemConsole("[WebSocket] LB_UPDATE pushed for user $randomUser (+$points XP).")

                // Reschedule in 10s
                simulationHandler.postDelayed(this, 10000)
            }
        }
        simulationHandler.postDelayed(simulateTask, 10000)
    }

    // --- PANEL 5: GUILD HUB ---
    private lateinit var guildChatScroll: ScrollView
    private lateinit var guildChatMessagesLayout: LinearLayout
    private lateinit var guildChatInput: EditText
    private lateinit var btnJoinRaid: Button

    private fun initGuildPanel() {
        val root = panels["guild"]!!
        guildChatScroll = root.findViewById(R.id.guild_chat_scroll)
        guildChatMessagesLayout = root.findViewById(R.id.guild_chat_messages_layout)
        guildChatInput = root.findViewById(R.id.guild_chat_input)
        btnJoinRaid = root.findViewById(R.id.guild_btn_join_raid)

        btnJoinRaid.setOnClickListener {
            playerXp += 500
            playerStats["collaboration"] = playerStats["collaboration"]!! + 20
            playerStatsGrowth["collaboration"] = playerStatsGrowth["collaboration"]!! + 20
            
            unlockSpecificBadge("guild_raid")
            btnJoinRaid.text = "Raid Complete ✔"
            btnJoinRaid.isEnabled = false
            
            Toast.makeText(this, "Guild Raid Completed! +500 XP & Collaboration boost.", Toast.LENGTH_SHORT).show()
            updateDashboardUI()
            logToSystemConsole("Guild Raid completed successfully: +500 XP pooled into Binary Beasts.")
        }

        root.findViewById<Button>(R.id.guild_btn_send).setOnClickListener { sendGuildMessage() }

        updateGuildUI()
    }

    private fun updateGuildUI() {
        guildChatMessagesLayout.removeAllViews()
        for (chat in guildChats) {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, guildChatMessagesLayout, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            val isSelf = chat.sender == "You"
            text1.text = chat.sender
            text1.setTextColor(ContextCompat.getColor(this, if (isSelf) R.color.neon_magenta else R.color.neon_cyan))
            text1.textSize = 12f

            text2.text = chat.text
            text2.setTextColor(ContextCompat.getColor(this, R.color.white))
            text2.textSize = 13f

            view.setPadding(0, 8, 0, 8)
            guildChatMessagesLayout.addView(view)
        }
        
        // Auto scroll to bottom
        guildChatScroll.post {
            guildChatScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun sendGuildMessage() {
        val text = guildChatInput.text.toString().trim()
        if (text.isEmpty()) return

        guildChats.add(ChatMessage("You", text))
        guildChatInput.setText("")
        updateGuildUI()

        // Mock automated replies from guild mates
        Handler(Looper.getMainLooper()).postDelayed({
            val responses = listOf(
                "Nice! Join the raid workspace.",
                "Let's write cleaner matrix transpositions.",
                "Awesome, that helped us gain 300 Guild XP!",
                "Did you solve the closure bug in JS yet?"
            )
            val reply = responses[tickerRandom.nextInt(responses.size)]
            guildChats.add(ChatMessage("CaptainCode", reply))
            updateGuildUI()
        }, 1500)
    }

    // --- PANEL 6: TROPHY BADGES ---
    private lateinit var trophyBadgesContainer: LinearLayout

    private fun initTrophyPanel() {
        val root = panels["trophy"]!!
        trophyBadgesContainer = root.findViewById(R.id.trophy_badges_container)
        updateTrophyUI()
    }

    private fun updateTrophyUI() {
        trophyBadgesContainer.removeAllViews()
        for (badge in badgeList) {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, trophyBadgesContainer, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            text1.text = "${badge.icon}  ${badge.name}"
            text1.setTextColor(ContextCompat.getColor(this, if (badge.isUnlocked) R.color.neon_yellow else R.color.text_muted))
            text1.textSize = 15f

            text2.text = badge.desc + (if (badge.isUnlocked) " [UNLOCKED - TAP TO EXPORT]" else " [LOCKED]")
            text2.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            text2.textSize = 11f

            view.setPadding(16, 16, 16, 16)
            view.setOnClickListener {
                if (badge.isUnlocked) {
                    exportOpenBadgesV3(badge)
                } else {
                    Toast.makeText(this, "Unlock this badge through gameplay achievements to export.", Toast.LENGTH_SHORT).show()
                }
            }
            trophyBadgesContainer.addView(view)
        }
    }

    private fun checkAchievementsUnlocks() {
        // Unlock Python sage if level is high
        val py = languageProgressList["python"]!!
        if (py.level >= 50) {
            unlockSpecificBadge("master_python")
        }

        // Unlock Polyglot if user has level 10+ in 3 languages
        var count10Plus = 0
        for ((_, data) in languageProgressList) {
            if (data.level >= 10) count10Plus++
        }
        if (count10Plus >= 3) {
            unlockSpecificBadge("polyglot")
        }
    }

    private fun unlockSpecificBadge(badgeId: String) {
        val b = badgeList.find { it.id == badgeId }
        if (b != null && !b.isUnlocked) {
            b.isUnlocked = true
            Toast.makeText(this, "🏆 Achievement Unlocked: ${b.name}!", Toast.LENGTH_LONG).show()
            logToSystemConsole("Achievement badge unlocked: ${b.id}")
            updateTrophyUI()
        }
    }

    private fun exportOpenBadgesV3(badge: Badge) {
        val schema = "{\n" +
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

        AlertDialog.Builder(this)
            .setTitle("Open Badges 3.0 Export Schema")
            .setMessage(schema)
            .setPositiveButton("Close", null)
            .show()
    }

    // --- PANEL 7: DEVELOPER LOGS & CLI CONSOLE ---
    private lateinit var techPayload1: TextView
    private lateinit var techPayload2: TextView
    private lateinit var techPayload3: TextView
    private lateinit var techPayload4: TextView
    private lateinit var techConsoleScroll: ScrollView
    private lateinit var techConsoleLogs: TextView
    private lateinit var techConsoleInput: EditText

    private fun initTechLogsPanel() {
        val root = panels["tech-logs"]!!
        techPayload1 = root.findViewById(R.id.tech_payload_1)
        techPayload2 = root.findViewById(R.id.tech_payload_2)
        techPayload3 = root.findViewById(R.id.tech_payload_3)
        techPayload4 = root.findViewById(R.id.tech_payload_4)
        techConsoleScroll = root.findViewById(R.id.tech_console_scroll)
        techConsoleLogs = root.findViewById(R.id.tech_console_logs)
        techConsoleInput = root.findViewById(R.id.tech_console_input)

        techConsoleInput.setOnEditorActionListener { _, _, _ ->
            executeTerminalCommand()
            true
        }
    }

    private fun updateTelemetryLog(step: Int, jsonPayload: String) {
        // Highlight active step visual layout
        val root = panels["tech-logs"]!!
        val steps = listOf(R.id.tech_step_1_layout, R.id.tech_step_2_layout, R.id.tech_step_3_layout, R.id.tech_step_4_layout)
        for ((idx, layId) in steps.withIndex()) {
            val lay = root.findViewById<LinearLayout>(layId)
            if (idx == step - 1) {
                lay.setBackgroundColor(Color.parseColor("#1500E5FF"))
            } else {
                lay.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        when (step) {
            1 -> techPayload1.text = jsonPayload
            2 -> techPayload2.text = jsonPayload
            3 -> techPayload3.text = jsonPayload
            4 -> techPayload4.text = jsonPayload
        }
        logToSystemConsole("Telemetry pipeline update for Step $step: $jsonPayload")
    }

    private fun logToSystemConsole(text: String) {
        if (::techConsoleLogs.isInitialized) {
            techConsoleLogs.append("> $text\n")
            techConsoleScroll.post {
                techConsoleScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun executeTerminalCommand() {
        val input = techConsoleInput.text.toString().trim()
        if (input.isEmpty()) return

        logToSystemConsole(input)
        techConsoleInput.setText("")

        val cmd = input.lowercase()
        when {
            cmd == "help" -> {
                logToSystemConsole("Available commands: status, db stats, kafka info, redis rank, clear")
            }
            cmd == "status" -> {
                logToSystemConsole("SYSTEMS: ONLINE | Kong: OK | NestJS: Active | nsjail: Pool ready (5 pre-warmed runtimes)")
            }
            cmd == "db stats" -> {
                logToSystemConsole("PostgreSQL Table Counts:\n  users: 432\n  submissions: 10430\n  your_xp: $playerXp")
            }
            cmd == "kafka info" -> {
                logToSystemConsole("Kafka Topics:\n  submissions-topic (3 partitions)\n  xp-awarded-topic (1 partition)")
            }
            cmd == "redis rank" -> {
                logToSystemConsole("Redis Sorted Set Global ranks: GuestCoder is at index 10 (Rank 11)")
            }
            cmd == "clear" -> {
                techConsoleLogs.text = ""
            }
            else -> {
                logToSystemConsole("Command not found: '$cmd'. Type 'help' for developer CLI details.")
            }
        }
    }
}