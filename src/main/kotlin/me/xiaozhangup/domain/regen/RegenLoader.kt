package me.xiaozhangup.domain.regen

import me.xiaozhangup.domain.OrangDomain.regen
import me.xiaozhangup.domain.regen.config.BasicRegenGroup
import me.xiaozhangup.domain.regen.tweak.ExtendBreak
import me.xiaozhangup.domain.regen.tweak.impl.TreeLinkedBreak
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common5.RandomList
import java.util.*
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
            val fallback = section.getBoolean("fallback", true)
            val check = section.getBoolean("check", true)
            val blocks = section.getStringList("blocks").map { Material.valueOf(it) }

            val random = RandomList<Material>()
            section.getStringList("random").forEach {
                try {
                    random.add(
                        Material.valueOf(it.substringBefore(':')),
                        it.substringAfter(':').toInt()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val config = BasicRegenGroup(
                blocks,
                replace,
                delay,
                regions,
                fallback,
                check,
                if (random.size() > 0) random else null
            )
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
    val data: BlockData?
) {
    fun fallback(check: Boolean = true) {
        if (loc.block.type === to || !check) {
            loc.block.type = from
            if (data != null) {
                loc.block.blockData = data
            }
        }
    }
}