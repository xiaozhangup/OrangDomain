package me.xiaozhangup.domain.balloon

import me.xiaozhangup.domain.utils.toLocation
import org.bukkit.Location
import taboolib.library.configuration.ConfigurationSection

data class BalloonWarp(
    val id: String,
    val name: String,
    val skull: String,
    val location: Location,
    val lore: List<String>,
    val level: Int
) {
    constructor(yamlSection: ConfigurationSection, id: String) : this(
        id,
        yamlSection.getString("name")!!,
        yamlSection.getString("skull")!!,
        toLocation(yamlSection.getString("loc")!!),
        yamlSection.getStringList("lore"),
        yamlSection.getInt("level")
    )
}