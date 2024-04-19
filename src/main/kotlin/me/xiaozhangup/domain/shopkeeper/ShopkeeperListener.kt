package me.xiaozhangup.domain.shopkeeper

import ink.ptms.adyeshach.core.event.AdyeshachEntityInteractEvent
import taboolib.common.platform.event.SubscribeEvent

object ShopkeeperListener {
    @SubscribeEvent
    fun e(e: AdyeshachEntityInteractEvent) {
        val entityId = e.entity.id
        if (entityId.startsWith("shopkeeper-") && e.isMainHand) {
            val id = entityId.substringAfter("shopkeeper-")
            val player = e.player

            ShopkeeperLoader.shops.firstOrNull { it.id == id }?.let {
                if (player.isOp && player.isSneaking) {
                    ShopkeeperEdit.openEdit(it, player)
                } else {
                    player.openMerchant(it.makeMerchant(), true)
                }
            }
        }
    }
}