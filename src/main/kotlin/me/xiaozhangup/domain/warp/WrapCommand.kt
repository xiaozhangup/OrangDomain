package me.xiaozhangup.domain.warp

import me.xiaozhangup.whale.util.ext.itemStack
import net.kyori.adventure.text.Component
import org.bukkit.util.Transformation
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.function.submit
import kotlin.math.PI
import kotlin.math.sin

object WrapCommand {

    private val warps = mutableListOf<WarpElement>()
    private var animationTick = 0L
    private const val UPDATE_INTERVAL = 6

    @Awake(LifeCycle.ENABLE)
    fun register() {
        command("warp", permissionDefault = PermissionDefault.OP, permission = "warp.command") {
            literal("create") {
                execute<Player> { sender, _, _ ->
                    createWarp(sender.location, Color.fromRGB(0xfcefc1))
                }
            }
        }

        submit(period = UPDATE_INTERVAL.toLong()) {
            val targetTick = animationTick + UPDATE_INTERVAL

            val iterator = warps.iterator()
            while (iterator.hasNext()) {
                val warp = iterator.next()
                val item = warp.item
                val text = warp.text

                if (!item.isValid || !text.isValid) {
                    item.remove()
                    text.remove()
                    iterator.remove()
                    continue
                }

                val phase = item.entityId * 0.35
                val offsetY = (sin(targetTick * 0.12 + phase) * 0.3).toFloat()
                val rotateY = ((targetTick * 0.08 + phase) % (2 * PI)).toFloat()
                val itemScale = Vector3f(item.transformation.scale)

                item.interpolationDelay = 0
                item.interpolationDuration = UPDATE_INTERVAL
                item.transformation = Transformation(
                    Vector3f(0f, offsetY, 0f),
                    Quaternionf().rotateY(rotateY),
                    itemScale,
                    Quaternionf()
                )

                val textScale = Vector3f(text.transformation.scale)
                text.interpolationDelay = 0
                text.interpolationDuration = UPDATE_INTERVAL
                text.transformation = Transformation(
                    Vector3f(0f, offsetY - 0.35f, 0f),
                    Quaternionf(),
                    textScale,
                    Quaternionf()
                )
            }

            animationTick = targetTick
        }
    }

    private fun createWarp(location: Location, color: Color) {
        val world = location.world
        val loc = Location(world, location.blockX.toDouble(), location.blockY.toDouble(), location.blockZ.toDouble())
        loc.add(0.5, 0.5, 0.5)

        val itemDisplay = world.spawn(loc, ItemDisplay::class.java) { display ->
            display.setItemStack(
                itemStack("craftengine:assiahland:location") {
                    color(color)
                }
            )

            display.brightness = Display.Brightness(15, 15)
            display.interpolationDelay = 0
            display.interpolationDuration = 1
            display.isPersistent = false
        }

        val textDisplay = world.spawn(loc, TextDisplay::class.java) { display ->
            display.text(Component.text('❌'))
            display.billboard = Billboard.VERTICAL
            display.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            display.brightness = Display.Brightness(15, 15)
            display.interpolationDelay = 0
            display.interpolationDuration = 1
            display.isPersistent = false

            val transform = display.transformation
            display.transformation = Transformation(
                transform.translation,
                transform.leftRotation,
                Vector3f(2.5f, 2.5f, 2.5f),
                transform.rightRotation
            )
        }

        warps.add(WarpElement(itemDisplay, textDisplay))
    }

    data class WarpElement(
        val item: ItemDisplay,
        val text: TextDisplay
    )
}