package net.dungeonhub.events.questmaster

import net.dungeonhub.events.questmaster.gui.Quest
import net.dungeonhub.events.questmaster.gui.QuestCategory
import net.dungeonhub.events.questmaster.gui.QuestDifficulty
import org.bukkit.Material
import java.util.*

/**
 * Manages the pool of all available quests
 */
object QuestPool {
    
    private val allQuests = mutableListOf<Quest>()
    
    init {
        loadAllQuests()
    }
    
    private fun loadAllQuests() {
        allQuests.clear()
        
        // Add 20 general quests
        allQuests.addAll(createGeneralQuests())
        
        // Add 10 quests per class (5 classes = 50 quests)
        allQuests.addAll(createTankQuests())
        allQuests.addAll(createBerserkerQuests())
        allQuests.addAll(createArcherQuests())
        allQuests.addAll(createHealerQuests())
        allQuests.addAll(createMageQuests())
    }
    
    /**
     * Gets a random quest for a player that they haven't seen yet
     */
    fun getRandomQuest(classId: String, seenQuestIds: Set<String>, difficulty: QuestDifficulty): Quest? {
        // Filter quests: general + class-specific, matching difficulty, not seen
        val availableQuests = allQuests.filter { quest ->
            !seenQuestIds.contains(quest.id) &&
            quest.difficulty == difficulty &&
            (quest.category == QuestCategory.GENERAL || quest.classId == classId)
        }
        
        return availableQuests.randomOrNull()
    }
    
    /**
     * Gets all quests (for admin/testing)
     */
    fun getAllQuests(): List<Quest> = allQuests.toList()
    
    // ==================== GENERAL QUESTS ====================
    
    private fun createGeneralQuests(): List<Quest> {
        return listOf(
            // Delivery Quests
            Quest("gen_delivery_1", "Iron Delivery", listOf("Bring back 32 iron ingots", "to the quest master"), Material.IRON_INGOT, "ITEM", "DIAMOND:2", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_2", "Emerald Collector", listOf("Bring back 8 emeralds", "to the quest master"), Material.EMERALD, "ITEM", "GOLDEN_APPLE:2", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_3", "Copper Miner", listOf("Bring back 64 copper ingots", "to the quest master"), Material.COPPER_INGOT, "ITEM", "IRON_INGOT:16", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_4", "Gold Rush", listOf("Bring back 16 gold ingots", "to the quest master"), Material.GOLD_INGOT, "ITEM", "DIAMOND:3", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_5", "Coal Supplier", listOf("Bring back 64 coal", "to the quest master"), Material.COAL, "ITEM", "IRON_INGOT:8", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_6", "Lapis Gatherer", listOf("Bring back 10 lapis", "to the quest master"), Material.LAPIS_LAZULI, "ITEM", "ENCHANTED_BOOK:1", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_7", "Cod Fisher", listOf("Bring back 8 cods", "to the quest master"), Material.COD, "ITEM", "GOLDEN_APPLE:1", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_delivery_8", "Salmon Hunter", listOf("Bring back 8 salmons", "to the quest master"), Material.SALMON, "ITEM", "GOLDEN_APPLE:1", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            
            // Kill Quests
            Quest("gen_kill_1", "Monster Slayer", listOf("Kill 15 monsters"), Material.IRON_SWORD, "ITEM", "DIAMOND_SWORD:1", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_kill_2", "Villager Hunter", listOf("Kill 1 villager"), Material.IRON_AXE, "ITEM", "EMERALD:5", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_kill_3", "Skeleton Destroyer", listOf("Kill 10 skeletons"), Material.BOW, "ITEM", "ARROW:64", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_kill_4", "Zombie Slayer", listOf("Kill 10 zombies"), Material.IRON_SWORD, "ITEM", "GOLDEN_APPLE:2", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900),
            Quest("gen_kill_5", "Spider Exterminator", listOf("Kill 10 spiders or", "cave spiders"), Material.SPIDER_EYE, "ITEM", "POTION:2", QuestDifficulty.EASY, QuestCategory.GENERAL, timeLimit = 900)
        )
    }
    
    // ==================== TANK QUESTS (Athena) ====================
    
    private fun createTankQuests(): List<Quest> {
        return listOf(
            // EASY
            Quest("tank_easy_1", "Damage Taker", listOf("Tank 100 hearts of damage"), Material.IRON_CHESTPLATE, "BUFF", "RESISTANCE:1:600", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_zeroth", timeLimit = 900),
            
            // MEDIUM
            Quest("tank_med_1", "Damage Absorber", listOf("Tank 300 hearts of damage"), Material.DIAMOND_CHESTPLATE, "BUFF", "RESISTANCE:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_zeroth", timeLimit = 1200),
            Quest("tank_med_2", "Skeleton Hunter", listOf("Bring back a wither", "skeleton head"), Material.WITHER_SKELETON_SKULL, "ITEM", "NETHERITE_INGOT:2", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_zeroth", timeLimit = 1200),
            Quest("tank_med_3", "Heart Sacrifice", listOf("Sacrifice 1 heart to", "the quest master"), Material.REDSTONE, "ITEM", "TOTEM_OF_UNDYING:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_zeroth", timeLimit = 1200)
        )
    }
    
    // ==================== BERSERKER QUESTS (Hercules) ====================
    
    private fun createBerserkerQuests(): List<Quest> {
        return listOf(
            // EASY
            Quest("berserk_easy_1", "Heart Dealer", listOf("Deal a total of 200 hearts"), Material.IRON_AXE, "ITEM", "DIAMOND_AXE:1", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_fire", timeLimit = 900),
            Quest("berserk_easy_2", "Player Damage", listOf("Deal a total of 20 hearts", "to a player"), Material.IRON_SWORD, "BUFF", "STRENGTH:1:600", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_fire", timeLimit = 900),
            
            // MEDIUM
            Quest("berserk_med_1", "Heart Destroyer", listOf("Deal a total of 400 hearts"), Material.DIAMOND_AXE, "BUFF", "STRENGTH:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_fire", timeLimit = 1200),
            Quest("berserk_med_2", "Player Hunter", listOf("Deal a total of 50 hearts", "to a player"), Material.DIAMOND_SWORD, "ITEM", "DIAMOND_SWORD:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_fire", timeLimit = 1200),
            Quest("berserk_med_3", "Heart Sacrifice", listOf("Sacrifice 1 heart to", "the quest master"), Material.REDSTONE, "ITEM", "TOTEM_OF_UNDYING:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_fire", timeLimit = 1200)
        )
    }
    
    // ==================== ARCHER QUESTS (Artemis) ====================
    
    private fun createArcherQuests(): List<Quest> {
        return listOf(
            // EASY
            Quest("archer_easy_1", "Bow Master", listOf("Deal a total of 150 hearts", "with a bow"), Material.BOW, "ITEM", "BOW:1", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 900),
            Quest("archer_easy_2", "Player Sniper", listOf("Deal a total of 20 hearts", "to a player with a bow"), Material.ARROW, "ITEM", "ARROW:64", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 900),
            Quest("archer_easy_3", "Runner", listOf("Run a total of 800 blocks"), Material.LEATHER_BOOTS, "BUFF", "SPEED:1:600", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 900),
            Quest("archer_easy_4", "Target Practice", listOf("Shoot a target block", "15 times"), Material.TARGET, "ITEM", "SPECTRAL_ARROW:16", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 900),
            
            // MEDIUM
            Quest("archer_med_1", "Bow Legend", listOf("Deal a total of 300 hearts", "with a bow"), Material.CROSSBOW, "ITEM", "CROSSBOW:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 1200),
            Quest("archer_med_2", "Player Hunter", listOf("Deal a total of 50 hearts", "to a player with a bow"), Material.SPECTRAL_ARROW, "BUFF", "SPEED:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 1200),
            Quest("archer_med_3", "Marathon Runner", listOf("Run a total of 1600 blocks"), Material.GOLDEN_BOOTS, "BUFF", "SPEED:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 1200),
            Quest("archer_med_4", "Sharpshooter", listOf("Shoot a target block", "30 times"), Material.TARGET, "ITEM", "SPECTRAL_ARROW:32", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 1200),
            Quest("archer_med_5", "Heart Sacrifice", listOf("Sacrifice 1 heart to", "the quest master"), Material.REDSTONE, "ITEM", "TOTEM_OF_UNDYING:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_ice", timeLimit = 1200)
        )
    }
    
    // ==================== HEALER QUESTS (Sushruta) ====================
    
    private fun createHealerQuests(): List<Quest> {
        return listOf(
            // EASY
            Quest("healer_easy_1", "Healing Hands", listOf("Heal 30 hearts"), Material.GLISTERING_MELON_SLICE, "ITEM", "GOLDEN_APPLE:2", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 900),
            Quest("healer_easy_2", "Life Bringer", listOf("Bring life into the world:", "Breed 3 different animals"), Material.WHEAT, "ITEM", "GOLDEN_APPLE:3", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 900),
            Quest("healer_easy_3", "Witch Doctor", listOf("Craft 3 different suspicious", "stews and drink them"), Material.SUSPICIOUS_STEW, "BUFF", "REGENERATION:1:600", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 900),
            Quest("healer_easy_4", "Poison Healer", listOf("Heal the poison effect", "with a bucket of milk"), Material.MILK_BUCKET, "ITEM", "POTION:3", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200),
            
            // MEDIUM
            Quest("healer_med_1", "Master Healer", listOf("Heal 150 hearts"), Material.ENCHANTED_GOLDEN_APPLE, "ITEM", "ENCHANTED_GOLDEN_APPLE:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200),
            Quest("healer_med_2", "Animal Breeder", listOf("Bring life into the world:", "Breed 10 animals"), Material.GOLDEN_CARROT, "ITEM", "GOLDEN_APPLE:5", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200),
            Quest("healer_med_3", "Witch Master", listOf("Craft 5 different suspicious", "stews and drink them"), Material.SUSPICIOUS_STEW, "BUFF", "REGENERATION:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200),
            Quest("healer_med_4", "Poison Master", listOf("Heal the poison effect with", "a bucket of milk 5 times"), Material.MILK_BUCKET, "ITEM", "POTION:5", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200),
            Quest("healer_med_5", "Heart Sacrifice", listOf("Sacrifice 1 heart to", "the quest master"), Material.REDSTONE, "ITEM", "TOTEM_OF_UNDYING:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_forest", timeLimit = 1200)
        )
    }
    
    // ==================== MAGE QUESTS (Merlin) ====================
    
    private fun createMageQuests(): List<Quest> {
        return listOf(
            // EASY
            Quest("mage_easy_1", "Fire Mage", listOf("Endure 30 hearts of", "fire damage"), Material.FIRE_CHARGE, "ITEM", "FIRE_RESISTANCE_POTION:2", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_light", timeLimit = 900),
            Quest("mage_easy_2", "Poison Mage", listOf("Endure 15 hearts of", "poison damage"), Material.SPIDER_EYE, "ITEM", "MILK_BUCKET:2", QuestDifficulty.EASY, QuestCategory.CLASS_SPECIFIC, "temple_light", timeLimit = 900),
            
            // MEDIUM
            Quest("mage_med_1", "Fire Master", listOf("Endure 80 hearts of", "fire damage"), Material.BLAZE_POWDER, "BUFF", "FIRE_RESISTANCE:2:900", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_light", timeLimit = 1200),
            Quest("mage_med_2", "Poison Master", listOf("Endure 50 hearts of", "poison damage"), Material.FERMENTED_SPIDER_EYE, "ITEM", "MILK_BUCKET:5", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_light", timeLimit = 1200),
            Quest("mage_med_3", "Heart Sacrifice", listOf("Sacrifice 1 heart to", "the quest master"), Material.REDSTONE, "ITEM", "TOTEM_OF_UNDYING:1", QuestDifficulty.MEDIUM, QuestCategory.CLASS_SPECIFIC, "temple_light", timeLimit = 1200)
        )
    }
}
