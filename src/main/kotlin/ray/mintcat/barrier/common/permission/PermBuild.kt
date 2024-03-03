package ray.mintcat.barrier.common.permission

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleDestroyEvent
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

    private val interactableEntity = listOf(
        EntityType.VILLAGER,
        EntityType.ARMOR_STAND,
        EntityType.PLAYER,
        EntityType.BOAT, // 节日特别添加
        EntityType.CHEST_BOAT // 节日特别添加
    )

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
            if (!hasPermission("build", e.player.name) && !isDestructible(e.block.type)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        } ?: run {
            if (worlds.contains(e.block.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
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
        } ?: run {
            if (worlds.contains(e.block.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
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
        } ?: run {
            if (worlds.contains(e.block.world.name) && !player.isOp) {
                e.isCancelled = true
            }
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
            } ?: run {
                if (worlds.contains(e.entity.world.name) && !player.isOp) {
                    e.isCancelled = true
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        if (clickedBlock.type == Material.FARMLAND) {
            clickedBlock.location.getPoly()?.run {
                if (!hasPermission("build", e.player.name)) {
                    e.isCancelled = true
                    return
                    //e.player.error("缺少权限 &f$id")
                }
            } ?: run {
                if (worlds.contains(clickedBlock.world.name) && !e.player.isOp) {
                    e.isCancelled = true
                }
            }
        }
    }

    // 实体保护特别判断
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractEntityEvent) {
        if (interactableEntity.contains(e.rightClicked.type)) return
        e.rightClicked.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        } ?: run {
            if (worlds.contains(e.rightClicked.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
        }
    }

    //盔甲架特别判断
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: PlayerInteractAtEntityEvent) {
        if (interactableEntity.contains(e.rightClicked.type)) return
        e.rightClicked.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        } ?: run {
            if (worlds.contains(e.rightClicked.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerArmorStandManipulateEvent) {
        e.rightClicked.location.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                return
                //player.error("缺少权限 &f$id")
            }
        } ?: run {
            if (worlds.contains(e.rightClicked.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
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
        } ?: run {
            if (worlds.contains(e.block.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
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
        } ?: run {
            if (worlds.contains(e.block.world.name) && !e.player.isOp) {
                e.isCancelled = true
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onVehicleDestroy(e: VehicleDestroyEvent) {
        if (e.attacker !is Player) return
        val player = e.attacker as Player
        e.vehicle.location.getPoly()?.run {
            if (!hasPermission("build", player.name)) {
                e.isCancelled = true
                return
                //e.player.error("缺少权限 &f$id")
            }
        } ?: run {
            if (worlds.contains(e.vehicle.world.name) && !player.isOp) {
                e.isCancelled = true
            }
        }
    }
}