package me.xiaozhangup.domain.balloon

import me.xiaozhangup.capybara.exec
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.XSkull
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import java.util.*

object BalloonUI {
    private val background = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) {
        name = " "
    }
    private val icon = buildItem(Material.PLAYER_HEAD) {
        skullTexture = XSkull.SkullTexture(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTA2ZTZkODNjZjdlZDVjZjdiZjBlMDE4ZWNiNjAzOWIwNDZkOGRjNmRiNTU2OTAxNGZjYWIzN2I2MTdmMTM5OSJ9fX0=",
            UUID(0, 0)
        )
        name = "&f主岛屿热气球"
        lore += "&7快速前往你已经解锁的岛屿"
        lore += "&7或是飞往一些特殊的场所"

        colored()
    }
    private val back = buildItem(Material.PLAYER_HEAD) {
        skullTexture = XSkull.SkullTexture(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTk3ZTRlMjdhMDRhZmE1ZjA2MTA4MjY1YTliZmI3OTc2MzAzOTFjN2YzZDg4MGQyNDRmNjEwYmIxZmYzOTNkOCJ9fX0=",
            UUID(0, 0)
        )
        name = "&{#29c0ff}自己的个人岛屿"
        lore += "&7自己的主岛屿"
        lore += "&7"
        lore += "&e单击传送"

        colored()
    }

    // 翻页按钮
    private val next: ItemStack = buildItem(Material.ARROW) {
        name = "&f下一页"
        colored()
    }
    private val pre: ItemStack = buildItem(Material.ARROW) {
        name = "&f上一页"
        colored()
    }
    private val nonext: ItemStack = buildItem(Material.FEATHER) {
        name = "&f没有下一页"
        colored()
    }
    private val nopre: ItemStack = buildItem(Material.FEATHER) {
        name = "&f没有上一页"
        colored()
    }

    // 传送点列表
    val balloons = mutableListOf<BalloonWarp>()

    fun openBalloon(player: Player) {
        player.openMenu<Linked<BalloonWarp>>(title = "待发的热气球") {
            map(
                "=========",
                "=i=     =",
                "=b=   pn=",
                "=========",
            )

            set('=', background)
            set('i', icon)
            set('b', back) {
                player.exec("isgo")
            }
            slotsBy(' ')

            elements { balloons.sortedBy { it.level } }
            onGenerate { _, element, _, _ ->
                buildItem(Material.PLAYER_HEAD) {
                    skullTexture = XSkull.SkullTexture(
                        element.skull,
                        UUID(0, 0)
                    )
                    name = element.name
                    lore.addAll(element.lore)

                    lore += "&7"
                    lore += if (player.checkLevel(element.level)) {
                        "&e单击传送"
                    } else {
                        "&c经验等级未达到 ${element.level} !"
                    }

                    colored()
                }
            }
            onClick { _, element ->
                if (player.checkLevel(element.level)) {
                    player.closeInventory()

                    console().performCommand("screeneffect fullscreen BLACK 5 10 10 nofreeze ${player.name}")
                    submit(delay = 6L) {
                        player.teleport(element.location)
                    }
                }
            }

            setNextPage(getFirstSlot('n')) { _, hasNextPage ->
                if (hasNextPage) {
                    next
                } else {
                    nonext
                }
            }
            setPreviousPage(getFirstSlot('p')) { _, hasNextPage ->
                if (hasNextPage) {
                    pre
                } else {
                    nopre
                }
            }

            onClick(lock = true)
        }
    }

    private fun Player.checkLevel(level: Int): Boolean {
        return getLevel() >= level
    }
}