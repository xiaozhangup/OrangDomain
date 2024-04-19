package me.xiaozhangup.domain.shopkeeper

import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toBase64
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toItemStack
import me.xiaozhangup.domain.shopkeeper.ShopkeeperLoader.notify
import me.xiaozhangup.domain.shopkeeper.`object`.Shopkeeper
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem

object ShopkeeperEdit {
    private val result = buildItem(Material.RED_STAINED_GLASS_PANE) {
        name = "&c结果 &7(纵向)"
        lore += "&7横向相连的三格为一个交易"
        colored()
    }
    private val recipe = buildItem(Material.BLACK_STAINED_GLASS_PANE) {
        name = "&f需求 &7(纵向)"
        lore += "&7横向相连的三格为一个交易"
        colored()
    }

    fun openEdit(shopkeeper: Shopkeeper, player: Player) {
        if (shopkeeper.merchants.size > 15) {
            player.notify("该村民不支持普通编辑操作!")
            return
        }
        player.openMenu<StorableChest>(title = "") {
            map(
                "iooiooioo",
                "x  x  x  ",
                "x  x  x  ",
                "x  x  x  ",
                "x  x  x  ",
                "x  x  x  ",
            )

            set('i', result)
            set('o', recipe)

            val results = getSlots('x')

            var slot = 0
            shopkeeper.merchants.forEach { (t, u) ->
                set(results[slot], t.toItemStack())
                set(results[slot] + 1, u.first.toItemStack())
                u.second?.toItemStack()?.let { set(results[slot] + 2, it) }
                slot++
            } // 放入原先的物品

            rule {
                checkSlot(9..53) { _, _ ->
                    true
                }
            }

            onClose { event ->
                val inv = event.inventory
                val recipes = hashMapOf<String, Pair<String, String?>>()
                results.filter { inv.getItem(it) != null }.forEach { input ->
                    val result = inv.getItem(input)!!.toBase64()
                    val rs = listOfNotNull(
                        inv.getItem(input + 1),
                        inv.getItem(input + 2)
                    )

                    if (rs.isNotEmpty()) {
                        recipes[result] = rs[0].toBase64() to rs.getOrNull(1)?.toBase64()
                    }

                    shopkeeper.merchants = recipes
                }
            }
        }
    }
}