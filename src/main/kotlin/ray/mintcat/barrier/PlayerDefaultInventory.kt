package ray.mintcat.barrier

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.utils.info
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.createHelper
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.deserializeToInventory
import taboolib.platform.util.serializeToByteArray
import java.util.*


object PlayerDefaultInventory {
    private var inventory: Array<ItemStack?> = arrayOf()
    private const val KIT = "kit"

    @Config("kit.yml")
    lateinit var kit: Configuration
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
                    kit["${KIT}_${System.currentTimeMillis()}"] = kit[KIT] // 备份
                    kit[KIT] = sender.inventory.serializeToBase64()

                    kit.saveToFile()
                    kit.reload()
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
        kit.getString(KIT)?.deserializeInventoryFromBase64()?.let {
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