package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

public class MyChaseCam extends BaseAppState {

    // https://github.com/Murph9/RallyGame/blob/master/src/main/java/rallygame/car/CarCamera.java
    private static final float CAMERA_CONST = 4;
    
    private final Camera c;
    private final SnowboarderControl control;
    private final Node playerNode;

    private Vector3f nextPos;
    private float tpf;

    public MyChaseCam(Camera c, SnowboarderControl control, Node playerNode) {
        this.c = c;
        this.control = control;
        this.playerNode = playerNode;
    }

    @Override
    public void initialize(Application app) {
        nextPos = playerNode.getLocalTranslation();
    }

    @Override
    public void update(float tpf) {
        this.tpf = tpf;
        super.update(tpf);
    }

    @Override
    public void render(RenderManager rm) {
        Vector3f pos = playerNode.getLocalTranslation();
        Vector3f wanted = getWantedPos(pos);
        nextPos = wanted.interpolateLocal(nextPos, wanted, tpf*CAMERA_CONST);

        c.setLocation(nextPos);
        c.lookAt(pos.add(0,2f,0), Vector3f.UNIT_Y);

        super.render(rm);
    }
        
    /** calculate world pos of a camera */
    private Vector3f getWantedPos(Vector3f pos) {
        var q = control.getViewRot();
        return pos.add(q.mult(Vector3f.UNIT_Z).negate().mult(4.5f).add(0, 3f, 0));
    }

    // #region unused methods
    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void cleanup(Application app) {
    }
    // #endregion
}
