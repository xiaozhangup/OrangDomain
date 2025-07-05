package me.xiaozhangup.domain.ores

import me.justeli.coins.Coins
import me.xiaozhangup.domain.OrangDomain.json
import me.xiaozhangup.domain.OrangDomain.plugin
import me.xiaozhangup.domain.utils.customBlockData
import me.xiaozhangup.slimecargo.utils.flexibleItem
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
import org.bukkit.persistence.PersistentDataType
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.expansion.createHelper
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import taboolib.platform.compat.replacePlaceholder
import java.io.File
import kotlin.math.max
import kotlin.math.min

object Ores {

    private var notify = Notify("矿石", "#54a4ff")
    private var selected: Pair<Location?, Location?> = null to null
    val data by lazy { newFile(getDataFolder(), "ore", folder = true, create = true) }
    val coins by lazy { Bukkit.getPluginManager().getPlugin("Coins") as Coins }
    val oreKey by lazy { NamespacedKey(plugin, "ore") }
    val refreshingKey by lazy { NamespacedKey(plugin, "refreshing") }
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
                        selected = null to null
                        notify.send(sender, "已创建刷新 {0}", name)
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

            literal("clear") {
                execute<Player> { sender, _, _ ->
                    val pos1 = selected.first
                    val pos2 = selected.second
                    if (pos1 == null || pos2 == null) {
                        notify.send(sender, "请选择两个位置")
                        return@execute
                    }

                    notify.send(sender, "正在清理选区内的所有矿物...")
                    var count = 0
                    val world = pos1.world!!
                    for (x in min(pos1.blockX, pos2.blockX)..max(pos1.blockX, pos2.blockX)) {
                        for (y in min(pos1.blockY, pos2.blockY)..max(pos1.blockY, pos2.blockY)) {
                            for (z in min(pos1.blockZ, pos2.blockZ)..max(pos1.blockZ, pos2.blockZ)) {
                                val block = world.getBlockAt(x, y, z)
                                if (block.type != Material.PLAYER_HEAD) continue
                                if (!block.customBlockData.has(oreKey)) continue
                                block.customBlockData.clear()
                                block.type = Material.AIR
                                count++
                            }
                        }
                    }
                    notify.send(sender, "已清理 {0} 个矿物", count)
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

            createHelper()
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
        val texture = block.customBlockData.get(oreKey, PersistentDataType.STRING) ?: return
        val ref = block.customBlockData.get(refreshingKey, PersistentDataType.STRING)?.let {
            refreshing[it]
        } ?: refreshing.values.firstOrNull { it.inRefreshing(block) } ?: return
        val setting = ref.setting ?: return
        val dropLoc = block.location.add(0.5, 0.3, 0.5)
        val world = block.world

        e.isCancelled = true
        block.type = Material.AIR

        // 破坏的自定义操作
        val rs = setting.getLoot("*") + setting.getLoot(texture)
        for (t in rs) {
            val args = t.split(':', limit = 2)
            when(args[0]) {
                "coin" -> {
                    repeat(args[1].toIntOrNull() ?: 1) {
                        world.dropItemNaturally(
                            dropLoc,
                            coins.createCoin.dropped()
                        )
                    }
                }
                "item" -> {
                    world.dropItemNaturally(
                        dropLoc,
                        flexibleItem(args[1]) ?: continue
                    )
                }
                "command" -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].replacePlaceholder(e.player))
                }
            }
        }
    }
}