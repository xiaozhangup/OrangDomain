package ray.mintcat.barrier.utils

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent

fun EntityDamageByEntityEvent.rootDamager(): Player? {
    val entity = damager as? Player
    if (entity != null) return entity

    if (damager is Projectile) {
        val shooter = (damager as Projectile).shooter as? Player
        if (shooter != null) return shooter
    }

    return null
}