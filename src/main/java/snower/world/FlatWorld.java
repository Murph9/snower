package snower.world;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import snower.base.Main;

public class FlatWorld extends BaseAppState implements IWorld {

    private Spatial worldNode;

    @Override
    public Vector3f startPos() {
        return new Vector3f(0, 3, 0);
    }

    @Override
    protected void initialize(Application app) {
        worldNode = new Geometry("level", new Box(10, 2, 10));
        var baseMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        baseMat.setColor("Color", ColorRGBA.LightGray);
        worldNode.setMaterial(baseMat);
        worldNode.setLocalTranslation(0, -2, 0);
        worldNode.addControl(new RigidBodyControl(0));
        worldNode.setShadowMode(ShadowMode.CastAndReceive);

        ((SimpleApplication)app).getRootNode().attachChild(worldNode);
        Main.physicsSpace.add(worldNode);
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        
    }

    @Override
    protected void onDisable() {
        
    }
    

}
