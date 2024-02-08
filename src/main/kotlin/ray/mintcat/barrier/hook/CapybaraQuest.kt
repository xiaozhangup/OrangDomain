package ray.mintcat.barrier.hook

import me.xiaozhangup.capybara.serves.quest.impl.once.OnceListeners
import org.bukkit.event.block.BlockBreakEvent
import ray.mintcat.barrier.event.BarrierPlayerLeavePolyEvent
import taboolib.common.platform.event.SubscribeEvent

object CapybaraQuest {
    @SubscribeEvent
    fun e(e: BarrierPlayerLeavePolyEvent) {
        if (e.poly.id != "new_balloon") return
        OnceListeners.passQuest(e.player, "leave_balloon")
    }
}