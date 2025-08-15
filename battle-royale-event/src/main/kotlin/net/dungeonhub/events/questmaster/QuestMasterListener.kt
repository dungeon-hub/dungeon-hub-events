package net.dungeonhub.events.questmaster

import net.dungeonhub.events.questmaster.gui.QuestGUIManager
import org.bukkit.entity.Villager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Handles player interactions with Quest Master NPCs
 */
class QuestMasterListener(
    private val questMasterManager: QuestMasterManager,
    private val questGUIManager: QuestGUIManager
) : Listener {
    
    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        
        // Check if the entity is a Villager (our Quest Master NPC)
        if (entity !is Villager) return
        
        // Find the Quest Master associated with this Villager
        val questMaster = questMasterManager.getAllQuestMasters()
            .find { it.villager == entity } ?: return
        
        // Cancel the event to prevent default behavior
        event.isCancelled = true
        
        // Open the Quest GUI instead of the old simple interaction
        val quests = questGUIManager.getQuestsForQuestMaster(questMaster.questId)
        questGUIManager.openQuestGUI(player, questMaster, quests)
        
        // Play interaction sound
        player.playSound(player.location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f)
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        if (entity !is Villager) return
        val questMaster = questMasterManager.getAllQuestMasters().find { it.villager == entity } ?: return
        // Cancel to prevent trading menu
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is Villager) return
        val questMaster = questMasterManager.getAllQuestMasters().find { it.villager == entity } ?: return
        // Cancel damage and suppress red particle
        event.isCancelled = true
        event.damage = 0.0
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clean up player data when they quit (optional - keeps data persistent)
        // If you want to clear progress on quit, uncomment the line below:
        // questGUIManager.getPlayerProgress(event.player).completedQuests.clear()
    }
}
