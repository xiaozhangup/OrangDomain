package ray.mintcat.barrier.common.permission

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.OrangDomain.worlds
import ray.mintcat.barrier.utils.display
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.register
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem


object PermBuild : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "build"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.BRICKS) {
            name = "&f建筑 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8放置方块, 破坏方块, 放置挂饰, 破坏挂饰",
                    "&8放置盔甲架, 破坏盔甲架, 装满桶, 倒空桶"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
        }
    }

    //方块破坏判断，加白名单了
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: BlockBreakEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name) && !destructible.contains(e.block.type.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.block.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: BlockPlaceEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.block.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: HangingPlaceEvent) {
        val player = e.player ?: return
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", player.name)) {
                e.isCancelled = true
                return
                //player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.block.world.name) && !player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: HangingBreakByEntityEvent) {
        if (e.remover is Player) {
            val player = e.remover as Player
            e.entity.location.block.location.getPoly()?.run {
                if (!hasPermission("build", player.name)) {
                    e.isCancelled = true
                    return
                    //player.error("缺少权限 &f$id")
                }
            }
            if (worlds.contains(e.entity.world.name) && !player.isOp) {
                e.isCancelled = true
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractEvent) {
        e.clickedBlock?.location?.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.clickedBlock?.world?.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractEntityEvent) {
        e.rightClicked.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.rightClicked.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractAtEntityEvent) {
        e.rightClicked.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.rightClicked.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    //特殊类型
    //如果打的是玩家，被打的也是玩家，就不处理
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: EntityDamageByEntityEvent) {
        val player = e.damager as? Player ?: return
        if (e.entity is Player) return
        e.entity.location.block.location.getPoly()?.run {
            if (!hasPermission("build", player.name)) {
                e.isCancelled = true
                return
            //player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.entity.world.name) && !player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerBucketFillEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.block.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerBucketEmptyEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        }
        if (worlds.contains(e.block.world.name) && !e.player.isOp) {
            e.isCancelled = true
        }
    }
}