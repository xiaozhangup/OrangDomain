package me.xiaozhangup.domain.refresh.block

import ink.pmc.advkt.component.component
import me.xiaozhangup.domain.utils.fromLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.data.type.Beehive
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import java.util.concurrent.TimeUnit

object BeeHive {
    private val baffle = Baffle.of(10, TimeUnit.MINUTES)
    private val component = Component.text("× 蜂巢还需一段时间刷新")
        .color(TextColor.fromHexString("#f7db29"))

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK) return
        val block = e.clickedBlock ?: return
        val beehive = block.blockData as? Beehive ?: return

        if (beehive.honeyLevel < 1) {
            if (baffle.hasNext(fromLocation(block.location))) {
                beehive.honeyLevel = 5
                block.blockData = beehive
            } else {
                e.player.sendActionBar(component)
            }
        }
    }
}