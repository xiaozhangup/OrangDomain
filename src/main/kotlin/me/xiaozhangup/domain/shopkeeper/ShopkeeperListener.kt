package me.xiaozhangup.domain.shopkeeper

import de.oliver.fancynpcs.api.actions.ActionTrigger
import de.oliver.fancynpcs.api.events.NpcInteractEvent
import taboolib.common.platform.event.SubscribeEvent

object ShopkeeperListener {
    @SubscribeEvent
    fun e(e: NpcInteractEvent) {
        val entityId = e.npc.data.name
        if (entityId.startsWith("shopkeeper-") && e.interactionType == ActionTrigger.RIGHT_CLICK) {
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