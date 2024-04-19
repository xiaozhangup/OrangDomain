package me.xiaozhangup.domain.refresh.config

import org.bukkit.Material

data class RefreshesGroup(
    val period: Long,
    val source: Material,
    val intensity: Int,
    val failedSkip: Int,
    val regions: List<String>,
    val blocks: Map<Material, Pair<Int, Int>>
) {
    fun runnable(): RefreshRunnable {
        return RefreshRunnable(this)
    }
}