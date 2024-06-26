package me.xiaozhangup.domain.portal

import kotlinx.serialization.Serializable
import me.xiaozhangup.domain.utils.isPlayerInRectangle
import me.xiaozhangup.domain.utils.serializable.LocationSerializer
import org.bukkit.Location
import org.bukkit.entity.Player

@Serializable
data class Portal(
    val id: String,
    @Serializable(with = LocationSerializer::class)
    val pos1: Location,
    @Serializable(with = LocationSerializer::class)
    val pos2: Location,
    @Serializable(with = LocationSerializer::class)
    var target: Location,
    val level: Int,
    val delay: Long = 0
) {
    fun isIn(player: Player, skipCheck: Boolean = false): Boolean {
        if (player.world != pos1.world) return false

        val isInAABB = isPlayerInRectangle(player.location, pos1, pos2)
        val hasRequiredLevel = player.level >= level
        return (skipCheck || (isInAABB && hasRequiredLevel))
    }

    fun isIn(location: Location): Boolean {
        if (location.world != pos1.world) return false

        return isPlayerInRectangle(location, pos1, pos2)
    }
}