package snower.world;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;

import snower.base.Main;

public class StaticWorldState extends BaseAppState {

    private static final String ASSET = "models/ski_slope_1.gltf";

    private Spatial worldNode;

    @Override
    protected void initialize(Application app) {
        worldNode = app.getAssetManager().loadModel(ASSET);

        worldNode.addControl(new RigidBodyControl(0));
        worldNode.setShadowMode(ShadowMode.CastAndReceive);

        ((SimpleApplication)app).getRootNode().attachChild(worldNode);
        Main.physicsSpace.add(worldNode);

        //TODO rails
    }

    
    public Vector3f startPos() {
        return new Vector3f(-154.1f, 24.265f, -5.0893f);
        // return new Vector3f(-1.1f, 104.265f, 0.0893f);
        // return new Vector3f();
    }


    @Override
    protected void cleanup(Application app) {
        worldNode.removeFromParent();
    }

    @Override
    protected void onEnable() {        
    }

    @Override
    protected void onDisable() {
    }
    
}
