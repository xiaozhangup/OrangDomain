package ray.mintcat.barrier.regen.tweak.impl

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import ray.mintcat.barrier.regen.RegenController.submitBlock
import ray.mintcat.barrier.regen.config.BasicRegenGroup
import ray.mintcat.barrier.regen.tweak.ExtendBreak
import ray.mintcat.barrier.utils.noLinked

class TreeLinkedBreak : ExtendBreak() {
    override fun hasExtend(block: Block, player: Player): Boolean {
        return block.type.name.endsWith("_LOG")
    }

    override fun execute(block: Block, player: Player, config: BasicRegenGroup) {
        findBlocksAbove(block).forEach {
            if (it.noLinked()) {
                it.drops.forEach { itemStack ->
                    block.world.dropItemNaturally(block.location, itemStack)
                }

                submitBlock(it, config)
            }
        }
    }

    private fun findBlocksAbove(startBlock: Block): List<Block> {
        val blocks = mutableListOf<Block>()
        var currentBlock = startBlock.getRelative(BlockFace.UP)

        while (currentBlock.type == startBlock.type) {
            blocks.add(currentBlock)
            currentBlock = currentBlock.getRelative(BlockFace.UP)
        }

        return blocks
    }
}