package me.xiaozhangup.domain.poly

import kotlinx.serialization.Serializable
import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.utils.serializable.LocationSerializer
import me.xiaozhangup.domain.utils.serializable.UUIDSerializable
import me.xiaozhangup.domain.utils.tpDelay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Serializable
class Poly(
    var name: String,
    var id: String = name,
    @Serializable(with = UUIDSerializable::class)
    var admin: UUID,
    @Serializable(with = LocationSerializer::class)
    var door: Location,
    val nodes: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf(),
    val destructible: MutableSet<String> = mutableSetOf(),
    val interactive: MutableSet<String> = mutableSetOf(),
    var priority: Int = 0,
    val permissions: MutableMap<String, Boolean> = mutableMapOf(),
    val users: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()
) {
    fun isDestructible(material: Material): Boolean {
        return destructible.contains(material.name)
    }

    fun teleport(player: Player) {
        if (player.isOp) {
            player.teleport(door)
        } else {
            player.tpDelay(3, door)
        }
    }

    fun hasPermission(key: String, player: String? = null, def: Boolean? = false): Boolean {
        return if (Bukkit.getPlayerExact(player ?: "null")?.isOp == true || users[player]?.get("admin") == true) {
            return true
        } else if (player != null && users.containsKey(player)) {
            if (users[player]!![key] == null) {
                permissions[key] ?: OrangDomain.permissions.firstOrNull { it.id == key }?.default ?: def!!
            } else {
                users[player]!![key]
            }
            OrangDomain.permissions.firstOrNull { it.id == key }?.default ?: def!!
        } else {
            permissions[key] ?: OrangDomain.permissions.firstOrNull { it.id == key }?.default ?: def!!
        }
    }

    fun worldName(): String {
        return nodes.first().world.name
    }

    fun inNode(location: Location): Boolean {
        if (nodes.first().world != location.world) {
            return false
        }
        if (nodes.size < 3) {
            return false
        }
        // 从点向右画一条射线，计算和多边形相交的次数。
        var numIntersections = 0
        for (i in nodes.indices) {
            val p1 = nodes[i]
            val p2 = nodes[(i + 1) % nodes.size]
            if (onSegment(location, p1, p2)) {
                // 如果点在多边形的边上，则被认为不在多边形内部。
                return false
            }
            if (location.z > min(p1.z, p2.z)
                && location.z <= max(p1.z, p2.z)
            ) {
                // 判断射线是否与当前边相交。
                val xIntersection = (location.z - p1.z) * (p2.x - p1.x) / (p2.z - p1.z) + p1.x
                if (xIntersection > location.x) {
                    numIntersections++
                }
            }
        }
        // 奇数次相交，则点在多边形内部；偶数次相交，则点在多边形外部。
        return numIntersections % 2 == 1
    }

    private fun onSegment(location: Location, p1: Location, p2: Location): Boolean {
        val d1 = distance(location, p1)
        val d2 = distance(location, p2)
        val edgeLength = distance(p1, p2)
        // 如果点到边端点的距离和边长度之和相等，则认为点在边上。
        return abs(d1 + d2 - edgeLength) < 0.00001
    }

    private fun distance(p1: Location, p2: Location): Double {
        val dx = p1.x - p2.x
        val dz = p1.z - p2.z
        return sqrt(dx * dx + dz * dz)
    }

}