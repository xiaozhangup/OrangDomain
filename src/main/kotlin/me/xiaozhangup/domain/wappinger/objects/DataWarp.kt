package me.xiaozhangup.domain.wappinger.objects

import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toItemStack
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toLocation
import java.util.*

data class DataWarp(
    val name: String,
    val icon: String,
    val location: String,
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
