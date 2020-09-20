package io.github.daruma256

import io.github.daruma256.extension.customId
import io.github.daruma256.extension.displayName
import io.github.daruma256.extension.isUnBreakable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector


class SimpleGrapple : JavaPlugin(), Listener {
    companion object {
        lateinit var PLUGIN:SimpleGrapple
    }

    override fun onEnable() {
        // Plugin startup logic
        PLUGIN = this
        Bukkit.getServer().pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return false
        }
        when (command.name) {
            "grapple" -> {
                sender.inventory.addItem(ItemStack(Material.FISHING_ROD).apply {
                    this.displayName = "${ChatColor.RESET}${ChatColor.BOLD}Grapple"
                    this.customId = "grapple"
                    this.isUnBreakable = true
                })
                return true
            }
        }
        return false
    }

    private val coolDown: MutableList<Player> = mutableListOf()
    private val noFall: MutableList<Player> = mutableListOf()

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if (event.player.inventory.itemInMainHand.customId == "grapple") {
            if (event.state == PlayerFishEvent.State.IN_GROUND || event.state == PlayerFishEvent.State.REEL_IN) {
                val player = event.player
                if (coolDown.contains(player)) {
                    player.sendMessage("${ChatColor.RESET}${ChatColor.RED}cooldown")
                    return
                }

                player.teleport(player.location.add(0.0, 0.05, 0.0))
                GlobalScope.launch {
                    player.velocity = getVec(player.location, event.hook.location)
                    coolDown.add(player)
                    if (!noFall.contains(player)) {
                        noFall.add(player)
                    }
                    delay(1500)
                    if (coolDown.contains(player)) {
                        coolDown.remove(player)
                    }
                    delay(1000)
                    noFall.remove(player)
                }
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity is Player && event.cause == EntityDamageEvent.DamageCause.FALL) {
            val player = event.entity
            if (noFall.contains(player)) {
                event.isCancelled = true
                noFall.remove(player)
            }
        }
    }

    private fun getVec(loc1: Location, loc2: Location): Vector {
        val g = -0.08
        val d = loc2.distance(loc1)
        val t = d
        val vX = (1.0+0.4*t) * (loc2.x - loc1.x) / t
        val vY = (1.0+0.03*t) * (loc2.y - loc1.y) / t - 0.5*g*t
        val vZ = (1.0+0.4*t) * (loc2.z - loc1.z) / t
        return Vector(vX, vY, vZ)
    }

}