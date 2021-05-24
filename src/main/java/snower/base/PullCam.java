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
    private final Spatial playerNode;

    private Vector3f lastPos;

    public PullCam(Camera c, Spatial playerNode) {
        this.c = c;
        this.playerNode = playerNode;
    }

    @Override
    public void initialize(Application app) {
        lastPos = playerNode.getWorldTranslation().add(0, 3, 6);
    }

    @Override
    public void render(RenderManager rm) {
        var playerPos = playerNode.getWorldTranslation();
        var camOffset = new Vector3f(0, 2.5f, 0); //offset so we are look over the shoulder
        
        if (lastPos.distance(playerPos) > 6) { // don't update when the player is really close
            Quaternion q = new Quaternion().lookAt(lastPos.subtract(playerPos), Vector3f.UNIT_Y);
            lastPos = q.mult(Vector3f.UNIT_Z).mult(6);
            lastPos.addLocal(playerPos);
        }

        c.setLocation(lastPos.add(camOffset));
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
