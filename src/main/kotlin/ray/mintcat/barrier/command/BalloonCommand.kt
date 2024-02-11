package ray.mintcat.barrier.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.balloon.BalloonUI
import ray.mintcat.barrier.balloon.BalloonWarp
import ray.mintcat.barrier.common.poly.BarrierPoly
import ray.mintcat.barrier.common.openMenu
import ray.mintcat.barrier.event.BarrierListener
import ray.mintcat.barrier.utils.error
import ray.mintcat.barrier.utils.fromLocation
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.info
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

@CommandHeader(
    name = "balloon",
    permission = "barrier.main"
)
object BalloonCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    //bres create 测试
    @CommandBody
    val create = subCommand {
        dynamic("id") {
            dynamic("name") {
                execute<Player> { sender, context, _ ->
                    initConfigSection(
                        BalloonWarp(
                            context["id"],
                            context["name"],
                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZiODAyMmM5ZDlhMDVlMDgzMTZkYTg3NDU3YmNhYjI3ODVjM2JhN2E1OTBkNDk0N2NlZjY4ODQzYjRkMDdhZCJ9fX0=",
                            sender.location,
                            listOf("新建的传送点", context["id"]),
                            12
                        )
                    )
                    sender.info("成功创建气球落脚点 ${context["id"]} !")
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<Player> { sender, _, _ ->
            OrangDomain.initBalloons()
            sender.info("成功重载所有气球落脚点!")
        }
    }

    @CommandBody
    val open = subCommand {
        execute<Player> { sender, _, _ ->
            BalloonUI.openBalloon(sender)
        }

        dynamic{
            suggestion<Player> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }

            execute<Player> { _, _, argument ->
                Bukkit.getPlayer(argument)?.let {
                    BalloonUI.openBalloon(it)
                }
            }
        }
    }

    private fun initConfigSection(build: BalloonWarp) {
        OrangDomain.balloon["${build.id}.name"] = build.name
        OrangDomain.balloon["${build.id}.skull"] = build.skull
        OrangDomain.balloon["${build.id}.loc"] = fromLocation(build.location)
        OrangDomain.balloon["${build.id}.lore"] = build.lore
        OrangDomain.balloon["${build.id}.level"] = build.level
        OrangDomain.balloon.saveToFile()
    }
}