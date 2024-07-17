import org.lwjgl.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import java.nio.IntBuffer
import kotlin.math.*
import kotlin.random.Random
import kotlin.time.measureTime

data class Vector2i(val x: Int, val y: Int)

data class Vector3d(var x: Double, var y: Double, var z: Double)

data class Vector3i(val x: Int, val y: Int, val z: Int)

class HelloWorld {
    private var window: Long = 0
    private var windowWidth: Int = 1200
    private var windowHeight: Int = 900
    private var cameraYaw = 0.0
    private var cameraPitch = 0.0
    private var cameraX = 0.0
    private var cameraY = 4.0
    private var cameraZ = 0.0
    private var cameraRotationSpeed = 0.1
    private var cameraMoveSpeed = 0.1
    private var mouseGrab = false
    private var level = Level()
    private var breakBlock: Vector3i? = null
    private var placeBlock: Vector3i? = null

    fun run() {
        println(("Hello LWJGL " + Version.getVersion()) + "!")

        init()
        loop()

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    fun gluPerspective(fovy: Float, aspect: Float, near: Float, far: Float) {
        val bottom = -near * tan(fovy / 2)
        val top = -bottom
        val left = aspect * bottom
        val right = -left
        glFrustum(left.toDouble(), right.toDouble(), bottom.toDouble(), top.toDouble(), near.toDouble(), far.toDouble())
    }

    private fun init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.

        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        window = glfwCreateWindow(windowWidth, windowHeight, "Hello World!", NULL, NULL)
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            // We will detect these in the rendering loop
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                mouseGrab = !mouseGrab
                applyMouseGrab(mouseGrab)
            }
            if (key == GLFW_KEY_Q && action == GLFW_RELEASE) glfwSetWindowShouldClose(
                window,
                true
            )
            if (key == GLFW_KEY_P && action == GLFW_RELEASE) level.chunks.forEach { it.render(level) }
            if (key == GLFW_KEY_E && action == GLFW_RELEASE) {
                level.setBlock(floor(cameraX).toInt(), floor(cameraY).toInt(), floor(cameraZ).toInt(), 1)
            }
            if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
                level.setBlock(floor(cameraX).toInt(), floor(cameraY).toInt(), floor(cameraZ).toInt(), 0)
            }
        }
        glfwSetMouseButtonCallback(window) { window, button, action, mods ->
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                if(breakBlock != null)
                    level.setBlock(breakBlock!!.x, breakBlock!!.y, breakBlock!!.z, 0)
            }else if(button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
                if(placeBlock != null)
                    level.setBlock(placeBlock!!.x, placeBlock!!.y, placeBlock!!.z, 1)
            }
        }
        glfwSetWindowSizeCallback(window) { _, width, height ->
            windowWidth = width
            windowHeight = height
            applyFOV()
        }

        stackPush().use { stack ->
            val pWidth: IntBuffer = stack.mallocInt(1) // int*
            val pHeight: IntBuffer = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)


            // Get the resolution of the primary monitor



            glfwGetVideoMode(glfwGetPrimaryMonitor())?.let { vidmode ->
                // Center the window
                glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth[0]) / 2,
                    (vidmode.height() - pHeight[0]) / 2
                )
            }
        }
        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun getCursorPos(): Vector2i {
        val xDoubleArray = arrayOf(0.0).toDoubleArray()
        val yDoubleArray = arrayOf(0.0).toDoubleArray()
        glfwGetCursorPos(window, xDoubleArray, yDoubleArray)
        return Vector2i(xDoubleArray[0].toInt(), yDoubleArray[0].toInt())
    }

    private fun applyFOV() {
        glViewport(0, 0, windowWidth, windowHeight)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        gluPerspective(1.5f, windowWidth.toFloat() / windowHeight.toFloat(), 0.0001f, 1000.0f)
        glMatrixMode(GL_MODELVIEW)
    }

    private fun applyMouseGrab(mouseGrab: Boolean) {
        if(mouseGrab) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }else{
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
        }
    }

    private fun calculateBreakBlock() {
        val steps = 100
        val maxDistance = 5.0
        val distancePerStep = maxDistance/steps
        var rayPos = Vector3d(cameraX, cameraY, cameraZ)
        var oldPos = rayPos
        val step = Vector3d(
            distancePerStep * sin(cameraYaw / 180 * Math.PI) * cos(cameraPitch / 180 * Math.PI),
            -distancePerStep * sin(cameraPitch / 180 * Math.PI),
            -distancePerStep * cos(cameraYaw / 180 * Math.PI) * cos(cameraPitch / 180 * Math.PI)
        )
        (0..steps).forEach { i ->
            val blockPos = Vector3i(floor(rayPos.x).toInt(), floor(rayPos.y).toInt(), floor(rayPos.z).toInt())
            if(level.getBlockAt(blockPos.x, blockPos.y, blockPos.z) != 0) {
                breakBlock = blockPos
                placeBlock = Vector3i(floor(oldPos.x).toInt(), floor(oldPos.y).toInt(), floor(oldPos.z).toInt())
                return
            }
            if(i < steps) {
                oldPos = rayPos
                rayPos = Vector3d(rayPos.x + step.x, rayPos.y + step.y, rayPos.z + step.z)
            }
        }
        breakBlock = null
        placeBlock = null
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Set the clear color
        glClearColor(0.0f, 0.5f, 1.0f, 1.0f)

        applyFOV()
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glEnable(GL_COLOR_MATERIAL)
        glEnable(GL_FOG)
        glDepthFunc(GL_LESS)

        //fog
        glFogi(GL_FOG_MODE, GL_LINEAR)
        glFogf(GL_FOG_DENSITY, 0.15f)
        glHint(GL_FOG_HINT, GL_FASTEST)
        glFogf(GL_FOG_START, 55.0f)
        glFogf(GL_FOG_END, 60.0f)
        val fogColor = arrayOf(0.0f, 0.5f, 1.0f, 1.0f).toFloatArray()
        GL11.glFogfv(GL_FOG_COLOR, fogColor)

        /*fun putSquare(pos: Vector3d, color: Vector3d) {
            val width = 1.0
            vertexBufferList.addAll(listOf(
                pos.x, pos.y, pos.z, color.x, color.y, color.z,
                pos.x, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z, color.x, color.y, color.z,
                pos.x, pos.y, pos.z, color.x, color.y, color.z))
        }*/

        /*(0..<16).forEach { x ->
            (0..<16).forEach { y ->
                (0..<16).forEach { z ->
                    if(Random.nextBoolean()) {
                        chunklet.setBlock(x, y, z, 1, render = false)
                    }
                }
            }
        }*/

        (-4..<4).forEach { chunkX ->
            (-4..<4).forEach { chunkZ ->
                (0..<2).forEach { chunkY ->
                    val chunklet = Chunklet(chunkX, chunkY, chunkZ)
                    if(chunkY == 0) {
                        (0..<16).forEach { x ->
                            (0..<16).forEach { y ->
                                chunklet.setBlock(x, 0, y, 1, render = false)
                            }
                        }
                    }
                    level.chunks.add(chunklet)
                }
            }
        }

        level.chunks.forEach { it.render(level) }

        var previousCursorPos = getCursorPos()
        var cursorPosDelta = Vector2i(0, 0)

        var deltaTime: Long = 0
        var fps: Double
        applyMouseGrab(mouseGrab)
        calculateBreakBlock()

        while (!glfwWindowShouldClose(window)) {
            val startTime = System.currentTimeMillis()

            level.chunks.filter { it.stateChanged }.forEach { chunk ->
                chunk.render(level)
                chunk.stateChanged = false
                //println("Chunk ${chunk.x} ${chunk.y} ${chunk.z} rendered")
            }

            glLoadIdentity()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            val timeMultiplier = deltaTime*0.1

            if(glfwGetKey(window, GLFW_KEY_W) == 1) {
                cameraZ -= cameraMoveSpeed * cos(cameraYaw / 180 * Math.PI) * timeMultiplier
                cameraX -= -cameraMoveSpeed * sin(cameraYaw / 180 * Math.PI) * timeMultiplier
            }
            if(glfwGetKey(window, GLFW_KEY_S) == 1) {
                cameraZ -= cameraMoveSpeed * cos((cameraYaw + 180) / 180 * Math.PI) * timeMultiplier
                cameraX -= -cameraMoveSpeed * sin((cameraYaw + 180) / 180 * Math.PI) * timeMultiplier
            }
            if(glfwGetKey(window, GLFW_KEY_A) == 1) {
                cameraZ -= cameraMoveSpeed * cos((cameraYaw - 90) / 180 * Math.PI) * timeMultiplier
                cameraX -= -cameraMoveSpeed * sin((cameraYaw - 90) / 180 * Math.PI) * timeMultiplier
            }
            if(glfwGetKey(window, GLFW_KEY_D) == 1) {
                cameraZ -= cameraMoveSpeed * cos((cameraYaw + 90) / 180 * Math.PI) * timeMultiplier
                cameraX -= -cameraMoveSpeed * sin((cameraYaw + 90) / 180 * Math.PI) * timeMultiplier
            }
            if(glfwGetKey(window, GLFW_KEY_SPACE) == 1) {
                cameraY += cameraMoveSpeed * timeMultiplier
            }
            if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == 1) {
                cameraY -= cameraMoveSpeed * timeMultiplier
            }

            if(mouseGrab) {
                calculateBreakBlock()
                cameraYaw += cursorPosDelta.x.toFloat() * cameraRotationSpeed * timeMultiplier
                cameraPitch += cursorPosDelta.y.toFloat() * cameraRotationSpeed * timeMultiplier
            }
            //normalizing yaw
            if (cameraYaw < -180) cameraYaw += 360
            else if (cameraYaw > 180) cameraYaw -= 360
            //limiting pitch
            cameraPitch = min(max(cameraPitch, -90.0), 90.0)

            //rotation scene
            glRotated(cameraPitch, 1.0, 0.0, 0.0)
            glRotated(cameraYaw, 0.0, 1.0, 0.0)

            //start drawing
            //glPushMatrix()
            //move scene
            glTranslated(-cameraX, -cameraY, -cameraZ)

            glEnableClientState(GL_VERTEX_ARRAY)
            glEnableClientState(GL_COLOR_ARRAY)

            level.chunks.forEach { chunk ->
                if(chunk.mesh != null) {
                    glPushMatrix()
                    glTranslated(chunk.x*16.0, chunk.y*16.0, chunk.z*16.0)
                    glBindBuffer(GL_ARRAY_BUFFER, chunk.mesh!!.vbo)
                    GL11.glVertexPointer(3, GL_DOUBLE, 8 * 6, 0)
                    GL11.glColorPointer(3, GL_DOUBLE, 8 * 6, 8 * 3)

                    glDrawArrays(GL_TRIANGLES, 0, chunk.mesh!!.vertexCount)
                    glPopMatrix()
                }
            }

            glDisableClientState(GL_VERTEX_ARRAY)
            glDisableClientState(GL_COLOR_ARRAY)

            //selected block

            if(breakBlock != null) {
                glPushMatrix()
                glTranslated(breakBlock!!.x*1.0, breakBlock!!.y*1.0, breakBlock!!.z*1.0)
                RenderUtils.renderSelectedBlock()
                glPopMatrix()
            }
            /*if(placeBlock != null) {
                glPushMatrix()
                glTranslated(placeBlock!!.x*1.0, placeBlock!!.y*1.0, placeBlock!!.z*1.0)
                RenderUtils.renderSelectedBlock(Vector3d(0.0, 0.9, 0.0))
                glPopMatrix()
            }*/

            //glPopMatrix()
            //stop drawing


            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            val currentCursorPos = getCursorPos()
            cursorPosDelta = Vector2i(currentCursorPos.x-previousCursorPos.x, currentCursorPos.y-previousCursorPos.y)
            if(mouseGrab) glfwSetCursorPos(window, windowWidth.toDouble()/2, windowHeight.toDouble()/2)
            previousCursorPos = getCursorPos()

            glfwPollEvents()

            deltaTime = System.currentTimeMillis()-startTime
            fps = 1000.0/deltaTime

            glfwSetWindowTitle(window, "deltaTime: $deltaTime fps: ${fps.roundToInt()} x: $cameraX y: $cameraY z: $cameraZ yaw: $cameraYaw pitch: $cameraPitch")
        }
        level.chunks.forEach { it.mesh?.delete() }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HelloWorld().run()
        }
    }
}