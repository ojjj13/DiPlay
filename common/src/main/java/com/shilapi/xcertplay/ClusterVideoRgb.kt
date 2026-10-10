package com.shilapi.xcertplay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Converts the decoder's external texture to an opaque RGBA8888 window buffer.
 * The driver still performs YUV sampling. This tests RGB composition, not a custom YUV matrix.
 * The default EGL color space keeps encoded RGB samples unchanged (no second sRGB encoding).
 */
internal class ClusterVideoRgb(context: Context, private val onPresented: () -> Unit,
    private val onSurface: (Surface?) -> Unit) : GLSurfaceView(context), java.io.Closeable {
    @Volatile private var closed = false
    @Volatile private var generation = 0
    @Volatile private var frameAvailable = false
    private var input: Surface? = null
    private var texture: SurfaceTexture? = null
    private var textureId = 0
    private var program = 0
    private val matrix = FloatArray(16)
    private val vertices = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        .apply { put(floatArrayOf(-1f,-1f,0f,0f, 1f,-1f,1f,0f, -1f,1f,0f,1f, 1f,1f,1f,1f)); position(0) }
    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFixedSize(DiLink4ClusterDisplay.STREAM_WIDTH, DiLink4ClusterDisplay.STREAM_HEIGHT)
        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                if (closed || !frameAvailable) return
                val currentGeneration = ++generation
                frameAvailable = false
                val ids = IntArray(1)
                GLES20.glGenTextures(1, ids, 0)
                textureId = ids[0]
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
                program = GLES20.glCreateProgram()
                val vertex = shader(GLES20.GL_VERTEX_SHADER, "attribute vec2 p; attribute vec2 uv; uniform mat4 transform; varying vec2 v; void main(){gl_Position=vec4(p,0.,1.);v=(transform*vec4(uv,0.,1.)).xy;}")
                val fragment = shader(GLES20.GL_FRAGMENT_SHADER, "#extension GL_OES_EGL_image_external : require\nprecision mediump float; uniform samplerExternalOES image; varying vec2 v; void main(){gl_FragColor=vec4(texture2D(image,v).rgb,1.);}")
                GLES20.glAttachShader(program, vertex); GLES20.glAttachShader(program, fragment)
                GLES20.glLinkProgram(program)
                val linked = IntArray(1); GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
                check(linked[0] != 0) { GLES20.glGetProgramInfoLog(program) }
                GLES20.glDeleteShader(vertex); GLES20.glDeleteShader(fragment)
                val next = SurfaceTexture(textureId).apply {
                    setDefaultBufferSize(DiLink4ClusterDisplay.STREAM_WIDTH, DiLink4ClusterDisplay.STREAM_HEIGHT)
                    setOnFrameAvailableListener { if (!closed) { frameAvailable = true; requestRender() } }
                }
                texture = next
                val nextSurface = Surface(next)
                post {
                    if (closed || generation != currentGeneration) nextSurface.release() else {
                        input = nextSurface
                        onSurface(nextSurface)
                        Log.i("xcertplay-usb", "Test27 cluster RGB bridge ready: external OES -> RGBA8888, identity RGB, opaque alpha")
                    }
                }
            }
            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
            }
            override fun onDrawFrame(gl: GL10?) {
                if (closed || !frameAvailable) return
                val current = texture ?: return
                current.updateTexImage(); current.getTransformMatrix(matrix)
                GLES20.glClearColor(0f, 0f, 0f, 1f); GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glUseProgram(program)
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
                GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "image"), 0)
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "transform"), 1, false, matrix, 0)
                val p = GLES20.glGetAttribLocation(program, "p")
                val uv = GLES20.glGetAttribLocation(program, "uv")
                vertices.position(0); GLES20.glVertexAttribPointer(p, 2, GLES20.GL_FLOAT, false, 16, vertices)
                vertices.position(2); GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 16, vertices)
                GLES20.glEnableVertexAttribArray(p); GLES20.glEnableVertexAttribArray(uv)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                post { if (!closed) onPresented() }
            }
        })
        renderMode = RENDERMODE_WHEN_DIRTY
    }
    private fun shader(type: Int, source: String): Int {
        val id = GLES20.glCreateShader(type)
        GLES20.glShaderSource(id, source); GLES20.glCompileShader(id)
        val compiled = IntArray(1); GLES20.glGetShaderiv(id, GLES20.GL_COMPILE_STATUS, compiled, 0)
        check(compiled[0] != 0) { GLES20.glGetShaderInfoLog(id) }
        return id
    }
    override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
        detach()
        super.surfaceDestroyed(holder)
    }
    override fun close() {
        if (closed) return
        closed = true
        detach()
    }
    private fun detach() {
        generation++
        frameAvailable = false
        // The host detaches every decoder synchronously before these app-owned objects are released.
        if (input != null) onSurface(null)
        input?.release(); input = null
        if (texture == null) return
        val done = CountDownLatch(1)
        queueEvent {
            try {
                texture?.release(); texture = null
                if (program != 0) GLES20.glDeleteProgram(program)
                if (textureId != 0) GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
                program = 0; textureId = 0
            } finally { done.countDown() }
        }
        done.await()
    }
}
