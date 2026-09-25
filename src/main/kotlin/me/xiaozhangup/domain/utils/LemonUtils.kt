package me.xiaozhangup.domain.utils

import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.poly.Poly
import me.xiaozhangup.domain.poly.permission.Permission
import me.xiaozhangup.whale.util.ext.executeCommand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions
import me.xiaozhangup.domain.utils.ext.executeConsole
import me.xiaozhangup.domain.utils.ext.submitTask
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import me.xiaozhangup.crab.compat.replacePlaceholder
import java.util.*


fun Location.getPoly(): Poly? {
    return OrangDomain.polys.filter { it.inNode(this) }.maxByOrNull { it.priority }
}

fun Permission.register() {
    if (!OrangDomain.permissions.map { it.id }.contains(this.id)) {
        OrangDomain.permissions.add(this)
    }
}

val tpMap = HashMap<UUID, Location>()

//延迟传送 单位s
fun Player.tpDelay(mint: Int, locationTo: Location) {
    tpMap[this.uniqueId] = this.location
    this.info("${mint}s 后开始传送 请勿移动!")
    submitTask(delay = mint.toLong() * 20) {
        val a = this@tpDelay.location
        val b = tpMap[this@tpDelay.uniqueId] ?: return@submitTask
        if (a.x != b.x || a.y != b.y || a.z != b.z) {
            this@tpDelay.error("由于您的移动已取消传送!")
            tpMap.remove(this@tpDelay.uniqueId)
            return@submitTask
        }
        this@tpDelay.teleport(locationTo)
        tpMap.remove(this@tpDelay.uniqueId)
    }
}

fun fromLocation(location: Location): String {
    return "${location.world?.name},${location.x},${location.y},${location.z},${location.yaw},${location.pitch}".replace(
        ".",
        "__"
    )
}

fun toLocation(source: String): Location {
    return source.replace("__", ".").split(",").run {
        Location(
            Bukkit.getWorld(get(0)),
            getOrElse(1) { "0" }.asDouble(),
            getOrElse(2) { "0" }.asDouble(),
            getOrElse(3) { "0" }.asDouble(),
            getOrElse(4) { "0" }.toFloat(),
            getOrElse(5) { "0" }.toFloat()
        )
    }
}

fun String.asDouble(): Double {
    return NumberConversions.toDouble(this)
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

/**
 * 给目标玩家发送一些消息 (提示)
 *
 * @receiver 目标玩家
 * @param block 发送的内容 可包含 & 会自动替换为
 * @since 1.0
 */
fun Player.info(vararg block: String) {
    block.forEach {
        toInfo(this, it)
    }
}

/**
 * 给目标玩家发送一些消息 (警告)
 *
 * @receiver 目标玩家
 * @param block 发送的内容 可包含 & 会自动替换为
 * @since 1.0
 */
fun Player.error(vararg block: String) {
    block.forEach {
        toError(this, it)
    }
}

fun CommandSender.error(vararg block: String) {
    block.forEach {
        toError(this, it)
    }
}

fun CommandSender.info(vararg block: String) {
    block.forEach {
        toInfo(this, it)
    }
}

/**
 * 发送信息
 *
 * @param sender 接收者
 * @param message 信息
 * @since 1.0
 */
fun toInfo(sender: CommandSender, message: String) {
    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&a区域&8] &7${message}".replace('§', '&')))
}

/**
 * 发送信息
 *
 * @param sender 接收者
 * @param message 信息
 * @since 1.0
 */
fun toError(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§4 OrangDomain §8] §7${message.replace("&", "§")}")
}

/**
 * 仅支持作为控制台执行或玩家本身执行
 */
fun Player.execute(command: String) {
    if (command.startsWith("console:")) {
        executeConsole(
            command.substringAfter("console:").replacePlaceholder(this)
        )
    } else {
        executeCommand(command.replacePlaceholder(this))
    }
}