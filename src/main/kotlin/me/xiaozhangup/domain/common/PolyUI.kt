package me.xiaozhangup.domain.common

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.common.permission.Permission
import me.xiaozhangup.domain.common.poly.BarrierPoly
import me.xiaozhangup.domain.utils.error
import me.xiaozhangup.domain.utils.info
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.module.ui.type.PageableChest
import taboolib.platform.util.Slots
import taboolib.platform.util.buildItem

fun BarrierPoly.openMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<Chest>("管理页面") {
        map(
            "#########",
            "#A#B#C#D#",
            "#########"
        )
        set('A', buildItem(XMaterial.ITEM_FRAME) {
            name = "&f${data.name}"
            lore.add("&7持有者:&f ${Bukkit.getOfflinePlayer(admin).name}")
            lore.add("&7唯一编号:&f ${data.name}")
            colored()
        })
        set('B', buildItem(XMaterial.COMMAND_BLOCK_MINECART) {
            name = "&f全局权限管理"
            colored()
        })
        onClick('B') {
            openPermissionMenu(player)
        }
        set('C', buildItem(XMaterial.WRITABLE_BOOK) {
            name = "&f私有权限管理"
            colored()
        })
        onClick('C') {
            openPermissionUserMenu(player)
        }
        set('D', buildItem(XMaterial.OBSERVER) {
            name = "&f领地设置"
            colored()
        })
        onClick('D') {
            openSettingMenu(player)
        }

        onClick(lock = true)
    }
}

fun BarrierPoly.openSettingMenu(player: Player) {
    val data = this
    player.openMenu<Chest>("${data.name}设置") {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        map(
            "#########",
            "#ABCDEFG#",
            "#<#######"
        )
        set('A', buildItem(Material.ENDER_EYE) {
            name = "&f设置传送点到当前位置"
            colored()
        }) {
            data.door = player.location
            OrangDomain.savePoly(data.id)
        }

        onClick(lock = true)
    }
}

fun BarrierPoly.openPermissionUserMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<PageableChest<String>>("${data.name}私有权限管理") {
        rows(6)
        slots(Slots.CENTER)
        elements {
            users.keys.filter { it != player.name }.toList()
        }
        onGenerate { _, element, _, _ ->
            if (hasPermission("admin", element)) {
                buildItem(XMaterial.PLAYER_HEAD) {
                    name = "&c管理员 $element"
                    lore.addAll(listOf(" &7- &fall", " "))
                    lore.add("&7点击修改权限")
                    skullOwner = element
                    colored()
                }
            } else {
                buildItem(XMaterial.PLAYER_HEAD) {
                    name = "&c用户 $element"
                    lore.addAll(users[element]!!.filter { it.value }.keys.map { " &7- &f${it}" })
                    lore.add(" ")
                    lore.add("&7点击修改权限")
                    skullOwner = element
                    colored()
                }
            }
        }
        onClick { _, element ->
            openPermissionUser(player, element)
        }
        set(49, buildItem(XMaterial.WRITABLE_BOOK) {
            name = "&f添加用户"
            lore.add("&7点击从列表里添加用户")
            colored()
        }) {
            openAddUserMenu(player)
        }
        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }

        onClick(lock = true)
    }
}

fun BarrierPoly.openAddUserMenu(player: Player) {
    val data = this
    player.openMenu<PageableChest<Player>>("点击要添加的头像") {
        rows(6)
        slots(Slots.CENTER)
        elements {
            Bukkit.getOnlinePlayers().filter { it.name != player.name || !users.keys.contains(it.name) }.toList()
        }
        onGenerate { _, element, _, _ ->
            buildItem(XMaterial.PLAYER_HEAD) {
                name = "&c用户 $${element.name}"
                lore.add("&7点击添加")
                skullOwner = element.name
                colored()
            }
        }
        onClick { _: ClickEvent, element: Player ->
            users[element.name] = HashMap()
            OrangDomain.savePoly(data.id)
            player.info("添加成功!")
            player.closeInventory()
            submit(delay = 1) {
                openPermissionUserMenu(player)
            }
        }
        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }

        onClick(lock = true)
    }
}

fun BarrierPoly.openPermissionUser(player: Player, user: String) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<PageableChest<Permission>>("$user 的权限设置") {
        rows(6)
        slots(Slots.CENTER)
        elements {
            val list = OrangDomain.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
            list.toList().forEach {
                if (it.adminSide && !player.isOp) {
                    list.remove(it)
                }
            }
            list
        }
        onGenerate { _, element, _, _ ->
            element.generateMenuItem(hasPermission(element.id, player = user, def = element.default))
        }
        set(49, buildItem(XMaterial.LAVA_BUCKET) {
            name = "&4删除用户"
            lore.add("&c将该用户从当前领地中移除")
            colored()
        }) {
            player.info("已删除 &f${user} 的所有权限!")
            users.remove(user)
            OrangDomain.savePoly(data.id)
            submit(delay = 1) {
                openPermissionUserMenu(player)
            }
        }
        onClick { _, element ->
            users[user]!![element.id] = !hasPermission(element.id, player = user, def = element.default)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
            OrangDomain.savePoly(data.id)
            player.info("已修改 &f${user} &7的 &f${element.id} &7权限!")
            submit(delay = 1) {
                openPermissionUser(player, user)
            }
        }

        onClick(lock = true)
    }
}

fun BarrierPoly.openPermissionMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<PageableChest<Permission>>("${name}全局权限管理") {
        rows(6)
        slots(Slots.CENTER)
        elements {
            val list = OrangDomain.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
            if (!player.isOp) {
                list.removeAll(list.filter { it.adminSide == player.isOp })
            }
            list
        }
        onGenerate { _, element, _, _ ->
            if (element.adminSide && !player.isOp) {
                ItemStack(Material.BARRIER)
            }
            element.generateMenuItem(hasPermission(element.id, def = element.default))
        }
        onClick { event, element ->
            if (element.adminSide && !player.isOp) {
                event.clicker.error("该选项无效!")
            }
            permissions[element.id] = !hasPermission(element.id, def = element.default)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
            OrangDomain.savePoly(data.id)
            openPermissionMenu(player)
        }
        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }

        onClick(lock = true)
    }
}