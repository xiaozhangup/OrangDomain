package ray.mintcat.barrier.common

import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityExplodeEvent
import ray.mintcat.barrier.OrangDomain.worlds
import taboolib.common.platform.event.SubscribeEvent

object GlobalProtect {
    @SubscribeEvent
    fun e(e: LeavesDecayEvent) {
        if (worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: BlockExplodeEvent) {
        if (worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityExplodeEvent) {
        if (worlds.contains(e.entity.world.name)) {
            e.isCancelled = true
        }
    }
}