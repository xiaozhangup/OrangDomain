package me.xiaozhangup.domain.poly.extension

import me.xiaozhangup.domain.OrangDomain.Companion.regions
import me.xiaozhangup.domain.OrangDomain.Companion.world
import me.xiaozhangup.domain.utils.getPoly
import org.bukkit.entity.Animals
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import me.xiaozhangup.crab.event.SubscribeEvent

object PolySpawn {
    @SubscribeEvent
    fun e(e: EntitySpawnEvent) {
        val entity = e.entity
        entity.location.getPoly()?.let {
            if (regions.getStringList("${it.id}.despawns").contains(entity.type.name)) {
                e.isCancelled = true
            }
            if (entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return

            if (entity is Monster) {
                if (!regions.getBoolean("${it.id}.spawnMonster")) {
                    e.isCancelled = true
                    return
                }
            }
            if (entity is Animals) {
                if (!regions.getBoolean("${it.id}.spawnAnimal")) {
                    e.isCancelled = true
                    return
                }
            }

            if (regions.getBoolean("${it.id}.neverSpawn", true)) {
                e.isCancelled = true
            }
        }

        if (world.globalProtect.contains(entity.world.name)) {
            e.isCancelled = true
        }
    }
}