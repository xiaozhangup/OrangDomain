package ray.mintcat.barrier.common.extension.impl

import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import ray.mintcat.barrier.OrangDomain.mm
import ray.mintcat.barrier.OrangDomain.worlds
import taboolib.common.platform.event.SubscribeEvent

object AntiFly {
    @SubscribeEvent
    fun onPlayerToggleGlide(event: EntityToggleGlideEvent) {
        val entity = event.entity
        if (!worlds.contains(event.entity.world.name)) return
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

        if (!worlds.contains(player.world.name)) return
        if (player.gameMode == GameMode.CREATIVE || player.isOp) return

        if (event.isFlying) {
            disableFlight(player)
            event.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (!worlds.contains(e.player.world.name)) return
        if (e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            e.isCancelled = true
            e.player.sendActionBar(mm.deserialize("<red>折跃能力在此岛屿被限制"))
        }
    }

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player

        if (!worlds.contains(player.world.name)) return
        if (player.gameMode == GameMode.CREATIVE || player.isOp) return
        if (player.isFlying || player.isGliding) {
            disableFlight(player)
        }
    }

    private fun disableFlight(player: Player) {
        player.isFlying = false
        player.isGliding = false

        player.sendActionBar(mm.deserialize("<red>飞行能力在此岛屿被限制"))
    }
}