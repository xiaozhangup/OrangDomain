package ray.mintcat.barrier

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Material
import ray.mintcat.barrier.balloon.BalloonUI
import ray.mintcat.barrier.balloon.BalloonWarp
import ray.mintcat.barrier.common.permission.Permission
import ray.mintcat.barrier.common.poly.BarrierPoly
import ray.mintcat.barrier.common.poly.RefreshPoly
import ray.mintcat.barrier.portal.Portal
import ray.mintcat.barrier.portal.PortalPacket.portals
import ray.mintcat.barrier.refresh.RefreshLoader
import ray.mintcat.barrier.regen.RegenLoader
import ray.mintcat.barrier.utils.toLocation
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.nio.charset.StandardCharsets

@RuntimeDependencies(
    RuntimeDependency(
        "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3",
        test = "!kotlinx.serialization.Serializer",
        relocate = ["!kotlin.", "!kotlin1822."]
    ),
    RuntimeDependency(
        "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3",
        test = "!kotlinx.serialization.json.Json",
        relocate = ["!kotlin.", "!kotlin1822."]
    )
)
object OrangDomain : Plugin() {

    @Config(migrate = true, value = "settings.yml")
    lateinit var config: Configuration
        private set

    @Config(value = "regions.yml")
    lateinit var regions: Configuration
        private set

    @Config(value = "regen.yml")
    lateinit var regen: Configuration
        private set

    @Config(value = "refresh.yml")
    lateinit var refresh: Configuration
        private set

    @Config(value = "balloon.yml")
    lateinit var balloon: Configuration
        private set

    val polys = ArrayList<BarrierPoly>()
    val refreshes = ArrayList<RefreshPoly>()
    val permissions = ArrayList<Permission>()
    val worlds = ArrayList<String>()
    val spawn = WorldSpawnCover
    val plugin by lazy { BukkitPlugin.getInstance() }
    private val json by lazy {
        Json {
            coerceInputValues = true
        }
    }

    lateinit var realisticTime: WorldRealisticTime

    fun getTool(): Material {
        return Material.valueOf(config.getString("ClaimTool", "APPLE")!!)
    }


    fun deletePoly(id: BarrierPoly) {
        newFile(
            getDataFolder(),
            "data/${id.id}.json"
        ).delete()
    }

    fun deleteRefresh(id: RefreshPoly) {
        refreshes.remove(id)
        newFile(
            getDataFolder(),
            "refresh/${id.id}.json"
        ).delete()
    }

    fun deletePortal(id: Portal) {
        portals.remove(id)
        newFile(
            getDataFolder(),
            "portal/${id.id}.json"
        ).delete()
    }

    fun savePoly(id: String) {
        val poly = polys.firstOrNull { it.id == id } ?: return
        newFile(
            getDataFolder(),
            "data/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    fun saveRefresh(id: String) {
        val poly = refreshes.firstOrNull { it.id == id } ?: return
        newFile(
            getDataFolder(),
            "refresh/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    fun savePortal(id: String) {
        val poly = portals.firstOrNull { it.id == id } ?: return
        newFile(
            getDataFolder(),
            "portal/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    @Awake(LifeCycle.ACTIVE)
    fun import() {
        worlds.addAll(config.getStringList("ProtectWorlds"))
        initPolys()
        initRefreshes()
        initBalloons()
        initPortals()
        initTimeSync()
        initWorldSpawn()

        RegenLoader.init()
        RefreshLoader.init()
    }

    fun initPolys() {
        polys.clear()
        newFile(getDataFolder(), "data", folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                polys.add(json.decodeFromString(BarrierPoly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

    fun initPortals() {
        portals.clear()
        newFile(getDataFolder(), "portal", folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                portals.add(json.decodeFromString(Portal.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

    fun initRefreshes() {
        refreshes.clear()
        newFile(getDataFolder(), "refresh", folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                refreshes.add(json.decodeFromString(RefreshPoly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

    fun initBalloons() {
        BalloonUI.balloons.clear()
        balloon.getKeys(false).forEach {
            BalloonUI.balloons += BalloonWarp(balloon.getConfigurationSection(it)!!, it)
        }
    }

    fun initTimeSync() {
        realisticTime = WorldRealisticTime(
            config.getStringList("TimeSyncWorlds")
        )
    }

    fun initWorldSpawn() {
        val location = config.getString("SpawnLocation")

        spawn.location = if (location != null) toLocation(location) else null
    }
}