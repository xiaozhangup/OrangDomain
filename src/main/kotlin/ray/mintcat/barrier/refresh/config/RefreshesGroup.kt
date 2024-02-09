package ray.mintcat.barrier.refresh.config

import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

data class RefreshesGroup(
    val period: Long,
    val source: Material,
    val regions: List<String>,
    val blocks: Map<Material, Pair<Int, Int>>
) {
    fun runnable(): RefreshRunnable {
        return RefreshRunnable(this)
    }
}