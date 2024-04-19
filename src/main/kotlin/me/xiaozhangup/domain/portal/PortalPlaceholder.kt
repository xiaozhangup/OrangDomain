package me.xiaozhangup.domain.portal

import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PortalPlaceholder : PlaceholderExpansion {
    override val identifier: String
        get() = "portal"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return ""
        val info = args.split("_")
        if (info.isEmpty()) return "变量参数不全"

        return when (info[0]) {
            "portal" -> {
                val portal = PortalPacket.portals.firstOrNull() {
                    it.id == info.getOrElse(1) { "?" }
                } ?: return "?"

                return if (
                    target.level >= portal.level
                ) {
                    "&e进入传送门前往"
                } else {
                    "&c经验未达到 ${portal.level} 级"
                }
            }

            else -> {
                "未知"
            }
        }
    }
}