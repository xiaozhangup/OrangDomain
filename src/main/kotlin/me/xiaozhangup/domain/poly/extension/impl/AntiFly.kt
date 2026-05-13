package me.xiaozhangup.domain.poly.extension.impl

import me.xiaozhangup.domain.OrangDomain.world
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import taboolib.common.platform.event.SubscribeEvent

object AntiFly {
    private val component = Component.text("× 飞行或折跃被阻止")
        .color(TextColor.fromHexString("#ed2e38"))

    @SubscribeEvent
    fun onPlayerToggleGlide(event: EntityToggleGlideEvent) {
        val entity = event.entity
        if (!world.globalProtect.contains(event.entity.world.name)) return
        if (entity.type != EntityType.PLAYER) return
        val player = entity as Player

        if (player.gameMode == GameMode.CREATIVE || player.isOp) return
        if (event.isGliding) {
            disableFlight(player)
            event.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        val player = event.player

        if (!world.globalProtect.contains(player.world.name)) return
        if (player.gameMode == GameMode.CREATIVE || player.isOp) return

        if (event.isFlying) {
            disableFlight(player)
            event.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (!world.globalProtect.contains(e.player.world.name)) return
        if (e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || e.cause == PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT) {
            e.isCancelled = true
            e.player.sendActionBar(component)
        }
    }

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player

        if (!world.globalProtect.contains(player.world.name)) return
        if (player.gameMode == GameMode.CREATIVE || player.isOp) return
        if (player.isFlying || player.isGliding) {
            disableFlight(player)
        }
    }

    private fun disableFlight(player: Player) {
        player.isFlying = false
        player.isGliding = false

        player.sendActionBar(component)
    }
}