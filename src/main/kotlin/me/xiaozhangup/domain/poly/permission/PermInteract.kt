package me.xiaozhangup.domain.poly.permission

import org.bukkit.Material

import me.xiaozhangup.domain.OrangDomain.Companion.world
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.register
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.event.SubscribeEvent
import me.xiaozhangup.crab.util.itemStack


object PermInteract : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "interact"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return itemStack(Material.OAK_DOOR) {
            name("<white>交互(操作) ${if (value) "<green>允许" else "<red>阻止"} <gray>($id)")
            lore(
                "",
                "<gray>允许行为:",
                "<dark_gray>方块交互"
            )
            hideAll()
            if (value) {
                shiny()
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            val poly = e.clickedBlock?.location ?: return

            poly.getPoly()?.run {
                if (!hasPermission("interact", e.player.name)) {
                    if (!interactive.contains(e.clickedBlock?.type?.name)) {
                        e.setUseInteractedBlock(Event.Result.DENY)
                        //e.player.error("缺少权限 <white>$id")
                    }
                }
            } ?: run {
                if (world.globalProtect.contains(e.player.world.name) && !e.player.isOp) {
                    e.setUseInteractedBlock(Event.Result.DENY)
                }
            }
        }
    }
}
