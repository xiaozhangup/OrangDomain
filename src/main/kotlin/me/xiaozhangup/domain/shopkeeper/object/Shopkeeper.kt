package me.xiaozhangup.domain.shopkeeper.`object`

import de.oliver.fancynpcs.api.FancyNpcsPlugin
import de.oliver.fancynpcs.api.NpcData
import de.oliver.fancynpcs.api.utils.SkinFetcher
import kotlinx.serialization.Serializable
import me.xiaozhangup.capybara.utils.miniMessage
import me.xiaozhangup.capybara.utils.modifiedColorCode
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toItemStack
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toLocation
import me.xiaozhangup.capybara.utils.serializer.BukkitSerializer.toRecorded
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.MerchantRecipe
import java.util.UUID

@Serializable
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
        val merchant = Bukkit.createMerchant(miniMessage(name))
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
        val data = NpcData("shopkeeper-$id", UUID.randomUUID(), getLocation())
        data.skin = SkinFetcher.fetchSkin(skin).get()
        data.displayName = "@none"
        data.isTurnToPlayer = true

        val npc = FancyNpcsPlugin.get().npcAdapter.apply(data)
        FancyNpcsPlugin.get().npcManager.registerNpc(npc)
        npc.create()
        npc.spawnForAll()
    }

    fun removeNPC(): Boolean {
        val npc = FancyNpcsPlugin.get().npcManager.getNpc("shopkeeper-$id")
        if (npc != null) {
            FancyNpcsPlugin.get().npcManager.removeNpc(npc)
            return true
        }
        return false
    }

    fun getLocation(): Location {
        return loc.toLocation()
    }
}