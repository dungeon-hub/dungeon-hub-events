package net.dungeonhub.events.questmaster

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Handles Quest Master configuration management
 */
class QuestMasterConfig(private val configFile: File) {
    private lateinit var config: YamlConfiguration
    
    fun load(): YamlConfiguration {
        if (!configFile.exists()) {
            createDefaultConfig()
        }
        config = YamlConfiguration.loadConfiguration(configFile)
        return config
    }
    
    fun save() {
        config.save(configFile)
    }
    
    private fun createDefaultConfig() {
        configFile.parentFile?.mkdirs()
        configFile.createNewFile()
        
        val defaultConfig = YamlConfiguration()
        
        // Header comments
        defaultConfig.options().header("""
            |Dungeon Hub Battle Royale - Quest Master Configuration
            |
            |This file defines the Quest Masters (NPCs) that will be spawned in your world.
            |Each Quest Master provides unique rewards and interactions for players.
            |
            |Configuration Format:
            |questmasters:
            |  <quest_id>:
            |    name: "Display name of the NPC"
            |    world: "world_name"
            |    x: 100.0
            |    y: 70.0
            |    z: 100.0
            |    message: "Message shown when interacting"
            |    one-time-use: true/false
            |    cooldown: 0 (seconds)
            |    reward-type: NONE/ITEM/BUFF/ABILITY/MESSAGE
            |    reward-data: "reward-specific data"
            |
            |Reward Data Formats:
            |  ITEM: "MATERIAL:AMOUNT" (e.g., "GOLDEN_APPLE:2")
            |  BUFF: "EFFECT:AMPLIFIER:DURATION" (e.g., "SPEED:1:600")
            |  ABILITY: Same as BUFF (e.g., "JUMP_BOOST:2:900")
            |  MESSAGE: "Custom message text"
            |  NONE: Can be empty or omitted
            |
        """.trimMargin())
        
        // Create 5 temple Quest Masters
        createTempleQuestMaster(defaultConfig, "temple_zeroth", "Shadow Temple", 100, 70, 100, 
            "Welcome to the Shadow Temple! I grant you swiftness through darkness.", "BUFF", "SPEED:1:600")
            
        createTempleQuestMaster(defaultConfig, "temple_fire", "Fire Temple", -100, 70, 100,
            "Greetings from the Fire Temple! Take this blazing sustenance.", "ITEM", "GOLDEN_APPLE:2")
            
        createTempleQuestMaster(defaultConfig, "temple_ice", "Ice Temple", 100, 70, -100,
            "The Ice Temple offers protection from the frozen depths.", "BUFF", "DAMAGE_RESISTANCE:0:600")
            
        createTempleQuestMaster(defaultConfig, "temple_forest", "Forest Temple", -100, 70, -100,
            "From the Forest Temple, receive this blade of nature's power.", "ITEM", "DIAMOND_SWORD:1")
            
        createTempleQuestMaster(defaultConfig, "temple_light", "Light Temple", 0, 80, 0,
            "The radiant Light Temple grants the power of great leaps!", "ABILITY", "JUMP_BOOST:2:900")
        
        // Save the default configuration
        defaultConfig.save(configFile)
    }
    
    private fun createTempleQuestMaster(
        config: YamlConfiguration,
        questId: String,
        templeName: String,
        x: Int,
        y: Int,
        z: Int,
        message: String,
        rewardType: String,
        rewardData: String
    ) {
        val path = "questmasters.$questId"
        config.set("$path.name", "Quest Master $templeName")
        config.set("$path.world", "world")
        config.set("$path.x", x.toDouble())
        config.set("$path.y", y.toDouble())
        config.set("$path.z", z.toDouble())
        config.set("$path.message", message)
        config.set("$path.one-time-use", true)
        config.set("$path.cooldown", 0)
        config.set("$path.reward-type", rewardType)
        config.set("$path.reward-data", rewardData)
    }
    
    /**
     * Adds a new Quest Master to the configuration
     */
    fun addQuestMaster(questMaster: QuestMaster) {
        val path = "questmasters.${questMaster.questId}"
        config.set("$path.name", questMaster.name)
        config.set("$path.world", questMaster.location.world?.name ?: "world")
        config.set("$path.x", questMaster.location.x)
        config.set("$path.y", questMaster.location.y)
        config.set("$path.z", questMaster.location.z)
        config.set("$path.message", questMaster.message)
        config.set("$path.one-time-use", questMaster.isOneTimeUse)
        config.set("$path.cooldown", questMaster.cooldownSeconds)
        config.set("$path.reward-type", questMaster.rewardType.name)
        config.set("$path.reward-data", questMaster.rewardData)
        save()
    }
    
    /**
     * Removes a Quest Master from the configuration
     */
    fun removeQuestMaster(questId: String) {
        config.set("questmasters.$questId", null)
        save()
    }
    
    /**
     * Updates a Quest Master's location in the configuration
     */
    fun updateQuestMasterLocation(questId: String, location: org.bukkit.Location) {
        val path = "questmasters.$questId"
        config.set("$path.world", location.world?.name)
        config.set("$path.x", location.x)
        config.set("$path.y", location.y)
        config.set("$path.z", location.z)
        save()
    }
}
