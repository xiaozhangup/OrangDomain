package me.xiaozhangup.domain.portal

import me.xiaozhangup.whale.util.PlayerBaffle
import me.xiaozhangup.whale.util.chat.Notify
import me.xiaozhangup.whale.util.chat.Screen
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.expansion.createHelper
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

object Portal {
    private var selected: Pair<Location?, Location?> = null to null
    private val notify = Notify("传送", "#f8ae77")
    private val baffle = PlayerBaffle(1, TimeUnit.SECONDS)
    val portals = ArrayList<PortalData>()

    @Config(value = "portal.yml")
    lateinit var portal: Configuration
        private set

    @Awake(LifeCycle.ENABLE)
    fun register() {
        command("portal", permissionDefault = PermissionDefault.OP) {
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
                        if (portals.any { it.id == id }) {
                            notify.send(sender, "该名字已存在")
                            return@execute
                        }

                        val target = sender.location
                        val portal = PortalData(
                            id = id,
                            world = sender.world.name,
                            pos1 = selected.first!!.add(0.5, 0.0, 0.5),
                            pos2 = selected.second!!.add(0.5, 0.0, 0.5),
                            target = target
                        )
                        portals.add(portal)
                        portal.saveTo(Portal.portal)
                        Portal.portal.saveToFile()
                        selected = null to null
                        notify.send(sender, "传送门 {0} 创建成功", name)
                    }
                }
            }

            literal("delete") {
                dynamic("name") {
                    suggestion<Player> { _, _ -> portals.map { it.id }.toList() }

                    execute<Player> { sender, _, name ->
                        val portal = portals.find { it.id == name }
                        if (portal == null) {
                            notify.send(sender, "不存在该传送门")
                            return@execute
                        }

                        portals.remove(portal)
                        Portal.portal[name] = null
                        Portal.portal.saveToFile()
                        notify.send(sender, "已删除传送门 {0}", name)
                    }
                }
            }

            createHelper()
        }
    }

    @Awake(LifeCycle.ACTIVE)
    fun loadPortal() {
        for (id in portal.getKeys(false)) {
            portals += PortalData(id, portal.getConfigurationSection(id)!!)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        val player = e.player
        val portal = portals.firstOrNull {
            it.inPortal(e.to) && !it.inPortal(e.from)
        } ?: return
        if (baffle.hasNext(player)) {
            Screen.sendScreen(player, fadeIn = 5, stay = 15, fadeOut = 10)
            submit(delay = 6) { player.teleport(portal.target) }
        }
    }
}