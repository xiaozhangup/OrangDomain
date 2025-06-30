package me.xiaozhangup.domain.poly.permission

import me.xiaozhangup.domain.utils.display
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.register
import me.xiaozhangup.domain.utils.rootDamager
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem


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
        return buildItem(XMaterial.DIAMOND_SWORD) {
            name = "&fPVP ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8PVP"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
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
                    //e.player.error("缺少权限 &f$id")
                }
            }
        }
    }

//    @SubscribeEvent(priority = EventPriority.LOWEST) // 丢出判断
//    fun e(e: ProjectileLaunchEvent) {
//        val shooter = e.entity.shooter
//        if (shooter is Player && !bootableEntity.contains(e.entity.type)) {
//            shooter.location.getPoly()?.run {
//                if (!hasPermission("pvp", shooter.name)) {
//                    e.isCancelled = true
//                    //e.player.error("缺少权限 &f$id")
//                }
//            }
//        }
//    }
}