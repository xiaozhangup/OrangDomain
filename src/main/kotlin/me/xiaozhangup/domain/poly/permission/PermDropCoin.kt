package me.xiaozhangup.domain.poly.permission

import org.bukkit.Material

import me.xiaozhangup.domain.utils.register
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.util.itemStack

object PermDropCoin : Permission {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "drop_coin"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override val default: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return itemStack(Material.GOLD_INGOT) {
            name("<white>死亡掉落金币 ${if (value) "<green>允许" else "<red>阻止"} <gray>($id)")
            lore(
                "",
                "<gray>允许行为:",
                "<dark_gray>死亡时掉落金币"
            )
            hideAll()
            if (value) {
                shiny()
            }
        }
    }
}
