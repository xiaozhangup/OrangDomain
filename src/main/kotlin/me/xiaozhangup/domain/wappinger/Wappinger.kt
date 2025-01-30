package me.xiaozhangup.domain.wappinger

import kotlinx.serialization.encodeToString
import me.xiaozhangup.capybara.serves.payment.AfdianContent.back
import me.xiaozhangup.capybara.serves.payment.AfdianContent.board
import me.xiaozhangup.capybara.serves.payment.AfdianContent.next
import me.xiaozhangup.capybara.serves.payment.AfdianContent.previous
import me.xiaozhangup.capybara.utils.exec
import me.xiaozhangup.capybara.serves.payment.AfdianPayment
import me.xiaozhangup.capybara.utils.buildMessage
import me.xiaozhangup.capybara.utils.whiteColorCode
import me.xiaozhangup.domain.OrangDomain.json
import me.xiaozhangup.domain.wappinger.objects.DataWarp
import me.xiaozhangup.domain.wappinger.objects.LocationWarp
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.util.buildItem
import java.io.File
import java.util.*

object Wappinger {
    private val warpsFolder by lazy { File(getDataFolder(), "warps") }
    private val warps = mutableListOf<LocationWarp>()
    private val color = "#9370DB"
    private val prefix = whiteColorCode(color)

    @Awake(LifeCycle.ENABLE)
    fun load() {
        if (!warpsFolder.exists()) return // 如果没有这个文件, 就不加载之后的内容

        command("warp", permissionDefault = PermissionDefault.TRUE) {
            literal("gui") {
                execute<Player> { sender, _, _ ->
                    openWarps(sender, true)
                }
            }
            literal("reload", permission = "wappinger.reload") {
                execute<CommandSender> { sender, _, _ ->
                    saveAll()
                    loadWarps()
                    sender.notify("成功重载了所有传送点, 总计 ${warps.size} 个")
                }
            }
            literal("add", permission = "wappinger.add") {
                dynamic {
                    execute<Player> { sender, _, argument ->
                        warps.add(
                            LocationWarp(
                                sender.inventory.itemInMainHand,
                                argument,
                                sender.location,
                                UUID.randomUUID()
                            )
                        )

                        saveAll()
                        loadWarps()
                        sender.notify("成功创建了一个名称为 $argument 的传送点!")
                    }
                }

                execute<Player> { sender, _, _ ->
                    sender.notify("还需要提供一个 ID 作为参数!")
                }
            }
            literal("merge", permission = "wappinger.merge") {
                execute<CommandSender> { sender, _, _ ->
                    merge()
                    sender.notify("已经从旧版数据文件迁移到新版")
                }
            }

            execute<Player> { sender, _, _ ->
                openWarps(sender)
            }
        }
    }

    @Awake(LifeCycle.ACTIVE)
    fun loadWarp() {
        loadWarps()
    }

    @Awake(LifeCycle.DISABLE)
    fun saveAll() {
        if (!warpsFolder.exists()) return // 如果没有这个文件, 就不加载之后的内容

        val allWarps = warps.map { it.uuid.toString() }
        warpsFolder.listFiles()?.filter { file ->
            !allWarps.contains(file.nameWithoutExtension)
        }?.forEach {
            it.delete()
        } // 删除所有不存在的点

        warps.forEach { warp ->
            val file = File(warpsFolder, "${warp.uuid}.json")
            if (!file.exists()) file.createNewFile()

            file.writeText(
                json.encodeToString(
                    warps.first { it.uuid.toString() == file.nameWithoutExtension }.toDataWarp()
                )
            )
        } // 保存现有的传送点
    }

    private fun CommandSender.notify(string: String) {
        sendMessage(buildMessage("传送", string, color, prefix))
    }

    private fun openWarps(player: Player, fromMain: Boolean = false) {
        player.openMenu<PageableChest<LocationWarp>>(title = "全部的可用传送点") {
            map(
                "========m",
                "         ",
                "         ",
                "         ",
                "=======pn"
            )

            slotsBy(' ')
            elements { warps }
            onGenerate { _, element, _, _ ->
                buildItem(element.icon) {
                    lore += "&7"
                    lore += "&e点击传送"
                    colored()
                }
            }

            if (fromMain) {
                set('m', back) {
                    player.exec("cd")
                }
            } else {
                set('m', board)
            }
            set('=') { board }

            setPreviousPage(getFirstSlot('p')) { _, _ ->
                previous
            }
            setNextPage(getFirstSlot('n')) { _, _ ->
                next
            }

            onClick { _, element ->
                player.closeInventory()
                player.teleportAsync(element.location).thenAccept {
                    player.notify("您已被传送至 ${element.name}...")
                }
            }

            onClick(lock = true)
        }
    }

    private fun loadWarps() {
        warps.clear()
        warpsFolder.listFiles()
            ?.filter { it.name.endsWith(".json") }
            ?.forEach { file ->
                val data = json.decodeFromString(DataWarp.serializer(), file.readText())
                warps += data.toLocationWarp()
            }
        info("[Wappinger] Loaded ${warps.size} warp!")
    }

    private fun merge() {
        warpsFolder.listFiles()
            ?.filter { it.name.endsWith(".yml") }
            ?.forEach { file ->
                val yaml = YamlConfiguration.loadConfiguration(file)

                val icon = yaml.getItemStack("icon")!!
                val loc = yaml.getLocation("destination")!!
                val id = yaml.getString("id")!!

                warps += LocationWarp(icon, id, loc, UUID.randomUUID())

                info("[Wappinger] 成功迁移文件 ${file.nameWithoutExtension}")
                file.delete()
            }

        saveAll()
        loadWarps()
    }

}