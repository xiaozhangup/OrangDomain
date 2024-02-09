package ray.mintcat.barrier.event

import org.bukkit.entity.Player
import ray.mintcat.barrier.common.poly.BarrierPoly
import taboolib.platform.type.BukkitProxyEvent

class BarrierPlayerLeavePolyEvent(
    val player: Player,
    val poly: BarrierPoly
) : BukkitProxyEvent()