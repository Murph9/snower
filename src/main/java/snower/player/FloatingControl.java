package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.AbstractPhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import snower.service.Helper;
import snower.service.RaySuspension;

public class FloatingControl extends AbstractPhysicsControl implements PhysicsTickListener {
    protected final PhysicsRigidBody rigidBody;
    private final RaySuspension sus;

    private final Vector3f location = new Vector3f();
    private final Quaternion rotation = new Quaternion(Quaternion.DIRECTION_Z);

    private Vector3f groundLocation = new Vector3f();
    private Vector3f groundNormal = new Vector3f();
    protected final Vector3f velocity = new Vector3f();

    public FloatingControl(float mass) {
        var s = new SphereCollisionShape(0.5f);
        rigidBody = new RigidBodyControl(s, mass);
        rigidBody.setFriction(1); // TODO set to 0 when the spring works
        rigidBody.setAngularFactor(0f);

        sus = new RaySuspension(rigidBody, 30f, 0.2f);
    }

    public Vector3f getGroundLocation() { return groundLocation; }
    public Vector3f getGroundNormal() { return groundNormal; }
    
    @Override
    public void update(float tpf) {
        if (!isEnabled()) {
            return;
        }

        rigidBody.getPhysicsLocation(location);
        rigidBody.getPhysicsRotation(rotation);
        applyPhysicsTransform(location, rotation);
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
        keepAboveGround(tpf);
    }

    private void keepAboveGround(float tpf) {
        Vector3f loc = new Vector3f().set(rigidBody.getPhysicsLocation()).addLocal(0, -0.4f, 0); // bottom of the sphere
        float susLength = 0.5f;
        var rayResult = Helper.findClosestResult(loc, susLength, rigidBody, true);

        if (rayResult == null || rayResult.pos == null) {
            groundLocation = null;
            groundNormal = null;
        } else {
            groundLocation = rayResult.pos;
            groundNormal = rayResult.normal;

            var result = sus.calcSusResult(rayResult.distance, susLength);
            rigidBody.applyCentralImpulse(new Vector3f(0, rigidBody.getMass()*result*tpf, 0));
        }
    }
}
