package ray.mintcat.barrier.common.extension

import org.bukkit.entity.Animals
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import ray.mintcat.barrier.OrangDomain.regions
import ray.mintcat.barrier.OrangDomain.worlds
import ray.mintcat.barrier.utils.getPoly
import taboolib.common.platform.event.SubscribeEvent

object PolySpawn {
    @SubscribeEvent
    fun e(e: EntitySpawnEvent) {
        val entity = e.entity
        if (entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return
        entity.location.getPoly()?.let {
            if (regions.getStringList("${it.id}.despawns").contains(entity.type.name)) {
                e.isCancelled = true
            }

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

        if (worlds.contains(entity.world.name)) {
            e.isCancelled = true
        }
    }
}