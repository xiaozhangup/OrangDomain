package me.xiaozhangup.domain.common.extension

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.OrangDomain.regions
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.rootDamager
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object PolyEntityControl {
    @SubscribeEvent
    fun e(e: EntityChangeBlockEvent) {
        if (e.entity.type == EntityType.PLAYER || e.block.location.getPoly() == null) return
        if (e.entity !is LivingEntity) return

        e.isCancelled = true
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: EntityDamageByEntityEvent) {
        val player = e.rootDamager() ?: return
        if (e.entity is Player || player.isOp) return

        val poly = e.entity.location.getPoly()
        if (poly != null) {
            val spawnAnimal = regions.getBoolean("${poly.id}.spawnAnimal")
            val spawnMonster = regions.getBoolean("${poly.id}.spawnMonster")

            if (e.entity is Animals) {
                if (!spawnAnimal) {
                    e.isCancelled = true
                }
                return
            } else if (e.entity is Monster) {
                if (!spawnMonster) {
                    e.isCancelled = true
                }
                return
            }
        }

        if (OrangDomain.worlds.contains(e.entity.world.name) && !player.isOp) {
            e.isCancelled = true
        }
    }
}