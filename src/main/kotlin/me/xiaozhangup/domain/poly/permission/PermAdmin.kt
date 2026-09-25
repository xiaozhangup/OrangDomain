package me.xiaozhangup.domain.poly.permission

import org.bukkit.Material

import kotlinx.serialization.ExperimentalSerializationApi
import me.xiaozhangup.domain.utils.register
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.util.itemStack

@ExperimentalSerializationApi
object PermAdmin : Permission {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "admin"

    override val priority: Int
        get() = -1

    override val worldSide: Boolean
        get() = false

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return itemStack(Material.COMMAND_BLOCK) {
            name("<white>最高权力 ${if (value) "<green>允许" else "<red>阻止"} <gray>($id)")
            lore(
                "",
                "<gray>允许行为:",
                "<dark_gray>破坏领域, 扩展领域, 管理领域",
                "",
                "<dark_red>注意!",
                "<red>对方将获得你的所有权力"
            )
            hideAll()
            if (value) {
                shiny()
            }
        }
    }
}
