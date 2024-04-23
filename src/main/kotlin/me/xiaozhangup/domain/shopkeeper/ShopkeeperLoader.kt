package me.xiaozhangup.domain.shopkeeper

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import me.xiaozhangup.capybara.utils.buildMessage
import me.xiaozhangup.capybara.utils.whiteColorCode
import me.xiaozhangup.domain.OrangDomain.json
import me.xiaozhangup.domain.shopkeeper.`object`.Shopkeeper
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.createHelper
import java.io.File

object ShopkeeperLoader {
    val shops = mutableListOf<Shopkeeper>()
    val shopkeepers by lazy { newFile(getDataFolder(), "shopkeepers", folder = true) }

    private const val color = "#7e867c"
    private val prefix = whiteColorCode(color)

    @Awake(LifeCycle.ACTIVE)
    fun init() {
        shopkeepers.listFiles()?.forEach {
            shops += json.decodeFromString(Shopkeeper.serializer(), it.readText())
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun shutdown() {
        shopkeepers.listFiles()?.filter { !shops.map { it.id }.contains(it.nameWithoutExtension) }?.forEach {
            it.delete()
        } // 清除不存在的配置文件

        shops.forEach {
            newFile(shopkeepers, "${it.id}.json").writeText(
                json.encodeToString(this)
            )
        } // 保存或覆盖的所有的文件
    }

    @Awake(LifeCycle.ENABLE)
    fun reg() {
        command("shopkeeper", permissionDefault = PermissionDefault.OP) {
            literal("list") {
                execute<CommandSender> { sender, _, _ ->
                    sender.notify("目前的全部村民如下: ${shops.joinToString(", ") { it.id }}")
                }
            }

            literal("import") {
                dynamic("id") {
                    dynamic("file") {
                        execute<Player> { sender, context, _ ->
                            val shop = shops.firstOrNull { it.id == context["id"] } ?: return@execute
                            val recipes = hashMapOf<String, Pair<String, String?>>()

                            val config = YamlConfiguration.loadConfiguration(
                                File(getDataFolder(), context["file"])
                            )
                            config.getKeys(false).forEach {
                                val section = config.getConfigurationSection(it)!!

                                recipes[section.getString("result")!!] =
                                    section.getString("item1")!! to section.getString("item2")
                            }

                            shop.merchants = recipes
                            sender.notify("成功从文件中导入交易内容!")
                        }
                    }
                }
            }

            literal("remove") {
                dynamic("id") {
                    suggestion<CommandSender> { _, _ ->
                        shops.map { it.id }
                    }

                    execute<CommandSender> { sender, _, arg ->
                        val shop = shops.firstOrNull { it.id == arg }

                        if (shop == null) {
                            sender.notify("没有到找这个村民!")
                            return@execute
                        }

                        shop.removeNPC()
                        shops.remove(shop)
                        sender.notify("成功移除了这个村民!")
                    }
                }
            }

            literal("create") {
                dynamic("id") {
                    dynamic("name") {
                        dynamic("skin") {
                            dynamic("color") {
                                execute<Player> { sender, context, _ ->
                                    val loc = sender
                                        .getTargetBlockExact(12)
                                        ?.location
                                        ?.add(0.5, 1.0, 0.5)
                                        ?: return@execute

                                    val keeper = Shopkeeper(
                                        context["id"],
                                        context["name"],
                                        context["color"],
                                        context["skin"],
                                        loc
                                    )
                                    shops += keeper
                                    keeper.createNPC()

                                    sender.notify("成功创建了一个ID为 ${context["id"]} 的村民!")
                                }
                            }
                        }
                    }
                }

                execute<CommandSender> { sender, _, _ ->
                    sender.notify("命令需求: /shopkeeper create [ID] [名字] [皮肤] [颜色]")
                    sender.notify("例子: /shopkeeper create spring 春节活动兑换 mlbjqjvz1y0uhduz #ffcb74")
                }
            }

            literal("edit") {
                dynamic("id") {
                    suggestion<Player> { _, _ ->
                        shops.map { it.id }
                    }

                    execute<Player> { sender, _, arg ->
                        val shop = shops.firstOrNull { it.id == arg }

                        if (shop == null) {
                            sender.notify("没有到找这个村民!")
                            return@execute
                        }

                        ShopkeeperEdit.openEdit(shop, sender)
                    }
                }
            }

            literal("refresh") {
                dynamic("id") {
                    suggestion<CommandSender> { _, _ ->
                        shops.map { it.id }
                    }

                    execute<CommandSender> { sender, _, arg ->
                        val shop = shops.firstOrNull { it.id == arg }

                        if (shop == null) {
                            sender.notify("没有到找这个村民!")
                            return@execute
                        }

                        shop.removeNPC()
                        shop.createNPC()
                        sender.notify("村民刷新成功!")
                    }
                }
            }

            literal("save") {
                execute<CommandSender> { sender, _, _ ->
                    shutdown()
                    sender.notify("所有村民已经全部保存!")
                }
            }

            createHelper()
        }
    }

    fun CommandSender.notify(string: String) {
        sendMessage(buildMessage("村民", string, color, prefix))
    }
}