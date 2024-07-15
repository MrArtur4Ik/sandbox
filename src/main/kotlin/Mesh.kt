import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20

class Mesh(val vertexArray: DoubleArray, var vertexCount: Int){
    var vbo: Int = 0

    fun init() {
        vbo = GL20.glGenBuffers()
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo)
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexArray, GL20.GL_STATIC_DRAW)
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0)
    }
    fun delete(){
        GL15.glDeleteBuffers(vbo)
    }
}