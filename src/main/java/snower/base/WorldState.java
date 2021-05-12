package snower.base;

import java.util.LinkedList;
import java.util.List;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class WorldState extends AbstractAppState {
    
    private Main m;
    private List<Geometry> levels;

    public WorldState(Main m) {
        this.m = m;
    }

    public Vector3f startPos() {
        return new Vector3f(0, 150/2, -360/2);
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        levels = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            // add level
            Geometry base = new Geometry("level", new Box(200, 2, 200));
            Material baseMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            baseMat.setColor("Color", ColorRGBA.LightGray);
            base.setMaterial(baseMat);
            base.setLocalTranslation(0, -150*i, 360*i);
            base.setLocalRotation(new Quaternion().fromAngles(FastMath.QUARTER_PI/2, 0, 0));
            base.addControl(new RigidBodyControl(0));
            base.setShadowMode(ShadowMode.CastAndReceive);
            m.getRootNode().attachChild(base);
            Main.physicsSpace.add(base);
            levels.add(base);

            
            // add small 'jump'
            Geometry baseLevel = new Geometry("level", new Box(20, 2, 20));
            Material baseLevelMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            baseLevelMat.setColor("Color", ColorRGBA.Brown);
            baseLevel.setMaterial(baseLevelMat);
            baseLevel.setLocalTranslation(0, -150*i, 360*i);
            baseLevel.addControl(new RigidBodyControl(0));
            baseLevel.setShadowMode(ShadowMode.CastAndReceive);
            m.getRootNode().attachChild(baseLevel);
            Main.physicsSpace.add(baseLevel);
            levels.add(baseLevel);
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        
        Main.physicsSpace.remove(levels);
    }
}
