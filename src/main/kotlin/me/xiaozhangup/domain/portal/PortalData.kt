package me.xiaozhangup.domain.portal

import me.xiaozhangup.whale.util.ext.asLocation
import me.xiaozhangup.whale.util.ext.asString
import me.xiaozhangup.whale.util.ext.asStringWithoutYawPitch
import org.bukkit.Location
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import kotlin.math.max
import kotlin.math.min

data class PortalData(
    val id: String,
    val world: String,
    val pos1: Location,
    val pos2: Location,
    val target: Location
) {
    val rx: ClosedFloatingPointRange<Double> = min(pos1.x, pos2.x)..max(pos1.x, pos2.x)
    val ry: ClosedFloatingPointRange<Double> = min(pos1.y, pos2.y)..max(pos1.y, pos2.y)
    val rz: ClosedFloatingPointRange<Double> = min(pos1.z, pos2.z)..max(pos1.z, pos2.z)

    constructor(id: String, config: ConfigurationSection) : this(
        id,
        config.getString("world")!!,
        config.getString("pos1")!!.asLocation(),
        config.getString("pos2")!!.asLocation(),
        config.getString("target")!!.asLocation()
    )

    fun saveTo(config: Configuration) {
        config["$id.world"] = world
        config["$id.pos1"] = pos1.asStringWithoutYawPitch()
        config["$id.pos2"] = pos2.asStringWithoutYawPitch()
        config["$id.target"] = target.asString()
    }

    fun inPortal(location: Location): Boolean {
        if (location.world?.name != world) return false
        return location.x in rx && location.z in rz && location.y in ry
    }
}