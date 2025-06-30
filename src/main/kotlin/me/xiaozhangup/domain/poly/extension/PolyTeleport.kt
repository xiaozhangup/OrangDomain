package me.xiaozhangup.domain.poly.extension

import me.xiaozhangup.domain.OrangDomain.regions
import me.xiaozhangup.domain.utils.getPoly
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityTeleportEvent
import taboolib.common.platform.event.SubscribeEvent

object PolyTeleport {
    private val typed = listOf(
        EntityType.ENDERMAN,
        EntityType.SHULKER
    )

    @SubscribeEvent
    fun e(e: EntityTeleportEvent) {
        val entity = e.entity

        if (!typed.contains(e.entityType)) return
        if (
            e.from.getPoly() === e.to?.getPoly()
        ) return

        entity.location.getPoly()?.let {
            if (!regions.getBoolean("${it.id}.entityTeleport")) {
                e.isCancelled = true
            }
        }
    }
}