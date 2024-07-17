import java.nio.IntBuffer

class Chunklet(val x: Int, val y: Int, val z: Int, val blocks: IntArray = IntArray(4096)) {
    //rendering things
    var mesh: Mesh? = null
    var stateChanged = false
    //logic
    fun getBlockAt(x: Int, y: Int, z: Int): Int {
        if (x < 0 || x > 15 || y < 0 || y > 15 || z < 0 || z > 15) return 0
        return blocks.getOrNull(x + y*16 + z*256) ?: 0
    }
    fun setBlock(x: Int, y: Int, z: Int, type: Int, render: Boolean = true, level: Level? = null) {
        val i = x + y*16 + z*256
        if(blocks[i] != type) {
            blocks[i] = type
            if(render) {
                if(level != null) {
                    RenderUtils.chunkNeighbours.forEach { neighbour ->
                        level.getChunkOrNull(Vector3i(neighbour.x+this.x, neighbour.y+this.y, neighbour.z+this.z))?.let { chunk ->
                            chunk.stateChanged = true
                        }
                    }
                }
                stateChanged = true
            }
        }
    }
    fun render(level: Level) {
        val vertexBufferList = mutableListOf<Double>()

        fun getBlock(x: Int, y: Int, z: Int): Int {
            if (x < 0 || x > 15 || y < 0 || y > 15 || z < 0 || z > 15) {
                return level.getBlockAt(this.x*16+x, this.y*16+y, this.z*16+z)
            }
            return blocks.getOrNull(x + y*16 + z*256) ?: 0
        }

        (0..<16).forEach { x ->
            (0..<16).forEach { y ->
                (0..<16).forEach { z ->
                    if (getBlockAt(x, y, z) != 0) {
                        val showedSides =
                            (if (getBlock(x, y, z + 1) != 0) 0 else 1).or(
                                (if (getBlock(x, y, z - 1) != 0) 0 else 1).shl(1)
                            ).or(
                                (if (getBlock(x - 1, y, z) != 0) 0 else 1).shl(2)
                            ).or(
                                (if (getBlock(x + 1, y, z) != 0) 0 else 1).shl(3)
                            ).or(
                                (if (getBlock(x, y + 1, z) != 0) 0 else 1).shl(4)
                            ).or(
                                (if (getBlock(x, y - 1, z) != 0) 0 else 1).shl(5)
                            )
                        RenderUtils.putCube(vertexBufferList,
                            Vector3d(x * 1.0, y * 1.0, z * 1.0),
                            Vector3d(0.6, 0.6, 0.6),
                            Vector3d(0.8, 0.8, 0.8),
                            Vector3d(0.5, 0.5, 0.5),
                            showedSides
                        )
                    }
                }
            }
        }

        val vertexBuffer = vertexBufferList.toDoubleArray()

        val vertexCount = vertexBuffer.size / 6
        mesh = Mesh(vertexBuffer, vertexCount)
        mesh!!.init()
    }
}