package me.xiaozhangup.domain.poly

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.module.RealisticTime
import me.xiaozhangup.domain.poly.permission.Permission
import me.xiaozhangup.domain.utils.error
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.info
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.expansion.createHelper
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.module.ui.type.PageableChest
import taboolib.platform.util.Slots
import taboolib.platform.util.buildItem

@Suppress("unused")
@CommandHeader(
    name = "poly",
    permission = "poly.main"
)
object PolyCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    //bres create 测试
    @CommandBody
    val create = subCommand {
        dynamic(comment = "领地名") {
            dynamic(comment = "领地ID") {
                execute<Player> { sender, context, _ ->
                    val name = context.argument(-1)
                    val nods = PolyListener.createMap[sender.uniqueId]
                    if (nods.isNullOrEmpty()) {
                        sender.error("记录点为空 请手持 &f${OrangDomain.getTool().name} &7点击地面")
                        sender.error("左键记录点 右键删除上一个记录的点")
                        return@execute
                    }
                    if (OrangDomain.polys.firstOrNull { it.id == name } != null) {
                        sender.error("ID冲突!")
                        return@execute
                    }
                    val build = Poly(
                        name,
                        context.argument(0),
                        sender.uniqueId,
                        nods.random(),
                        nods
                    )
                    PolyListener.createMap[sender.uniqueId] = mutableListOf()
                    OrangDomain.polys.add(build)
                    OrangDomain.savePoly(build.id)

                    initConfigSection(build)

                    sender.info("领地创建成功!")
                }
            }
        }

        createHelper()
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val player = sender as? Player
            OrangDomain.polys.filter {
                if (player == null) true
                else it.worldName() == player.world.name
            }.forEach {
                sender.info("${it.name} (${it.id})")
            }
        }
    }

    @CommandBody
    val edit = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                poly.openMenu(sender)
            }
        }
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute run {
                sender.error("您必须在一个领地内")
            }
            poly.openMenu(sender)
        }
    }

    @CommandBody
    val addDestructible = subCommand {
        dynamic(comment = "领地ID") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                val name = sender.getTargetBlockExact(3)?.type?.name
                if (name == null) {
                    sender.info("请指向你要添加的方块!")
                    return@execute
                }
                poly.destructible.add(name)
                OrangDomain.savePoly(poly.id)
                sender.info("已添加 $name 到可破坏列表!")
            }
        }
    }

    @CommandBody
    val listDestructible = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                sender.info("当前领地可破坏的物品有 ${poly.destructible.joinToString(", ")}")
            }
        }
    }

    @CommandBody
    val removeDestructible = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                val name = sender.getTargetBlockExact(3)?.type?.name
                if (name == null) {
                    sender.info("请指向你要移除的方块!")
                    return@execute
                }
                if (poly.destructible.remove(name)) {
                    OrangDomain.savePoly(poly.id)
                    sender.info("已从可破坏列表移除 $name !")
                } else {
                    sender.error("此领地本身就不可破坏 $name !")
                }
            }
        }
    }

    @CommandBody
    val addInteractive = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                val name = sender.getTargetBlockExact(3)?.type?.name
                if (name == null) {
                    sender.info("请指向你要添加的方块!")
                    return@execute
                }
                poly.interactive.add(name)
                OrangDomain.savePoly(poly.id)
                sender.info("已添加 $name 到可交互列表!")
            }
        }
    }

    @CommandBody
    val listInteractive = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                sender.info("当前领地可交互的物品有 ${poly.interactive.joinToString(", ")}")
            }
        }
    }

    @CommandBody
    val removeInteractive = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                val name = sender.getTargetBlockExact(3)?.type?.name
                if (name == null) {
                    sender.info("请指向你要移除的方块!")
                    return@execute
                }
                if (poly.interactive.remove(name)) {
                    OrangDomain.savePoly(poly.id)
                    sender.info("已从可交互列表移除 $name !")
                } else {
                    sender.error("此领地本身就不可交互 $name !")
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute run {
                        sender.error("领地不存在")
                    }
                OrangDomain.polys.remove(poly)
                OrangDomain.deletePoly(poly)
                sender.info("成功删除 &f${context.argument(0)} ")
            }
        }
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute run {
                sender.error("您必须在一个领地内")
            }
            sender.info("成功删除 &f${poly.name} ")
            OrangDomain.polys.remove(poly)
            OrangDomain.deletePoly(poly)
        }
    }

    @CommandBody
    val priority = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            dynamic(comment = "优先级 (越大越高)") {
                execute<Player> { sender, context, _ ->
                    try {
                        val poly =
                            OrangDomain.polys.firstOrNull { it.id == context.argument(-1) }
                                ?: return@execute run {
                                    sender.error("领地不存在")
                                }
                        poly.priority = context.argument(0).toInt()
                        OrangDomain.savePoly(poly.id)
                        sender.info("${poly.name} 的优先级已经设置为 ${poly.priority} !")
                    } catch (e: Exception) {
                        sender.info("设置时遇到错误: ${e.message}")
                    }
                }
            }
        }
    }

    @CommandBody
    val tp = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { sender, _ ->
                val player = sender as? Player
                OrangDomain.polys.filter {
                    if (player == null) true
                    else it.worldName() == player.world.name
                }.map { it.id }
            }
            dynamic(comment = "玩家名") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { _, context, _ ->
                    val name = OrangDomain.polys.firstOrNull { it.id == context.argument(-1) } ?: return@execute
                    val player = Bukkit.getPlayerExact(context.argument(0)) ?: return@execute
                    name.teleport(player)
                }
            }
            execute<Player> { sender, context, _ ->
                val name = OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute
                name.teleport(sender)
            }
        }
    }

    @CommandBody
    val addPoint = subCommand {
        execute<Player> { sender, _, _ ->
            PolyListener.addPoint(sender, sender.location)
        }
    }

    @CommandBody
    val clearPoint = subCommand {
        execute<Player> { sender, _, _ ->
            PolyListener.createMap.remove(sender.uniqueId)
            sender.info("已清除所有选点")
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<Player> { sender, _, _ ->
            OrangDomain.regions.reload()
            OrangDomain.config.reload()

            runCatching {
                OrangDomain.worlds.clear()
                OrangDomain.worlds.addAll(OrangDomain.config.getStringList("ProtectWorlds"))
                OrangDomain.initPolys()
            }.exceptionOrNull()

            runCatching {
                RealisticTime.loadWorlds()
            }.exceptionOrNull()

            sender.info("已成功重载所有配置文件")
        }
    }

    @CommandBody
    val current = subCommand {
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute run {
                sender.error("您必须在一个领地内")
            }
            sender.info("当前领地: ${poly.name} (${poly.id})")
        }
    }

    fun initConfigSection(build: Poly) {
        OrangDomain.regions["${build.id}.spawnAnimal"] = false
        OrangDomain.regions["${build.id}.spawnMonster"] = false
        OrangDomain.regions["${build.id}.entityTeleport"] = false
        OrangDomain.regions["${build.id}.despawns"] = listOf("VILLAGER")
        OrangDomain.regions.saveToFile()
    }

    fun Poly.openMenu(player: Player) {
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

    fun Poly.openSettingMenu(player: Player) {
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

    fun Poly.openPermissionUserMenu(player: Player) {
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

    fun Poly.openAddUserMenu(player: Player) {
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

    fun Poly.openPermissionUser(player: Player, user: String) {
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

    fun Poly.openPermissionMenu(player: Player) {
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
}