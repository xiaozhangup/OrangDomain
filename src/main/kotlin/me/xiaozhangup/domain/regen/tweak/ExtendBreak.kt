package me.xiaozhangup.domain.regen.tweak

import me.xiaozhangup.domain.regen.RegenLoader
import me.xiaozhangup.domain.regen.config.BasicRegenGroup
import org.bukkit.block.Block
import org.bukkit.entity.Player

open class ExtendBreak {
    open fun hasExtend(block: Block, player: Player): Boolean = false

    open fun execute(block: Block, player: Player, config: BasicRegenGroup) {

    }

    fun register() {
        RegenLoader.extendbreak.add(this)
    }
}