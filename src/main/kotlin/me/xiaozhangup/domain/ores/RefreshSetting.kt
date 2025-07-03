package me.xiaozhangup.domain.ores

import org.bukkit.Material
import taboolib.common5.RandomList
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap

class RefreshSetting(
    val interval: Int, // 间隔
    val radius: Int, // 生成矿物的检测半径
    val materials: List<Material>, // 可以生成在什么材质上
    val weight: RandomList<String>,
    val loot: Map<String, List<String>> // 额外的奖励
) {
    constructor(id: String, config: Configuration) : this(
        interval = 5,
        materials = listOf(),
        radius = 3,
        weight = RandomList(),
        loot = mapOf()
    ) {
        config["$id.interval"] = interval
        config["$id.materials"] = materials.map { it.name }
        config["$id.radius"] = radius
        config["$id.weight"] = weight.values().map { it.element to it.index }
        config["$id.loot"] = loot
    }

    constructor(section: ConfigurationSection) : this(
        interval = section.getInt("interval", 5),
        materials = section.getStringList("materials").map {
            Material.getMaterial(it.uppercase()) ?: throw IllegalArgumentException("Invalid material: $it")
        },
        radius = section.getInt("radius", 3),
        weight = RandomList<String>().apply {
            section.getMap<String, Int>("weight").forEach { (key, value) ->
                add(key, value)
            }
        },
        loot = section.getMap<String, List<String>>("loot")
    )

    fun getLoot(type: String): List<String> {
        return loot[type] ?: listOf()
    }
}