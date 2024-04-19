package me.xiaozhangup.domain.event

import me.xiaozhangup.domain.common.poly.BarrierPoly
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class BarrierPlayerJoinPolyEvent(
    val player: Player,
    val poly: BarrierPoly
) : BukkitProxyEvent()