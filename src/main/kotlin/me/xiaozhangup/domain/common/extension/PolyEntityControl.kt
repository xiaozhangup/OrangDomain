package me.xiaozhangup.domain.common.extension

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.OrangDomain.regions
import me.xiaozhangup.domain.common.poly.BarrierPoly
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

    /**
     * 判断是否可以伤害动物
     *
     * @param poly Poly对象
     * @return 如果允许伤害动物则返回true
     */
    fun canHurtAnimal(poly: BarrierPoly): Boolean {
        return regions.getBoolean("${poly.id}.spawnAnimal")
    }

    /**
     * 判断是否可以伤害怪物
     *
     * @param poly Poly对象
     * @return 如果允许伤害怪物则返回true
     */
    fun canHurtMonster(poly: BarrierPoly): Boolean {
        return regions.getBoolean("${poly.id}.spawnMonster")
    }
}