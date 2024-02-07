package ray.mintcat.barrier.common.extension.impl

import org.bukkit.event.EventHandler
import org.bukkit.event.block.LeavesDecayEvent
import ray.mintcat.barrier.OrangDomain.worlds

object LeafProtect {
    @EventHandler
    fun on(e: LeavesDecayEvent) {
        if (worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }
}