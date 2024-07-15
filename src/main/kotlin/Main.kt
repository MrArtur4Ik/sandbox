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

data class Vector2i(val x: Int, val y: Int)

data class Vector3d(val x: Double, val y: Double, val z: Double)

class HelloWorld {
    // The window handle
    private var window: Long = 0
    private var windowWidth: Int = 1200
    private var windowHeight: Int = 900
    private var cameraYaw = 180.0
    private var cameraPitch = 0.0
    private var cameraX = 0.0
    private var cameraY = 4.0
    private var cameraZ = 0.0
    private var cameraRotationSpeed = 0.1
    private var cameraMoveSpeed = 0.1
    private var mouseGrab = true

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
        // Setup an error callback. The default implementation
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

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
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

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Set the clear color
        glClearColor(0.0f, 0.3f, 0.0f, 0.0f)

        applyFOV()
        glEnable(GL_DEPTH_TEST)
        //glEnable(GL_CULL_FACE)

        /*val vertexBuffer = arrayOf(
            -1.0, -1.0, -4.0,
            1.0, 1.0, 1.0,
            1.0, -1.0, -4.0,
            1.0, 1.0, 1.0,
            0.0,  1.0, -4.0,
            1.0, 1.0, 1.0,
            -1.0, -1.0, -10.0,
            1.0, 0.0, 0.0,
            1.0, -1.0, -10.0,
            0.0, 1.0, 0.0,
            0.0,  1.0, -10.0,
            0.0, 0.0, 1.0
        ).toDoubleArray()*/

        val vertexBufferList = mutableListOf<Double>()

        fun putSquare(pos: Vector3d, color: Vector3d) {
            val width = 1.0
            vertexBufferList.addAll(listOf(
                pos.x, pos.y, pos.z, color.x, color.y, color.z,
                pos.x, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z+width, color.x, color.y, color.z,
                pos.x+width, pos.y, pos.z, color.x, color.y, color.z,
                pos.x, pos.y, pos.z, color.x, color.y, color.z))
        }
        (0..<128).forEach { x ->
            (0..<128).forEach { z ->
                putSquare(Vector3d(x*1.0, 0.0, z*1.0), Vector3d(
                    (x.xor(z))%256/256.0,
                    (x.xor(z))%256/256.0,
                    (x.xor(z))%256/256.0))
            }
        }

        val vertexBuffer = vertexBufferList.toDoubleArray()

        val vertexCount = vertexBuffer.size/6
        println(vertexCount)
        val triangleMesh = Mesh(vertexBuffer, vertexCount)
        triangleMesh.init()

        var previousCursorPos = getCursorPos()
        var cursorPosDelta = Vector2i(0, 0)

        var deltaTime: Long = 0
        var fps: Double
        applyMouseGrab(mouseGrab)

        while (!glfwWindowShouldClose(window)) {
            val startTime = System.currentTimeMillis()

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
                cameraYaw += cursorPosDelta.x.toFloat() * cameraRotationSpeed// * timeMultiplier
                cameraPitch += cursorPosDelta.y.toFloat() * cameraRotationSpeed// * timeMultiplier
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
            glPushMatrix()
            //move scene
            glTranslated(-cameraX, -cameraY, -cameraZ)

            glEnableClientState(GL_VERTEX_ARRAY)
            glEnableClientState(GL_COLOR_ARRAY)
            glBindBuffer(GL_ARRAY_BUFFER, triangleMesh.vbo)
            GL11.glVertexPointer(3, GL_DOUBLE, 8*6, 0)
            GL11.glColorPointer(3, GL_DOUBLE, 8*6, 8*3)

            glDrawArrays(GL_TRIANGLES, 0, triangleMesh.vertexCount)

            /*
            //glColor3f(0.0f, 0.0f, 1.0f)
            //center triangle
            glDrawArrays(GL_TRIANGLES, 0, triangleMesh.vertexCount)

            //right triangle
            glTranslatef(3.0f, 0.5f, 0.0f)
            //glColor3f(1.0f, 0.0f, 0.0f)
            glDrawArrays(GL_TRIANGLES, 0, triangleMesh.vertexCount)

            //left triangle
            glTranslatef(-3.0f, -0.5f, 0.0f)
            glTranslatef(-3.0f, 0.5f, 0.0f)
            //glColor3f(1.0f, 1.0f, 1.0f)

            glTranslated(-0.0, 1.0, -4.0)
            //glRotated(cameraPitch, -1.0, 0.0, 0.0)
            //glRotated(cameraYaw, 0.0, -1.0, 0.0)
            glRotated(cameraYaw, 0.0, -1.0, 0.0)
            glRotated(cameraPitch, -1.0, 0.0, 0.0)
            //glRotated(45.0, 0.0, 1.0, 0.0)
            glTranslated(0.0, -1.0, 4.0)
            glDrawArrays(GL_TRIANGLES, 0, 6)
            */

            glDisableClientState(GL_VERTEX_ARRAY)
            glDisableClientState(GL_COLOR_ARRAY)

            glPopMatrix()
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
        triangleMesh.delete()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HelloWorld().run()
        }
    }
}