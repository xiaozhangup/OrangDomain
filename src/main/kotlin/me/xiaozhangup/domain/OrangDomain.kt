package me.xiaozhangup.domain


import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.Crab
import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import me.xiaozhangup.domain.config.WorldSettings
import me.xiaozhangup.domain.poly.Poly
import me.xiaozhangup.domain.poly.permission.Permission
import org.bukkit.Material
import me.xiaozhangup.crab.common.io.newFile
import org.bukkit.plugin.java.JavaPlugin
import me.xiaozhangup.domain.utils.ext.getDataFolder
import me.xiaozhangup.crab.configuration.Config
import me.xiaozhangup.crab.configuration.Configuration
import java.nio.charset.StandardCharsets

class OrangDomain : JavaPlugin() {
    internal val crab = Crab(this, dataFolder)

    init { instance = this }

    override fun onLoad() {
        crab.lifecycle.run(LifeCycle.CONST)
        crab.lifecycle.run(LifeCycle.INIT)
        crab.lifecycle.run(LifeCycle.LOAD)
    }

    override fun onEnable() {
        crab.lifecycle.run(LifeCycle.ENABLE)
        crab.registerEvents()
        crab.registerPlaceholders()
        enablePlugin()
        crab.start()
        crab.submitTask(delay = 1) {
            crab.lifecycle.run(LifeCycle.ACTIVE)
            activePlugin()
        }
    }

    override fun onDisable() {
        try {
            try { crab.lifecycle.run(LifeCycle.DISABLE) } finally {
                disablePlugin()
            }
        } finally { crab.close() }
    }

    companion object {
        lateinit var instance: OrangDomain
            private set

        internal val crab get() = instance.crab


        @Awake(LifeCycle.INIT)
        fun loadSharedConfigurations() {
            crab.loadConfigurations()
        }


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

        fun enablePlugin() {
            crab.commands.registerAnnotated(crab.scanner)
            CustomBlockData.registerListener(plugin)
            loadWorldSettings()
        }

        fun activePlugin() {
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
        fun disablePlugin() {
            crab.close()
        }
    }
}
