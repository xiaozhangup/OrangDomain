package me.xiaozhangup.domain.wappinger.objects

import kotlinx.serialization.Serializable
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toItemStack
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toLocation
import me.xiaozhangup.domain.utils.serializable.UUIDSerializable
import java.util.*

@Serializable
data class DataWarp(
    val name: String,
    val icon: String,
    val location: String,
    @Serializable(with = UUIDSerializable::class)
    val uuid: UUID
) {
    fun toLocationWarp(): LocationWarp {
        return LocationWarp(
            icon.toItemStack(),
            name,
            location.toLocation(),
            uuid
        )
    }
}
