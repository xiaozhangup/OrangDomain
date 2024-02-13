package ray.mintcat.barrier.portal

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.Player
import ray.mintcat.barrier.utils.isPlayerInRectangle
import ray.mintcat.barrier.utils.serializable.LocationSerializer

@Serializable
data class Portal(
    val id: String,
    @Serializable(with = LocationSerializer::class)
    val pos1: Location,
    @Serializable(with = LocationSerializer::class)
    val pos2: Location,
    @Serializable(with = LocationSerializer::class)
    var target: Location,
    val level: Int
) {
    fun isIn(player: Player, skipCheck: Boolean = false): Boolean {
        val sameWorld = player.world == pos1.world
        val isInAABB = isPlayerInRectangle(player, pos1, pos2)
        val hasRequiredLevel = player.level >= level

        return sameWorld && (skipCheck || (isInAABB && hasRequiredLevel))
    }
}