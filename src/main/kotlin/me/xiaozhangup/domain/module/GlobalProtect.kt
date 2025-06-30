package me.xiaozhangup.domain.module

import me.xiaozhangup.domain.OrangDomain
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityExplodeEvent
import taboolib.common.platform.event.SubscribeEvent

object GlobalProtect {
    @SubscribeEvent
    fun e(e: LeavesDecayEvent) {
        if (OrangDomain.worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: BlockExplodeEvent) {
        if (OrangDomain.worlds.contains(e.block.world.name)) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityExplodeEvent) {
        if (OrangDomain.worlds.contains(e.entity.world.name)) {
            e.isCancelled = true
        }
    }
}