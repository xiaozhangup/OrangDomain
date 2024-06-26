package me.xiaozhangup.domain.refresh

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.OrangDomain.refresh
import me.xiaozhangup.domain.refresh.config.RefreshRunnable
import me.xiaozhangup.domain.refresh.config.RefreshesGroup
import org.bukkit.Material

object RefreshLoader {
    private val refreshes = mutableListOf<RefreshesGroup>()
    private val runnable = mutableListOf<RefreshRunnable>()

    fun init() {
        stopAll()
        refresh.reload()
        refreshes.clear()

        refresh.getKeys(false).forEach {
            val section = refresh.getConfigurationSection(it)!!

            val source = Material.valueOf(section.getString("source")!!)
            val period = section.getLong("period")
            val regions = section.getStringList("regions")
            val intensity = section.getInt("intensity", 2)
            val skipFailed = section.getInt("skip_failed", Int.MAX_VALUE)
            val blocksConfig = section.getConfigurationSection("blocks")!!

            val blocks = blocksConfig.getKeys(false).associate { material ->
                val pair = blocksConfig.getString(material)!!.split("..")
                Material.valueOf(material) to (pair[0].toInt() to pair[1].toInt())
            }

            refreshes += RefreshesGroup(
                period,
                source,
                intensity,
                skipFailed,
                regions,
                blocks
            )
        }

        runnable.addAll(refreshes.map { it.runnable() })
        startAll()
    }

    private fun startAll() {
        runnable.forEach {
            it.runTaskTimer(OrangDomain.plugin, 0L, it.group.period)
        }
    }

    private fun stopAll() {
        runnable.forEach {
            it.cancel()
        }
        runnable.clear()
    }
}