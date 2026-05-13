package me.xiaozhangup.domain.config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import taboolib.library.configuration.ConfigurationSection

data class WorldSettings(
    val spawn: String,
    val location: String,
    val realisticTime: List<String>,
    val globalProtect: List<String>,
    val accessible: List<String>
) {
    constructor(config: ConfigurationSection) : this(
        spawn = config.getString("spawn", "world")!!,
        location = config.getString("location", "")!!,
        realisticTime = config.getStringList("realistic_time"),
        globalProtect = config.getStringList("global_protect"),
        accessible = config.getStringList("accessible")
    )

    fun spawnWorld() : World? {
        return Bukkit.getWorld(spawn)
    }

    fun spawnLocation() : Location? {
        if (location.isEmpty()) return null
        val world = spawnWorld() ?: return null
        val section = location.split(':')
        if (section.size != 3) return null
        val pos = section[0].split(',')
        if (pos.size != 3) return null
        val yaw = section[1]
        val pitch = section[2]
        return Location(world, pos[0].toDouble(), pos[1].toDouble(), pos[2].toDouble(), yaw.toFloat(), pitch.toFloat())
    }
}