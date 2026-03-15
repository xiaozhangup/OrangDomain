package me.xiaozhangup.domain.module

import me.xiaozhangup.domain.OrangDomain
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent

object GlobalProtect {
    @SubscribeEvent
    fun e(e: LeavesDecayEvent) {
        if (e.block.world.name in OrangDomain.worlds) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: BlockExplodeEvent) {
        if (e.block.world.name in OrangDomain.worlds) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityExplodeEvent) {
        if (e.entity.world.name in OrangDomain.worlds) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityChangeBlockEvent) {
        if (e.entity.world.name in OrangDomain.worlds) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.player.world.name in OrangDomain.worlds && e.action == Action.PHYSICAL) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityInteractEvent) {
        if (e.block.world.name in OrangDomain.worlds) {
            e.isCancelled = true
        }
    }
}