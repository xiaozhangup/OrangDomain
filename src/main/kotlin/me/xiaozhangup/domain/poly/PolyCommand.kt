package me.xiaozhangup.domain.poly



import me.xiaozhangup.whale.service.menu.MenuPagedHolder
import me.xiaozhangup.whale.util.ext.openMenu
import me.xiaozhangup.whale.util.ext.openPagedMenu
import net.kyori.adventure.text.Component
import me.xiaozhangup.domain.OrangDomain
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
import me.xiaozhangup.carbkotlin.command.CommandBody
import me.xiaozhangup.carbkotlin.command.CommandHeader
import me.xiaozhangup.carbkotlin.command.mainCommand
import me.xiaozhangup.carbkotlin.command.subCommand
import me.xiaozhangup.domain.utils.ext.submitTask
import me.xiaozhangup.carbkotlin.command.createHelper
import me.xiaozhangup.carbkotlin.util.itemStack

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
            var success = true

            try {
                OrangDomain.initPolys()
            } catch (e: Exception) {
                sender.error("加载领地数据时遇到错误: ${e.message}")
                e.printStackTrace()
                success = false
            }

            try {
                OrangDomain.loadWorldSettings()
            } catch (e: Exception) {
                sender.error("加载世界设置时遇到错误: ${e.message}")
                e.printStackTrace()
                success = false
            }

            if (success) sender.info("已成功重载所有配置文件")
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
        player.openMenu(Component.text("管理页面"), size = 3) {
            map(
                "#########",
                "#A#B#C#D#",
                "#########"
            )
            set('A', itemStack(Material.ITEM_FRAME) {
                name("<white>${data.name}")
                lore(
                    "<gray>持有者:<white> ${Bukkit.getOfflinePlayer(admin).name}",
                    "<gray>唯一编号:<white> ${data.name}",
                )

            })
            set('B', itemStack(Material.COMMAND_BLOCK_MINECART) {
                name("<white>全局权限管理")
            }) { _, _ ->
                submitTask(delay = 1) { openPermissionMenu(player) }
            }
            set('C', itemStack(Material.WRITABLE_BOOK) {
                name("<white>私有权限管理")
            }) { _, _ ->
                submitTask(delay = 1) { openPermissionUserMenu(player) }
            }
            set('D', itemStack(Material.OBSERVER) {
                name("<white>领地设置")
            }) { _, _ ->
                submitTask(delay = 1) { openSettingMenu(player) }
            }

            lock(true)
            move(false)
        }
    }

    fun Poly.openSettingMenu(player: Player) {
        val data = this
        player.openMenu(Component.text("${data.name}设置"), size = 3) {
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
            map(
                "#########",
                "#ABCDEFG#",
                "#<#######"
            )
            set('A', itemStack(Material.ENDER_EYE) {
                name("<white>设置传送点到当前位置")
            }) { _, _ ->
                data.door = player.location
                OrangDomain.savePoly(data.id)
            }

            lock(true)
            move(false)
        }
    }

    fun Poly.openPermissionUserMenu(player: Player) {
        val data = this
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openPagedMenu<String>(Component.text("${data.name}私有权限管理"), size = 6) {
            domainPage {
                elements(run {
                    users.keys.filter { it != player.name }.toList()
                })
                elementGenerate { element ->
                    if (hasPermission("admin", element)) {
                        itemStack(Material.PLAYER_HEAD) {
                            name("<red>管理员 $element")
                            lore(" <gray>- <white>all", " ", "<gray>点击修改权限")
                            meta { (this as org.bukkit.inventory.meta.SkullMeta).owner = element }
                        }
                    } else {
                        itemStack(Material.PLAYER_HEAD) {
                            name("<red>用户 $element")
                            lore(*(users[element]!!.filter { it.value }.keys.map { " <gray>- <white>$it" }
                                + listOf(" ", "<gray>点击修改权限")).toTypedArray())
                            meta { (this as org.bukkit.inventory.meta.SkullMeta).owner = element }
                        }
                    }
                }
                click { element, _, _ ->
                    submitTask(delay = 1) { openPermissionUser(player, element) }
                }
            }

            set(49, itemStack(Material.WRITABLE_BOOK) {
                name("<white>添加用户")
                lore("<gray>点击从列表里添加用户")
            }) { _, _ ->
                submitTask(delay = 1) { openAddUserMenu(player) }
            }

            lock(true)
            move(false)
        }
    }

    fun Poly.openAddUserMenu(player: Player) {
        val data = this
        player.openPagedMenu<Player>(Component.text("点击要添加的头像"), size = 6) {
            domainPage {
                elements(run {
                    Bukkit.getOnlinePlayers().filter { it.name != player.name || !users.keys.contains(it.name) }.toList()
                })
                elementGenerate { element ->
                    itemStack(Material.PLAYER_HEAD) {
                        name("<red>用户 $${element.name}")
                        lore("<gray>点击添加")
                        meta { (this as org.bukkit.inventory.meta.SkullMeta).owner = element.name }
                    }
                }
                click { element, _, _ ->
                    users[element.name] = HashMap()
                    OrangDomain.savePoly(data.id)
                    player.info("添加成功!")
                    submitTask(delay = 1) {
                        openPermissionUserMenu(player)
                    }
                }
            }

            lock(true)
            move(false)
        }
    }

    fun Poly.openPermissionUser(player: Player, user: String) {
        val data = this
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openPagedMenu<Permission>(Component.text("$user 的权限设置"), size = 6) {
            domainPage {
                elements(run {
                    val list = OrangDomain.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                    list.toList().forEach {
                        if (it.adminSide && !player.isOp) {
                            list.remove(it)
                        }
                    }
                    list
                })
                elementGenerate { element ->
                    element.generateMenuItem(hasPermission(element.id, player = user, def = element.default))
                }
                click { element, _, _ ->
                    users[user]!![element.id] = !hasPermission(element.id, player = user, def = element.default)
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                    OrangDomain.savePoly(data.id)
                    player.info("已修改 &f${user} &7的 &f${element.id} &7权限!")
                    submitTask(delay = 1) {
                        openPermissionUser(player, user)
                    }
                }
            }

            set(49, itemStack(Material.LAVA_BUCKET) {
                name("<dark_red>删除用户")
                lore("<red>将该用户从当前领地中移除")
            }) { _, _ ->
                player.info("已删除 &f${user} 的所有权限!")
                users.remove(user)
                OrangDomain.savePoly(data.id)
                submitTask(delay = 1) {
                    openPermissionUserMenu(player)
                }
            }

            lock(true)
            move(false)
        }
    }

    fun Poly.openPermissionMenu(player: Player) {
        val data = this
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        player.openPagedMenu<Permission>(Component.text("${name}全局权限管理"), size = 6) {
            domainPage {
                elements(run {
                    val list = OrangDomain.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                    if (!player.isOp) {
                        list.removeAll(list.filter { it.adminSide == player.isOp })
                    }
                    list
                })
                elementGenerate { element ->
                    if (element.adminSide && !player.isOp) {
                        ItemStack(Material.BARRIER)
                    }
                    element.generateMenuItem(hasPermission(element.id, def = element.default))
                }
                click { element, _, _ ->
                    if (element.adminSide && !player.isOp) {
                        player.error("该选项无效!")
                    }
                    permissions[element.id] = !hasPermission(element.id, def = element.default)
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                    OrangDomain.savePoly(data.id)
                    submitTask(delay = 1) { openPermissionMenu(player) }
                }
            }

            lock(true)
            move(false)
        }
    }

    private fun <T> MenuPagedHolder<T>.domainPage(builder: MenuPagedHolder.PageableBuilder<T>.() -> Unit) {
        map("#########", "#EEEEEEE#", "#EEEEEEE#", "#EEEEEEE#", "#EEEEEEE#", "##<###>##")
        pageable {
            elementSlot('E')
            previousButton('<', pageArrow("上一页", true), pageArrow("上一页", false))
            nextButton('>', pageArrow("下一页", true), pageArrow("下一页", false))
            builder()
        }
    }

    private fun pageArrow(label: String, enabled: Boolean): ItemStack =
        itemStack(if (enabled) Material.SPECTRAL_ARROW else Material.ARROW) {
            name((if (enabled) "<white>" else "<gray>") + label)
        }
}
