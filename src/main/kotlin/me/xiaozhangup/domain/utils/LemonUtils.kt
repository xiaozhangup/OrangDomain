package me.xiaozhangup.domain.utils

import me.xiaozhangup.capybara.utils.exec
import me.xiaozhangup.domain.OrangDomain
import me.xiaozhangup.domain.common.permission.Permission
import me.xiaozhangup.domain.common.poly.BarrierPoly
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.NumberConversions
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import java.util.*


fun Location.getPoly(): BarrierPoly? {
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
    submit(delay = mint.toLong() * 20) {
        val a = this@tpDelay.location
        val b = tpMap[this@tpDelay.uniqueId] ?: return@submit
        if (a.x != b.x || a.y != b.y || a.z != b.z) {
            this@tpDelay.error("由于您的移动已取消传送!")
            tpMap.remove(this@tpDelay.uniqueId)
            return@submit
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

operator fun Chest.set(c: Char, buildItem: ItemStack, function: (event: ClickEvent) -> Unit) {
    set(c, buildItem(buildItem) {
        colored()
    })
    onClick(c, function)
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
    sender.sendMessage("&8[&a区域&8] &7${message}".colored())
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
        console().performCommand(
            command.substringAfter("console:").replacePlaceholder(this)
        )
    } else {
        exec(command.replacePlaceholder(this))
    }
}