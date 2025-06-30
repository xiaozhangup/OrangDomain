package me.xiaozhangup.domain.poly.event

import me.xiaozhangup.domain.poly.Poly
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class PlayerJoinPolyEvent(
    val player: Player,
    val poly: Poly
) : BukkitProxyEvent()