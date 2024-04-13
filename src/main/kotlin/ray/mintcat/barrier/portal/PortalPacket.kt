package ray.mintcat.barrier.portal

import io.papermc.paper.entity.TeleportFlag
import me.xiaozhangup.capybara.taboolib.common5.Baffle
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import ray.mintcat.barrier.OrangDomain
import ray.mintcat.barrier.utils.execute
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import java.util.concurrent.TimeUnit

object PortalPacket {
    private val baffle = Baffle.of(50L, TimeUnit.MILLISECONDS)
    val portals = mutableListOf<Portal>()

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerMoveEvent) {
        val to = e.to.getPortal() ?: return
        val from = e.from.getPortal()
        val player = e.player

        if (from === to) return
        if (player.level < to.level) return
        if (!baffle.hasNext(player.name)) return

        OrangDomain.config.getStringList("Join.${to.id}").forEach {
            player.execute(it)
        }

        submit(delay = to.delay) {
            player.teleport(
                to.target,
                TeleportFlag.Relative.YAW,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z
            )
        }
    }

    private fun Location.getPortal(): Portal? {
        return portals.firstOrNull { it.isIn(this) }
    }

    private fun Player.getPortal(): Portal? {
        return portals.firstOrNull { it.isIn(this) }
    }
}