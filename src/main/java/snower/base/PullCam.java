package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;

public class PullCam extends BaseAppState {

    private final Camera c;
    private final Spatial node;
    private final boolean keepLevel;

    private Vector3f lastPos;

    public PullCam(Camera c, Spatial node) {
        this(c, node, false);
    }

    public PullCam(Camera c, Spatial node, boolean keepLevel) {
        this.c = c;
        this.node = node;
        this.keepLevel = keepLevel;
    }

    @Override
    public void initialize(Application app) {
        lastPos = node.getWorldTranslation().add(0, 3, 6);
    }

    @Override
    public void render(RenderManager rm) {
        var playerPos = node.getWorldTranslation();
        var camOffset = new Vector3f(0, 2f, 0); // offset so we are look over it
        
        if (lastPos.distance(playerPos) > 6) { // don't update when the player is really close
            Quaternion q = new Quaternion().lookAt(lastPos.subtract(playerPos), Vector3f.UNIT_Y);
            lastPos = q.mult(Vector3f.UNIT_Z).mult(6);
            lastPos.addLocal(playerPos);
        }

        var newPos = lastPos.add(camOffset);
        if (keepLevel)
            newPos = new Vector3f(newPos.x, 3, newPos.z); //keep level for testing

        c.setLocation(newPos);
        c.lookAt(playerPos.add(0, 1.5f, 0), Vector3f.UNIT_Y);

        super.render(rm);
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
