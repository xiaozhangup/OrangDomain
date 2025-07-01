package me.xiaozhangup.domain.ores

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.xiaozhangup.domain.ores.Ores.oreKey
import me.xiaozhangup.domain.ores.Ores.rotations
import me.xiaozhangup.domain.ores.Ores.textures
import me.xiaozhangup.domain.utils.IntervalTrigger
import me.xiaozhangup.domain.utils.customBlockData
import me.xiaozhangup.domain.utils.serializable.LocationSerializer
import me.xiaozhangup.whale.util.ext.ItemStackBuilder.Companion.getTextureURL
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Skull
import org.bukkit.persistence.PersistentDataType
import taboolib.library.configuration.ConfigurationSection
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Refreshing(
    val id: String,
    @Serializable(with = LocationSerializer::class)
    val pos1: Location,
    @Serializable(with = LocationSerializer::class)
    val pos2: Location
) {
    @Transient
    var setting: RefreshSetting? = null

    @Transient
    var interval: IntervalTrigger? = null

    @Transient
    val ry: IntRange = min(pos1.blockY, pos2.blockY)..max(pos1.blockY, pos2.blockY)

    @Transient
    val rx: IntRange = min(pos1.blockX, pos2.blockX)..max(pos1.blockX, pos2.blockX)

    @Transient
    val rz: IntRange = min(pos1.blockZ, pos2.blockZ)..max(pos1.blockZ, pos2.blockZ)

    fun heightRange(): IntRange = ry
    fun widthXRange(): IntRange = rx
    fun widthZRange(): IntRange = rz

    fun loadSetting(section: ConfigurationSection?) {
        if (section == null) {
            setting = null
            return
        }
        loadSetting(RefreshSetting(section))
    }

    fun loadSetting(setting: RefreshSetting) {
        this.setting = setting
        interval = IntervalTrigger(setting.interval)
    }

    fun refresh(): Boolean {
        val x = widthXRange().random()
        val z = widthZRange().random()
        val loc = Location(pos1.world, x.toDouble(), 0.0, z.toDouble())
        val blocks = findAir(loc).filterNot { b ->
            anyNear(b) {
                it.type == Material.PLAYER_HEAD && it.customBlockData.has(oreKey)
            }
        }
        if (blocks.isNotEmpty()) {
            val b = blocks.random()
            val id = setting!!.weight.random()!!
            placeSkullBlock(
                textures[id] ?: throw IllegalArgumentException("Texture $id not found"),
                b
            )
            b.customBlockData.set(oreKey, PersistentDataType.STRING, id)
            return true
        }

        return false
    }

    fun inRefreshing(block: Block): Boolean {
        return inRefreshing(block.location)
    }

    fun inRefreshing(location: Location): Boolean {
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        return y in ry && x in rx && z in rz
    }

    private fun findAir(location: Location): List<Block> {
        val blocks = mutableListOf<Block>()
        for (i in heightRange()) {
            location.y = i.toDouble()
            val block = location.block
            val down = block.getRelative(BlockFace.DOWN)
            if (block.type.isAir && down.isSolid && down.type in setting!!.materials) {
                blocks += block
            }
        }
        return blocks
    }

    private fun placeSkullBlock(
        texture: String,
        block: Block
    ) {
        block.type = Material.PLAYER_HEAD

        val state = block.state as org.bukkit.block.Skull
        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.toByteArray()), "Ore")
        val textures = profile.textures.apply {
            skin = getTextureURL(texture)
        }
        profile.setTextures(textures)
        state.setPlayerProfile(profile)
        state.update()

        val data = block.blockData as Skull
        data.rotation = rotations.random()
        block.blockData = data
    }

    private fun anyNear(block: Block, boolean: (Block) -> Boolean): Boolean {
        val range = setting!!.radius
        for (x in -range..range) {
            for (y in -range..range) {
                for (z in -range..range) {
                    val block = block.getRelative(x, y, z)
                    if (boolean(block)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
