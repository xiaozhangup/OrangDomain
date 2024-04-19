package me.xiaozhangup.domain.shopkeeper.`object`

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.entity.EntityTypes
import ink.ptms.adyeshach.core.entity.manager.ManagerType
import ink.ptms.adyeshach.core.entity.type.AdyHuman
import ink.ptms.adyeshach.impl.entity.controller.ControllerLookAtPlayer
import ink.ptms.adyeshach.impl.entity.trait.impl.setTraitTitle
import ink.ptms.adyeshach.impl.entity.trait.impl.setTraitTitleHeight
import me.xiaozhangup.capybara.mini
import me.xiaozhangup.capybara.utils.modifiedColorCode
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toItemStack
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toLocation
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toRecorded
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.MerchantRecipe

data class Shopkeeper(
    val id: String,
    val loc: String,
    val color: String,
    val name: String,
    val skin: String,
    var merchants: HashMap<String, Pair<String, String?>>
) {
    constructor(
        id: String,
        name: String,
        color: String,
        skin: String,
        location: Location,
    ) : this(
        id,
        location.toRecorded(),
        color,
        name,
        skin,
        hashMapOf()
    )

    fun makeMerchant(): Merchant {
        val merchant = Bukkit.createMerchant(mini(name))
        val recipes = mutableListOf<MerchantRecipe>()

        merchants.forEach { (r, c) ->
            val result = r.toItemStack()
            val first = c.first.toItemStack()
            val second = c.second?.toItemStack()

            recipes.add(
                MerchantRecipe(
                    result,
                    Int.MAX_VALUE
                ).apply {
                    addIngredient(first)
                    second?.let { addIngredient(it) }
                }
            )
        }
        merchant.recipes = recipes

        return merchant
    }

    fun createNPC() {
        val manager = Adyeshach.api().getPublicEntityManager(ManagerType.PERSISTENT)

        manager.create(
            EntityTypes.PLAYER,
            getLocation()
        ) { entity ->
            entity as AdyHuman

            entity.registerController(ControllerLookAtPlayer(entity, 8.0, 1.0))
            entity.setTraitTitleHeight(0.0)

            entity.id = "shopkeeper-$id"

            // 加载已有的数据并应用
            entity.setTexture(skin)
            entity.setSkinCapeEnabled(false)

            // 名称和头衔加载
            entity.setName("")
            entity.setTraitTitle(null)

            val nameColor = try {
                modifiedColorCode(color, 0.7f)
            } catch (_: Exception) {
                color
            }
            entity.setTraitTitle(listOf("&{$nameColor}&l🛒", "&{$color}$name"))
        }
    }

    fun removeNPC(): Boolean {
        val manager = Adyeshach.api().getPublicEntityManager(ManagerType.PERSISTENT)

        manager.getEntities().filter { instance ->
            instance.id == "shopkeeper-$id"
        }.forEach { instance ->
            instance.remove()
            return true
        }

        return false
    }

    fun getLocation(): Location {
        return loc.toLocation()
    }
}