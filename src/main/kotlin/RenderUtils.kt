import org.lwjgl.opengl.GL20.*

object RenderUtils {
    val chunkNeighbours = mutableListOf(
        Vector3i(-1, 0, 0),
        Vector3i(0, 0, 1),
        Vector3i(1, 0, 0),
        Vector3i(0, 0, -1),
        Vector3i(0, -1, 0),
        Vector3i(0, 1, 0)
    )
    fun putCube(vertexBufferList: MutableList<Double>, pos: Vector3d, colorX: Vector3d, colorY: Vector3d, colorZ: Vector3d, showedSides: Int) {
        val width = 1.0
        //front
        if(showedSides.and(1) != 0)
            vertexBufferList.addAll(
                listOf(
                    pos.x, pos.y + width, pos.z + width, colorZ.x, colorZ.y, colorZ.z,
                    pos.x, pos.y, pos.z + width, colorZ.x, colorZ.y, colorZ.z,
                    pos.x + width, pos.y, pos.z + width, colorZ.x, colorZ.y, colorZ.z,
                    pos.x + width, pos.y, pos.z + width, colorZ.x, colorZ.y, colorZ.z,
                    pos.x + width, pos.y + width, pos.z + width, colorZ.x, colorZ.y, colorZ.z,
                    pos.x, pos.y + width, pos.z + width, colorZ.x, colorZ.y, colorZ.z
                )
            )
        //back
        if(showedSides.shr(1).and(1) != 0)
            vertexBufferList.addAll(listOf(
                pos.x+width, pos.y+width, pos.z, colorZ.x, colorZ.y, colorZ.z,
                pos.x+width, pos.y, pos.z, colorZ.x, colorZ.y, colorZ.z,
                pos.x, pos.y, pos.z, colorZ.x, colorZ.y, colorZ.z,
                pos.x, pos.y, pos.z, colorZ.x, colorZ.y, colorZ.z,
                pos.x, pos.y+width, pos.z, colorZ.x, colorZ.y, colorZ.z,
                pos.x+width, pos.y+width, pos.z, colorZ.x, colorZ.y, colorZ.z))
        //left
        if(showedSides.shr(2).and(1) != 0)
            vertexBufferList.addAll(listOf(
                pos.x, pos.y+width, pos.z, colorX.x, colorX.y, colorX.z,
                pos.x, pos.y, pos.z, colorX.x, colorX.y, colorX.z,
                pos.x, pos.y, pos.z+width, colorX.x, colorX.y, colorX.z,
                pos.x, pos.y, pos.z+width, colorX.x, colorX.y, colorX.z,
                pos.x, pos.y+width, pos.z+width, colorX.x, colorX.y, colorX.z,
                pos.x, pos.y+width, pos.z, colorX.x, colorX.y, colorX.z))
        //right
        if(showedSides.shr(3).and(1) != 0)
            vertexBufferList.addAll(listOf(
                pos.x+width, pos.y+width, pos.z+width, colorX.x, colorX.y, colorX.z,
                pos.x+width, pos.y, pos.z+width, colorX.x, colorX.y, colorX.z,
                pos.x+width, pos.y, pos.z, colorX.x, colorX.y, colorX.z,
                pos.x+width, pos.y, pos.z, colorX.x, colorX.y, colorX.z,
                pos.x+width, pos.y+width, pos.z, colorX.x, colorX.y, colorX.z,
                pos.x+width, pos.y+width, pos.z+width, colorX.x, colorX.y, colorX.z))
        //up
        if(showedSides.shr(4).and(1) != 0)
            vertexBufferList.addAll(listOf(
                pos.x, pos.y+width, pos.z, colorY.x, colorY.y, colorY.z,
                pos.x, pos.y+width, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x+width, pos.y+width, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x+width, pos.y+width, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x+width, pos.y+width, pos.z, colorY.x, colorY.y, colorY.z,
                pos.x, pos.y+width, pos.z, colorY.x, colorY.y, colorY.z))
        //down
        if(showedSides.shr(5).and(1) != 0)
            vertexBufferList.addAll(listOf(
                pos.x+width, pos.y, pos.z, colorY.x, colorY.y, colorY.z,
                pos.x+width, pos.y, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x, pos.y, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x, pos.y, pos.z+width, colorY.x, colorY.y, colorY.z,
                pos.x, pos.y, pos.z, colorY.x, colorY.y, colorY.z,
                pos.x+width, pos.y, pos.z, colorY.x, colorY.y, colorY.z))
    }

    fun renderSelectedBlock(color: Vector3d = Vector3d(0.0, 0.0, 0.0)) {
        glPushMatrix()
        glTranslated(0.5, 0.5, 0.5)
        glScaled(1.02, 1.02, 1.02)
        glTranslated(-0.5, -0.5, -0.5)
        //glTranslated(0.5, 0.5, 0.5)
        glBegin(GL_LINES)
        glColor3d(color.x, color.y, color.z)
        //down base
        glVertex3d(0.0, 0.0, 0.0)
        glVertex3d(0.0, 0.0, 1.0)

        glVertex3d(0.0, 0.0, 1.0)
        glVertex3d(1.0, 0.0, 1.0)

        glVertex3d(1.0, 0.0, 1.0)
        glVertex3d(1.0, 0.0, 0.0)

        glVertex3d(1.0, 0.0, 0.0)
        glVertex3d(0.0, 0.0, 0.0)
        //sides
        glVertex3d(0.0, 0.0, 0.0)
        glVertex3d(0.0, 1.0, 0.0)

        glVertex3d(0.0, 0.0, 1.0)
        glVertex3d(0.0, 1.0, 1.0)

        glVertex3d(1.0, 0.0, 1.0)
        glVertex3d(1.0, 1.0, 1.0)

        glVertex3d(1.0, 0.0, 0.0)
        glVertex3d(1.0, 1.0, 0.0)

        //up base
        glVertex3d(0.0, 1.0, 0.0)
        glVertex3d(0.0, 1.0, 1.0)

        glVertex3d(0.0, 1.0, 1.0)
        glVertex3d(1.0, 1.0, 1.0)

        glVertex3d(1.0, 1.0, 1.0)
        glVertex3d(1.0, 1.0, 0.0)

        glVertex3d(1.0, 1.0, 0.0)
        glVertex3d(0.0, 1.0, 0.0)

        glEnd()
        glPopMatrix()
    }
}