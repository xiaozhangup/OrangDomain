package me.xiaozhangup.domain

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object WorldSpawnCover {
    var location: Location? = null

    @SubscribeEvent
    fun on(e: PlayerJoinEvent) {
        val player = e.player
        if (location == null) return

        if (player.hasPlayedBefore()) player.teleportAsync(location!!)
        else submit(delay = 1) {
            player.teleportAsync(location!!)
        }
    }

    @SubscribeEvent
    fun on(e: EntityDamageEvent) {
        if (location == null) return
        if (e.entity.type == EntityType.PLAYER && e.cause == EntityDamageEvent.DamageCause.VOID) {
            val player = e.entity as Player

            e.isCancelled = true
            player.fallDistance = 0f
            player.teleportAsync(location!!)
        }
    }

    @SubscribeEvent
    fun on(e: PlayerRespawnEvent) {
        if (location == null) return
        e.respawnLocation = location!!
    }
}