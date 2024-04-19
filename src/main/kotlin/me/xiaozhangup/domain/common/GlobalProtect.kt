package me.xiaozhangup.domain.common

import me.xiaozhangup.domain.OrangDomain.worlds
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityExplodeEvent
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