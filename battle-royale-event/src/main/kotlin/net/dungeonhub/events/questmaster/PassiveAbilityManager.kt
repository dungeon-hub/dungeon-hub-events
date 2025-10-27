package net.dungeonhub.events.questmaster

import net.dungeonhub.events.BattleRoyaleEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * Manages permanent passive abilities for players who complete 3 quests
 */
class PassiveAbilityManager(private val plugin: BattleRoyaleEvent) : Listener {
    
    private val activePassives = mutableMapOf<UUID, String>() // Player UUID -> Class ID
    
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        startPassiveTask()
    }
    
    /**
     * Grants a passive ability to a player based on their class
     */
    fun grantPassive(player: Player, classId: String) {
        activePassives[player.uniqueId] = classId
        applyPassiveEffect(player, classId)
        
        val passiveName = getPassiveName(classId)
        player.sendMessage("§6✦ PASSIVE UNLOCKED ✦")
        player.sendMessage("§7You have unlocked: §a$passiveName")
        player.sendMessage("§7This passive is now §lpermanent§r§7!")
        
        // Play epic sound
        player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.5f)
        player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
    
    /**
     * Removes a player's passive (for reset)
     */
    fun removePassive(player: Player) {
        val classId = activePassives.remove(player.uniqueId)
        if (classId != null) {
            removePassiveEffect(player, classId)
        }
    }
    
    /**
     * Checks if player has a passive
     */
    fun hasPassive(player: Player): Boolean {
        return activePassives.containsKey(player.uniqueId)
    }
    
    /**
     * Gets the passive name for a class
     */
    private fun getPassiveName(classId: String): String {
        return when (classId) {
            "temple_zeroth" -> "Iron Skin (Resistance I)"
            "temple_fire" -> "Berserker Rage (Strength I)"
            "temple_ice" -> "Swift Arrows (Speed I)"
            "temple_forest" -> "Natural Healing (Regeneration I)"
            "temple_light" -> "Arcane Power (Luck I)"
            else -> "Unknown Passive"
        }
    }
    
    /**
     * Applies the passive effect to a player
     */
    private fun applyPassiveEffect(player: Player, classId: String) {
        val effect = when (classId) {
            "temple_zeroth" -> PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 0, false, false, true)
            "temple_fire" -> PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, 0, false, false, true)
            "temple_ice" -> PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, false, false, true)
            "temple_forest" -> PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 0, false, false, true)
            "temple_light" -> PotionEffect(PotionEffectType.LUCK, Int.MAX_VALUE, 0, false, false, true)
            else -> return
        }
        
        player.addPotionEffect(effect)
    }
    
    /**
     * Removes the passive effect from a player
     */
    private fun removePassiveEffect(player: Player, classId: String) {
        val effectType = when (classId) {
            "temple_zeroth" -> PotionEffectType.RESISTANCE
            "temple_fire" -> PotionEffectType.STRENGTH
            "temple_ice" -> PotionEffectType.SPEED
            "temple_forest" -> PotionEffectType.REGENERATION
            "temple_light" -> PotionEffectType.LUCK
            else -> return
        }
        
        player.removePotionEffect(effectType)
    }
    
    /**
     * Starts a task to reapply passives every 30 seconds (in case they get removed)
     */
    private fun startPassiveTask() {
        object : BukkitRunnable() {
            override fun run() {
                for ((uuid, classId) in activePassives) {
                    val player = plugin.server.getPlayer(uuid)
                    if (player != null && player.isOnline) {
                        // Check if they still have the effect
                        val effectType = when (classId) {
                            "temple_zeroth" -> PotionEffectType.RESISTANCE
                            "temple_fire" -> PotionEffectType.STRENGTH
                            "temple_ice" -> PotionEffectType.SPEED
                            "temple_forest" -> PotionEffectType.REGENERATION
                            "temple_light" -> PotionEffectType.LUCK
                            else -> null
                        }
                        
                        if (effectType != null && !player.hasPotionEffect(effectType)) {
                            applyPassiveEffect(player, classId)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 600L, 600L) // Every 30 seconds
    }
    
    /**
     * Reapply passives when player joins
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val classId = activePassives[player.uniqueId]
        if (classId != null) {
            applyPassiveEffect(player, classId)
        }
    }
    
    /**
     * Clean up when player quits
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Keep the passive in memory for when they rejoin
        // Don't remove from activePassives map
    }
    
    /**
     * Gets all players with passives (for admin)
     */
    fun getPlayersWithPassives(): Map<UUID, String> {
        return activePassives.toMap()
    }
}
