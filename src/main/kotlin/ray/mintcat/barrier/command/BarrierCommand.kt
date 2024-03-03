package ray.mintcat.barrier.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.common.openMenu
import ray.mintcat.barrier.common.poly.BarrierPoly
import ray.mintcat.barrier.event.BarrierListener
import ray.mintcat.barrier.utils.error
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.info
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

@CommandHeader(
    name = "barrier",
    aliases = ["bres"],
    permission = "barrier.main"
)
object BarrierCommand {

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
                    val nods = BarrierListener.createMap[sender.uniqueId]
                    if (nods.isNullOrEmpty()) {
                        sender.error("记录点为空 请手持 &f${OrangDomain.getTool().name} &7点击地面")
                        sender.error("左键记录点 右键删除上一个记录的点")
                        return@execute
                    }
                    if (OrangDomain.polys.firstOrNull { it.id == name } != null) {
                        sender.error("ID冲突!")
                        return@execute
                    }
                    val build = BarrierPoly(
                        name,
                        context.argument(0),
                        sender.uniqueId,
                        nods.random(),
                        nods
                    )
                    BarrierListener.createMap[sender.uniqueId] = mutableListOf()
                    OrangDomain.polys.add(build)
                    OrangDomain.savePoly(build.id)

                    initConfigSection(build)

                    sender.info("领地创建成功!")
                }
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            OrangDomain.polys.forEach {
                sender.info("${it.name} (${it.id})")
            }
        }
    }

    @CommandBody
    val edit = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                poly.openMenu(sender)
            }
        }
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute kotlin.run {
                sender.error("您必须在一个领地内")
            }
            poly.openMenu(sender)
        }
    }

    @CommandBody
    val addDestructible = subCommand {
        dynamic(comment = "领地ID") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                sender.info("当前领地可破坏的物品有 ${poly.destructible.joinToString(", ")}")
            }
        }
    }

    @CommandBody
    val removeDestructible = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                sender.info("当前领地可交互的物品有 ${poly.interactive.joinToString(", ")}")
            }
        }
    }

    @CommandBody
    val removeInteractive = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.id == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                OrangDomain.polys.remove(poly)
                OrangDomain.deletePoly(poly)
                sender.info("成功删除 &f${context.argument(0)} ")
            }
        }
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
            }
            dynamic(comment = "优先级 (越大越高)") {
                execute<Player> { sender, context, _ ->
                    try {
                        val poly =
                            OrangDomain.polys.firstOrNull { it.id == context.argument(-1) }
                                ?: return@execute kotlin.run {
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
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.id }
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
            BarrierListener.addPoint(sender, sender.location)
        }
    }

    @CommandBody
    val clearPoint = subCommand {
        execute<Player> { sender, _, _ ->
            BarrierListener.createMap.remove(sender.uniqueId)
            sender.info("已清除所有选点")
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<Player> { sender, _, _ ->
            OrangDomain.regions.reload()
            OrangDomain.config.reload()

            OrangDomain.worlds.clear()
            OrangDomain.worlds.addAll(OrangDomain.config.getStringList("ProtectWorlds"))
            OrangDomain.initPolys()

            sender.info("已成功重载所有配置文件")
        }
    }

    private fun initConfigSection(build: BarrierPoly) {
        OrangDomain.regions["${build.id}.spawnAnimal"] = false
        OrangDomain.regions["${build.id}.spawnMonster"] = false
        OrangDomain.regions["${build.id}.entityTeleport"] = false
        OrangDomain.regions["${build.id}.despawns"] = listOf("VILLAGER")
        OrangDomain.regions.saveToFile()
    }
}