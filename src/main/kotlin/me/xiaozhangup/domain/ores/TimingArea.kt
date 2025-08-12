package me.xiaozhangup.domain.ores

import me.xiaozhangup.domain.OrangDomain.plugin
import me.xiaozhangup.domain.utils.toLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.library.configuration.ConfigurationSection
import kotlin.math.max
import kotlin.math.min

class TimingArea(
    val backPoint: Location,
    val area: List<Pair<Location, Location>>
) {
    private val world = backPoint.world
    private val areaList = area.map {
        val pos1 = it.first
        val pos2 = it.second
        Triple(
            min(pos1.blockX, pos2.blockX)..max(pos1.blockX, pos2.blockX),
            min(pos1.blockY, pos2.blockY)..max(pos1.blockY, pos2.blockY),
            min(pos1.blockZ, pos2.blockZ)..max(pos1.blockZ, pos2.blockZ)
        )
    }
    private var task: PlatformExecutor.PlatformTask? = null

    @Suppress("UNCHECKED_CAST")
    constructor(section: ConfigurationSection) : this(
        toLocation(section.getString("back_point")!!),
        (section["areas", listOf<List<String>>()] as List<List<String>>).map {
            val pos1 = toLocation(it[0])
            val pos2 = toLocation(it[1])
            pos1 to pos2
        }
    )

    fun inArea(location: Location): Boolean {
        if (location.world !== world) return false
        return areaList.any {
            location.blockX in it.first && location.blockY in it.second && location.blockZ in it.third
        }
    }

    fun scheduleTask() {
        task = submit(period = 20L) {
            for (player in world.players.filter { inArea(it.location) }) {
                val item = player.inventory.contents.firstOrNull { itemStack ->
                    val t = itemStack?.persistentDataContainer?.get(timingKey, PersistentDataType.INTEGER) ?: 0
                    t > 0
                }
                if (item == null || !item.hasItemMeta()) {
                    teleportToSpawn(player)
                    continue
                }

                val meta = item.itemMeta as? Damageable
                if (meta == null) {
                    teleportToSpawn(player)
                    continue
                }

                val time = meta.persistentDataContainer.get(timingKey, PersistentDataType.INTEGER) ?: 0
                if (time <= 0) {
                    teleportToSpawn(player)
                    continue
                }

                meta.damage = 100 - ((time * 100.0) / MAX_TIME).toInt()
                meta.persistentDataContainer.set(timingKey, PersistentDataType.INTEGER, time - 1)
                item.itemMeta = meta

                player.sendActionBar(
                    MiniMessage.miniMessage().deserialize("<color:#fff5d0>⏳ 沙漏剩余 ${formatSeconds(time)}")
                )
            }
        }
    }

    fun unscheduleTask() {
        task?.cancel()
    }

    private fun teleportToSpawn(player: Player) {
        player.teleport(backPoint)
        player.sendActionBar(TIME_RUNOUT)
    }

    companion object {
        val timingKey by lazy { NamespacedKey(plugin, "timing") }
        val TIME_RUNOUT = MiniMessage.miniMessage().deserialize("<color:#fff5d0>⏳ 你没有更多时间了")
        const val MAX_TIME = 13 * 60

        private fun formatSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return "$minutes:${String.format("%02d", remainingSeconds)}"
        }
    }
}