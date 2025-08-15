package net.dungeonhub.events.questmaster.gui

import net.dungeonhub.events.BattleRoyaleEvent
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Manages quest configuration loading and saving
 */
class QuestConfig(private val plugin: BattleRoyaleEvent) {
    
    private val questFile = File(plugin.dataFolder, "quests.yml")
    private lateinit var config: YamlConfiguration
    
    fun load(): YamlConfiguration {
        if (!questFile.exists()) {
            createDefaultQuestConfig()
        }
        config = YamlConfiguration.loadConfiguration(questFile)
        return config
    }
    
    fun save() {
        config.save(questFile)
    }
    
    private fun createDefaultQuestConfig() {
        plugin.dataFolder.mkdirs()
        questFile.createNewFile()
        
        val defaultConfig = YamlConfiguration()
        
        // Header
        defaultConfig.options().header("""
            |Dungeon Hub Battle Royale - Quest Configuration
            |
            |This file defines the 5 quests for each Quest Master.
            |Each Quest Master can have exactly 5 quests.
            |
            |Configuration Format:
            |quest-masters:
            |  <quest_master_id>:
            |    quests:
            |      <quest_number>:
            |        id: "unique_quest_id"
            |        name: "Display Name"
            |        description:
            |          - "First line of description"
            |          - "Second line of description"
            |        icon: "MATERIAL_NAME"
            |        reward-type: "ITEM/BUFF/MESSAGE"
            |        reward-data: "reward specific data"
            |        requirements:
            |          level: 0
            |          completed-quests: []
            |          items: {}
            |        available: true
            |
        """.trimMargin())
        
        // Create quest configurations for each temple
        createTempleQuests(defaultConfig, "temple_zeroth", "Shadow & Darkness")
        createTempleQuests(defaultConfig, "temple_fire", "Flame & Fury")
        createTempleQuests(defaultConfig, "temple_ice", "Frost & Resilience")
        createTempleQuests(defaultConfig, "temple_forest", "Nature & Growth")
        createTempleQuests(defaultConfig, "temple_light", "Radiance & Ascension")
        
        defaultConfig.save(questFile)
    }
    
    private fun createTempleQuests(config: YamlConfiguration, questMasterId: String, theme: String) {
        val basePath = "quest-masters.$questMasterId"
        
        when (questMasterId) {
            "temple_zeroth" -> {
                createQuest(config, "$basePath.quests.1", "zeroth_1", "Shadow Hunter", 
                    listOf("Eliminate 3 opponents in darkness", "without being detected"), 
                    "IRON_SWORD", "ITEM", "POTION:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.2", "zeroth_2", "Night Walker", 
                    listOf("Survive for 5 minutes at night", "without using torches"), 
                    "BLACK_WOOL", "BUFF", "NIGHT_VISION:1:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.3", "zeroth_3", "Void Stalker", 
                    listOf("Travel 1000 blocks in darkness", "without taking damage"), 
                    "LEATHER_BOOTS", "ITEM", "ENDER_PEARL:5", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.4", "zeroth_4", "Shadow Strike", 
                    listOf("Get 5 eliminations while", "invisible"), 
                    "FERMENTED_SPIDER_EYE", "BUFF", "INVISIBILITY:1:300", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.5", "zeroth_5", "Master of Shadows", 
                    listOf("Complete all Shadow Temple", "trials"), 
                    "NETHER_STAR", "ITEM", "DIAMOND_BOOTS:1", mapOf(), listOf("zeroth_1", "zeroth_2", "zeroth_3", "zeroth_4"), true)
            }
            
            "temple_fire" -> {
                createQuest(config, "$basePath.quests.1", "fire_1", "Flame Bearer", 
                    listOf("Set 10 opponents on fire", "using flame weapons"), 
                    "FLINT_AND_STEEL", "ITEM", "FIRE_CHARGE:5", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.2", "fire_2", "Inferno Walker", 
                    listOf("Survive in lava for", "30 seconds"), 
                    "MAGMA_CREAM", "BUFF", "FIRE_RESISTANCE:1:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.3", "fire_3", "Blazing Warrior", 
                    listOf("Deal 500 fire damage", "to opponents"), 
                    "BLAZE_ROD", "ITEM", "ENCHANTED_BOOK:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.4", "fire_4", "Phoenix Rising", 
                    listOf("Resurrect 3 times using", "fire resistance"), 
                    "PHOENIX_FEATHER", "ITEM", "TOTEM_OF_UNDYING:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.5", "fire_5", "Master of Flames", 
                    listOf("Prove your mastery over", "all things fire"), 
                    "NETHER_STAR", "BUFF", "FIRE_RESISTANCE:2:1800", mapOf(), listOf("fire_1", "fire_2", "fire_3", "fire_4"), true)
            }
            
            "temple_ice" -> {
                createQuest(config, "$basePath.quests.1", "ice_1", "Frost Walker", 
                    listOf("Freeze 10 opponents using", "ice-based attacks"), 
                    "ICE", "ITEM", "SNOWBALL:16", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.2", "ice_2", "Frozen Guardian", 
                    listOf("Survive in snow biome", "for 10 minutes"), 
                    "PACKED_ICE", "BUFF", "RESISTANCE:1:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.3", "ice_3", "Blizzard Master", 
                    listOf("Create a snowstorm that", "affects 5 players"), 
                    "SNOW_BLOCK", "ITEM", "ENCHANTED_BOOK:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.4", "ice_4", "Crystal Shield", 
                    listOf("Block 100 damage using", "ice-based defenses"), 
                    "BLUE_ICE", "BUFF", "ABSORPTION:2:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.5", "ice_5", "Master of Frost", 
                    listOf("Become one with the", "eternal winter"), 
                    "NETHER_STAR", "ITEM", "DIAMOND_CHESTPLATE:1", mapOf(), listOf("ice_1", "ice_2", "ice_3", "ice_4"), true)
            }
            
            "temple_forest" -> {
                createQuest(config, "$basePath.quests.1", "forest_1", "Nature's Ally", 
                    listOf("Tame 5 animals to fight", "alongside you"), 
                    "BONE", "ITEM", "LEAD:3", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.2", "forest_2", "Tree Hugger", 
                    listOf("Plant 50 saplings during", "the battle"), 
                    "OAK_SAPLING", "BUFF", "REGENERATION:1:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.3", "forest_3", "Woodland Warrior", 
                    listOf("Get 10 eliminations using", "wooden weapons"), 
                    "WOODEN_SWORD", "ITEM", "ENCHANTED_BOOK:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.4", "forest_4", "Guardian of Green", 
                    listOf("Protect a forest area", "for 15 minutes"), 
                    "OAK_LOG", "BUFF", "SATURATION:1:1200", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.5", "forest_5", "Master of Nature", 
                    listOf("Prove your harmony with", "all living things"), 
                    "NETHER_STAR", "ITEM", "DIAMOND_AXE:1", mapOf(), listOf("forest_1", "forest_2", "forest_3", "forest_4"), true)
            }
            
            "temple_light" -> {
                createQuest(config, "$basePath.quests.1", "light_1", "Beacon of Hope", 
                    listOf("Light up 100 blocks with", "torches or glowstone"), 
                    "TORCH", "ITEM", "GLOWSTONE:8", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.2", "light_2", "Divine Healer", 
                    listOf("Heal 10 players using", "light-based abilities"), 
                    "GOLDEN_APPLE", "BUFF", "REGENERATION:2:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.3", "light_3", "Radiant Warrior", 
                    listOf("Deal 500 damage while", "standing in sunlight"), 
                    "GLOWSTONE_DUST", "ITEM", "ENCHANTED_BOOK:1", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.4", "light_4", "Solar Flare", 
                    listOf("Blind 5 opponents using", "bright light attacks"), 
                    "DAYLIGHT_DETECTOR", "BUFF", "GLOWING:1:600", mapOf(), listOf(), true)
                    
                createQuest(config, "$basePath.quests.5", "light_5", "Master of Radiance", 
                    listOf("Ascend to become the", "ultimate light bearer"), 
                    "NETHER_STAR", "ITEM", "ELYTRA:1", mapOf(), listOf("light_1", "light_2", "light_3", "light_4"), true)
            }
        }
    }
    
    private fun createQuest(
        config: YamlConfiguration, 
        path: String, 
        id: String, 
        name: String, 
        description: List<String>, 
        icon: String, 
        rewardType: String, 
        rewardData: String,
        items: Map<String, Int>,
        completedQuests: List<String>,
        available: Boolean
    ) {
        config.set("$path.id", id)
        config.set("$path.name", name)
        config.set("$path.description", description)
        config.set("$path.icon", icon)
        config.set("$path.reward-type", rewardType)
        config.set("$path.reward-data", rewardData)
        config.set("$path.requirements.level", 0)
        config.set("$path.requirements.items", items)
        config.set("$path.requirements.completed-quests", completedQuests)
        config.set("$path.available", available)
    }
    
    /**
     * Loads quests for a specific Quest Master from configuration
     */
    fun loadQuestsForQuestMaster(questMasterId: String): List<Quest> {
        val quests = mutableListOf<Quest>()
        val basePath = "quest-masters.$questMasterId.quests"
        
        val questSection = config.getConfigurationSection(basePath)
        if (questSection != null) {
            for (questNumber in 1..5) {
                val questPath = "$questNumber"
                val section = questSection.getConfigurationSection(questPath)
                
                if (section != null) {
                    val quest = Quest(
                        id = section.getString("id") ?: "${questMasterId}_$questNumber",
                        name = section.getString("name") ?: "Quest $questNumber",
                        description = section.getStringList("description"),
                        icon = Material.valueOf(section.getString("icon") ?: "BOOK"),
                        rewardType = section.getString("reward-type") ?: "ITEM",
                        rewardData = section.getString("reward-data") ?: "BREAD:1",
                        requirements = QuestRequirements(
                            minimumLevel = section.getInt("requirements.level", 0),
                            completedQuests = section.getStringList("requirements.completed-quests")
                        ),
                        isAvailable = section.getBoolean("available", true)
                    )
                    quests.add(quest)
                }
            }
        }
        
        return quests
    }
}
