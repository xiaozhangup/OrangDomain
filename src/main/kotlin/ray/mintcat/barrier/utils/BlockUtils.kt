package ray.mintcat.barrier.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPhysicsEvent
import ray.mintcat.barrier.regen.RegenLoader
import kotlin.math.max
import kotlin.math.min

private val skipCheck = listOf(
    Material.TALL_GRASS,
    Material.SHORT_GRASS,
    Material.PINK_PETALS
)

fun Block.isLinkedSolidBlock(block: Block, skip: List<Material> = listOf()): Boolean {
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
fun Block.isHangingBlock(block: Block): Boolean {
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

fun isPlayerInRectangle(player: Player, loc1: Location, loc2: Location): Boolean {
    val world = player.world
    val minX = min(loc1.x, loc2.x)
    val minY = min(loc1.y, loc2.y)
    val minZ = min(loc1.z, loc2.z)
    val maxX = max(loc1.x, loc2.x)
    val maxY = max(loc1.y, loc2.y)
    val maxZ = max(loc1.z, loc2.z)

    val playerLocation = player.location

    return playerLocation.world == world &&
            playerLocation.x >= minX && playerLocation.x <= maxX &&
            playerLocation.y >= minY && playerLocation.y <= maxY &&
            playerLocation.z >= minZ && playerLocation.z <= maxZ
}