package snower.world;

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

import snower.base.Main;

public class WorldState extends AbstractAppState {
    
    private Main m;
    private List<Geometry> levels;

    public WorldState(Main m) {
        this.m = m;
    }

    public Vector3f startPos() {
        return new Vector3f(0, 75/2, -180/2);
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        levels = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            // add level
            Geometry base = new Geometry("level", new Box(100, 2, 100));
            Material baseMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            baseMat.setColor("Color", ColorRGBA.LightGray);
            base.setMaterial(baseMat);
            base.setLocalTranslation(0, -75*i, 180*i);
            base.setLocalRotation(new Quaternion().fromAngles(FastMath.QUARTER_PI/2, 0, 0));
            base.addControl(new RigidBodyControl(0));
            base.setShadowMode(ShadowMode.CastAndReceive);

            m.getRootNode().attachChild(base);
            Main.physicsSpace.add(base);
            
            levels.add(base);

            
            // add small 'jump'
            Geometry baseLevel = new Geometry("level", new Box(10, 2, 10));
            Material baseLevelMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            baseLevelMat.setColor("Color", ColorRGBA.Brown);
            baseLevel.setMaterial(baseLevelMat);
            baseLevel.setLocalTranslation(0, -75*i, 180*i);
            baseLevel.addControl(new RigidBodyControl(0));
            baseLevel.setShadowMode(ShadowMode.CastAndReceive);

            m.getRootNode().attachChild(baseLevel);
            Main.physicsSpace.add(baseLevel);

            levels.add(baseLevel);

            // add some small rails
            var rail = createRail(new Vector3f(10, -75*i, 180*i + 20), 15, 2);
            m.getRootNode().attachChild(rail);
            Main.physicsSpace.add(rail);

            rail = createRail(new Vector3f(0, -75*i, 180*i + 10), 10, 2);
            m.getRootNode().attachChild(rail);
            Main.physicsSpace.add(rail);

            levels.add(rail);
        }
    }

    private Geometry createRail(Vector3f translation, float length, float height) {
        Geometry baseRail = new Geometry("level", new Box(0.2f, height, length));
        Material baseRailMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        baseRailMat.setColor("Color", ColorRGBA.Black);
        baseRail.setMaterial(baseRailMat);
        baseRail.setLocalTranslation(translation);
        baseRail.addControl(new RigidBodyControl(0));
        baseRail.setShadowMode(ShadowMode.CastAndReceive);

        var pos = baseRail.getLocalTranslation();
        baseRail.setUserData("rail", new RailPath(pos.add(0, height, -length), pos.add(0, height, length)));
        return baseRail;
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        
        for (Geometry l: levels)
            Main.physicsSpace.remove(l);
    }
}
