package me.xiaozhangup.domain.module

import me.xiaozhangup.whale.command.minecraft.MinecraftSpawn
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.SubscribeEvent

object VoidBack {

    @SubscribeEvent
    fun on(e: EntityDamageEvent) {
        if (e.entity.type == EntityType.PLAYER && e.cause == EntityDamageEvent.DamageCause.VOID) {
            val player = e.entity as Player

            e.isCancelled = true
            player.fallDistance = 0f
            player.teleportAsync(
                MinecraftSpawn.Companion.getPreciseSpawn(player.world)
            )
        }
    }
}