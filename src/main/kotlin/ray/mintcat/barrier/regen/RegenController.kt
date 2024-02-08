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
        fallback[uid] = FallbackBlock(
            location,
            to,
            config.replace,
            block.blockData
        )

        submit(delay = 1L) {
            // 放置替代方块
            location.block.setType(config.replace, false)
        }
        submit(delay = config.delay) {
            fallback.remove(uid)?.fallback() // 定时恢复
        }
    }

    fun submitBlock(block: Block, config: BasicRegenGroup) {
        submitBlock(block, block.type, config)
    }
}