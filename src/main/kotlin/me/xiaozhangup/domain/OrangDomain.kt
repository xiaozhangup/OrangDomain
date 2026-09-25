package me.xiaozhangup.domain

import me.xiaozhangup.crab.CrabPlugin
import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import me.xiaozhangup.domain.config.WorldSettings
import me.xiaozhangup.domain.poly.Poly
import me.xiaozhangup.domain.poly.permission.Permission
import org.bukkit.Material
import me.xiaozhangup.crab.common.io.newFile
import me.xiaozhangup.domain.utils.ext.getDataFolder
import me.xiaozhangup.crab.configuration.Config
import me.xiaozhangup.crab.configuration.Configuration
import java.nio.charset.StandardCharsets

class OrangDomain : CrabPlugin() {

    init { instance = this }

    override fun enable() {
        CustomBlockData.registerListener(plugin)
        loadWorldSettings()
    }

    override fun active() {
        initPolys()
    }

    companion object {
        lateinit var instance: OrangDomain
            private set

        internal val crab get() = instance.crab

        @Config(migrate = true, value = "settings.yml")
        lateinit var config: Configuration
            private set

        @Config(value = "regions.yml")
        lateinit var regions: Configuration
            private set

        val polys = ArrayList<Poly>()
        val permissions = ArrayList<Permission>()
        val plugin get() = instance
        val json by lazy {
            Json {
                coerceInputValues = true
                allowStructuredMapKeys = true
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        }
        lateinit var world: WorldSettings

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
}
