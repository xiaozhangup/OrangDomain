package ray.mintcat.barrier.regen

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import ray.mintcat.barrier.regen.RegenController.submitBlock
import ray.mintcat.barrier.utils.anyHanging
import ray.mintcat.barrier.utils.getPoly
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object RegenListener {
    private val rootMaterial = Material.REDSTONE_TORCH
    private val component = Component.text("× 不能采集有悬挂物的方块")
        .color(TextColor.fromHexString("#ed2e38"))

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun e(e: BlockBreakEvent) {
        val block = e.block
        val player = e.player
        block.location.getPoly()?.let {
            val regens = RegenLoader.regens[it.id] ?: return
            val config = regens.first() // 只能选定一个配置操作

            if (!config.materials.contains(block.type)) return
            if (e.player.isOp && e.player.inventory.itemInMainHand.type === rootMaterial) return
            if (block.anyHanging()) {
                e.isCancelled = true
                player.sendActionBar(component)
                return
            }

            submitBlock(block.location, block.type, config)
        }
    }
}