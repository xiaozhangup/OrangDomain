package ray.mintcat.barrier.common.poly

import kotlinx.serialization.Serializable
import org.bukkit.Location
import ray.mintcat.barrier.utils.serializable.LocationSerializer

@Serializable
data class RefreshPoly(
    val id: String,
    @Serializable(with = LocationSerializer::class)
    val pos1: Location,
    @Serializable(with = LocationSerializer::class)
    val pos2: Location
)
