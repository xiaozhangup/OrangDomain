package me.xiaozhangup.domain.poly.permission

import me.xiaozhangup.domain.utils.display
import me.xiaozhangup.domain.utils.register
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

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
        return buildItem(XMaterial.GOLD_INGOT) {
            name = "&f死亡掉落金币 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8死亡时掉落金币"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
        }
    }
}
