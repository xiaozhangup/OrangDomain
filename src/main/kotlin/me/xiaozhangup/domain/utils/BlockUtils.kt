package me.xiaozhangup.domain.utils

import com.jeff_media.customblockdata.CustomBlockData
import me.xiaozhangup.domain.OrangDomain.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.deserializeToItemStack
import taboolib.platform.util.serializeToByteArray
import java.util.*
import kotlin.math.max
import kotlin.math.min

private val skipCheck = listOf(
    Material.TALL_GRASS,
    Material.SHORT_GRASS,
    Material.PINK_PETALS
)

fun Block.isLinkedSolidBlock(
    block: Block = location.clone().apply {
        y = world.maxHeight - 2.0
    }.block,
    skip: List<Material> = listOf()
): Boolean {
    return listOf(
        getRelative(0, 0, 1),
        getRelative(0, 0, -1),
        getRelative(1, 0, 0),
        getRelative(-1, 0, 0)
    ).map {
        val type = it.type
        if (
            skipCheck.contains(type) || skip.contains(type)
        ) true else blockData.isSupported(block)
    }.all { it }
}

// 此方法着重检查上下
fun Block.isHangingBlock(
    block: Block = location.clone().apply {
        y = world.maxHeight - 2.0
    }.block
): Boolean {
    val blockAbove: Block = this.getRelative(0, 1, 0) // 获取方块上方的方块
    val blockBelow: Block = this.getRelative(0, -1, 0) // 获取方块下方的方块

    return !blockBelow.blockData.isSupported(block) || !blockAbove.blockData.isSupported(block)
}

fun Block.noLinked(skip: List<Material> = listOf()): Boolean {
    val block = location.clone().apply {
        y = world.maxHeight - 2.0
    }.block

    return isLinkedSolidBlock(block, skip) && !isHangingBlock(block)
}

fun isPlayerInRectangle(location: Location, loc1: Location, loc2: Location): Boolean {
    val world = location.world
    val minX = min(loc1.x, loc2.x)
    val minY = min(loc1.y, loc2.y)
    val minZ = min(loc1.z, loc2.z)
    val maxX = max(loc1.x, loc2.x)
    val maxY = max(loc1.y, loc2.y)
    val maxZ = max(loc1.z, loc2.z)

    return location.world == world &&
            location.x >= minX && location.x <= maxX &&
            location.y >= minY && location.y <= maxY &&
            location.z >= minZ && location.z <= maxZ
}

fun ItemStack.toBase64(): String {
    return Base64.getEncoder().encodeToString(serializeToByteArray())
}

fun String.toItemStack(): ItemStack {
    return Base64.getDecoder().decode(this).deserializeToItemStack()
}

fun Location.toRecorded(): String {
    val loc = this
    return loc.world!!.name + ":" + loc.x + ":" + loc.y + ":" + loc.z + ":" + loc.yaw + ":" + loc.pitch
}

fun String.toLocation(): Location {
    val locData = this.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val world: World? = Bukkit.getWorld(locData[0])
    val x = locData[1].toDouble()
    val y = locData[2].toDouble()
    val z = locData[3].toDouble()
    val yaw = locData[4].toFloat()
    val pitch = locData[5].toFloat()
    return Location(world, x, y, z, yaw, pitch)
}

val Block.customBlockData get() : CustomBlockData = CustomBlockData(this, plugin)