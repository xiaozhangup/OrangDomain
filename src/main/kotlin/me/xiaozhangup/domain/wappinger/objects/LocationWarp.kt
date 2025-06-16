package me.xiaozhangup.domain.wappinger.objects

import me.xiaozhangup.domain.utils.toBase64
import me.xiaozhangup.domain.utils.toRecorded
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*

data class LocationWarp(
    val icon: ItemStack,
    val name: String, // 传送点的名字, 应该是英文
    val location: Location,
    val uuid: UUID
) {
    fun toDataWarp(): DataWarp {
        return DataWarp(
            name,
            icon.toBase64(),
            location.toRecorded(),
            uuid
        )
    }
}