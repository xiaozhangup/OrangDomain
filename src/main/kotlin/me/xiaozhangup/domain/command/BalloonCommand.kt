package me.xiaozhangup.domain.command

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.balloon.BalloonUI
import me.xiaozhangup.domain.balloon.BalloonWarp
import me.xiaozhangup.domain.utils.fromLocation
import me.xiaozhangup.domain.utils.info
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

@CommandHeader(
    name = "balloon",
    permissionDefault = PermissionDefault.TRUE
)
object BalloonCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    //bres create 测试
    @CommandBody(permission = "barrier.main")
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

    @CommandBody(permission = "barrier.main")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            OrangDomain.balloon.reload()
            OrangDomain.initBalloons()
            sender.info("成功重载所有气球落脚点!")
        }
    }

    @CommandBody
    val open = subCommand {
        execute<Player> { sender, _, _ ->
            BalloonUI.openBalloon(sender)
        }

        dynamic {
            suggestion<CommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }

            execute<CommandSender> { _, _, argument ->
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