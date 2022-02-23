package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.AbstractPhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import snower.service.Helper;
import snower.service.RaySuspension;

public class FloatingControl extends AbstractPhysicsControl implements PhysicsTickListener {
    protected final PhysicsRigidBody rigidBody;
    private final RaySuspension sus;
    
    private final Vector3f location = new Vector3f();
    private final Quaternion rotation = new Quaternion();

    protected Spatial charSpatial;

    private Vector3f groundLocation = new Vector3f();
    private Vector3f groundNormal = new Vector3f();
    protected final Vector3f velocity = new Vector3f();
    protected float distanceToGround = -1;

    public FloatingControl(float mass) {
        var s = new SphereCollisionShape(0.5f);
        rigidBody = new RigidBodyControl(s, mass);
        rigidBody.setFriction(1); // TODO set to 0 when the spring works
        rigidBody.setAngularFactor(0f);
        rigidBody.setLinearDamping(0.05f); //TODO test

        sus = new RaySuspension(rigidBody, 30f, 0.2f);
    }

    public Vector3f getGroundLocation() { return groundLocation; }
    public Vector3f getGroundLocationInLocalSpatial() {
        if (groundLocation == null) return null;
        var out = new Vector3f();
        return this.getSpatial().worldToLocal(groundLocation, out);
    }
    public Vector3f getGroundNormal() { return groundNormal; }
    
    @Override
    public void setSpatial(Spatial newSpatial) {
        super.setSpatial(newSpatial);

        // validate some assumptions made in the class, to protect all the uses of .getChild(0)
        assert newSpatial instanceof Node;
        var children = ((Node)newSpatial).getChildren();
        assert children.size() == 1;
        assert children.get(0) instanceof Spatial;

        charSpatial = children.get(0);
    }

    @Override
    public void update(float tpf) {
        if (!isEnabled()) {
            return;
        }

        rigidBody.getPhysicsLocation(location);
        rigidBody.getPhysicsRotation(rotation);
        applyPhysicsTransform(location, rotation);

        var pos = getGroundLocationInLocalSpatial();
        if (pos != null)
            charSpatial.setLocalTranslation(pos); //set the bottom of the char to ground
    }

    @Override
    protected void addPhysics() {
        PhysicsSpace space = getPhysicsSpace();
        space.addCollisionObject(rigidBody);
        space.addTickListener(this);
    }

    @Override
    protected void createSpatialData(Spatial spatial) {
        rigidBody.setUserObject(spatial);
    }

    @Override
    protected void removePhysics() {
        PhysicsSpace space = getPhysicsSpace();
        space.removeCollisionObject(rigidBody);
        space.removeTickListener(this);
    }

    @Override
    protected void removeSpatialData(Spatial spatial) {
        rigidBody.setUserObject(null);
    }

    @Override
    protected void setPhysicsLocation(Vector3f location) {
        rigidBody.setPhysicsLocation(location);
        location.set(location);
    }

    @Override
    protected void setPhysicsRotation(Quaternion orientation) {
        rotation.set(orientation);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        rigidBody.getLinearVelocity(velocity);
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        // keep above the ground
        Vector3f loc = rigidBody.getPhysicsLocation().add(0, -0.4f, 0); // bottom of the sphere
        float susLength = 0.5f;
        var rayResult = Helper.findClosestResult(loc, susLength, rigidBody, true);

        if (rayResult == null || rayResult.pos == null) {
            groundLocation = null;
            groundNormal = null;
        } else {
            // assume tpf is for the previous frame
            float relativeGroundVelocity = 0;
            if (groundLocation != null)
                relativeGroundVelocity = (groundLocation.y - rayResult.pos.y)/tpf;
            groundLocation = rayResult.pos;
            groundNormal = rayResult.normal;

            var result = sus.calcSusResult(rayResult.distance, susLength, relativeGroundVelocity);
            rigidBody.applyCentralImpulse(new Vector3f(0, rigidBody.getMass()*result*tpf, 0));
        }

        var distanceResult = Helper.findClosestResult(loc, 100, rigidBody, true);
        distanceToGround = distanceResult.distance;
    }
}
