package me.xiaozhangup.domain.common.poly

import kotlinx.serialization.Serializable
import me.xiaozhangup.domain.utils.serializable.LocationSerializer
import org.bukkit.Location

@Serializable
data class RefreshPoly(
    val id: String,
    @Serializable(with = LocationSerializer::class)
    val pos1: Location,
    @Serializable(with = LocationSerializer::class)
    val pos2: Location
)
