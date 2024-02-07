package ray.mintcat.barrier.common

import org.bukkit.event.block.LeavesDecayEvent
import ray.mintcat.barrier.OrangDomain.worlds
import taboolib.common.platform.event.SubscribeEvent

object GlobalProtect {
    @SubscribeEvent
    fun e(e: LeavesDecayEvent) {
        if (worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }
}