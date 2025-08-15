package net.dungeonhub.events.questmaster

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffectType

/**
 * Utility class for Quest Master rewards and validation
 */
object QuestMasterUtils {
    
    /**
     * Validates reward data format
     */
    fun validateRewardData(rewardType: RewardType, rewardData: String?): Boolean {
        if (rewardData.isNullOrBlank()) return rewardType == RewardType.NONE
        
        return when (rewardType) {
            RewardType.ITEM -> validateItemReward(rewardData)
            RewardType.BUFF -> validateBuffReward(rewardData)
            RewardType.ABILITY -> validateBuffReward(rewardData) // Abilities use buff format
            RewardType.MESSAGE -> true // Any string is valid for messages
            RewardType.NONE -> true
        }
    }
    
    /**
     * Validates item reward format: MATERIAL:AMOUNT[:ENCHANT:LEVEL]
     */
    private fun validateItemReward(rewardData: String): Boolean {
        val parts = rewardData.split(":")
        if (parts.size < 2) return false
        
        // Check material
        val material = try {
            Material.valueOf(parts[0].uppercase())
        } catch (e: IllegalArgumentException) {
            return false
        }
        
        // Check amount
        val amount = parts[1].toIntOrNull() ?: return false
        if (amount <= 0 || amount > material.maxStackSize) return false
        
        // Check enchantments (optional)
        if (parts.size >= 4) {
            try {
                val enchant = Enchantment.getByName(parts[2].uppercase()) ?: return false
                val level = parts[3].toIntOrNull() ?: return false
                if (level <= 0) return false
            } catch (e: Exception) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Validates buff reward format: EFFECT:AMPLIFIER:DURATION
     */
    private fun validateBuffReward(rewardData: String): Boolean {
        val parts = rewardData.split(":")
        if (parts.size != 3) return false
        
        // Check effect type
        val effectType = PotionEffectType.getByName(parts[0].uppercase()) ?: return false
        
        // Check amplifier
        val amplifier = parts[1].toIntOrNull() ?: return false
        if (amplifier < 0 || amplifier > 255) return false
        
        // Check duration
        val duration = parts[2].toIntOrNull() ?: return false
        if (duration <= 0) return false
        
        return true
    }
    
    /**
     * Creates an enhanced item with name, lore, and enchantments
     */
    fun createEnhancedItem(material: Material, amount: Int, name: String?, lore: List<String>? = null, enchantments: Map<Enchantment, Int>? = null): ItemStack {
        val item = ItemStack(material, amount)
        val meta = item.itemMeta
        
        meta?.let {
            if (name != null) {
                it.setDisplayName(name)
            }
            if (lore != null) {
                it.lore = lore
            }
            
            enchantments?.forEach { (enchant, level) ->
                it.addEnchant(enchant, level, true)
            }
            
            item.itemMeta = it
        }
        
        return item
    }
    
    /**
     * Gets common potion effects for Quest Masters
     */
    fun getCommonEffects(): Map<String, String> {
        return mapOf(
            "Speed Boost" to "SPEED:1:600",
            "Strength Boost" to "INCREASE_DAMAGE:1:600",
            "Resistance" to "DAMAGE_RESISTANCE:0:600",
            "Regeneration" to "REGENERATION:1:300",
            "Jump Boost" to "JUMP:2:600",
            "Night Vision" to "NIGHT_VISION:0:1200",
            "Water Breathing" to "WATER_BREATHING:0:1200",
            "Fire Resistance" to "FIRE_RESISTANCE:0:600"
        )
    }
    
    /**
     * Gets common item rewards for Quest Masters
     */
    fun getCommonItems(): Map<String, String> {
        return mapOf(
            "Golden Apple" to "GOLDEN_APPLE:1",
            "Enchanted Golden Apple" to "ENCHANTED_GOLDEN_APPLE:1",
            "Diamond Sword" to "DIAMOND_SWORD:1",
            "Diamond Armor Set" to "DIAMOND_CHESTPLATE:1",
            "Bow with Power" to "BOW:1:ARROW_DAMAGE:3",
            "Shield" to "SHIELD:1",
            "Ender Pearls" to "ENDER_PEARL:3",
            "Arrows" to "ARROW:64"
        )
    }
    
    /**
     * Formats time in seconds to a readable string
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        
        return when {
            minutes > 0 -> "${minutes}m ${remainingSeconds}s"
            else -> "${remainingSeconds}s"
        }
    }
    
    /**
     * Gets the display name for a reward type
     */
    fun getRewardTypeDisplay(rewardType: RewardType): String {
        return when (rewardType) {
            RewardType.NONE -> "No Reward"
            RewardType.ITEM -> "Item Reward"
            RewardType.BUFF -> "Buff Effect"
            RewardType.ABILITY -> "Special Ability"
            RewardType.MESSAGE -> "Special Message"
        }
    }
}
