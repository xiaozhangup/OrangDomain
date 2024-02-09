package ray.mintcat.barrier.command

import org.bukkit.command.CommandSender
import ray.mintcat.barrier.regen.RegenLoader
import ray.mintcat.barrier.utils.info
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper

@CommandHeader(
    name = "blockregen",
    permission = "barrier.main"
)
object RegenCommand {
    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val fallback = subCommand {
        execute<CommandSender> { sender, _, _ ->
            RegenLoader.quit()
            sender.info("已经恢复所有未恢复的方块!")
        }
    }

    @CommandBody
    val relaod = subCommand {
        execute<CommandSender> { sender, _, _ ->
            RegenLoader.init()
            sender.info("所有配置已重载!")
        }
    }

    @CommandBody
    val size = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.info("当前在缓冲的方块共有 ${RegenLoader.fallback.size} 个")
        }
    }
}