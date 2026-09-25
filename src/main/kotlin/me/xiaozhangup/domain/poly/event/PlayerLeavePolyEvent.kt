package me.xiaozhangup.domain.poly.event

import me.xiaozhangup.domain.poly.Poly
import org.bukkit.entity.Player
import me.xiaozhangup.crab.event.CrabEvent

class PlayerLeavePolyEvent(
    val player: Player,
    val poly: Poly
) : CrabEvent() {

    override fun getHandlers(): org.bukkit.event.HandlerList = handlerList

    companion object {
        private val handlerList = org.bukkit.event.HandlerList()
        @JvmStatic fun getHandlerList(): org.bukkit.event.HandlerList = handlerList
    }
}
