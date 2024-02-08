package ray.mintcat.barrier.regen.tweak

import org.bukkit.block.Block
import org.bukkit.entity.Player
import ray.mintcat.barrier.regen.RegenLoader
import ray.mintcat.barrier.regen.config.BasicRegenGroup

open class ExtendBreak {
    open fun hasExtend(block: Block, player: Player): Boolean = false

    open fun execute(block: Block, player: Player, config: BasicRegenGroup) {

    }

    fun registers() {
        RegenLoader.extendbreak.add(this)
    }
}