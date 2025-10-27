package net.dungeonhub.events

import net.dungeonhub.events.questmaster.QuestMasterManager
import net.dungeonhub.events.questmaster.QuestMasterCommand
import net.dungeonhub.events.questmaster.QuestMasterListener
import net.dungeonhub.events.questmaster.gui.QuestGUIManager
import org.bukkit.plugin.java.JavaPlugin

class BattleRoyaleEvent : JavaPlugin() {

    lateinit var questMasterManager: QuestMasterManager
        private set
    
    lateinit var questGUIManager: net.dungeonhub.events.questmaster.gui.QuestGUIManager
        private set
    
    lateinit var passiveAbilityManager: net.dungeonhub.events.questmaster.PassiveAbilityManager
        private set
    
    lateinit var questTimerManager: net.dungeonhub.events.questmaster.QuestTimerManager
        private set
    
    lateinit var objectiveTracker: net.dungeonhub.events.questmaster.ObjectiveTracker
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
        
        // Initialize NEW Quest System
        passiveAbilityManager = net.dungeonhub.events.questmaster.PassiveAbilityManager(this)
        questTimerManager = net.dungeonhub.events.questmaster.QuestTimerManager(this)
        objectiveTracker = net.dungeonhub.events.questmaster.ObjectiveTracker(this)
        questGUIManager = net.dungeonhub.events.questmaster.gui.QuestGUIManager(this)
        
        // Register command
        getCommand("questmaster")?.setExecutor(QuestMasterCommand(questMasterManager, this))
        
        // Register listeners  
        server.pluginManager.registerEvents(net.dungeonhub.events.questmaster.QuestMasterListener(questMasterManager, questGUIManager), this)
        
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
        
        // Clean up timer system
        if (::questTimerManager.isInitialized) {
            questTimerManager.stopAllTimers()
        }
        
        // Clean up objective tracker
        if (::objectiveTracker.isInitialized) {
            objectiveTracker.resetAll()
        }
        
        logger.info("Dungeon Hub Battle Royale Plugin disabled!")
    }
}
