package snower.player;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import snower.world.RailPath;

public class SnowboarderRailDetector implements PhysicsTickListener {
    
    private final GhostControl ghost;
    private final SnowboarderControl control;
    private final PhysicsSpace space;

    private final Geometry collisionGeom;

    public SnowboarderRailDetector(AssetManager am, PhysicsSpace space, SnowboarderControl control) {
        this.control = control;
        this.space = space;

        this.collisionGeom = new Geometry("ghost", new Box(0.2f, 0.2f, 0.8f));
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        collisionGeom.setMaterial(mat);
        collisionGeom.setLocalTranslation(0, -0.2f, 0);

        this.ghost = new GhostControl(CollisionShapeFactory.createBoxShape(collisionGeom));
        collisionGeom.addControl(ghost);

        this.setEnabled(false);
    }

    public void setEnabled(boolean value) {
        var playerNode = (Node)control.getSpatial();
        if (value) {
            playerNode.attachChild(collisionGeom);
            space.add(ghost);
            space.addTickListener(this);
        } else {
            playerNode.detachChild(collisionGeom);
            space.remove(ghost);
            space.removeTickListener(this);
        }
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // nothing by choice
    }

    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        var objects = this.ghost.getOverlappingObjects();
        for (var obj: objects) {
            if (obj instanceof PhysicsRigidBody) {
                var body = (PhysicsRigidBody)obj;
                if (!(body.getUserObject() instanceof Spatial))
                    continue;
                
                var spat = (Spatial)body.getUserObject();
                var rail = spat.getUserData("rail");
                if (rail == null)
                    continue;
                    
                if (rail instanceof RailPath) {
                    var path = (RailPath) rail;
                    this.control.getOnRail(path);
                } else {
                    System.out.println("!!!! Rail with invalid type: " + rail);
                }
            }
        }
    }
}
