package me.xiaozhangup.domain.module

import me.xiaozhangup.domain.OrangDomain.world
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object WorldAccessLimit {

    private val component = Component.text("× 无法前往目标世界")
        .color(TextColor.fromHexString("#ed2e38"))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerTeleportEvent) {
        val accessible = world.accessible
        val player = e.player
        if (e.to.world.name in accessible) return

        e.isCancelled = true
        player.sendActionBar(component)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerChangedWorldEvent) {
        val accessible = world.accessible
        val player = e.player
        if (player.world.name in accessible) return

        world.spawnLocation()?.let {
            player.teleport(it)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerJoinEvent) {
        val accessible = world.accessible
        val player = e.player
        if (player.world.name in accessible) return

        submit(delay = 1) {
            world.spawnLocation()?.let {
                player.teleport(it)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: PlayerRespawnEvent) {
        val accessible = world.accessible
        if (e.respawnLocation.world.name in accessible) return

        world.spawnLocation()?.let {
            e.respawnLocation = it
        }
    }
}