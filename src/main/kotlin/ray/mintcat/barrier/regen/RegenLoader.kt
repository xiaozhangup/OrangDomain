package ray.mintcat.barrier.regen

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import ray.mintcat.barrier.OrangDomain.regen
import ray.mintcat.barrier.regen.config.BasicRegenGroup
import ray.mintcat.barrier.utils.info
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object RegenLoader {
    val regens = HashMap<String, MutableList<BasicRegenGroup>>() // 所有的配置文件 (区域ID 配置)
    val fallback = ConcurrentHashMap<UUID, FallbackBlock>() // 缓存器

    fun init() { // 加载的入口函数
        regens.clear()
        regen.reload()

        regen.getKeys(false).forEach { name ->
            val section = regen.getConfigurationSection(name)!!
            val delay = section.getLong("delay", 20)
            val replace = Material.valueOf(section.getString("replace")!!)
            val regions = section.getStringList("regions")
            val blocks = section.getStringList("blocks").map { Material.valueOf(it) }

            val config = BasicRegenGroup(blocks, replace, delay, regions)
            regions.forEach { region ->
                regens.computeIfAbsent(region) { mutableListOf() }.add(config)
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun reg() {
        // 命令注册相关内容
        command("blockregen", permissionDefault = PermissionDefault.OP) {
            literal("fallback") {
                execute<CommandSender> { sender, _, _ ->
                    quit()
                    sender.info("已经恢复所有未恢复的方块!")
                }
            }

            literal("reload") {
                execute<CommandSender> { sender, _, _ ->
                    init()
                    sender.info("所有配置已重载!")
                }
            }

            literal("size") {
                execute<CommandSender> { sender, _, _ ->
                    sender.info("当前在缓冲的方块共有 ${fallback.size} 个")
                }
            }
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun quit() { // 恢复所有的方块
        fallback.forEach { (_, fallback) ->
            fallback.fallback()
        }
        fallback.clear()
    }
}

data class FallbackBlock(
    val loc: Location,
    val from: Material,
    val to: Material
) {
    constructor(from: Material, block: Block, config: BasicRegenGroup) : this(
        block.location,
        from,
        config.replace
    )

    fun fallback(check: Boolean = true) {
        if (loc.block.type === to || !check) {
            loc.block.type = from
        }
    }
}