package me.xiaozhangup.domain.common.placeholder

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.common.extension.PolyEntityControl
import me.xiaozhangup.domain.utils.getPoly
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Animals
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.module.nms.getI18nName
import taboolib.platform.compat.PlaceholderExpansion

object LookingPlaceholder : PlaceholderExpansion {
    private val icons = hashMapOf<String, String>().apply {
        OrangDomain.config.getConfigurationSection("Looking")?.getKeys(false)?.forEach {
            this[it] = OrangDomain.config.getString("Looking.$it")!!
        } ?: warning("No looking icons found!")
    }

    override val identifier: String
        get() = "looking"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return ""

        try {
            val entity = target.getTargetEntity(3, false)
            if (entity == null) {
                target.getTargetBlockExact(5, FluidCollisionMode.NEVER)?.let { block ->
                    val stack = ItemStack(block.type)
                    val name = stack.getI18nName()
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
                val name = entity.getI18nName()
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
}