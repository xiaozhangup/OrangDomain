package me.xiaozhangup.domain.poly.permission

import org.bukkit.Material

import me.xiaozhangup.domain.OrangDomain.Companion.world
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.register
import org.bukkit.block.data.type.Bed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.util.itemStack


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
        return itemStack(Material.BLUE_BED) {
            name("<white>睡觉(设置重生点) ${if (value) "<green>允许" else "<red>阻止"} <gray>($id)")
            lore(
                "",
                "<gray>允许行为:",
                "<dark_gray>使用床"
            )
            hideAll()
            if (value) {
                shiny()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock != null && e.clickedBlock!! is Bed) {
            e.clickedBlock?.location?.getPoly()?.run {
                if (!hasPermission("bed", e.player.name)) {
                    e.isCancelled = true
                    //e.player.error("缺少权限 <white>$id")
                }
            } ?: run {
                if (world.globalProtect.contains(e.player.world.name) && !e.player.isOp) {
                    e.isCancelled = true
                }
            }
        }
    }
}
