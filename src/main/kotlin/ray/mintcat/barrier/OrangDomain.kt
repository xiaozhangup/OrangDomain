package ray.mintcat.barrier

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import ray.mintcat.barrier.common.BarrierPoly
import ray.mintcat.barrier.common.permission.Permission
import ray.mintcat.barrier.regen.RegenLoader
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.nio.charset.StandardCharsets
import java.util.*

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

    @Config(migrate = true, value = "settings.yml", autoReload = true)
    lateinit var config: Configuration
        private set

    @Config(value = "regions.yml")
    lateinit var regions: Configuration
        private set

    @Config(value = "regen.yml")
    lateinit var regen: Configuration
        private set

    val polys = ArrayList<BarrierPoly>()

    val permissions = ArrayList<Permission>()

    val worlds = ArrayList<String>()

    fun getTool(): Material {
        return Material.valueOf(config.getString("ClaimTool", "APPLE")!!)
    }

    private val json = Json {
        coerceInputValues = true
    }

    fun delete(id: BarrierPoly) {
        newFile(
            getDataFolder(),
            "data/${id.id}.json"
        ).delete()
    }

    fun save(id: String) {
        val poly = polys.firstOrNull { it.id == id } ?: return
        newFile(
            getDataFolder(),
            "data/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    @Suppress("UNCHECKED_CAST")
    @Awake(LifeCycle.ACTIVE)
    fun import() {
        worlds.addAll(config.getStringList("ProtectWorlds"))
        initPolys()
        RegenLoader.init()
    }

    fun initPolys() {
        polys.clear()
        newFile(getDataFolder(), "data", create = false, folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                polys.add(json.decodeFromString(BarrierPoly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }
}