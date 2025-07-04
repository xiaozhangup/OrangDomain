package me.xiaozhangup.domain.poly.permission

import me.xiaozhangup.domain.OrangDomain.worlds
import me.xiaozhangup.domain.utils.display
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.register
import org.bukkit.block.data.type.Bed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem


object PermBed : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "bed"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.BLUE_BED) {
            name = "&f睡觉(设置重生点) ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8使用床"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock != null && e.clickedBlock!! is Bed) {
            e.clickedBlock?.location?.getPoly()?.run {
                if (!hasPermission("bed", e.player.name)) {
                    e.isCancelled = true
                    //e.player.error("缺少权限 &f$id")
                }
            } ?: run {
                if (worlds.contains(e.player.world.name) && !e.player.isOp) {
                    e.isCancelled = true
                }
            }
        }
    }
}