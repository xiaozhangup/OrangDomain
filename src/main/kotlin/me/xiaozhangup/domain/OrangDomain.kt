package me.xiaozhangup.domain

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import me.xiaozhangup.domain.config.WorldSettings
import me.xiaozhangup.domain.poly.Poly
import me.xiaozhangup.domain.poly.permission.Permission
import org.bukkit.Material
import taboolib.common.io.newFile
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.nio.charset.StandardCharsets

object OrangDomain : Plugin() {

    @Config(migrate = true, value = "settings.yml")
    lateinit var config: Configuration
        private set

    @Config(value = "regions.yml")
    lateinit var regions: Configuration
        private set

    val polys = ArrayList<Poly>()
    val permissions = ArrayList<Permission>()
    val plugin by lazy { BukkitPlugin.getInstance() }
    val json by lazy {
        Json {
            coerceInputValues = true
            allowStructuredMapKeys = true
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
    lateinit var world: WorldSettings

    override fun onEnable() {
        CustomBlockData.registerListener(plugin)
        loadWorldSettings()
    }

    override fun onActive() {
        initPolys()
    }

    fun initPolys() {
        regions.reload()
        polys.clear()
        newFile(getDataFolder(), "data", folder = true).listFiles()?.forEach { file ->
            if (file.name.endsWith(".json")) {
                polys.add(json.decodeFromString(Poly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

    fun deletePoly(id: Poly) {
        newFile(
            getDataFolder(),
            "data/${id.id}.json"
        ).delete()
    }

    fun savePoly(id: String) {
        val poly = polys.firstOrNull { it.id == id } ?: return
        newFile(
            getDataFolder(),
            "data/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    fun getTool(): Material {
        return Material.matchMaterial(config.getString("ClaimTool", "APPLE")!!) ?: Material.APPLE
    }

    fun loadWorldSettings() {
        config.reload()
        world = WorldSettings(config.getConfigurationSection("worlds")!!)
    }
}