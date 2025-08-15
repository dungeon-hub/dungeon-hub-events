package net.dungeonhub.events

import net.dungeonhub.events.questmaster.QuestMasterManager
import net.dungeonhub.events.questmaster.QuestMasterCommand
import net.dungeonhub.events.questmaster.QuestMasterListener
import net.dungeonhub.events.questmaster.gui.QuestGUIManager
import org.bukkit.plugin.java.JavaPlugin

class BattleRoyaleEvent : JavaPlugin() {

    lateinit var questMasterManager: QuestMasterManager
        private set
    
    lateinit var questGUIManager: QuestGUIManager
        private set

    override fun onEnable() {
        // Save default config (create if doesn't exist)
        try {
            saveDefaultConfig()
        } catch (e: IllegalArgumentException) {
            logger.warning("No default config.yml found, creating basic configuration...")
            // Create a basic config if none exists
            config.set("debug", false)
            config.set("auto-spawn-questmasters", true)
            config.set("default-world", "world")
            saveConfig()
        }
        
        // Initialize Quest Master system
        questMasterManager = QuestMasterManager(this)
        questMasterManager.initialize()
        
        // Initialize Quest GUI system
        questGUIManager = QuestGUIManager(this)
        
        // Register command
        getCommand("questmaster")?.setExecutor(QuestMasterCommand(questMasterManager, this))
        
        // Register listeners
        server.pluginManager.registerEvents(QuestMasterListener(questMasterManager, questGUIManager), this)
        
        logger.info("Dungeon Hub Battle Royale Plugin enabled with Quest GUI system!")
    }

    override fun onDisable() {
        // Clean up Quest Masters
        if (::questMasterManager.isInitialized) {
            questMasterManager.cleanup()
        }
        
        // Clean up GUI system
        if (::questGUIManager.isInitialized) {
            questGUIManager.clearAllProgress()
        }
        
        logger.info("Dungeon Hub Battle Royale Plugin disabled!")
    }
}
