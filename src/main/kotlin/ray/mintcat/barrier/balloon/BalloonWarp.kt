package ray.mintcat.barrier.balloon

import org.bukkit.Location
import ray.mintcat.barrier.utils.toLocation
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigSection

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