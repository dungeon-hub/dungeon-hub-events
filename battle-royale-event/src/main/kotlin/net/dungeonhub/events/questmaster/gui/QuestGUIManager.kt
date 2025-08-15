package net.dungeonhub.events.questmaster.gui

import net.dungeonhub.events.BattleRoyaleEvent
import net.dungeonhub.events.questmaster.QuestMaster
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Manages the Quest Master GUI system
 */
class QuestGUIManager(private val plugin: BattleRoyaleEvent) : Listener {
    
    private val openGUIs = mutableMapOf<UUID, String>() // Player UUID -> Quest Master ID
    private val playerQuestProgress = mutableMapOf<UUID, PlayerQuestProgress>()
    private val questConfig = QuestConfig(plugin)
    
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        questConfig.load()
    }
    
    /**
     * Opens the Quest Master GUI for a player
     */
    fun openQuestGUI(player: Player, questMaster: QuestMaster, quests: List<Quest>) {
        val inventory = Bukkit.createInventory(null, 27, "§6${questMaster.name} - Quests")
        
        // Add quest items (slots 10-14 for 5 quests)
        val questSlots = listOf(10, 11, 12, 13, 14)
        val playerProgress = getPlayerProgress(player)
        
        quests.forEachIndexed { index, quest ->
            if (index < questSlots.size) {
                val slot = questSlots[index]
                val questItem = createQuestItem(quest, playerProgress)
                inventory.setItem(slot, questItem)
            }
        }
        
        // Add decorative items
        addDecorativeItems(inventory)
        
        // Add close button
        val closeItem = ItemStack(Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta?.setDisplayName("§cClose")
        closeMeta?.lore = listOf("§7Click to close this menu")
        closeItem.itemMeta = closeMeta
        inventory.setItem(22, closeItem)
        
        // Track open GUI
        openGUIs[player.uniqueId] = questMaster.questId
        
        player.openInventory(inventory)
    }
    
    /**
     * Creates a quest item with proper status and styling
     */
    private fun createQuestItem(quest: Quest, playerProgress: PlayerQuestProgress): ItemStack {
        val isCompleted = playerProgress.isQuestCompleted(quest.id)
        val isActive = playerProgress.isQuestActive(quest.id)
        
        val material = when {
            isCompleted -> Material.LIME_DYE  // Green for completed
            isActive -> Material.YELLOW_DYE   // Yellow for active
            quest.isAvailable -> quest.icon   // Original icon for available
            else -> Material.GRAY_DYE         // Gray for unavailable
        }
        
        val item = ItemStack(material)
        val meta = item.itemMeta
        
        meta?.let {
            val displayName = when {
                isCompleted -> "§a✓ ${quest.name}"
                isActive -> "§e⚡ ${quest.name}"
                quest.isAvailable -> "§6${quest.name}"
                else -> "§8${quest.name}"
            }
            it.setDisplayName(displayName)
            
            val lore = mutableListOf<String>()
            
            // Description
            lore.add("§7Description:")
            quest.description.forEach { line -> lore.add("§f  $line") }
            lore.add("")
            
            // Status
            when {
                isCompleted -> {
                    lore.add("§a✓ COMPLETED")
                    lore.add("§7You have already finished this quest.")
                }
                isActive -> {
                    lore.add("§e⚡ IN PROGRESS")
                    val objective = playerProgress.activeQuests[quest.id]
                    if (objective != null) {
                        lore.add("§7Progress: §f${objective.getProgressText()}")
                    }
                }
                quest.isAvailable -> {
                    lore.add("§a► AVAILABLE")
                    lore.add("§7Click to accept this quest!")
                }
                else -> {
                    lore.add("§c✗ NOT AVAILABLE")
                    if (quest.requirements.hasRequirements()) {
                        lore.add("§7Requirements:")
                        quest.requirements.getDisplayText().forEach { req -> 
                            lore.add("§c  ✗ $req") 
                        }
                    }
                }
            }
            
            lore.add("")
            lore.add("§7Reward: §f${quest.rewardData}")
            
            // Add quest type info
            lore.add("§7Type: §f${quest.rewardType}")
            
            it.lore = lore
            item.itemMeta = it
        }
        
        return item
    }
    
    /**
     * Adds decorative items to make the GUI look better
     */
    private fun addDecorativeItems(inventory: Inventory) {
        val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val glassMeta = glass.itemMeta
        glassMeta?.setDisplayName(" ")
        glass.itemMeta = glassMeta
        
        // Fill borders with glass panes
        val borderSlots = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 23, 24, 25, 26)
        borderSlots.forEach { slot ->
            inventory.setItem(slot, glass)
        }
        
        // Add quest master info item
        val infoItem = ItemStack(Material.ENCHANTED_BOOK)
        val infoMeta = infoItem.itemMeta
        infoMeta?.setDisplayName("§6Quest Master")
        infoMeta?.lore = listOf(
            "§7Welcome to my quest offerings!",
            "§7Choose a quest to begin your journey.",
            "",
            "§7Available quests: §a5",
            "§7Complete quests to earn rewards!"
        )
        infoItem.itemMeta = infoMeta
        inventory.setItem(4, infoItem)
    }
    
    /**
     * Handles inventory click events
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val questMasterId = openGUIs[player.uniqueId] ?: return
        
        if (event.view.title.contains("Quests")) {
            event.isCancelled = true
            
            val clickedItem = event.currentItem ?: return
            val slot = event.slot
            
            when (slot) {
                22 -> {
                    // Close button
                    player.closeInventory()
                    return
                }
                10, 11, 12, 13, 14 -> {
                    // Quest slots
                    handleQuestClick(player, questMasterId, slot, clickedItem)
                }
            }
        }
    }
    
    /**
     * Handles when a quest is clicked
     */
    private fun handleQuestClick(player: Player, questMasterId: String, slot: Int, clickedItem: ItemStack) {
        val questIndex = when (slot) {
            10 -> 0
            11 -> 1
            12 -> 2
            13 -> 3
            14 -> 4
            else -> return
        }
        
        // Get the quest master and its quests
        val questMaster = plugin.questMasterManager.getQuestMaster(questMasterId) ?: return
        val quests = getQuestsForQuestMaster(questMasterId)
        
        if (questIndex >= quests.size) return
        val quest = quests[questIndex]
        
        val playerProgress = getPlayerProgress(player)
        
        when {
            playerProgress.isQuestCompleted(quest.id) -> {
                player.sendMessage("§cYou have already completed this quest!")
            }
            playerProgress.isQuestActive(quest.id) -> {
                player.sendMessage("§eThis quest is already in progress!")
                // Show progress
                val objective = playerProgress.activeQuests[quest.id]
                if (objective != null) {
                    player.sendMessage("§7Progress: ${objective.getProgressText()}")
                }
            }
            !quest.isAvailable -> {
                player.sendMessage("§cThis quest is not available yet!")
                if (quest.requirements.hasRequirements()) {
                    player.sendMessage("§7Requirements:")
                    quest.requirements.getDisplayText().forEach { req ->
                        player.sendMessage("§c  ✗ $req")
                    }
                }
            }
            else -> {
                // Start the quest
                startQuest(player, quest)
                player.closeInventory()
            }
        }
    }
    
    /**
     * Starts a quest for a player
     */
    private fun startQuest(player: Player, quest: Quest) {
        val playerProgress = getPlayerProgress(player)
        
        // Create quest objective based on quest type
        val objective = createQuestObjective(quest)
        playerProgress.startQuest(quest.id, objective)
        
        player.sendMessage("§a✓ Quest Started: §f${quest.name}")
        player.sendMessage("§7Objective: ${objective.getProgressText()}")
        
        // Play sound
        player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f)
    }
    
    /**
     * Creates a quest objective based on the quest configuration
     */
    private fun createQuestObjective(quest: Quest): QuestObjective {
        // This is a simple implementation - you can expand this based on your quest types
        return when (quest.rewardType.uppercase()) {
            "KILL" -> QuestObjective(ObjectiveType.KILL_MOBS, "players", 0, 1)
            "COLLECT" -> QuestObjective(ObjectiveType.COLLECT_ITEMS, "items", 0, 5)
            "SURVIVE" -> QuestObjective(ObjectiveType.SURVIVE_TIME, "time", 0, 300) // 5 minutes
            "EXPLORE" -> QuestObjective(ObjectiveType.REACH_LOCATION, "location", 0, 1)
            else -> QuestObjective(ObjectiveType.CUSTOM, "objective", 0, 1, mapOf<String, String>("description" to (quest.description.firstOrNull() ?: "Complete the objective")))
        }
    }
    
    /**
     * Handles inventory close events
     */
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        openGUIs.remove(player.uniqueId)
    }
    
    /**
     * Gets or creates player quest progress
     */
    fun getPlayerProgress(player: Player): PlayerQuestProgress {
        return playerQuestProgress.getOrPut(player.uniqueId) {
            PlayerQuestProgress(player.uniqueId.toString())
        }
    }
    
    /**
     * Gets the quests for a specific Quest Master
     * This loads from configuration
     */
    fun getQuestsForQuestMaster(questMasterId: String): List<Quest> {
        return questConfig.loadQuestsForQuestMaster(questMasterId)
    }
    
    /**
     * Creates default quests for each Quest Master
     */
    private fun getDefaultQuests(questMasterId: String): List<Quest> {
        return when (questMasterId) {
            "temple_zeroth" -> listOf(
                Quest("zeroth_1", "Shadow Hunter", listOf("Eliminate 3 opponents in darkness", "without being detected"), Material.IRON_SWORD, "ITEM", "POTION:1"),
                Quest("zeroth_2", "Night Walker", listOf("Survive for 5 minutes at night", "without using torches"), Material.BLACK_WOOL, "BUFF", "NIGHT_VISION:1:600"),
                Quest("zeroth_3", "Void Stalker", listOf("Travel 1000 blocks in darkness", "without taking damage"), Material.LEATHER_BOOTS, "ITEM", "ENDER_PEARL:5"),
                Quest("zeroth_4", "Shadow Strike", listOf("Get 5 eliminations while", "invisible"), Material.FERMENTED_SPIDER_EYE, "BUFF", "INVISIBILITY:1:300"),
                Quest("zeroth_5", "Master of Shadows", listOf("Complete all Shadow Temple", "trials"), Material.NETHER_STAR, "ITEM", "DIAMOND_BOOTS:1", QuestRequirements(completedQuests = listOf("zeroth_1", "zeroth_2", "zeroth_3", "zeroth_4")))
            )
            "temple_fire" -> listOf(
                Quest("fire_1", "Flame Bearer", listOf("Set 10 opponents on fire", "using flame weapons"), Material.FLINT_AND_STEEL, "ITEM", "FIRE_CHARGE:5"),
                Quest("fire_2", "Inferno Walker", listOf("Survive in lava for", "30 seconds"), Material.MAGMA_CREAM, "BUFF", "FIRE_RESISTANCE:1:600"),
                Quest("fire_3", "Blazing Warrior", listOf("Deal 500 fire damage", "to opponents"), Material.BLAZE_ROD, "ITEM", "ENCHANTED_BOOK:1"),
                Quest("fire_4", "Phoenix Rising", listOf("Resurrect 3 times using", "fire resistance"), Material.FEATHER, "ITEM", "TOTEM_OF_UNDYING:1"),
                Quest("fire_5", "Master of Flames", listOf("Prove your mastery over", "all things fire"), Material.NETHER_STAR, "BUFF", "FIRE_RESISTANCE:2:1800", QuestRequirements(completedQuests = listOf("fire_1", "fire_2", "fire_3", "fire_4")))
            )
            "temple_ice" -> listOf(
                Quest("ice_1", "Frost Walker", listOf("Freeze 10 blocks of water", "by walking over them"), Material.FROSTED_ICE, "ITEM", "ICE:10"),
                Quest("ice_2", "Chill Survivor", listOf("Survive 5 minutes in snow", "without taking damage"), Material.SNOWBALL, "BUFF", "RESISTANCE:1:600"),
                Quest("ice_3", "Glacial Warrior", listOf("Deal 500 damage with", "ice-based weapons"), Material.PACKED_ICE, "ITEM", "ENCHANTED_BOOK:1"),
                Quest("ice_4", "Frozen Heart", listOf("Take 100 damage from cold", "and survive"), Material.BLUE_ICE, "BUFF", "ABSORPTION:1:600"),
                Quest("ice_5", "Master of Frost", listOf("Complete all Ice Temple", "challenges"), Material.NETHER_STAR, "ITEM", "DIAMOND_HELMET:1", QuestRequirements(completedQuests = listOf("ice_1", "ice_2", "ice_3", "ice_4")))
            )
            "temple_forest" -> listOf(
                Quest("forest_1", "Nature's Ally", listOf("Plant 20 saplings", "during a match"), Material.OAK_SAPLING, "ITEM", "BONE_MEAL:10"),
                Quest("forest_2", "Beast Tamer", listOf("Tame 3 animals in the forest", "during a match"), Material.LEAD, "BUFF", "SPEED:1:600"),
                Quest("forest_3", "Woodland Warrior", listOf("Deal 500 damage with", "wooden weapons"), Material.WOODEN_SWORD, "ITEM", "ENCHANTED_BOOK:1"),
                Quest("forest_4", "Leaf Cloak", listOf("Hide in leaves for 2 minutes", "without being found"), Material.OAK_LEAVES, "BUFF", "INVISIBILITY:1:300"),
                Quest("forest_5", "Master of Nature", listOf("Complete all Forest Temple", "quests"), Material.NETHER_STAR, "ITEM", "DIAMOND_AXE:1", QuestRequirements(completedQuests = listOf("forest_1", "forest_2", "forest_3", "forest_4")))
            )
            "temple_light" -> listOf(
                Quest("light_1", "Radiant Defender", listOf("Defeat 10 mobs with", "light-based weapons"), Material.GLOWSTONE, "ITEM", "GLOWSTONE_DUST:10"),
                Quest("light_2", "Sun Walker", listOf("Survive a full day in sunlight", "without taking damage"), Material.SUNFLOWER, "BUFF", "REGENERATION:1:600"),
                Quest("light_3", "Blinding Warrior", listOf("Blind 5 opponents", "using flash potions"), Material.SPECTRAL_ARROW, "ITEM", "ENCHANTED_BOOK:1"),
                Quest("light_4", "Beacon Builder", listOf("Build and activate a beacon", "during a match"), Material.BEACON, "BUFF", "SPEED:2:600"),
                Quest("light_5", "Master of Light", listOf("Complete all Light Temple", "quests"), Material.NETHER_STAR, "ITEM", "ELYTRA:1", QuestRequirements(completedQuests = listOf("light_1", "light_2", "light_3", "light_4")))
            )
            else -> emptyList()
        }
    }
    
    /**
     * Completes a quest for a player
     */
    fun completeQuest(player: Player, questId: String) {
        val playerProgress = getPlayerProgress(player)
        
        if (playerProgress.isQuestActive(questId)) {
            playerProgress.completeQuest(questId)
            
            // Find the quest to get reward info
            val quest = getAllQuests().find { it.id == questId }
            if (quest != null) {
                player.sendMessage("§a✓ Quest Completed: §f${quest.name}")
                player.sendMessage("§7Reward: §f${quest.rewardData}")
                
                // Give reward (you can integrate this with your existing reward system)
                giveQuestReward(player, quest)
                
                // Play completion sound
                player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
            }
        }
    }
    
    /**
     * Gets all quests from all Quest Masters
     */
    private fun getAllQuests(): List<Quest> {
        val allQuests = mutableListOf<Quest>()
        listOf("temple_zeroth", "temple_fire", "temple_ice", "temple_forest", "temple_light").forEach { questMasterId ->
            allQuests.addAll(getQuestsForQuestMaster(questMasterId))
        }
        return allQuests
    }
    
    /**
     * Gives quest rewards to player
     */
    private fun giveQuestReward(player: Player, quest: Quest) {
        // This is a simple implementation - integrate with your existing reward system
        when (quest.rewardType.uppercase()) {
            "ITEM" -> {
                val parts = quest.rewardData.split(":")
                if (parts.size >= 2) {
                    try {
                        val material = Material.valueOf(parts[0])
                        val amount = parts[1].toIntOrNull() ?: 1
                        val item = ItemStack(material, amount)
                        player.inventory.addItem(item)
                    } catch (e: Exception) {
                        plugin.logger.warning("Invalid quest reward item: ${quest.rewardData}")
                    }
                }
            }
            "BUFF" -> {
                // Apply potion effect - integrate with your existing buff system
                player.sendMessage("§aBuff applied: ${quest.rewardData}")
            }
        }
    }
    
    /**
     * Clears all player data (useful for testing or resets)
     */
    fun clearAllProgress() {
        playerQuestProgress.clear()
        openGUIs.clear()
    }
}
