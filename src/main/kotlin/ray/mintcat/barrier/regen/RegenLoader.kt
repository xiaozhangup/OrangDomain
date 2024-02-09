package ray.mintcat.barrier.regen

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import ray.mintcat.barrier.OrangDomain.regen
import ray.mintcat.barrier.regen.config.BasicRegenGroup
import ray.mintcat.barrier.regen.tweak.ExtendBreak
import ray.mintcat.barrier.regen.tweak.impl.TreeLinkedBreak
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
    val breakables = mutableSetOf<Material>()
    val extendbreak = mutableListOf<ExtendBreak>()

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
            breakables.addAll(blocks)

            regions.forEach { region ->
                regens.computeIfAbsent(region) { mutableListOf() }.add(config)
            }
        }
    }

    fun isBreakable(material: Material): Boolean {
        return breakables.contains(material)
    }

    @Awake(LifeCycle.ENABLE)
    fun reg() {
        // 注册采集优化
        TreeLinkedBreak().register()
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
    val to: Material,
    val data: BlockData
) {
    fun fallback(check: Boolean = true) {
        if (loc.block.type === to || !check) {
            loc.block.type = from
            loc.block.blockData = data
        }
    }
}