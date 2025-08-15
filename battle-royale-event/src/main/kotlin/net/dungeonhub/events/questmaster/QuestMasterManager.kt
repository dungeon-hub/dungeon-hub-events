package net.dungeonhub.events.questmaster

import net.dungeonhub.events.BattleRoyaleEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Villager
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.*

/**
 * Manages all Quest Master NPCs and player interactions
 */
class QuestMasterManager(private val plugin: BattleRoyaleEvent) {
    // Update a quest master in memory
    fun updateQuestMaster(questId: String, updated: QuestMaster) {
        questMasters[questId] = updated
    }

    // Expose config for command use if needed
    fun getConfig(): YamlConfiguration = config
    
    private val questMasters = mutableMapOf<String, QuestMaster>()
    private val playerProgress = mutableMapOf<UUID, QuestProgress>()
    private val configFile = File(plugin.dataFolder, "questmasters.yml")
    private lateinit var config: YamlConfiguration
    
    /**
     * Initializes the Quest Master system
     */
    fun initialize() {
        loadConfig()
        loadQuestMasters()
        spawnAllQuestMasters()
    }
    
    /**
     * Loads the configuration file
     */
    private fun loadConfig() {
        val validTemples = listOf(
            mapOf("id" to "zeroth", "name" to "Shadow Temple", "x" to 100, "y" to 70, "z" to 100, "rewardType" to "BUFF", "rewardData" to "SPEED:2:300"),
            mapOf("id" to "fire", "name" to "Fire Temple", "x" to -100, "y" to 70, "z" to 100, "rewardType" to "ITEM", "rewardData" to "GOLDEN_APPLE:2"),
            mapOf("id" to "ice", "name" to "Ice Temple", "x" to 100, "y" to 70, "z" to -100, "rewardType" to "BUFF", "rewardData" to "RESISTANCE:1:600"),
            mapOf("id" to "forest", "name" to "Forest Temple", "x" to -100, "y" to 70, "z" to -100, "rewardType" to "ITEM", "rewardData" to "DIAMOND_SWORD:1"),
            mapOf("id" to "light", "name" to "Light Temple", "x" to 0, "y" to 80, "z" to 0, "rewardType" to "ABILITY", "rewardData" to "JUMP_BOOST:3:900")
        )
        val validTempleIds = validTemples.map { "temple_${it["id"]}" }.toSet()
        if (!configFile.exists()) {
            createDefaultConfig()
        }
        config = YamlConfiguration.loadConfiguration(configFile)
        // Purge legacy quest masters
        val questmastersSection = config.getConfigurationSection("questmasters")
        if (questmastersSection != null) {
            val toRemove = questmastersSection.getKeys(false).filter { it !in validTempleIds }
            toRemove.forEach { questmastersSection.set(it, null) }
        }
        // Add any missing quest masters
        validTemples.forEach { temple ->
            val questId = "temple_${temple["id"]}"
            if (config.getConfigurationSection("questmasters.$questId") == null) {
                config.set("questmasters.$questId.name", "Quest Master ${temple["name"]}")
                config.set("questmasters.$questId.world", "world")
                config.set("questmasters.$questId.x", temple["x"])
                config.set("questmasters.$questId.y", temple["y"])
                config.set("questmasters.$questId.z", temple["z"])
                config.set("questmasters.$questId.message", "Welcome to the ${temple["name"]}! I grant you ancient power.")
                config.set("questmasters.$questId.one-time-use", true)
                config.set("questmasters.$questId.cooldown", 0)
                config.set("questmasters.$questId.reward-type", temple["rewardType"])
                config.set("questmasters.$questId.reward-data", temple["rewardData"])
            }
        }
        config.save(configFile)
    }
    
    /**
     * Creates a default configuration with example Quest Masters
     */
    private fun createDefaultConfig() {
        plugin.dataFolder.mkdirs()
        configFile.createNewFile()
        
        val defaultConfig = YamlConfiguration()
        
        // Example Quest Masters for 5 temples
        val temples = listOf(
            mapOf("id" to "zeroth", "name" to "Shadow Temple", "x" to 100, "y" to 70, "z" to 100, "rewardType" to "BUFF", "rewardData" to "SPEED:2:300"),
            mapOf("id" to "fire", "name" to "Fire Temple", "x" to -100, "y" to 70, "z" to 100, "rewardType" to "ITEM", "rewardData" to "GOLDEN_APPLE:2"),
            mapOf("id" to "ice", "name" to "Ice Temple", "x" to 100, "y" to 70, "z" to -100, "rewardType" to "BUFF", "rewardData" to "RESISTANCE:1:600"),
            mapOf("id" to "forest", "name" to "Forest Temple", "x" to -100, "y" to 70, "z" to -100, "rewardType" to "ITEM", "rewardData" to "DIAMOND_SWORD:1"),
            mapOf("id" to "light", "name" to "Light Temple", "x" to 0, "y" to 80, "z" to 0, "rewardType" to "ABILITY", "rewardData" to "JUMP_BOOST:3:900")
        )

        temples.forEach { temple ->
            val questId = "temple_${temple["id"]}"
            defaultConfig.set("questmasters.$questId.name", "Quest Master ${temple["name"]}")
            defaultConfig.set("questmasters.$questId.world", "world")
            defaultConfig.set("questmasters.$questId.x", temple["x"])
            defaultConfig.set("questmasters.$questId.y", temple["y"])
            defaultConfig.set("questmasters.$questId.z", temple["z"])
            defaultConfig.set("questmasters.$questId.message", "Welcome to the ${temple["name"]}! I grant you ancient power.")
            defaultConfig.set("questmasters.$questId.one-time-use", true)
            defaultConfig.set("questmasters.$questId.cooldown", 0)
            defaultConfig.set("questmasters.$questId.reward-type", temple["rewardType"])
            defaultConfig.set("questmasters.$questId.reward-data", temple["rewardData"])
        }
        
        defaultConfig.save(configFile)
    }
    
    /**
     * Loads Quest Masters from configuration
     */
    private fun loadQuestMasters() {
        questMasters.clear()
        
        val questMasterSection = config.getConfigurationSection("questmasters") ?: return
        
        for (questId in questMasterSection.getKeys(false)) {
            val section = questMasterSection.getConfigurationSection(questId) ?: continue
            
            val worldName = section.getString("world", "world") ?: "world"
            val world = Bukkit.getWorld(worldName)
            
            if (world == null) {
                plugin.logger.warning("World '$worldName' not found for Quest Master '$questId'")
                continue
            }
            
            val location = Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z")
            )
            
            val questMaster = QuestMaster(
                questId = questId,
                name = section.getString("name") ?: "Quest Master",
                location = location,
                message = section.getString("message") ?: "Greetings, adventurer!",
                isOneTimeUse = section.getBoolean("one-time-use", true),
                cooldownSeconds = section.getInt("cooldown", 0),
                rewardType = RewardType.valueOf(section.getString("reward-type") ?: "NONE"),
                rewardData = section.getString("reward-data")
            )
            
            questMasters[questId] = questMaster
        }
        
        plugin.logger.info("Loaded ${questMasters.size} Quest Masters")
    }
    
    /**
     * Spawns all Quest Masters in the world
     */
    fun spawnAllQuestMasters() {
        questMasters.values.forEach { spawnQuestMaster(it) }
    }
    
    /**
     * Spawns a single Quest Master
     */
    fun spawnQuestMaster(questMaster: QuestMaster) {
        if (questMaster.isSpawned()) {
            questMaster.removeVillager()
        }
        
        val location = questMaster.location
        val villager = location.world.spawnEntity(location, EntityType.VILLAGER) as org.bukkit.entity.Villager
        
        villager.apply {
            customName = "${ChatColor.GOLD}${questMaster.name}"
            isCustomNameVisible = true
            setAI(false)  // Disable AI so villager doesn't move
            isInvulnerable = true
            setCanPickupItems(false)
            isSilent = true  // No villager sounds
            
            // Set villager profession to make them look unique
            profession = when (questMaster.questId) {
                "temple_zeroth" -> org.bukkit.entity.Villager.Profession.CLERIC
                "temple_fire" -> org.bukkit.entity.Villager.Profession.WEAPONSMITH
                "temple_ice" -> org.bukkit.entity.Villager.Profession.LIBRARIAN
                "temple_forest" -> org.bukkit.entity.Villager.Profession.FARMER
                "temple_light" -> org.bukkit.entity.Villager.Profession.CARTOGRAPHER
                else -> org.bukkit.entity.Villager.Profession.CLERIC
            }
            
            // Set villager type for visual variety
            villagerType = when (questMaster.questId) {
                "temple_zeroth" -> org.bukkit.entity.Villager.Type.SWAMP
                "temple_fire" -> org.bukkit.entity.Villager.Type.DESERT
                "temple_ice" -> org.bukkit.entity.Villager.Type.SNOW
                "temple_forest" -> org.bukkit.entity.Villager.Type.JUNGLE
                "temple_light" -> org.bukkit.entity.Villager.Type.PLAINS
                else -> org.bukkit.entity.Villager.Type.PLAINS
            }
            // Remove all trades to prevent trading menu
            recipes = emptyList()
        }
        
        questMaster.setVillager(villager)
    }
    
    /**
     * Handles player interaction with a Quest Master
     */
    fun handleInteraction(player: Player, questMaster: QuestMaster) {
        val progress = getOrCreateProgress(player)
        
        if (!progress.canInteract(questMaster)) {
            if (questMaster.isOneTimeUse && progress.completedQuests.contains(questMaster.questId)) {
                player.sendMessage("${ChatColor.YELLOW}You have already completed this quest.")
            } else {
                val remaining = progress.getRemainingCooldown(questMaster.questId)
                player.sendMessage("${ChatColor.YELLOW}You must wait $remaining seconds before interacting again.")
            }
            return
        }
        
        // Send message
        player.sendMessage("${ChatColor.AQUA}${questMaster.message}")
        
        // Give reward
        giveReward(player, questMaster)
        
        // Record interaction
        progress.recordInteraction(questMaster)
        
        // Visual/audio feedback
        player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }
    
    /**
     * Gives a reward to the player based on the Quest Master's reward type
     */
    private fun giveReward(player: Player, questMaster: QuestMaster) {
        val rewardData = questMaster.rewardData ?: return
        
        when (questMaster.rewardType) {
            RewardType.ITEM -> {
                val parts = rewardData.split(":")
                if (parts.size >= 2) {
                    val material = Material.valueOf(parts[0])
                    val amount = parts[1].toIntOrNull() ?: 1
                    val item = ItemStack(material, amount)
                    player.inventory.addItem(item)
                    player.sendMessage("${ChatColor.GREEN}Received ${amount}x ${material.name.replace("_", " ").lowercase()}!")
                }
            }
            
            RewardType.BUFF -> {
                val parts = rewardData.split(":")
                if (parts.size >= 3) {
                    val effectType = PotionEffectType.getByName(parts[0])
                    val amplifier = parts[1].toIntOrNull() ?: 0
                    val duration = parts[2].toIntOrNull() ?: 600 // 30 seconds default
                    
                    if (effectType != null) {
                        val effect = PotionEffect(effectType, duration, amplifier, false, true, true)
                        player.addPotionEffect(effect)
                        player.sendMessage("${ChatColor.GREEN}Received ${effectType.name.replace("_", " ").lowercase()} buff!")
                    }
                }
            }
            
            RewardType.ABILITY -> {
                // For now, treat abilities as buffs
                giveReward(player, questMaster.copy(rewardType = RewardType.BUFF))
                player.sendMessage("${ChatColor.LIGHT_PURPLE}You have been granted a special ability!")
            }
            
            RewardType.MESSAGE -> {
                player.sendMessage("${ChatColor.LIGHT_PURPLE}$rewardData")
            }
            
            RewardType.NONE -> {
                // No reward
            }
        }
    }
    
    /**
     * Gets or creates quest progress for a player
     */
    private fun getOrCreateProgress(player: Player): QuestProgress {
        return playerProgress.getOrPut(player.uniqueId) {
            QuestProgress(player.uniqueId.toString())
        }
    }
    
    /**
     * Gets a Quest Master by ID
     */
    fun getQuestMaster(questId: String): QuestMaster? = questMasters[questId]
    
    /**
     * Gets all Quest Masters
     */
    fun getAllQuestMasters(): Collection<QuestMaster> = questMasters.values
    
    /**
     * Finds the nearest Quest Master to a location
     */
    fun getNearestQuestMaster(location: Location, maxDistance: Double = 3.0): QuestMaster? {
        return questMasters.values
            .filter { it.location.world == location.world }
            .filter { it.location.distance(location) <= maxDistance }
            .minByOrNull { it.location.distance(location) }
    }
    
    /**
     * Saves configuration
     */
    fun saveConfig() {
        config.save(configFile)
    }
    
    /**
     * Reloads the Quest Master system
     */
    fun reload() {
        cleanup()
        loadConfig()
        loadQuestMasters()
        spawnAllQuestMasters()
    }
    
    /**
     * Despawns a specific Quest Master
     */
    fun despawnQuestMaster(questId: String) {
        val questMaster = questMasters[questId]
        questMaster?.removeVillager()
    }
    
    /**
     * Cleans up all Quest Masters
     */
    fun cleanup() {
        questMasters.values.forEach { it.removeVillager() }
        questMasters.clear()
        playerProgress.clear()
    }
}
