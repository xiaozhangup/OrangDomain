package me.xiaozhangup.domain.ores

import me.justeli.coins.Coins
import me.xiaozhangup.domain.OrangDomain.json
import me.xiaozhangup.domain.OrangDomain.plugin
import me.xiaozhangup.domain.utils.customBlockData
import me.xiaozhangup.whale.util.chat.Notify
import me.xiaozhangup.whale.util.ext.recursiveFiles
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import java.io.File

object Ores {

    private var notify = Notify("矿石", "#54a4ff")
    private var selected: Pair<Location?, Location?> = null to null
    val data by lazy { newFile(getDataFolder(), "ore", folder = true, create = true) }
    val coins by lazy { Bukkit.getPluginManager().getPlugin("Coins") as Coins }
    val oreKey by lazy { NamespacedKey(plugin, "ore") }
    val refreshing: MutableMap<String, Refreshing> = mutableMapOf()
    var textures: Map<String, String> = mapOf()
    val rotations = listOf(
        BlockFace.NORTH_EAST,
        BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST,
        BlockFace.SOUTH_WEST,
        BlockFace.WEST_NORTH_WEST,
        BlockFace.NORTH_NORTH_WEST,
        BlockFace.NORTH_NORTH_EAST,
        BlockFace.EAST_NORTH_EAST,
        BlockFace.EAST_SOUTH_EAST,
        BlockFace.SOUTH_SOUTH_EAST,
        BlockFace.SOUTH_SOUTH_WEST,
        BlockFace.WEST_SOUTH_WEST
    )

    @Config(value = "ore.yml")
    lateinit var ore: Configuration
        private set

    @Awake(LifeCycle.ENABLE)
    fun register() {
        command("ores", permissionDefault = PermissionDefault.OP) {
            literal("select") {
                literal("pos1") {
                    execute<Player> { sender, _, _ ->
                        selected = sender.getTargetBlockExact(8)?.location to selected.second
                        notify.send(sender, "已选择第一个位置")
                    }
                }

                literal("pos2") {
                    execute<Player> { sender, _, _ ->
                        selected = selected.first to sender.getTargetBlockExact(8)?.location
                        notify.send(sender, "已选择第二个位置")
                    }
                }
            }

            literal("create") {
                dynamic("name") {
                    execute<Player> { sender, _, name ->
                        if (selected.first == null || selected.second == null) {
                            notify.send(sender, "请选择两个位置")
                            return@execute
                        }

                        val id = name.lowercase()
                        if (refreshing.contains(id)) {
                            notify.send(sender, "该名字已存在")
                            return@execute
                        }

//                        if (refreshing.values.any {
//                                it.inRefreshing(selected.first!!) || it.inRefreshing(selected.second!!)
//                            }) {
//                            notify.send(sender, "与其他刷新区域交叉")
//                            return@execute
//                        } 允许交叉

                        val ref = Refreshing(
                            id,
                            selected.first!!,
                            selected.second!!
                        ).apply {
                            loadSetting(
                                RefreshSetting(id, ore)
                            )
                        }
                        ore.saveToFile()
                        refreshing[id] = ref
                        ref.saveRefreshing()
                    }
                }
            }

            literal("delete") {
                dynamic("name") {
                    suggestion<Player> { _, _ -> refreshing.keys.toList() }

                    execute<Player> { sender, _, name ->
                        val ref = refreshing.remove(name)
                        if (ref == null) {
                            notify.send(sender, "不存在该刷新")
                            return@execute
                        }

                        File(data, "${ref.id}.json").apply {
                            if (exists()) delete()
                        }
                        ore[ref.id] = null
                        notify.send(sender, "已删除刷新 {0}", name)
                    }
                }
            }

            literal("reload") {
                execute<CommandSender> { sender, _, _ ->
                    ore.reload()
                    refreshing.clear()
                    loadRefreshing()
                    notify.send(sender, "已重载刷新")
                }
            }
        }
        schedule()
    }

    @Awake(LifeCycle.ACTIVE)
    fun loadRefreshing() {
        textures = ore.getMap("textures")
        data.recursiveFiles {
            val ref = json.decodeFromString<Refreshing>(it.readText())
            ref.loadSetting(ore.getConfigurationSection(ref.id))
            refreshing[ref.id] = ref
        }
    }

    fun schedule() {
        submit(period = 1) {
            for (it in refreshing.values) {
                if (it.setting == null) continue
                val i = it.interval ?: continue
                if (i.trigger()) {
                    it.refresh()
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        val block = e.block
        if (block.type != Material.PLAYER_HEAD) return
        if (!block.customBlockData.has(oreKey)) return
        val ref = refreshing.values.firstOrNull { it.inRefreshing(block) } ?: return
        val setting = ref.setting ?: return

        // 金币的掉落
        val dropLoc = block.location.add(0.5, 0.3, 0.5)
        repeat(setting.coin) {
            block.world.dropItemNaturally(
                dropLoc,
                coins.createCoin.dropped()
            )
        }
        // TODO 其他掉落物的处理

        e.isCancelled = true
        block.type = Material.AIR
    }
}