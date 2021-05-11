package snower.base;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class SnowTrail {
    //#region Trail fields
    private static final int QUAD_COUNT = 200;

    // Vector3f.size * triangle size * 2 (2 tri per quad) * count
    private static final int VERTEX_BUFFER_SIZE = 4 * QUAD_COUNT;
    private static final int[] indexes = { 2, 0, 1, 1, 3, 2 }; // vertex order
    private static final Vector2f[] texCoord = new Vector2f[] { // texture of quad with order
            new Vector2f(0, 0), new Vector2f(0, 1), new Vector2f(1, 0), new Vector2f(1, 1),
    };
    private static final int[] i_s = new int[6 * QUAD_COUNT];
    private static final Vector2f[] coord = new Vector2f[4 * QUAD_COUNT];
    static {
        //init some more complex static fields
        int j = 0;
        for (int i = 0; i < i_s.length; i += 6) {
            i_s[i + 0] = j * 4 + indexes[0];
            i_s[i + 1] = j * 4 + indexes[1];
            i_s[i + 2] = j * 4 + indexes[2];
            i_s[i + 3] = j * 4 + indexes[3];
            i_s[i + 4] = j * 4 + indexes[4];
            i_s[i + 5] = j * 4 + indexes[5];
            j++;
        }
        j = 0;
        for (int i = 0; i < coord.length; i += 4) {
            coord[i + 0] = texCoord[0].add(new Vector2f(0, j));
            coord[i + 1] = texCoord[0].add(new Vector2f(0, j));
            coord[i + 2] = texCoord[0].add(new Vector2f(0, j));
            coord[i + 3] = texCoord[0].add(new Vector2f(0, j));
            j++;
        }
    }
    //#endregion
    
    private static final ColorRGBA BASE_TRAIL_COLOUR = ColorRGBA.Pink;

    private final Geometry trailLine;
    private final Vector3f[] vertices;
    private int verticesPos;

    private Vector3f lastL;
    private Vector3f lastR;

    public SnowTrail(Application app) {

        vertices = new Vector3f[VERTEX_BUFFER_SIZE];

        this.trailLine = new Geometry();
        // create skid mesh
        Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(i_s));
        //mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(coord));
        // this is the default, but i have to come looking for this again...
        mesh.setMode(Mode.Triangles);
        mesh.setStreamed(); //will be updated rather often
        this.trailLine.setMesh(mesh);
        
        // set material
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        mat.setColor("Color", BASE_TRAIL_COLOUR);

        this.trailLine.setMaterial(mat);
        this.trailLine.setShadowMode(ShadowMode.Receive);
    }

    public Geometry getGeom() {
        return this.trailLine;
    }

    protected void viewUpdate(float tpf, Vector3f[] boardExtents) {
        if (boardExtents == null || boardExtents.length != 4) {
            //set trail end
            lastL = null;
            lastR = null;
            return;
        }

        var points = Helper.getMinMaxX(boardExtents);
        points[0].y += 0.1f; //TODO testing
        points[1].y += 0.1f;

        if (lastL == null || lastR == null) {
            lastL = points[0];
            lastR = points[1];
        }
        
        updateSegment(lastL, lastR, points[0], points[1]);
        lastL = points[0];
        lastR = points[1];
    }

    
    private void updateSegment(Vector3f lastL, Vector3f lastR, Vector3f curL, Vector3f curR) {
        this.vertices[verticesPos] = lastL;
        this.vertices[verticesPos + 1] = curL;
        this.vertices[verticesPos + 2] = lastR;
        this.vertices[verticesPos + 3] = curR;

        Mesh mesh = this.trailLine.getMesh();
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));

        this.trailLine.updateModelBound();

        this.verticesPos += 4;
        this.verticesPos = this.verticesPos % VERTEX_BUFFER_SIZE;
    }
}
