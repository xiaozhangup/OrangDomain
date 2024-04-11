package ray.mintcat.barrier.command

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.common.poly.RefreshPoly
import ray.mintcat.barrier.refresh.RefreshLoader
import ray.mintcat.barrier.utils.info
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import java.util.*

@CommandHeader(
    name = "refresh",
    permission = "barrier.main"
)
object RefreshCommand {
    private val select: HashMap<UUID, Pair<Location?, Location?>> = hashMapOf()

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val pos1 = subCommand {
        execute<Player> { sender, _, _ ->
            val pos1 = sender.getTargetBlockExact(12)
            pos1?.let {
                setFirstPosition(sender, it.location)
                sender.info("成功选择点一!")
            } ?: sender.info("你没有指向任何方块!")
        }
    }

    @CommandBody
    val pos2 = subCommand {
        execute<Player> { sender, _, _ ->
            val pos2 = sender.getTargetBlockExact(12)
            pos2?.let {
                setSecondPosition(sender, it.location)
                sender.info("成功选择点二!")
            } ?: sender.info("你没有指向任何方块!")
        }
    }

    @CommandBody
    val create = subCommand {
        dynamic("id") {
            execute<Player> { sender, context, _ ->
                val selected = select[sender.uniqueId]
                if (selected?.first == null || selected.second == null) {
                    sender.info("你还没有完成选点!")
                    return@execute
                }

                select.remove(sender.uniqueId)
                val id = context["id"]

                val poly = RefreshPoly(
                    id,
                    selected.first!!,
                    selected.second!!
                )
                OrangDomain.refreshes += poly
                OrangDomain.saveRefresh(poly.id)
                sender.info("成功创建新的刷新领地 ${poly.id} !")
            }
        }

        execute<Player> { sender, _, _ ->
            sender.info("请输入领地的 ID !")
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic {
            suggestion<CommandSender> { _, _ ->
                OrangDomain.refreshes.map { it.id }
            }

            execute<CommandSender> { sender, _, argument ->
                OrangDomain.deleteRefresh(
                    OrangDomain.refreshes.first {
                        it.id == argument
                    }
                )
                sender.info("成功删除刷新区域 $argument !")
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<Player> { sender, _, _ ->
            OrangDomain.initRefreshes()
            RefreshLoader.init()

            sender.info("已成功重载所有配置文件")
        }
    }

    private fun setFirstPosition(player: Player, location: Location) {
        select[player.uniqueId] = location to select[player.uniqueId]?.second
    }

    private fun setSecondPosition(player: Player, location: Location) {
        select[player.uniqueId] = select[player.uniqueId]?.first to location
    }
}