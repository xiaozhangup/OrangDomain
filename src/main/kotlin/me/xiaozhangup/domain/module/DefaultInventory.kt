package me.xiaozhangup.domain.module

import me.xiaozhangup.domain.utils.info
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import me.xiaozhangup.crab.lifecycle.LifeCycle
import me.xiaozhangup.crab.lifecycle.Awake
import me.xiaozhangup.crab.command.PermissionDefault
import me.xiaozhangup.domain.utils.ext.command
import org.bukkit.event.EventPriority
import me.xiaozhangup.crab.event.SubscribeEvent
import me.xiaozhangup.crab.command.createHelper
import me.xiaozhangup.crab.configuration.Config
import me.xiaozhangup.crab.configuration.Configuration
import me.xiaozhangup.crab.util.deserializeToInventory
import me.xiaozhangup.crab.util.serializeToByteArray
import java.util.*


object DefaultInventory {
    private var inventory: Array<ItemStack?> = arrayOf()
    private const val KIT = "kit"

    @Config("inventory.yml")
    lateinit var inv: Configuration
        private set

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        if (player.hasPlayedBefore()) return

        setDefaultInventory(player)
    }

    @Awake(LifeCycle.ENABLE)
    fun setup() {
        command("defaultinventory", listOf("di"), permissionDefault = PermissionDefault.OP) {
            literal("set") {
                execute<Player> { sender, _, _ ->
                    inv["${KIT}_${System.currentTimeMillis()}"] = inv[KIT] // 备份
                    inv[KIT] = sender.inventory.serializeToBase64()

                    inv.saveToFile()
                    inv.reload()
                    loadDefaultInventory()

                    sender.info("成功保存默认物品组!")
                }
            }
            literal("get") {
                execute<Player> { sender, _, _ ->
                    setDefaultInventory(sender)

                    sender.info("已将你的背包设置为默认物品组!")
                }
            }

            createHelper()
        }

        loadDefaultInventory()
    }

    private fun setDefaultInventory(sender: Player) {
        sender.inventory.contents = inventory.clone()
    }

    private fun loadDefaultInventory() {
        inv.getString(KIT)?.deserializeInventoryFromBase64()?.let {
            inventory = it.contents
        }
    }

    private fun Inventory.serializeToBase64(): String {
        return Base64.getEncoder().encodeToString(serializeToByteArray(36))
    }

    private fun String.deserializeInventoryFromBase64(): Inventory {
        return Base64.getDecoder().decode(this).deserializeToInventory()
    }
}