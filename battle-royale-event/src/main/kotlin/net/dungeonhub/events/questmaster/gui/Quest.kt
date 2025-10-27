package net.dungeonhub.events.questmaster.gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Quest difficulty levels
 */
enum class QuestDifficulty {
    EASY,    // Quests 1-3
    MEDIUM,  // Quests 4-6
    HARD     // Quests 7+
}

/**
 * Quest category - determines which quest masters can offer it
 */
enum class QuestCategory {
    GENERAL,        // Available to all quest masters
    CLASS_SPECIFIC  // Only available to specific class
}

/**
 * Represents an individual quest that can be offered by a Quest Master
 */
data class Quest(
    val id: String,
    val name: String,
    val description: List<String>,
    val icon: Material = Material.BOOK,
    val rewardType: String,
    val rewardData: String,
    val difficulty: QuestDifficulty = QuestDifficulty.EASY,
    val category: QuestCategory = QuestCategory.GENERAL,
    val classId: String? = null, // Only for CLASS_SPECIFIC quests
    val timeLimit: Int = 900, // Time limit in seconds (default 15 minutes)
    val requirements: QuestRequirements = QuestRequirements(),
    val isCompleted: Boolean = false,
    val isAvailable: Boolean = true
) {
    /**
     * Creates an ItemStack representation of this quest for the GUI
     */
    fun toItemStack(): ItemStack {
        val item = ItemStack(icon)
        val meta = item.itemMeta
        
        meta?.let {
            it.setDisplayName("§6$name")
            
            val lore = mutableListOf<String>()
            lore.add("§7Description:")
            description.forEach { line -> lore.add("§f  $line") }
            lore.add("")
            
            if (isCompleted) {
                lore.add("§a✓ COMPLETED")
            } else if (!isAvailable) {
                lore.add("§c✗ NOT AVAILABLE")
                if (requirements.hasRequirements()) {
                    lore.add("§7Requirements:")
                    requirements.getDisplayText().forEach { req -> lore.add("§f  $req") }
                }
            } else {
                lore.add("§e► AVAILABLE")
                lore.add("§7Click to accept quest!")
            }
            
            lore.add("")
            lore.add("§7Reward: §f$rewardData")
            
            it.lore = lore
            item.itemMeta = it
        }
        
        return item
    }
}

/**
 * Represents requirements that must be met before a quest becomes available
 */
data class QuestRequirements(
    val minimumLevel: Int = 0,
    val requiredItems: Map<Material, Int> = emptyMap(),
    val completedQuests: List<String> = emptyList(),
    val timeRequirement: Long = 0 // Time in milliseconds since plugin start
) {
    fun hasRequirements(): Boolean {
        return minimumLevel > 0 || requiredItems.isNotEmpty() || completedQuests.isNotEmpty() || timeRequirement > 0
    }
    
    fun getDisplayText(): List<String> {
        val requirements = mutableListOf<String>()
        
        if (minimumLevel > 0) {
            requirements.add("Level $minimumLevel required")
        }
        
        requiredItems.forEach { (material, amount) ->
            requirements.add("$amount x ${material.name.replace("_", " ").lowercase()}")
        }
        
        completedQuests.forEach { questId ->
            requirements.add("Complete quest: $questId")
        }
        
        if (timeRequirement > 0) {
            val minutes = timeRequirement / 60000
            requirements.add("Wait ${minutes} minutes")
        }
        
        return requirements
    }
}

/**
 * Progress tracking for a player's quests - NEW SYSTEM
 */
data class PlayerQuestProgress(
    val playerId: String,
    val completedQuests: MutableSet<String> = mutableSetOf(),
    val activeQuests: MutableMap<String, QuestObjective> = mutableMapOf(),
    val questStartTimes: MutableMap<String, Long> = mutableMapOf(),
    
    // New fields for the updated system
    var totalQuestsCompleted: Int = 0,  // Total quests completed
    var hasPassive: Boolean = false,     // Has unlocked passive (after 3 quests)
    var currentQuestId: String? = null,  // Currently offered quest
    var activeQuestId: String? = null,   // Currently active quest (with timer)
    var questActivationTime: Long = 0,   // When quest was activated
    var questTimeLimit: Int = 900,       // Time limit in seconds (15 min default)
    val seenQuestIds: MutableSet<String> = mutableSetOf() // Quests already shown to player
) {
    fun isQuestCompleted(questId: String): Boolean {
        return completedQuests.contains(questId)
    }
    
    fun isQuestActive(questId: String): Boolean {
        return activeQuests.containsKey(questId)
    }
    
    fun startQuest(questId: String, objective: QuestObjective) {
        activeQuests[questId] = objective
        questStartTimes[questId] = System.currentTimeMillis()
    }
    
    fun completeQuest(questId: String) {
        activeQuests.remove(questId)
        completedQuests.add(questId)
        questStartTimes.remove(questId)
        totalQuestsCompleted++
        
        // Check if player unlocked passive
        if (totalQuestsCompleted >= 3 && !hasPassive) {
            hasPassive = true
        }
    }
    
    /**
     * Activates the current quest and starts the timer
     */
    fun activateQuest(questId: String, timeLimit: Int) {
        activeQuestId = questId
        questActivationTime = System.currentTimeMillis()
        questTimeLimit = timeLimit
    }
    
    /**
     * Gets remaining time in seconds for active quest
     */
    fun getRemainingTime(): Int {
        if (activeQuestId == null) return 0
        val elapsed = (System.currentTimeMillis() - questActivationTime) / 1000
        return maxOf(0, questTimeLimit - elapsed.toInt())
    }
    
    /**
     * Checks if active quest has expired
     */
    fun isQuestExpired(): Boolean {
        return activeQuestId != null && getRemainingTime() <= 0
    }
    
    /**
     * Gets current difficulty stage based on completed quests
     */
    fun getCurrentDifficulty(): QuestDifficulty {
        return when {
            totalQuestsCompleted < 3 -> QuestDifficulty.EASY
            totalQuestsCompleted < 6 -> QuestDifficulty.MEDIUM
            else -> QuestDifficulty.HARD
        }
    }
    
    /**
     * Fails the current quest and applies penalty
     */
    fun failQuest() {
        activeQuestId = null
        questActivationTime = 0
        
        // Apply penalty based on difficulty
        when (getCurrentDifficulty()) {
            QuestDifficulty.EASY -> {
                // Reset to quest 1
                totalQuestsCompleted = 0
                completedQuests.clear()
                hasPassive = false
            }
            QuestDifficulty.MEDIUM, QuestDifficulty.HARD -> {
                // Reset to quest 4
                totalQuestsCompleted = 3
                // Keep first 3 quests completed
                val toKeep = completedQuests.take(3).toSet()
                completedQuests.clear()
                completedQuests.addAll(toKeep)
            }
        }
    }
}

/**
 * Represents the objective/progress of an active quest
 */
data class QuestObjective(
    val type: ObjectiveType,
    val target: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int = 1,
    val data: Map<String, String> = emptyMap()
) {
    fun isCompleted(): Boolean = currentProgress >= requiredProgress
    
    fun getProgressText(): String {
        return when (type) {
            ObjectiveType.KILL_MOBS -> "Kill $target: $currentProgress/$requiredProgress"
            ObjectiveType.COLLECT_ITEMS -> "Collect $target: $currentProgress/$requiredProgress"
            ObjectiveType.REACH_LOCATION -> "Reach $target: ${if (isCompleted()) "✓" else "✗"}"
            ObjectiveType.SURVIVE_TIME -> "Survive: $currentProgress/${requiredProgress}s"
            ObjectiveType.INTERACT_NPC -> "Talk to $target: ${if (isCompleted()) "✓" else "✗"}"
            ObjectiveType.CUSTOM -> data["description"] ?: "Complete objective: $currentProgress/$requiredProgress"
        }
    }
}

/**
 * Types of quest objectives
 */
enum class ObjectiveType {
    KILL_MOBS,
    COLLECT_ITEMS,
    REACH_LOCATION,
    SURVIVE_TIME,
    INTERACT_NPC,
    CUSTOM
}
