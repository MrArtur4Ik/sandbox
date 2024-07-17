import kotlin.math.floor

class Level(val chunks: MutableList<Chunklet> = mutableListOf()) {
    fun getBlockAt(x: Int, y: Int, z: Int): Int {
        val chunkX = floor(x/16.0).toInt()
        val chunkY = floor(y/16.0).toInt()
        val chunkZ = floor(z/16.0).toInt()
        val chunk = chunks.firstOrNull { it.x == chunkX && it.y == chunkY && it.z == chunkZ } ?: return 0
        return chunk.blocks.getOrNull((x.mod(16)) + (y.mod(16))*16 + (z.mod(16))*256) ?: 0
    }
    fun setBlock(x: Int, y: Int, z: Int, type: Int, render: Boolean = true): Boolean {
        val chunkX = floor(x/16.0).toInt()
        val chunkY = floor(y/16.0).toInt()
        val chunkZ = floor(z/16.0).toInt()
        val chunk = chunks.firstOrNull { it.x == chunkX && it.y == chunkY && it.z == chunkZ } ?: return false
        /*val i = (x.mod(16)) + (y.mod(16))*16 + (z.mod(16))*256
        if(chunk.blocks[i] != type) {
            chunk.blocks[i] = type
            if(render) chunk.stateChanged = true
        }*/
        chunk.setBlock(x.mod(16), y.mod(16), z.mod(16), type, true, this)
        return true
    }
    fun getChunkOrNull(pos: Vector3i): Chunklet? {
        return chunks.firstOrNull { it.x == pos.x && it.y == pos.y && it.z == pos.z }
    }
}