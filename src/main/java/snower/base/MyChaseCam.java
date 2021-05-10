package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

public class MyChaseCam extends BaseAppState {

    // https://github.com/Murph9/RallyGame/blob/master/src/main/java/rallygame/car/CarCamera.java
    
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
        nextPos = wanted.interpolateLocal(nextPos, wanted, tpf);
        Vector3f nextPlayerPos = pos;

        c.setLocation(nextPos);
        c.lookAt(nextPlayerPos, Vector3f.UNIT_Y); //TODO not unit y if the char is going down hill

        super.render(rm);
    }
        
    // calculate world pos of a camera
    private Vector3f getWantedPos(Vector3f pos) {
        // calculate world pos of a camera
        Vector3f localVel = new Vector3f();
        control.getViewDirection(localVel);

        // TODO calc angle of ground and 'look' down the hill a little better

        return pos.add(localVel.normalize().negate().add(0, 0.4f, 0).mult(new Vector3f(8, 8, 8)));
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
