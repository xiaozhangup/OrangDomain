package me.xiaozhangup.domain.refresh.config

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.common.poly.RefreshPoly
import me.xiaozhangup.domain.utils.isHangingBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.util.random

class RefreshRunnable(
    val group: RefreshesGroup
) : BukkitRunnable() {
    private var failed = 0

    override fun run() {
        if (failed >= group.failedSkip) {
            failed--
            return
        }

        val polys = group.regions.mapNotNull { id ->
            OrangDomain.refreshes.firstOrNull { it.id == id }
        }

        polys.forEach { poly ->
            val blocks = tryFind(poly)
            if (blocks.isEmpty()) return

            for (block in blocks.shuffled()) {
                val material =
                    group.blocks.filter { block.location.y.toInt() in it.value.first..it.value.second }.map { it.key }
                if (material.isEmpty()) continue

                val type = material.random()
                if (
                    getLinkedBlocks(block).filter { group.blocks.containsKey(it.type) }.size < group.intensity &&
                    !block.isHangingBlock()
                ) {
                    if (failed > 0) {
                        failed--
                    }

                    block.type = type
                    break
                } else {
                    failed++
                    continue
                }
            }
        }
    }

    private fun tryFind(poly: RefreshPoly): List<Block> {
        val pos1 = poly.pos1
        val pos2 = poly.pos2

        val x = random(getXRange(pos1, pos2))
        val z = random(getZRange(pos1, pos2))
        val loc = Location(pos1.world, x.toDouble(), 0.0, z.toDouble())

        return traverseBlocksByYRange(loc, getYRange(pos1, pos2), group.source)
    }

    private fun traverseBlocksByYRange(location: Location, yRange: Pair<Int, Int>, type: Material): List<Block> {
        val (minY, maxY) = yRange

        return (minY..maxY).flatMap { y ->
            val block = Location(location.world, location.x, y.toDouble(), location.z).block
            if (block.type == type && getAdjacentBlocks(block).any { it.type == Material.AIR }) {
                listOf(block)
            } else {
                emptyList()
            }
        }
    }

    private fun getAdjacentBlocks(block: Block): List<Block> {
        return listOf(
            block.getRelative(BlockFace.UP),
            block.getRelative(BlockFace.DOWN),
            block.getRelative(BlockFace.EAST),
            block.getRelative(BlockFace.SOUTH),
            block.getRelative(BlockFace.NORTH),
            block.getRelative(BlockFace.WEST)
        )
    }

    private fun getLinkedBlocks(block: Block): List<Block> {
        val blocks = mutableListOf<Block>()
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    blocks += block.getRelative(x, y, z)
                }
            }
        }
        return blocks
    }

    private fun getXRange(location1: Location, location2: Location): Pair<Int, Int> {
        val minX = minOf(location1.x, location2.x)
        val maxX = maxOf(location1.x, location2.x)
        return minX.toInt() to maxX.toInt()
    }

    private fun getYRange(location1: Location, location2: Location): Pair<Int, Int> {
        val minY = minOf(location1.y, location2.y)
        val maxY = maxOf(location1.y, location2.y)
        return minY.toInt() to maxY.toInt()
    }

    private fun getZRange(location1: Location, location2: Location): Pair<Int, Int> {
        val minZ = minOf(location1.z, location2.z)
        val maxZ = maxOf(location1.z, location2.z)
        return minZ.toInt() to maxZ.toInt()
    }

    private fun random(pair: Pair<Int, Int>): Int {
        return random(pair.first, pair.second)
    }
}