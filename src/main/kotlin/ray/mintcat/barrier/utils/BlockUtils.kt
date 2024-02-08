package ray.mintcat.barrier.utils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockPhysicsEvent

fun Block.isLinkedSolidBlock(block: Block): Boolean {
    return this.getRelative(0, 0, 1).blockData.isSupported(block) &&
            this.getRelative(0, 0, -1).blockData.isSupported(block) &&
            this.getRelative(1, 0, 0).blockData.isSupported(block) &&
            this.getRelative(-1, 0, 0).blockData.isSupported(block)
}

// 此方法着重检查上下
fun Block.isHangingBlock(block: Block): Boolean {
    val blockAbove: Block = this.getRelative(0, 1, 0) // 获取方块上方的方块
    val blockBelow: Block = this.getRelative(0, -1, 0) // 获取方块下方的方块

    return !blockBelow.blockData.isSupported(block) || !blockAbove.blockData.isSupported(block)
}

fun Block.noLinked(): Boolean {
    val block = location.clone().apply {
        y = world.maxHeight - 2.0
    }.block

    return isLinkedSolidBlock(block) && !isHangingBlock(block)
}