package me.xiaozhangup.domain.poly.permission

import org.bukkit.Material

import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.register
import me.xiaozhangup.domain.utils.rootDamager
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.carbkotlin.lifecycle.LifeCycle
import me.xiaozhangup.carbkotlin.lifecycle.Awake
import org.bukkit.event.EventPriority
import me.xiaozhangup.carbkotlin.event.SubscribeEvent
import me.xiaozhangup.carbkotlin.util.itemStack


object PermPvp : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "pvp"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return itemStack(Material.DIAMOND_SWORD) {
            name("<white>PVP ${if (value) "<green>允许" else "<red>阻止"} <gray>($id)")
            lore(
                "",
                "<gray>允许行为:",
                "<dark_gray>PVP"
            )
            hideAll()
            if (value) {
                shiny()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: EntityDamageByEntityEvent) {
        val entity = e.entity
        val damager = e.rootDamager() ?: return

        if (entity is Player) {
            entity.location.getPoly()?.run {
                if (!hasPermission("pvp", damager.name)) {
                    e.isCancelled = true
                    return
                    //e.player.error("缺少权限 <white>$id")
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerRespawnEvent) {
        if (e.isAnchorSpawn) return
        if (e.respawnReason != PlayerRespawnEvent.RespawnReason.DEATH) return

        val player = e.player
        val poly = player.lastDeathLocation?.getPoly() ?: return
        if (poly.hasPermission("pvp", player.name)) {
            e.respawnLocation = poly.door
        }
    }

    private val bootableEntity = listOf(
        EntityType.FISHING_BOBBER,
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT
    )

    @SubscribeEvent(priority = EventPriority.LOWEST) // 丢出判断
    fun e(e: ProjectileLaunchEvent) {
        val shooter = e.entity.shooter
        if (shooter is Player && !bootableEntity.contains(e.entity.type)) {
            shooter.location.getPoly()?.run {
                if (!hasPermission("pvp", shooter.name)) {
                    e.isCancelled = true
                    //e.player.error("缺少权限 <white>$id")
                }
            }
        }
    }
}
