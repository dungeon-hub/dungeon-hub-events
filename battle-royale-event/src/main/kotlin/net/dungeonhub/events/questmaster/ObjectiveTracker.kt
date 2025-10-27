package net.dungeonhub.events.questmaster

import net.dungeonhub.events.BattleRoyaleEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
// PlayerJumpEvent doesn't exist in Bukkit - we'll track jumps differently
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import java.util.*

/**
 * Tracks player objectives for quest completion
 */
class ObjectiveTracker(private val plugin: BattleRoyaleEvent) : Listener {
    
    // Tracking data
    private val damageDealt = mutableMapOf<UUID, Double>()
    private val healingDone = mutableMapOf<UUID, Double>()
    private val kills = mutableMapOf<UUID, Int>()
    private val distanceTraveled = mutableMapOf<UUID, Double>()
    private val jumps = mutableMapOf<UUID, Int>()
    private val itemsCrafted = mutableMapOf<UUID, Int>()
    private val potionsUsed = mutableMapOf<UUID, Int>()
    private val survivalTime = mutableMapOf<UUID, Long>()
    private val lastLocation = mutableMapOf<UUID, org.bukkit.Location>()
    
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    
    // ==================== GETTERS ====================
    
    fun getDamageDealt(player: Player): Double = damageDealt.getOrDefault(player.uniqueId, 0.0)
    fun getHealingDone(player: Player): Double = healingDone.getOrDefault(player.uniqueId, 0.0)
    fun getKills(player: Player): Int = kills.getOrDefault(player.uniqueId, 0)
    fun getDistanceTraveled(player: Player): Double = distanceTraveled.getOrDefault(player.uniqueId, 0.0)
    fun getJumps(player: Player): Int = jumps.getOrDefault(player.uniqueId, 0)
    fun getItemsCrafted(player: Player): Int = itemsCrafted.getOrDefault(player.uniqueId, 0)
    fun getPotionsUsed(player: Player): Int = potionsUsed.getOrDefault(player.uniqueId, 0)
    fun getSurvivalTime(player: Player): Long {
        val startTime = survivalTime.getOrDefault(player.uniqueId, System.currentTimeMillis())
        return (System.currentTimeMillis() - startTime) / 1000
    }
    
    // ==================== RESET ====================
    
    fun resetPlayer(player: Player) {
        val uuid = player.uniqueId
        damageDealt.remove(uuid)
        healingDone.remove(uuid)
        kills.remove(uuid)
        distanceTraveled.remove(uuid)
        jumps.remove(uuid)
        itemsCrafted.remove(uuid)
        potionsUsed.remove(uuid)
        survivalTime[uuid] = System.currentTimeMillis()
        lastLocation.remove(uuid)
    }
    
    fun resetAll() {
        damageDealt.clear()
        healingDone.clear()
        kills.clear()
        distanceTraveled.clear()
        jumps.clear()
        itemsCrafted.clear()
        potionsUsed.clear()
        survivalTime.clear()
        lastLocation.clear()
    }
    
    // ==================== EVENT HANDLERS ====================
    
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val damage = event.finalDamage
        
        damageDealt[damager.uniqueId] = damageDealt.getOrDefault(damager.uniqueId, 0.0) + damage
    }
    
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val killer = event.entity.killer ?: return
        kills[killer.uniqueId] = kills.getOrDefault(killer.uniqueId, 0) + 1
    }
    
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from = event.from
        val to = event.to ?: return
        
        // Only count horizontal movement
        if (from.world != to.world) return
        
        val last = lastLocation[player.uniqueId]
        if (last != null && last.world == to.world) {
            val distance = last.distance(to)
            if (distance > 0.1 && distance < 10) { // Sanity check
                distanceTraveled[player.uniqueId] = distanceTraveled.getOrDefault(player.uniqueId, 0.0) + distance
            }
        }
        
        lastLocation[player.uniqueId] = to.clone()
    }
    
    // Note: PlayerJumpEvent doesn't exist in Bukkit API
    // Jumps would need to be tracked via statistics or custom implementation
    
    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        itemsCrafted[player.uniqueId] = itemsCrafted.getOrDefault(player.uniqueId, 0) + 1
    }
    
    @EventHandler
    fun onPotionUse(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item
        
        if (item.type.name.contains("POTION")) {
            potionsUsed[player.uniqueId] = potionsUsed.getOrDefault(player.uniqueId, 0) + 1
        }
    }
    
    // ==================== QUEST-SPECIFIC CHECKS ====================
    
    /**
     * Checks if a player has completed a specific objective
     */
    fun checkObjective(player: Player, questId: String): Pair<Int, Int> {
        // Returns (current, required) progress
        return when {
            // Damage quests
            questId.contains("damage") || questId.contains("warrior") || questId.contains("destroyer") -> {
                val current = getDamageDealt(player).toInt()
                val required = when {
                    questId.contains("easy") -> 100
                    questId.contains("med") -> 500
                    questId.contains("hard") -> 1000
                    else -> 200
                }
                Pair(current, required)
            }
            
            // Kill quests
            questId.contains("eliminator") || questId.contains("slayer") || questId.contains("champion") -> {
                val current = getKills(player)
                val required = when {
                    questId.contains("easy") -> 2
                    questId.contains("med") -> 4
                    questId.contains("hard") -> 6
                    else -> 3
                }
                Pair(current, required)
            }
            
            // Survival quests
            questId.contains("survivor") || questId.contains("endurance") -> {
                val current = getSurvivalTime(player).toInt()
                val required = when {
                    questId.contains("easy") -> 300  // 5 min
                    questId.contains("med") -> 600   // 10 min
                    questId.contains("hard") -> 900  // 15 min
                    else -> 300
                }
                Pair(current, required)
            }
            
            // Distance quests
            questId.contains("explorer") || questId.contains("sprinter") -> {
                val current = getDistanceTraveled(player).toInt()
                val required = when {
                    questId.contains("easy") -> 500
                    questId.contains("med") -> 1000
                    questId.contains("hard") -> 2000
                    else -> 500
                }
                Pair(current, required)
            }
            
            // Jump quests
            questId.contains("jumper") || questId.contains("acrobat") -> {
                val current = getJumps(player)
                val required = when {
                    questId.contains("easy") -> 100
                    questId.contains("med") -> 200
                    questId.contains("hard") -> 500
                    else -> 100
                }
                Pair(current, required)
            }
            
            // Craft quests
            questId.contains("craft") || questId.contains("resourceful") -> {
                val current = getItemsCrafted(player)
                val required = when {
                    questId.contains("easy") -> 5
                    questId.contains("med") -> 10
                    questId.contains("hard") -> 20
                    else -> 10
                }
                Pair(current, required)
            }
            
            // Potion quests (Mage)
            questId.contains("potion") || questId.contains("spell") || questId.contains("mage") -> {
                val current = getPotionsUsed(player)
                val required = when {
                    questId.contains("easy") -> 10
                    questId.contains("med") -> 25
                    questId.contains("hard") -> 50
                    else -> 10
                }
                Pair(current, required)
            }
            
            // Healing quests (Healer)
            questId.contains("heal") || questId.contains("medic") -> {
                val current = getHealingDone(player).toInt()
                val required = when {
                    questId.contains("easy") -> 100
                    questId.contains("med") -> 500
                    questId.contains("hard") -> 1000
                    else -> 100
                }
                Pair(current, required)
            }
            
            // Tank quests - blocking/resistance
            questId.contains("tank") || questId.contains("block") || questId.contains("shield") || questId.contains("protector") -> {
                val current = getDamageDealt(player).toInt() // Placeholder - should track damage blocked
                val required = when {
                    questId.contains("easy") -> 50
                    questId.contains("med") -> 200
                    questId.contains("hard") -> 500
                    else -> 100
                }
                Pair(current, required)
            }
            
            // Berserker quests - melee damage
            questId.contains("berserk") || questId.contains("rage") || questId.contains("fury") || questId.contains("rampage") -> {
                val current = getDamageDealt(player).toInt()
                val required = when {
                    questId.contains("easy") -> 200
                    questId.contains("med") -> 500
                    questId.contains("hard") -> 1000
                    else -> 200
                }
                Pair(current, required)
            }
            
            // Archer quests - ranged combat
            questId.contains("archer") || questId.contains("marksman") || questId.contains("sniper") || questId.contains("hunter") -> {
                val current = getDamageDealt(player).toInt() // Placeholder - should track ranged damage
                val required = when {
                    questId.contains("easy") -> 150
                    questId.contains("med") -> 400
                    questId.contains("hard") -> 800
                    else -> 150
                }
                Pair(current, required)
            }
            
            // Default - NOT complete, requires manual completion or specific tracking
            else -> {
                // For quests without specific tracking, return 0/1 so they need manual completion
                Pair(0, 1)
            }
        }
    }
    
    /**
     * Checks if objective is complete
     */
    fun isObjectiveComplete(player: Player, questId: String): Boolean {
        val (current, required) = checkObjective(player, questId)
        return current >= required
    }
    
    /**
     * Gets progress percentage
     */
    fun getProgressPercentage(player: Player, questId: String): Int {
        val (current, required) = checkObjective(player, questId)
        return minOf(100, (current * 100) / maxOf(1, required))
    }
}
