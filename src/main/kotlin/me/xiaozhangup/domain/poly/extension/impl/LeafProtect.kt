package me.xiaozhangup.domain.poly.extension.impl

import me.xiaozhangup.domain.OrangDomain.world
import org.bukkit.event.EventHandler
import org.bukkit.event.block.LeavesDecayEvent

object LeafProtect {
    @EventHandler
    fun on(e: LeavesDecayEvent) {
        if (world.globalProtect.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }
}