package ray.mintcat.barrier.utils

import org.bukkit.Material
import org.bukkit.block.Block

fun Block.isLinkedBlock(material: Material): Boolean {
    return this.getRelative(0, 0, 1).type == material ||
            this.getRelative(0, 0, -1).type == material ||
            this.getRelative(1, 0, 0).type == material ||
            this.getRelative(-1, 0, 0).type == material
}

fun Block.isLinkedSolidBlock(): Boolean {
    return this.getRelative(0, 0, 1).type.isSolidOrAir() &&
            this.getRelative(0, 0, -1).type.isSolidOrAir() &&
            this.getRelative(1, 0, 0).type.isSolidOrAir() &&
            this.getRelative(-1, 0, 0).type.isSolidOrAir()
}

fun Block.isLinkedTransparentBlock(): Boolean {
    return this.getRelative(0, 0, 1).type.isTransparent ||
            this.getRelative(0, 0, -1).type.isTransparent ||
            this.getRelative(1, 0, 0).type.isTransparent ||
            this.getRelative(-1, 0, 0).type.isTransparent
}

// 此方法着重检查上下
fun Block.isHangingBlock(list: List<Material> = listOf(Material.POINTED_DRIPSTONE)): Boolean {
    val blockAbove: Block = this.getRelative(0, 1, 0) // 获取方块上方的方块
    val blockBelow: Block = this.getRelative(0, -1, 0) // 获取方块下方的方块

    if (!blockAbove.isSolid && !blockAbove.type.isTransparent && !blockAbove.isLiquid) return true
    if (!blockBelow.isSolid && !blockBelow.type.isTransparent && !blockAbove.isLiquid) return true
    if (list.contains(blockAbove.type) || list.contains(blockBelow.type)) return true

    return false
}

fun Block.anyHanging(): Boolean {
    return !isLinkedTransparentBlock() && isLinkedSolidBlock() && !isHangingBlock()
}

private fun Material.isSolidOrAir() = isSolid || isAir