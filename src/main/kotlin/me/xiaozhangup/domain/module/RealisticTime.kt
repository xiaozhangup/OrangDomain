package me.xiaozhangup.domain.module

import me.xiaozhangup.domain.OrangDomain.config
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object RealisticTime {
    private var worlds: List<String> = listOf()
    private val scheduledTask: PlatformExecutor.PlatformTask

    init {
        scheduledTask = submit(period = 20) {
            worlds
                .mapNotNull { Bukkit.getWorld(it) }
                .forEach { syncTime(it) }
        }
    }

    fun loadWorlds() {
        worlds = config.getStringList("TimeSyncWorlds")
    }

    private fun syncTime(world: World) {
        if (world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true) return
        val formatHours = DateTimeFormatter.ofPattern("HH")
        val formatMinutes = DateTimeFormatter.ofPattern("mm")

        val date: ZonedDateTime = ZonedDateTime.now()

        val computedTime: Long = convertTimeToMinecraftTicks(
            date.format(formatHours).toInt(),
            date.format(formatMinutes).toInt()
        )

        world.time = computedTime
    }

    private fun convertTimeToMinecraftTicks(hours: Int, minutes: Int): Long {
        val currentHoursConverted = (hours * 1000) - 6000
        val currentMinutesConverted = (minutes * 10)

        var calculated = currentHoursConverted + currentMinutesConverted

        if (calculated < 0) {
            calculated += 24000
        }

        return calculated.toLong()
    }

}