package me.xiaozhangup.domain.utils.ext

import me.xiaozhangup.domain.OrangDomain

import me.xiaozhangup.crab.command.Notify
import me.xiaozhangup.crab.command.PermissionDefault
import me.xiaozhangup.crab.command.component.CommandBase
import me.xiaozhangup.crab.chain.Chain
import me.xiaozhangup.crab.chain.DispatcherType
import me.xiaozhangup.crab.task.Task

// Kotlin module-local facade: other plugins use their own Crab instance.
internal fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    newParser: Boolean = false,
    notify: Notify? = null,
    commandBuilder: CommandBase.() -> Unit,
) = OrangDomain.crab.command(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, newParser, notify, commandBuilder)

internal fun submitTask(now: Boolean = false, async: Boolean = false, delay: Long = 0, period: Long = 0,
                        executor: Task.() -> Unit): Task =
    OrangDomain.crab.submitTask(now, async, delay, period, executor)

internal fun submitAsyncTask(now: Boolean = false, delay: Long = 0, period: Long = 0,
                             executor: Task.() -> Unit): Task =
    OrangDomain.crab.submitAsyncTask(now, delay, period, executor)

internal fun <R> submitChain(type: DispatcherType = DispatcherType.ASYNC, block: suspend Chain<R>.() -> R) =
    OrangDomain.crab.submitChain(type, block)

@PublishedApi
internal fun getDataFolder() = OrangDomain.crab.getDataFolder()
@PublishedApi
internal fun info(vararg messages: Any?) = OrangDomain.crab.info(*messages)
@PublishedApi
internal fun warning(vararg messages: Any?) = OrangDomain.crab.warning(*messages)
@PublishedApi
internal fun severe(vararg messages: Any?) = OrangDomain.crab.severe(*messages)
@PublishedApi
internal fun releaseResourceFolder(path: String, overwrite: Boolean = false) = OrangDomain.crab.releaseResourceFolder(path, overwrite)
@PublishedApi
internal fun releaseResourceFile(path: String, replace: Boolean = false) = OrangDomain.crab.releaseResourceFile(path, replace)
internal fun console() = OrangDomain.crab.console()

internal fun executeConsole(command: String) = OrangDomain.crab.executeConsole(command)
