package net.dungeonhub.events.questmaster

import net.dungeonhub.events.BattleRoyaleEvent
import net.dungeonhub.events.questmaster.gui.PlayerQuestProgress
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * Manages quest timers and auto-fail on expiry
 */
class QuestTimerManager(private val plugin: BattleRoyaleEvent) {
    
    private val timerTasks = mutableMapOf<UUID, BukkitRunnable>()
    
    /**
     * Starts a timer for a player's active quest
     */
    fun startTimer(player: Player, progress: PlayerQuestProgress, onExpire: () -> Unit) {
        // Cancel existing timer if any
        stopTimer(player)
        
        val task = object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    return
                }
                
                val remaining = progress.getRemainingTime()
                
                when {
                    remaining <= 0 -> {
                        // Timer expired - fail quest
                        cancel()
                        timerTasks.remove(player.uniqueId)
                        onExpire()
                    }
                    remaining == 300 -> {
                        // 5 minutes warning
                        player.sendMessage("§e⚠ Quest Timer: §c5 minutes remaining!")
                        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
                    }
                    remaining == 120 -> {
                        // 2 minutes warning
                        player.sendMessage("§e⚠ Quest Timer: §c2 minutes remaining!")
                        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.7f)
                    }
                    remaining == 60 -> {
                        // 1 minute warning
                        player.sendMessage("§c⚠ Quest Timer: §c§l1 MINUTE REMAINING!")
                        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                    }
                    remaining == 30 -> {
                        // 30 seconds warning
                        player.sendMessage("§c⚠ Quest Timer: §c§l30 SECONDS!")
                        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f)
                    }
                    remaining <= 10 -> {
                        // Final countdown
                        player.sendMessage("§c§l$remaining")
                        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f)
                    }
                }
            }
        }
        
        task.runTaskTimer(plugin, 0L, 20L) // Run every second
        timerTasks[player.uniqueId] = task
    }
    
    /**
     * Stops a player's timer
     */
    fun stopTimer(player: Player) {
        timerTasks.remove(player.uniqueId)?.cancel()
    }
    
    /**
     * Stops all timers
     */
    fun stopAllTimers() {
        timerTasks.values.forEach { it.cancel() }
        timerTasks.clear()
    }
    
    /**
     * Formats time in MM:SS format
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}
