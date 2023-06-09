package ray.mintcat.barrier.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.common.BarrierPoly
import ray.mintcat.barrier.common.openMenu
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
        dynamic {
            execute<Player> { sender, context, _ ->
                val name = context.argument(0)
                val nods = BarrierListener.createMap[sender.uniqueId]
                if (nods.isNullOrEmpty()) {
                    sender.error("记录点为空 请手持 &f${OrangDomain.getTool().name} &7点击地面")
                    sender.error("左键记录点 右键删除上一个记录的点")
                    return@execute
                }
                if (OrangDomain.polys.firstOrNull { it.name == name } != null) {
                    sender.error("名称冲突!")
                    return@execute
                }
                val build = BarrierPoly(
                    name,
                    sender.uniqueId,
                    nods.random(),
                    nods
                )
                //money
                if (OrangDomain.polys.firstOrNull { it.anyInside(build) } != null) {
                    sender.error("您的领地和其他领地冲突了 请重新设定领地范围")
                    return@execute
                }
                BarrierListener.createMap[sender.uniqueId] = mutableListOf()
                OrangDomain.polys.add(build)
                OrangDomain.save(build.name)
                sender.info("领地创建成功!")
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            OrangDomain.polys.forEach {
                sender.info(it.name)
            }
        }
    }

    @CommandBody
    val edit = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.name }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
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
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.name }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                val name = sender.inventory.itemInMainHand.type.name
                poly.destructible.add(name)
                OrangDomain.save(poly.name)
                sender.info("已添加 $name 到可破坏列表!")
            }
        }
    }

    @CommandBody
    val listDestructible = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.name }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
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
                OrangDomain.polys.map { it.name }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                val name = sender.inventory.itemInMainHand.type.name
                if (poly.destructible.remove(name)) {
                    OrangDomain.save(poly.name)
                    sender.info("已从可破坏列表移除 $name !")
                } else {
                    sender.error("此领地本身就不可破坏 $name !")
                }
            }
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.name }
            }
            execute<Player> { sender, context, _ ->
                val poly =
                    OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                        sender.error("领地不存在")
                    }
                OrangDomain.polys.remove(poly)
                OrangDomain.delete(poly)
                OrangDomain.export()
                sender.info("成功删除 &f${context.argument(0)} ")
            }
        }
        execute<Player> { sender, _, _ ->
            val poly = sender.location.getPoly() ?: return@execute kotlin.run {
                sender.error("您必须在一个领地内")
            }
            sender.info("成功删除 &f${poly.name} ")
            OrangDomain.polys.remove(poly)
            OrangDomain.delete(poly)
            OrangDomain.export()
        }
    }

    @CommandBody
    val tp = subCommand {
        dynamic(comment = "领地名") {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.polys.map { it.name }
            }
            dynamic(comment = "玩家名") {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { _, context, _ ->
                    val name = OrangDomain.polys.firstOrNull { it.name == context.argument(-1) } ?: return@execute
                    val player = Bukkit.getPlayerExact(context.argument(0)) ?: return@execute
                    name.teleport(player)
                }
            }
            execute<Player> { sender, context, _ ->
                val name = OrangDomain.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute
                name.teleport(sender)
            }
        }
    }
}