package me.xiaozhangup.domain.regen

import me.xiaozhangup.domain.regen.RegenController.submitBlock
import me.xiaozhangup.domain.regen.RegenLoader.isBreakable
import me.xiaozhangup.domain.utils.getPoly
import me.xiaozhangup.domain.utils.noLinked
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object RegenListener {
    private val rootMaterial = Material.REDSTONE_TORCH
    private val component = Component.text("× 不能采集有悬挂物的方块")
        .color(TextColor.fromHexString("#ed2e38"))
    private val unregion = Component.text("× 此区域不能采集此方块")
        .color(TextColor.fromHexString("#ed2e38"))

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val player = event.player

        if (player.isOp && player.inventory.itemInMainHand.type === rootMaterial) {
            return
        }
        if (player.gameMode == GameMode.CREATIVE) return

        val poly = block.location.getPoly()
        if (poly != null) {
            val regens = RegenLoader.regens[poly.id]
            val breakable = isBreakable(block.type)

            if (regens == null) {
                if (breakable) {
                    player.sendActionBar(unregion)
                }
                return
            }

            val config = regens.firstOrNull { group ->
                group.materials.contains(block.type)
            }

            if (config == null) {
                if (breakable) {
                    player.sendActionBar(unregion)
                }
                return
            }

            if (!block.noLinked(config.materials) && config.check) {
                event.isCancelled = true
                player.sendActionBar(component)
                return
            }

            RegenLoader.extendbreak.filter { it.hasExtend(block, player) }.forEach {
                it.execute(block, player, config)
            }
            submitBlock(block, config)
        }
    }
}