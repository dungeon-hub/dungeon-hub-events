package net.dungeonhub.events.questmaster.gui

import net.dungeonhub.events.BattleRoyaleEvent
import net.dungeonhub.events.questmaster.QuestMaster
import net.dungeonhub.events.questmaster.QuestPool
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Quest GUI Manager - Single quest at a time with activation system
 */
class QuestGUIManager(private val plugin: BattleRoyaleEvent) : Listener {
    
    private val openGUIs = mutableMapOf<UUID, String>() // Player UUID -> GUI Type
    private val playerQuestProgress = mutableMapOf<UUID, PlayerQuestProgress>()
    private val playerChosenClass = mutableMapOf<UUID, String>()
    private val pendingClassSelection = mutableMapOf<UUID, String>()
    
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    
    // ==================== MAIN ENTRY POINT ====================
    
    fun openQuestGUI(player: Player, questMaster: QuestMaster, quests: List<Quest>) {
        val chosenClass = playerChosenClass[player.uniqueId]
        
        // Check if player has already chosen a different class
        if (chosenClass != null && chosenClass != questMaster.questId) {
            player.sendMessage("§c✗ You have already chosen the path of ${getClassName(chosenClass)}!")
            player.sendMessage("§7You cannot change your class once selected.")
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
            return
        }
        
        // If player hasn't chosen a class yet, show confirmation GUI
        if (chosenClass == null) {
            openClassConfirmationGUI(player, questMaster)
            return
        }
        
        // Player has chosen this class - show quest system
        val progress = getPlayerProgress(player)
        
        // Check if they have an active quest with timer
        if (progress.activeQuestId != null) {
            openActiveQuestGUI(player, progress)
        } else {
            // Offer a new quest
            offerNewQuest(player, progress, questMaster.questId)
        }
    }
    
    // ==================== CLASS SELECTION ====================
    
    private fun openClassConfirmationGUI(player: Player, questMaster: QuestMaster) {
        val inventory = Bukkit.createInventory(null, 27, "§6Choose Your Path")
        
        pendingClassSelection[player.uniqueId] = questMaster.questId
        
        val className = questMaster.name
        val role = className.split(" - ").getOrNull(1) ?: "Warrior"
        
        // Accept button
        val acceptItem = ItemStack(Material.LIME_WOOL)
        val acceptMeta = acceptItem.itemMeta
        acceptMeta?.setDisplayName("§a✓ ACCEPT")
        acceptMeta?.lore = listOf(
            "§7Join the path of:",
            "§6$className",
            "",
            "§7This choice is §c§lPERMANENT§7!",
            "§7You cannot change your class",
            "§7once you accept.",
            "",
            "§aClick to accept this path"
        )
        acceptItem.itemMeta = acceptMeta
        inventory.setItem(11, acceptItem)
        
        // Decline button
        val declineItem = ItemStack(Material.RED_WOOL)
        val declineMeta = declineItem.itemMeta
        declineMeta?.setDisplayName("§c✗ DECLINE")
        declineMeta?.lore = listOf("§7Return without choosing", "§7a class.", "", "§cClick to decline")
        declineItem.itemMeta = declineMeta
        inventory.setItem(15, declineItem)
        
        // Info item
        val infoItem = ItemStack(Material.ENCHANTED_BOOK)
        val infoMeta = infoItem.itemMeta
        infoMeta?.setDisplayName("§6$className")
        infoMeta?.lore = listOf(
            "§7Role: §f$role",
            "",
            "§7Are you sure you want to join",
            "§7the path of §6$className§7?",
            "",
            "§c⚠ WARNING:",
            "§7This decision is §c§lPERMANENT§7!",
            "§7You will not be able to change",
            "§7your class after accepting.",
            "",
            "§7Choose wisely!"
        )
        infoItem.itemMeta = infoMeta
        inventory.setItem(4, infoItem)
        
        // Decorative glass
        val glass = ItemStack(Material.YELLOW_STAINED_GLASS_PANE)
        val glassMeta = glass.itemMeta
        glassMeta?.setDisplayName(" ")
        glass.itemMeta = glassMeta
        
        val borderSlots = listOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
        borderSlots.forEach { slot -> inventory.setItem(slot, glass) }
        
        openGUIs[player.uniqueId] = "class_confirmation:${questMaster.questId}"
        player.openInventory(inventory)
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f)
    }
    
    // ==================== QUEST OFFER ====================
    
    private fun offerNewQuest(player: Player, progress: PlayerQuestProgress, classId: String) {
        // Get a random quest they haven't seen
        val difficulty = progress.getCurrentDifficulty()
        val quest = QuestPool.getRandomQuest(classId, progress.seenQuestIds, difficulty)
        
        if (quest == null) {
            player.sendMessage("§c✗ No more quests available at your difficulty level!")
            player.sendMessage("§7You have completed all available quests.")
            return
        }
        
        // Store as current quest
        progress.currentQuestId = quest.id
        progress.seenQuestIds.add(quest.id)
        
        // Open quest offer GUI
        openQuestOfferGUI(player, quest, progress)
    }
    
    private fun openQuestOfferGUI(player: Player, quest: Quest, progress: PlayerQuestProgress) {
        val inventory = Bukkit.createInventory(null, 27, "§6Quest Offer")
        
        // Quest display (center)
        val questItem = createQuestDisplayItem(quest, progress)
        inventory.setItem(13, questItem)
        
        // Activate button
        val activateItem = ItemStack(Material.LIME_CONCRETE)
        val activateMeta = activateItem.itemMeta
        activateMeta?.setDisplayName("§a§l✓ ACTIVATE QUEST")
        activateMeta?.lore = listOf(
            "§7Start this quest and begin",
            "§7the §e${plugin.questTimerManager.formatTime(quest.timeLimit)}§7 timer.",
            "",
            "§c⚠ Warning:",
            "§7If you fail to complete within",
            "§7the time limit, you will lose progress!",
            "",
            "§aClick to activate"
        )
        activateItem.itemMeta = activateMeta
        inventory.setItem(11, activateItem)
        
        // Decline button
        val declineItem = ItemStack(Material.RED_CONCRETE)
        val declineMeta = declineItem.itemMeta
        declineMeta?.setDisplayName("§c§l✗ DECLINE QUEST")
        declineMeta?.lore = listOf(
            "§7Skip this quest and get",
            "§7a different one.",
            "",
            "§7You cannot get this quest again.",
            "",
            "§cClick to decline"
        )
        declineItem.itemMeta = declineMeta
        inventory.setItem(15, declineItem)
        
        // Progress info
        val progressItem = createProgressInfoItem(progress)
        inventory.setItem(4, progressItem)
        
        // Decorative glass
        addDecorativeGlass(inventory)
        
        openGUIs[player.uniqueId] = "quest_offer:${quest.id}"
        player.openInventory(inventory)
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }
    
    // ==================== ACTIVE QUEST ====================
    
    private fun openActiveQuestGUI(player: Player, progress: PlayerQuestProgress) {
        val questId = progress.activeQuestId ?: return
        val quest = QuestPool.getAllQuests().find { it.id == questId } ?: return
        
        val inventory = Bukkit.createInventory(null, 27, "§6Active Quest")
        
        // Quest display
        val questItem = createActiveQuestItem(quest, progress, player)
        inventory.setItem(13, questItem)
        
        // Timer display
        val timerItem = createTimerItem(progress)
        inventory.setItem(4, timerItem)
        
        // Progress bar
        val progressPercentage = plugin.objectiveTracker.getProgressPercentage(player, questId)
        createProgressBar(inventory, progressPercentage)
        
        // Abandon button
        val abandonItem = ItemStack(Material.BARRIER)
        val abandonMeta = abandonItem.itemMeta
        abandonMeta?.setDisplayName("§c§lABANDON QUEST")
        abandonMeta?.lore = listOf(
            "§7Give up on this quest.",
            "",
            "§c⚠ WARNING:",
            "§7This will apply failure penalties!",
            "",
            "§cClick to abandon"
        )
        abandonItem.itemMeta = abandonMeta
        inventory.setItem(22, abandonItem)
        
        // Decorative glass
        addDecorativeGlass(inventory)
        
        openGUIs[player.uniqueId] = "active_quest:$questId"
        player.openInventory(inventory)
    }
    
    // ==================== ITEM CREATION ====================
    
    private fun createQuestDisplayItem(quest: Quest, progress: PlayerQuestProgress): ItemStack {
        val item = ItemStack(quest.icon)
        val meta = item.itemMeta
        
        meta?.setDisplayName("§6§l${quest.name}")
        
        val lore = mutableListOf<String>()
        lore.add("§7Difficulty: ${getDifficultyColor(quest.difficulty)}${quest.difficulty}")
        lore.add("§7Category: §f${if (quest.category == QuestCategory.GENERAL) "General" else "Class-Specific"}")
        lore.add("")
        lore.add("§7Description:")
        quest.description.forEach { line -> lore.add("§f  $line") }
        lore.add("")
        lore.add("§7Time Limit: §e${plugin.questTimerManager.formatTime(quest.timeLimit)}")
        lore.add("§7Reward: §a${quest.rewardData}")
        lore.add("")
        lore.add("§7Your Progress: §e${progress.totalQuestsCompleted}/3 §7quests")
        if (progress.hasPassive) {
            lore.add("§a✓ Passive Unlocked")
        }
        
        meta?.lore = lore
        item.itemMeta = meta
        return item
    }
    
    private fun createActiveQuestItem(quest: Quest, progress: PlayerQuestProgress, player: Player): ItemStack {
        val item = ItemStack(quest.icon)
        val meta = item.itemMeta
        
        meta?.setDisplayName("§6§l${quest.name}")
        
        val (current, required) = plugin.objectiveTracker.checkObjective(player, quest.id)
        val percentage = plugin.objectiveTracker.getProgressPercentage(player, quest.id)
        
        val lore = mutableListOf<String>()
        lore.add("§7Status: §e§lACTIVE")
        lore.add("")
        lore.add("§7Objective Progress:")
        lore.add("§f  $current / $required §7(§e$percentage%§7)")
        lore.add("")
        quest.description.forEach { line -> lore.add("§7  $line") }
        lore.add("")
        lore.add("§7Time Remaining: §e${plugin.questTimerManager.formatTime(progress.getRemainingTime())}")
        
        meta?.lore = lore
        item.itemMeta = meta
        return item
    }
    
    private fun createTimerItem(progress: PlayerQuestProgress): ItemStack {
        val remaining = progress.getRemainingTime()
        val item = when {
            remaining > 300 -> ItemStack(Material.LIME_STAINED_GLASS_PANE)
            remaining > 120 -> ItemStack(Material.YELLOW_STAINED_GLASS_PANE)
            else -> ItemStack(Material.RED_STAINED_GLASS_PANE)
        }
        
        val meta = item.itemMeta
        meta?.setDisplayName("§6⏱ Timer")
        meta?.lore = listOf(
            "§7Time Remaining:",
            "§e§l${plugin.questTimerManager.formatTime(remaining)}",
            "",
            when {
                remaining > 300 -> "§a✓ Plenty of time"
                remaining > 120 -> "§e⚠ Time running out"
                else -> "§c⚠ HURRY!"
            }
        )
        item.itemMeta = meta
        return item
    }
    
    private fun createProgressInfoItem(progress: PlayerQuestProgress): ItemStack {
        val item = ItemStack(Material.BOOK)
        val meta = item.itemMeta
        
        meta?.setDisplayName("§6Your Progress")
        meta?.lore = listOf(
            "§7Quests Completed: §e${progress.totalQuestsCompleted}",
            "§7Current Stage: ${getDifficultyColor(progress.getCurrentDifficulty())}${progress.getCurrentDifficulty()}",
            "",
            if (progress.hasPassive) "§a✓ Passive Unlocked" else "§7Complete 3 quests to unlock passive",
            "",
            "§7Quests until passive: §e${maxOf(0, 3 - progress.totalQuestsCompleted)}"
        )
        item.itemMeta = meta
        return item
    }
    
    private fun createProgressBar(inventory: Inventory, percentage: Int): Unit {
        val filledSlots = (percentage / 10).coerceIn(0, 9)
        
        // Use slots 18-26 (9 slots) for progress bar in a 27-slot inventory
        for (i in 0 until 9) {
            val slot = 18 + i
            val item = if (i < filledSlots) {
                ItemStack(Material.LIME_STAINED_GLASS_PANE)
            } else {
                ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            }
            
            val meta = item.itemMeta
            meta?.setDisplayName(if (i < filledSlots) "§a■" else "§7□")
            item.itemMeta = meta
            inventory.setItem(slot, item)
        }
    }
    
    private fun addDecorativeGlass(inventory: Inventory) {
        val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val glassMeta = glass.itemMeta
        glassMeta?.setDisplayName(" ")
        glass.itemMeta = glassMeta
        
        val borderSlots = listOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 17)
        borderSlots.forEach { slot -> inventory.setItem(slot, glass) }
    }
    
    private fun getDifficultyColor(difficulty: QuestDifficulty): String {
        return when (difficulty) {
            QuestDifficulty.EASY -> "§a"
            QuestDifficulty.MEDIUM -> "§e"
            QuestDifficulty.HARD -> "§c"
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val guiId = openGUIs[player.uniqueId] ?: return
        
        event.isCancelled = true
        val clickedItem = event.currentItem ?: return
        val slot = event.slot
        
        when {
            guiId.startsWith("class_confirmation:") -> handleClassConfirmation(player, slot)
            guiId.startsWith("quest_offer:") -> handleQuestOffer(player, slot, guiId)
            guiId.startsWith("active_quest:") -> handleActiveQuest(player, slot)
        }
    }
    
    private fun handleClassConfirmation(player: Player, slot: Int) {
        when (slot) {
            11 -> { // Accept
                val questMasterId = pendingClassSelection[player.uniqueId] ?: return
                playerChosenClass[player.uniqueId] = questMasterId
                pendingClassSelection.remove(player.uniqueId)
                
                val className = getClassName(questMasterId)
                player.closeInventory()
                player.sendMessage("§a✓ You have chosen the path of §6$className§a!")
                player.sendMessage("§7This choice is permanent. Your journey begins now!")
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
                
                // Offer first quest
                val progress = getPlayerProgress(player)
                offerNewQuest(player, progress, questMasterId)
            }
            15 -> { // Decline
                pendingClassSelection.remove(player.uniqueId)
                player.closeInventory()
                player.sendMessage("§7You have declined to choose a class.")
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
            }
        }
    }
    
    private fun handleQuestOffer(player: Player, slot: Int, guiId: String) {
        val questId = guiId.substringAfter("quest_offer:")
        val progress = getPlayerProgress(player)
        val quest = QuestPool.getAllQuests().find { it.id == questId } ?: return
        
        when (slot) {
            11 -> { // Activate
                activateQuest(player, quest, progress)
            }
            15 -> { // Decline
                progress.currentQuestId = null
                player.closeInventory()
                player.sendMessage("§7Quest declined. Getting a new quest...")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f)
                
                // Offer new quest
                val classId = playerChosenClass[player.uniqueId] ?: return
                offerNewQuest(player, progress, classId)
            }
        }
    }
    
    private fun handleActiveQuest(player: Player, slot: Int) {
        when (slot) {
            22 -> { // Abandon
                abandonQuest(player)
            }
        }
    }
    
    // ==================== QUEST ACTIONS ====================
    
    private fun activateQuest(player: Player, quest: Quest, progress: PlayerQuestProgress) {
        progress.activateQuest(quest.id, quest.timeLimit)
        plugin.objectiveTracker.resetPlayer(player)
        
        player.closeInventory()
        player.sendMessage("§a✓ Quest Activated: §6${quest.name}")
        player.sendMessage("§7You have §e${plugin.questTimerManager.formatTime(quest.timeLimit)}§7 to complete it!")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
        
        // Start timer
        plugin.questTimerManager.startTimer(player, progress) {
            failQuest(player, "Time expired!")
        }
        
        // Start checking for completion
        startCompletionChecker(player, quest, progress)
    }
    
    private fun abandonQuest(player: Player) {
        player.closeInventory()
        player.sendMessage("§c✗ Quest abandoned!")
        failQuest(player, "Quest abandoned")
    }
    
    private fun failQuest(player: Player, reason: String) {
        val progress = getPlayerProgress(player)
        plugin.questTimerManager.stopTimer(player)
        
        player.sendMessage("§c§l✗ QUEST FAILED!")
        player.sendMessage("§7Reason: §c$reason")
        
        val difficulty = progress.getCurrentDifficulty()
        progress.failQuest()
        
        when (difficulty) {
            QuestDifficulty.EASY -> {
                player.sendMessage("§c§lPenalty: Reset to Quest 1")
                player.sendMessage("§7You have lost all progress.")
            }
            QuestDifficulty.MEDIUM, QuestDifficulty.HARD -> {
                player.sendMessage("§c§lPenalty: Reset to Quest 4")
                player.sendMessage("§7You kept your passive ability.")
            }
        }
        
        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f)
    }
    
    private fun completeQuest(player: Player, quest: Quest, progress: PlayerQuestProgress) {
        plugin.questTimerManager.stopTimer(player)
        progress.completeQuest(quest.id)
        progress.activeQuestId = null
        
        player.sendMessage("§a§l✓ QUEST COMPLETED!")
        player.sendMessage("§6${quest.name}")
        player.sendMessage("§7Reward: §a${quest.rewardData}")
        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
        
        // Check for passive unlock
        if (progress.totalQuestsCompleted == 3 && !progress.hasPassive) {
            val classId = playerChosenClass[player.uniqueId] ?: return
            plugin.passiveAbilityManager.grantPassive(player, classId)
        }
        
        // Give reward
        giveReward(player, quest)
    }
    
    private fun giveReward(player: Player, quest: Quest) {
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
                        plugin.logger.warning("Invalid reward: ${quest.rewardData}")
                    }
                }
            }
            "BUFF" -> {
                val parts = quest.rewardData.split(":")
                if (parts.size >= 3) {
                    try {
                        val effectType = org.bukkit.potion.PotionEffectType.getByName(parts[0])
                        val amplifier = parts[1].toIntOrNull() ?: 0
                        val duration = parts[2].toIntOrNull() ?: 600
                        
                        if (effectType != null) {
                            val effect = org.bukkit.potion.PotionEffect(effectType, duration, amplifier, false, true, true)
                            player.addPotionEffect(effect)
                        }
                    } catch (e: Exception) {
                        plugin.logger.warning("Invalid buff: ${quest.rewardData}")
                    }
                }
            }
        }
    }
    
    private fun startCompletionChecker(player: Player, quest: Quest, progress: PlayerQuestProgress) {
        object : org.bukkit.scheduler.BukkitRunnable() {
            override fun run() {
                if (!player.isOnline || progress.activeQuestId != quest.id) {
                    cancel()
                    return
                }
                
                if (plugin.objectiveTracker.isObjectiveComplete(player, quest.id)) {
                    cancel()
                    completeQuest(player, quest, progress)
                }
            }
        }.runTaskTimer(plugin, 20L, 20L) // Check every second
    }
    
    // ==================== UTILITY ====================
    
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        openGUIs.remove(player.uniqueId)
    }
    
    fun getPlayerProgress(player: Player): PlayerQuestProgress {
        return playerQuestProgress.getOrPut(player.uniqueId) {
            PlayerQuestProgress(player.uniqueId.toString())
        }
    }
    
    fun hasChosenClass(player: Player): Boolean = playerChosenClass.containsKey(player.uniqueId)
    
    fun getPlayerClass(player: Player): String? = playerChosenClass[player.uniqueId]
    
    fun getPlayerClassName(player: Player): String? {
        val classId = playerChosenClass[player.uniqueId] ?: return null
        return getClassName(classId)
    }
    
    private fun getClassName(questMasterId: String): String {
        return when (questMasterId) {
            "temple_zeroth" -> "Athena - Tank"
            "temple_fire" -> "Hercules - Berserker"
            "temple_ice" -> "Artemis - Archer"
            "temple_forest" -> "Sushruta - Healer"
            "temple_light" -> "Merlin - Mage"
            else -> "Unknown Class"
        }
    }
    
    fun resetPlayerClass(player: Player) {
        playerChosenClass.remove(player.uniqueId)
        val progress = getPlayerProgress(player)
        progress.completedQuests.clear()
        progress.activeQuests.clear()
        progress.questStartTimes.clear()
        progress.totalQuestsCompleted = 0
        progress.hasPassive = false
        progress.currentQuestId = null
        progress.activeQuestId = null
        progress.seenQuestIds.clear()
        pendingClassSelection.remove(player.uniqueId)
        openGUIs.remove(player.uniqueId)
        plugin.questTimerManager.stopTimer(player)
        plugin.passiveAbilityManager.removePassive(player)
        plugin.objectiveTracker.resetPlayer(player)
    }
    
    fun clearAllProgress() {
        playerQuestProgress.clear()
        openGUIs.clear()
        playerChosenClass.clear()
        pendingClassSelection.clear()
    }
    
    // Dummy methods for compatibility
    fun getQuestsForQuestMaster(questMasterId: String): List<Quest> = emptyList()
}
