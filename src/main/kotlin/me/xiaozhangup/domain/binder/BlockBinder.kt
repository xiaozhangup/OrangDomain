package me.xiaozhangup.domain.binder

import me.xiaozhangup.capybara.utils.exec
import me.xiaozhangup.capybara.utils.buildSimpleMessage
import me.xiaozhangup.capybara.utils.serializer.StringSerializer
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.expansion.dispatchCommandAsOp
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.compat.replacePlaceholder

object BlockBinder {
    private val binders: HashMap<String, String> by lazy {
        HashMap<String, String>().apply {
            binder.getKeys(false).forEach {
                this[StringSerializer.deserializeFromString(it)] = binder.getString(it)!!
            }
        }
    }

    @Config("binder.yml")
    lateinit var binder: Configuration
        private set

    @Awake(LifeCycle.ENABLE)
    fun setup() {
        command("blockbinder", permissionDefault = PermissionDefault.OP) {
            dynamic {
                suggestion<Player>(uncheck = true) { _, _ ->
                    listOf("null", "op:", "console:")
                }

                execute<Player> { sender, _, argument ->
                    val target = sender.getTargetBlockExact(5)

                    target?.let {
                        val key = it.location.toRecorded()
                        if (argument == "null") {
                            binders.remove(key)
                            binder[StringSerializer.serializeToString(key)] = null
                            binder.saveToFile()
                            sender.sendMessage(
                                buildSimpleMessage("成功移除此方块的绑定!")
                            )
                            return@execute
                        }

                        binders[key] = argument
                        binder[StringSerializer.serializeToString(key)] = argument
                        binder.saveToFile()
                        sender.sendMessage(
                            buildSimpleMessage("成功给此方块添加绑定!")
                        )
                    } ?: sender.sendMessage(
                        buildSimpleMessage("你没有对准你要绑定命令的方块")
                    )
                }
            }
        }

    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK || e.clickedBlock == null) return
        if (e.hand != EquipmentSlot.HAND) return

        val key = e.clickedBlock!!.location.toRecorded()
        binders[key]?.let { command ->
            e.player.execute(command)
            e.isCancelled = true
        }
    }

    private fun Player.execute(command: String) {
        if (command.startsWith("console:")) {
            console().performCommand(
                command.substringAfter("console:").apply(this)
            )
        } else if (command.startsWith("op:")) {
            this.dispatchCommandAsOp(command.substringAfter("op:").apply(this))
        } else {
            exec(command)
        }
    }

    private fun String.apply(player: Player): String {
        return this.replacePlaceholder(player)
    }

    private fun Location.toRecorded(): String {
        val loc = this
        return loc.world!!.name + ":" + loc.x + ":" + loc.y + ":" + loc.z + ":" + loc.yaw + ":" + loc.pitch
    }
}