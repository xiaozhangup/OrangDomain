package ray.mintcat.barrier.regen

import org.bukkit.Location
import org.bukkit.Material
import ray.mintcat.barrier.regen.RegenLoader.fallback
import ray.mintcat.barrier.regen.config.BasicRegenGroup
import taboolib.common.platform.function.submit
import java.util.UUID

object RegenController {
    fun submitBlock(location: Location, to: Material, config: BasicRegenGroup) {
        val uid = UUID.randomUUID()
        fallback[uid] = FallbackBlock(
            location,
            to,
            config.replace
        )

        submit(delay = 1L) {
            // 放置替代方块
            location.block.setType(config.replace, false)
        }
        submit(delay = config.delay) {
            fallback[uid]?.fallback() // 定时恢复
        }
    }
}