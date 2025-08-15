package net.dungeonhub.events.questmaster

import org.bukkit.Location
import org.bukkit.entity.Villager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Represents a Quest Master NPC with associated quest data
 */
data class QuestMaster(
    val questId: String,
    val name: String,
    val location: Location,
    val message: String,
    val isOneTimeUse: Boolean = true,
    val cooldownSeconds: Int = 0,
    val rewardType: RewardType = RewardType.NONE,
    val rewardData: String? = null
) {
    var villager: Villager? = null
        private set
    
    /**
     * Sets the villager entity for this Quest Master
     */
    fun setVillager(entity: Villager) {
        this.villager = entity
    }
    
    /**
     * Removes the villager entity
     */
    fun removeVillager() {
        villager?.remove()
        villager = null
    }
    
    /**
     * Checks if the Quest Master is spawned in the world
     */
    fun isSpawned(): Boolean = villager != null && villager?.isValid == true
}

/**
 * Types of rewards that Quest Masters can give
 */
enum class RewardType {
    NONE,           // No reward, just interaction
    ITEM,           // Give an item
    BUFF,           // Apply a potion effect
    ABILITY,        // Unlock a temporary ability
    MESSAGE         // Show a special message
}

/**
 * Represents a player's interaction history with Quest Masters
 */
data class QuestProgress(
    val playerId: String,
    val completedQuests: MutableSet<String> = mutableSetOf(),
    val questCooldowns: MutableMap<String, Long> = mutableMapOf()
) {
    /**
     * Checks if a player can interact with a Quest Master
     */
    fun canInteract(questMaster: QuestMaster): Boolean {
        // Check if it's one-time use and already completed
        if (questMaster.isOneTimeUse && completedQuests.contains(questMaster.questId)) {
            return false
        }
        
        // Check cooldown
        val cooldownEnd = questCooldowns[questMaster.questId] ?: 0
        return System.currentTimeMillis() >= cooldownEnd
    }
    
    /**
     * Records an interaction with a Quest Master
     */
    fun recordInteraction(questMaster: QuestMaster) {
        if (questMaster.isOneTimeUse) {
            completedQuests.add(questMaster.questId)
        }
        
        if (questMaster.cooldownSeconds > 0) {
            val cooldownEnd = System.currentTimeMillis() + (questMaster.cooldownSeconds * 1000L)
            questCooldowns[questMaster.questId] = cooldownEnd
        }
    }
    
    /**
     * Gets remaining cooldown time in seconds
     */
    fun getRemainingCooldown(questId: String): Int {
        val cooldownEnd = questCooldowns[questId] ?: 0
        val remaining = (cooldownEnd - System.currentTimeMillis()) / 1000
        return maxOf(0, remaining.toInt())
    }
}
