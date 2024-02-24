package ray.mintcat.barrier.regen

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import ray.mintcat.barrier.regen.RegenLoader.fallback
import ray.mintcat.barrier.regen.config.BasicRegenGroup
import taboolib.common.platform.function.submit
import java.util.UUID

object RegenController {
    fun submitBlock(block: Block, to: Material, config: BasicRegenGroup) {
        val uid = UUID.randomUUID()
        val location = block.location

        submit(delay = 1L) {
            // 放置替代方块
            location.block.setType(config.replace, false)
        }

        if (config.fallback) {
            val type = config.random?.random()
            fallback[uid] = FallbackBlock(
                location,
                type ?: to,
                config.replace,
                if (config.random != null) null else block.blockData
            )

            submit(delay = config.delay) {
                fallback.remove(uid)?.fallback() // 定时恢复
            }
        }
    }

    fun submitBlock(block: Block, config: BasicRegenGroup) {
        submitBlock(block, block.type, config)
    }
}