package me.xiaozhangup.domain.common.extension.impl

import me.xiaozhangup.domain.OrangDomain.worlds
import org.bukkit.event.EventHandler
import org.bukkit.event.block.LeavesDecayEvent

object LeafProtect {
    @EventHandler
    fun on(e: LeavesDecayEvent) {
        if (worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }
}