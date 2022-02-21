package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import snower.world.IWorld;

public class FloatingSnowControl extends FloatingControl {
    private static final Quaternion Rotate_About_Y = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);

    private static final float MASS = 80;

    private final IWorld w;
    private final Vector3f jumpForce;

    private Vector3f forward;
    private Vector3f side;

    public FloatingSnowControl(IWorld w) {
        super(MASS);
        this.w = w;
        jumpForce = new Vector3f(0, rigidBody.getMass() * 5, 0);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        super.prePhysicsTick(space, tpf);

        var normal = super.getGroundNormal();
        if (normal != null) {
            // calc forward vector (ignoring vertical component)
            forward = rigidBody.getLinearVelocity().normalize();
            side = Rotate_About_Y.mult(forward);

            float projVel = super.getGroundNormal().dot(getXZNormalized(forward));
            float projSlip = super.getGroundNormal().dot(getXZNormalized(side));

            // a gravity force from the combined direction, Mass, gravity and time step
            // rigidBody.getLinearVelocity() is wrong here, but to help falling physics its here
            rigidBody.applyCentralImpulse(forward.mult(9.81f * rigidBody.getMass() * projVel * tpf));

            // rotate character naturally down the slope
            this.rigidBody.applyCentralImpulse(side.mult(9.81f * rigidBody.getMass() * projSlip * tpf));
            // NOTE: add drag so this doesn't add speed
        } else {
        }
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        super.physicsTick(space, tpf);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        // VIEW update

        var normal = this.getGroundNormal();
        if (normal != null && forward != null) {
            // update char based on ground direction
            // the char wants to be down the wrong X or Z axis, so rotate
            this.charSpatial.setLocalRotation(new Quaternion().lookAt(getXZNormalized(side), normal));
        }
    }

    private static Vector3f getXZNormalized(Vector3f in) {
        return new Vector3f(in.x, 0, in.z).normalize();
    }
}
