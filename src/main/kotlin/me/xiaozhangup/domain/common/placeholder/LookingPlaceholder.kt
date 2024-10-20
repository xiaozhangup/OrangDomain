package me.xiaozhangup.domain.common.placeholder

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.common.extension.PolyEntityControl
import me.xiaozhangup.domain.utils.getPoly
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.nms.MinecraftLanguage
import taboolib.module.nms.NMSTranslate
import taboolib.module.nms.getI18nName
import taboolib.module.nms.getMinecraftLanguageFile
import taboolib.platform.compat.PlaceholderExpansion
import java.lang.System.currentTimeMillis

object LookingPlaceholder : PlaceholderExpansion {
    private val icons = hashMapOf<String, String>().apply {
        OrangDomain.config.getConfigurationSection("Looking")?.getKeys(false)?.forEach {
            this[it] = OrangDomain.config.getString("Looking.$it")!!
        } ?: warning("No looking icons found!")
    }

    private val translateMapping by lazy { newFile(getDataFolder(), "translate.mapping") }
    private val mapping by lazy {
        Configuration.loadFromFile(
            translateMapping,
            Type.YAML
        )
    }
    private var lastModification = 0L
    private var lastAccess = 0L

    override val identifier: String
        get() = "looking"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return ""

        try {
            val entity = target.getTargetEntity(3, false)
            if (entity == null) {
                target.getTargetBlockExact(5, FluidCollisionMode.NEVER)?.let { block ->
                    val name = getTranslate(block.type)
                    val poly = block.location.getPoly() ?: return ""

                    return if (poly.isDestructible(block.type)) {
                        "${icons["breakable"]} $name"
                    } else if (poly.interactive.contains(block.type.name)) {
                        "${icons["interactable"]} $name"
                    } else {
                        "${icons["unusable"]} $name"
                    }
                }
            } else {
                val name = getTranslate(entity)
                val poly = entity.location.getPoly() ?: return ""
                if (entity is Animals || entity is Monster) {
                    return if (
                        PolyEntityControl.canHurtAnimal(poly) ||
                        PolyEntityControl.canHurtMonster(poly)
                    ) {
                        "${icons["hurtable"]} $name"
                    } else {
                        "${icons["unusable"]} $name"
                    }
                }
            }
        } catch (_: Exception) { }

        return args.split("_").getOrElse(0) { "" }
    }

    fun refreshIcons() {
        icons.clear()
        OrangDomain.config.getConfigurationSection("Looking")?.getKeys(false)?.forEach {
            icons[it] = OrangDomain.config.getString("Looking.$it")!!
        } ?: warning("No looking icons found!")
    }

    fun getTranslate(stack: Material): String {
        val file = MinecraftLanguage.getDefaultLanguageFile() ?: return "NO_LOCALE"
        val key = stack.key.key
        val result = file[key] ?: run {
            if (currentTimeMillis() - lastAccess > 3000) {
                if (translateMapping.lastModified() > lastModification) {
                    lastModification = translateMapping.lastModified()
                    mapping.reload()
                    info("[Looking] Translate mapping has been updated. (Total: ${mapping.getKeys(false).size})")
                }
                lastAccess = currentTimeMillis()
            } // 自动同步的操作
            mapping.getString(key.replace('.', '_'), "")!!
        }
        if (result.isBlank()) {
            mapping[key.replace('.', '_')] = ""
            mapping.saveToFile()
            warning("[Looking] No translate found for $key, please translate it in translate.mapping.")
        }
        return result
    } // 就方块搞个接口得了

    fun getTranslate(entity: Entity): String {
        return entity.getI18nName()
    }
}