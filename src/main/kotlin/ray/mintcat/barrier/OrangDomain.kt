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
        value = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
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
    val refreshs = ArrayList<RefreshPoly>()
    val permissions = ArrayList<Permission>()
    val worlds = ArrayList<String>()
    val plugin by lazy { BukkitPlugin.getInstance() }

    fun getTool(): Material {
        return Material.valueOf(config.getString("ClaimTool", "APPLE")!!)
    }

    private val json = Json {
        coerceInputValues = true
    }

    fun deletePoly(id: BarrierPoly) {
        newFile(
            getDataFolder(),
            "data/${id.id}.json"
        ).delete()
    }

    fun deleteRefresh(id: RefreshPoly) {
        refreshs.remove(id)
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
        val poly = refreshs.firstOrNull { it.id == id } ?: return
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

    @Suppress("UNCHECKED_CAST")
    @Awake(LifeCycle.ACTIVE)
    fun import() {
        worlds.addAll(config.getStringList("ProtectWorlds"))
        initPolys()
        initRefreshes()
        initBalloons()
        initPortals()

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
        refreshs.clear()
        newFile(getDataFolder(), "refresh", folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                refreshs.add(json.decodeFromString(RefreshPoly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

    fun initBalloons() {
        BalloonUI.balloons.clear()
        balloon.getKeys(false).forEach {
            BalloonUI.balloons += BalloonWarp(balloon.getConfigurationSection(it)!!, it)
        }
    }
}