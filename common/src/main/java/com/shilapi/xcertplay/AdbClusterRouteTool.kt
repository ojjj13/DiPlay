package com.shilapi.xcertplay

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.IBinder

/** Runs through app_process as shell. Never moves a mixed stack or any stock task. */
object AdbClusterRouteTool {
    internal data class Stack(val id: Int, val display: Int, val tasks: List<Int>, val names: List<String>)
    internal fun owns(stack: Stack, task: Int, component: String): Boolean {
        val index = stack.tasks.indexOf(task)
        return index >= 0 && stack.names.getOrNull(index) == component
    }
    internal fun isolated(stack: Stack, task: Int, component: String): Boolean =
        stack.tasks == listOf(task) && owns(stack, task, component)

    @JvmStatic
    @SuppressLint("PrivateApi")
    fun main(args: Array<String>) {
        Thread({ Thread.sleep(4_000); println("routeTimeout=true"); System.exit(2) }, "route-watchdog")
            .apply { isDaemon = true; start() }
        var service: Any? = null
        var api: Class<*>? = null
        var mainStackId: Int? = null
        try {
            require(args.size == 4)
            val pkg = args[0]
            require(pkg == "com.shihab.diplay.hudtest" || pkg == "com.shihab.diplay")
            val task = args[1].toInt()
            val host = args[2].toInt()
            val display = args[3].toInt()
            require(task >= 0 && host >= 0 && task != host && display > 0)
            val binder = Class.forName("android.os.ServiceManager")
                .getMethod("getService", String::class.java).invoke(null, "activity_task") as IBinder
            val methods = Class.forName("android.app.IActivityTaskManager")
            api = methods
            val manager = Class.forName("android.app.IActivityTaskManager\$Stub")
                .getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            service = manager
            fun stacks(): List<Stack> = (methods.getMethod("getAllStackInfos").invoke(manager) as List<*>)
                .filterNotNull().map { value ->
                    val type = value.javaClass
                    Stack(type.getField("stackId").getInt(value), type.getField("displayId").getInt(value),
                        (type.getField("taskIds").get(value) as IntArray).toList(),
                        (type.getField("taskNames").get(value) as Array<*>).map { it?.toString().orEmpty() })
                }
            val component = "$pkg/com.shilapi.xcertplay.AdbClusterActivity"
            val hostComponent = "$pkg/com.shilapi.xcertplay.CarPlayHostActivity"
            val before = stacks()
            val source = before.single { owns(it, task, component) }
            val main = before.single { owns(it, host, hostComponent) }
            require(main.display == 0) { "Main CarPlay task must remain on display 0" }
            mainStackId = main.id
            println("routeSource stack=${source.id} display=${source.display} tasks=${source.tasks}")
            if (source.display != display) {
                // Android 10 fullscreen tasks can share a stack. Freeform makes this task independent.
                if (!isolated(source, task, component)) {
                    methods.getMethod("setTaskWindowingMode", Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
                        .invoke(manager, task, 5, true)
                }
                val separate = stacks().single { owns(it, task, component) }
                require(isolated(separate, task, component)) { "Refusing to move a mixed stack" }
                require(separate.id != mainStackId) { "Refusing to move the main stack" }
                println("routeIsolated stack=${separate.id} tasks=${separate.tasks}")
                methods.getMethod("moveStackToDisplay", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    .invoke(manager, separate.id, display)
            }
            val moved = stacks().single { owns(it, task, component) }
            require(moved.display == display && isolated(moved, task, component)) { "Cluster task move was not verified" }
            // Retain an independent freeform stack; do not merge it with the stock projection task.
            methods.getMethod("resizeTask", Int::class.javaPrimitiveType, Rect::class.java, Int::class.javaPrimitiveType)
                .invoke(manager, task, Rect(0, 0, 1920, 720), 0)
            val retainedMain = stacks().single { owns(it, host, hostComponent) }
            require(retainedMain.display == 0) { "Main task moved unexpectedly" }
            println("routeVerified clusterDisplay=${moved.display} mainDisplay=${retainedMain.display}")
            println("routeSuccess=true")
        } catch (error: Throwable) {
            val cause = error.cause ?: error
            println("routeError=${cause.javaClass.simpleName}: ${cause.message}")
        } finally {
            // Restore head-unit input focus without restarting the CarPlay activity or its session.
            mainStackId?.let { stack ->
                runCatching { api?.getMethod("setFocusedStack", Int::class.javaPrimitiveType)?.invoke(service, stack) }
                    .onFailure { println("routeFocusError=${it.cause ?: it}") }
            }
        }
    }
}
