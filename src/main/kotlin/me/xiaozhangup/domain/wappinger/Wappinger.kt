package me.xiaozhangup.domain.wappinger

import kotlinx.serialization.encodeToString
import me.xiaozhangup.domain.OrangDomain.json
import me.xiaozhangup.domain.wappinger.objects.DataWarp
import me.xiaozhangup.domain.wappinger.objects.LocationWarp
import me.xiaozhangup.whale.menu.component.icon.MenuIcon
import me.xiaozhangup.whale.util.chat.Notify
import me.xiaozhangup.whale.util.ext.executeCommand
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
    private val notify = Notify("传送", "#9370DB")

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
                    notify.send(sender, "成功重载了所有传送点, 总计 ${warps.size} 个")
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
                        notify.send(sender, "成功创建了一个名称为 $argument 的传送点!")
                    }
                }

                execute<Player> { sender, _, _ ->
                    notify.send(sender, "还需要提供一个 ID 作为参数!")
                }
            }
            literal("merge", permission = "wappinger.merge") {
                execute<CommandSender> { sender, _, _ ->
                    merge()
                    notify.send(sender, "已经从旧版数据文件迁移到新版")
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
                set('m', MenuIcon.BACK) {
                    player.executeCommand("cd")
                }
            } else {
                set('m', MenuIcon.BLACK_PANE)
            }
            set('=') { MenuIcon.BLACK_PANE }

            setPreviousPage(getFirstSlot('p')) { _, _ ->
                MenuIcon.PRE
            }
            setNextPage(getFirstSlot('n')) { _, _ ->
                MenuIcon.NEXT
            }

            onClick { _, element ->
                player.closeInventory()
                player.teleportAsync(element.location).thenAccept {
                    notify.send(player, "您已被传送至 {0}...", element.name)
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