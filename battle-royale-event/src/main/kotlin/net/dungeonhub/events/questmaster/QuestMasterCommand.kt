package net.dungeonhub.events.questmaster

import net.dungeonhub.events.BattleRoyaleEvent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Handles Quest Master related commands
 */
class QuestMasterCommand(
    private val questMasterManager: QuestMasterManager,
    private val plugin: BattleRoyaleEvent
) : CommandExecutor, TabCompleter {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("dungeonhub.questmaster.admin")) {
            sender.sendMessage("${ChatColor.RED}You don't have permission to use this command.")
            return true
        }
        
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "spawn" -> handleSpawn(sender, args)
            "remove" -> handleRemove(sender, args)
            "list" -> handleList(sender)
            "reload" -> handleReload(sender)
            "tp", "teleport" -> handleTeleport(sender, args)
            "info" -> handleInfo(sender, args)
            "gui" -> handleGUI(sender, args)
            "quest" -> handleQuest(sender, args)
            else -> sendHelp(sender)
        }
        
        return true
    }
    
    private fun handleSpawn(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster spawn <quest_id>")
            return
        }
        val questId = args[1]
        val questMaster = questMasterManager.getQuestMaster(questId)
        if (questMaster == null) {
            sender.sendMessage("${ChatColor.RED}Quest Master '$questId' not found.")
            return
        }
        // Remove old villager if present
        questMaster.removeVillager()
        // Update config with new location
        val loc = sender.location
    val config = questMasterManager.getConfig()
        config.set("questmasters.$questId.world", loc.world?.name ?: "world")
        config.set("questmasters.$questId.x", loc.x)
        config.set("questmasters.$questId.y", loc.y)
        config.set("questmasters.$questId.z", loc.z)
        questMasterManager.saveConfig()
        // Update in-memory QuestMaster and respawn
        val updatedQuestMaster = questMaster.copy(location = loc)
        questMasterManager.updateQuestMaster(questId, updatedQuestMaster)
        questMasterManager.spawnQuestMaster(updatedQuestMaster)
        sender.sendMessage("${ChatColor.GREEN}Moved Quest Master '$questId' to your location and respawned it.")
    }
    
    private fun handleRemove(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
            return
        }
        
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster remove <quest_id>")
            return
        }
        
        val questId = args[1]
        val questMaster = questMasterManager.getQuestMaster(questId)
        
        if (questMaster == null) {
            sender.sendMessage("${ChatColor.RED}Quest Master '$questId' not found.")
            return
        }
        
        questMaster.removeVillager()
        // Also inform the manager that it's been removed
        questMasterManager.despawnQuestMaster(questId)
        sender.sendMessage("${ChatColor.GREEN}Removed Quest Master '$questId'.")
    }
    
    private fun handleList(sender: CommandSender) {
        val questMasters = questMasterManager.getAllQuestMasters()
        
        if (questMasters.isEmpty()) {
            sender.sendMessage("${ChatColor.YELLOW}No Quest Masters configured.")
            return
        }
        
        sender.sendMessage("${ChatColor.GOLD}=== Quest Masters ===")
        questMasters.forEach { qm ->
            val status = if (qm.isSpawned()) "${ChatColor.GREEN}SPAWNED" else "${ChatColor.RED}NOT SPAWNED"
            val location = "${qm.location.world?.name}:${qm.location.blockX},${qm.location.blockY},${qm.location.blockZ}"
            sender.sendMessage("${ChatColor.YELLOW}${qm.questId} - ${qm.name} [$status${ChatColor.YELLOW}] ($location)")
        }
    }
    
    private fun handleReload(sender: CommandSender) {
        questMasterManager.reload()
        sender.sendMessage("${ChatColor.GREEN}Quest Master system reloaded.")
    }
    
    private fun handleTeleport(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
            return
        }
        
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster tp <quest_id>")
            return
        }
        
        val questId = args[1]
        val questMaster = questMasterManager.getQuestMaster(questId)
        
        if (questMaster == null) {
            sender.sendMessage("${ChatColor.RED}Quest Master '$questId' not found.")
            return
        }
        
        sender.teleport(questMaster.location)
        sender.sendMessage("${ChatColor.GREEN}Teleported to Quest Master '$questId'.")
    }
    
    private fun handleInfo(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster info <quest_id>")
            return
        }
        
        val questId = args[1]
        val questMaster = questMasterManager.getQuestMaster(questId)
        
        if (questMaster == null) {
            sender.sendMessage("${ChatColor.RED}Quest Master '$questId' not found.")
            return
        }
        
        sender.sendMessage("${ChatColor.GOLD}=== Quest Master Info: ${questMaster.questId} ===")
        sender.sendMessage("${ChatColor.YELLOW}Name: ${questMaster.name}")
        sender.sendMessage("${ChatColor.YELLOW}Location: ${questMaster.location.world?.name} ${questMaster.location.blockX}, ${questMaster.location.blockY}, ${questMaster.location.blockZ}")
        sender.sendMessage("${ChatColor.YELLOW}Message: ${questMaster.message}")
        sender.sendMessage("${ChatColor.YELLOW}One-time use: ${questMaster.isOneTimeUse}")
        sender.sendMessage("${ChatColor.YELLOW}Cooldown: ${questMaster.cooldownSeconds}s")
        sender.sendMessage("${ChatColor.YELLOW}Reward: ${questMaster.rewardType} (${questMaster.rewardData ?: "none"})")
        sender.sendMessage("${ChatColor.YELLOW}Status: ${if (questMaster.isSpawned()) "Spawned" else "Not spawned"}")
    }
    
    private fun handleGUI(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
            return
        }
        
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster gui <quest_id>")
            return
        }
        
        val questId = args[1]
        val questMaster = questMasterManager.getQuestMaster(questId)
        
        if (questMaster == null) {
            sender.sendMessage("${ChatColor.RED}Quest Master '$questId' not found.")
            return
        }
        
        // Open the GUI for the specified Quest Master
        val quests = plugin.questGUIManager.getQuestsForQuestMaster(questId)
        plugin.questGUIManager.openQuestGUI(sender, questMaster, quests)
    }
    
    private fun handleQuest(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /questmaster quest <list|complete> [args]")
            return
        }
        
        when (args[1].lowercase()) {
            "list" -> {
                if (sender !is Player) {
                    sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
                    return
                }
                
                val progress = plugin.questGUIManager.getPlayerProgress(sender)
                sender.sendMessage("${ChatColor.GOLD}=== Your Quest Progress ===")
                sender.sendMessage("${ChatColor.YELLOW}Completed Quests: ${progress.completedQuests.size}")
                sender.sendMessage("${ChatColor.YELLOW}Active Quests: ${progress.activeQuests.size}")
                
                if (progress.activeQuests.isNotEmpty()) {
                    sender.sendMessage("${ChatColor.GREEN}Active Quests:")
                    progress.activeQuests.forEach { (questId, objective) ->
                        sender.sendMessage("${ChatColor.WHITE}  - $questId: ${objective.getProgressText()}")
                    }
                }
                
                if (progress.completedQuests.isNotEmpty()) {
                    sender.sendMessage("${ChatColor.AQUA}Completed Quests:")
                    progress.completedQuests.forEach { questId ->
                        sender.sendMessage("${ChatColor.WHITE}  - $questId")
                    }
                }
            }
            
            "complete" -> {
                if (sender !is Player) {
                    sender.sendMessage("${ChatColor.RED}This command can only be used by players.")
                    return
                }
                
                if (args.size < 3) {
                    sender.sendMessage("${ChatColor.RED}Usage: /questmaster quest complete <quest_id>")
                    return
                }
                
                val questId = args[2]
                plugin.questGUIManager.completeQuest(sender, questId)
            }
            
            else -> {
                sender.sendMessage("${ChatColor.RED}Usage: /questmaster quest <list|complete> [args]")
            }
        }
    }
    
    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GOLD}=== Quest Master Commands ===")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster spawn <quest_id> - Spawn a Quest Master at your location")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster remove <quest_id> - Remove a Quest Master")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster list - List all Quest Masters")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster reload - Reload the Quest Master system")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster tp <quest_id> - Teleport to a Quest Master")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster info <quest_id> - Show Quest Master details")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster gui <quest_id> - Open Quest Master GUI")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster quest list - Show your quest progress")
        sender.sendMessage("${ChatColor.YELLOW}/questmaster quest complete <quest_id> - Complete a quest (admin)")
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val completions = mutableListOf<String>()
        
        when (args.size) {
            1 -> {
                completions.addAll(listOf("spawn", "remove", "list", "reload", "tp", "teleport", "info", "gui", "quest"))
            }
            2 -> {
                when (args[0].lowercase()) {
                    "spawn", "remove", "tp", "teleport", "info", "gui" -> {
                        completions.addAll(questMasterManager.getAllQuestMasters().map { it.questId })
                    }
                    "quest" -> {
                        completions.addAll(listOf("list", "complete"))
                    }
                }
            }
            3 -> {
                if (args[0].lowercase() == "quest" && args[1].lowercase() == "complete") {
                    // Add quest IDs for completion
                    completions.addAll(plugin.questGUIManager.getQuestsForQuestMaster("temple_zeroth").map { it.id })
                    completions.addAll(plugin.questGUIManager.getQuestsForQuestMaster("temple_fire").map { it.id })
                    completions.addAll(plugin.questGUIManager.getQuestsForQuestMaster("temple_ice").map { it.id })
                    completions.addAll(plugin.questGUIManager.getQuestsForQuestMaster("temple_forest").map { it.id })
                    completions.addAll(plugin.questGUIManager.getQuestsForQuestMaster("temple_light").map { it.id })
                }
            }
        }
        
        return completions.filter { it.startsWith(args.last(), ignoreCase = true) }
    }
}
