package ray.mintcat.barrier.portal

import com.destroystokyo.paper.ParticleBuilder
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.Baffle
import taboolib.module.effect.ParticleSpawner
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PortalPacket {
    private val baffle = Baffle.of(50L, TimeUnit.MILLISECONDS)
    val portals = mutableListOf<Portal>()

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        if (!baffle.hasNext(e.player.name)) return

        portals.firstOrNull { it.isIn(e.player) }?.let {
            e.player.teleport(
                it.target,
                TeleportFlag.Relative.YAW,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z
            )
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        baffle.reset(e.player.name)
    }
}