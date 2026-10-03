package com.shilapi.xcertplay

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.shilapi.xcertplay.host.R
import java.lang.ref.WeakReference

/** A separate task for stream 111. Routing is control traffic; video stays in the app process. */
class AdbClusterActivity : Activity() {
    private var waiting: TextView? = null
    private var surface: Surface? = null
    private var routing = false
    private var attemptedRoute = false
    internal var routeStatus = ""
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AirPlayPersistence.loadAdbClusterEnabled(this)) { finish(); return }
        ClusterActivityOutput.activity = WeakReference(this)
        ClusterActivityOutput.launchPending = false
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val video = SurfaceView(this)
        video.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
                ClusterActivityOutput.attach(this@AdbClusterActivity, holder.surface)
                ensureRoute()
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                surface?.let { ClusterActivityOutput.detach(this@AdbClusterActivity, it) }
                surface = null
            }
        })
        root.addView(video, FrameLayout.LayoutParams(-1, -1))
        waiting = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
        }
        root.addView(waiting, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        updateStream()
    }

    internal fun updateStream() {
        waiting?.visibility = if (ClusterActivityOutput.streamActive) View.GONE else View.VISIBLE
        waiting?.text = getString(R.string.cluster_waiting_for_map) + "\n" + routeStatus
    }

    internal fun ensureRoute() {
        if (!attemptedRoute && surface != null && ClusterActivityOutput.mainTaskId >= 0) {
            attemptedRoute = true
            route()
        }
    }

    internal fun route() {
        if (routing || isFinishing || isDestroyed) return
        routing = true
        routeStatus = getString(R.string.adb_cluster_routing)
        updateStream()
        val clusterTask = taskId
        val hostTask = ClusterActivityOutput.mainTaskId
        val app = applicationContext
        Thread({
            val result = AdbClusterRouter.route(app, clusterTask, hostTask)
            runOnUiThread {
                routing = false
                if (isFinishing || isDestroyed) return@runOnUiThread
                routeStatus = if (result.success) getString(R.string.adb_cluster_routed)
                    else getString(R.string.adb_cluster_route_failed)
                updateStream()
            }
        }, "adb-cluster-route").start()
    }

    // Losing focus must not detach stream 111. SurfaceHolder owns the rendering lifetime.
    override fun onDestroy() {
        surface?.let { ClusterActivityOutput.detach(this, it) }
        if (ClusterActivityOutput.activity.get() === this) {
            ClusterActivityOutput.activity.clear()
            ClusterActivityOutput.launchPending = false
        }
        super.onDestroy()
    }
}

/** Main-thread handoff, with identity checks so a stale activity cannot clear a newer surface. */
internal object ClusterActivityOutput {
    var activity = WeakReference<AdbClusterActivity>(null)
    var launchPending = false
    var mainTaskId = -1
        private set
    var surface: Surface? = null
        private set
    private var surfaceOwner: Any? = null
    private var hostOwner: Any? = null
    private var onSurface: ((Surface?) -> Unit)? = null
    var streamActive = false
        private set

    fun bind(owner: Any, taskId: Int, callback: (Surface?) -> Unit) {
        hostOwner = owner
        mainTaskId = taskId
        onSurface = callback
        callback(surface)
    }

    fun ensure(host: Activity) {
        activity.get()?.let { it.ensureRoute(); return }
        if (launchPending) return
        launchPending = true
        try {
            host.startActivity(Intent(host, AdbClusterActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (error: RuntimeException) {
            launchPending = false
            throw error
        }
    }

    fun attach(owner: Any, next: Surface) {
        surfaceOwner = owner
        surface = next
        onSurface?.invoke(next)
    }

    fun detach(owner: Any, old: Surface) {
        if (surfaceOwner !== owner || surface !== old) return
        surfaceOwner = null
        surface = null
        onSurface?.invoke(null)
    }

    fun setStreamActive(active: Boolean) {
        streamActive = active
        activity.get()?.updateStream()
    }

    fun stop(owner: Any) {
        if (hostOwner !== owner) return
        onSurface?.invoke(null)
        onSurface = null
        hostOwner = null
        mainTaskId = -1
        surface = null
        surfaceOwner = null
        launchPending = false
        setStreamActive(false)
        activity.get()?.finish()
    }
}
