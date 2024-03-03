package ray.mintcat.barrier.portal

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.utils.execute
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common5.Baffle
import java.util.concurrent.TimeUnit

object PortalPacket {
    private val baffle = Baffle.of(50L, TimeUnit.MILLISECONDS)
    val portals = mutableListOf<Portal>()

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        if (!baffle.hasNext(e.player.name)) return

        portals.firstOrNull { it.isIn(e.player) }?.let {
            OrangDomain.config.getStringList("Join.${it.id}").forEach {
                e.player.execute(it)
            }
            
            submit(delay = it.delay) {
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
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        baffle.reset(e.player.name)
    }
}