package me.xiaozhangup.domain.command

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.portal.Portal
import me.xiaozhangup.domain.portal.PortalPacket.portals
import me.xiaozhangup.domain.utils.info
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import java.util.*

@CommandHeader(
    name = "portal",
    permission = "barrier.main"
)
object PortalCommand {
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
            dynamic("level") {
                execute<Player> { sender, context, _ ->
                    val selected = select[sender.uniqueId]
                    if (selected?.first == null || selected.second == null) {
                        sender.info("你还没有完成选点!")
                        return@execute
                    }

                    select.remove(sender.uniqueId)
                    val id = context["id"]

                    val poly = Portal(
                        id,
                        selected.first!!,
                        selected.second!!,
                        sender.location,
                        context["level"].toInt()
                    )
                    portals += poly
                    OrangDomain.savePortal(poly.id)
                    sender.info("成功创建新的门 ${poly.id} !")
                }
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
                portals.map { it.id }
            }

            execute<CommandSender> { sender, _, argument ->
                OrangDomain.deletePortal(
                    portals.first {
                        it.id == argument
                    }
                )
                sender.info("成功删除门 $argument !")
            }
        }
    }

    @CommandBody
    val target = subCommand {
        dynamic {
            suggestion<Player> { _, _ ->
                portals.map { it.id }
            }

            execute<Player> { sender, _, argument ->
                val portal = portals.first {
                    it.id == argument
                }
                portal.target = sender.location
                OrangDomain.savePortal(portal.id)

                sender.info("成功重置门 $argument 的目标地点!")
            }
        }
    }

    private fun setFirstPosition(player: Player, location: Location) {
        select[player.uniqueId] = location to select[player.uniqueId]?.second
    }

    private fun setSecondPosition(player: Player, location: Location) {
        select[player.uniqueId] = select[player.uniqueId]?.first to location
    }
}