package me.xiaozhangup.domain.common.placeholder

import me.xiaozhangup.domain.utils.getPoly
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PolyPlaceholder : PlaceholderExpansion {
    override val identifier: String
        get() = "barrier"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return "未知"
        val info = args.split("_")
        if (info.size < 2) return "变量参数不全"
        //%lemon_chat%
        return when (info[0]) {
            "poly" -> {
                target.location.getPoly()?.name ?: info[1]
            }

            else -> {
                "未知"
            }
        }
    }
}