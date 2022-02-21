package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import snower.world.IWorld;

public class FloatingSnowControl extends FloatingControl {

    private static final float MASS = 80;

    private final IWorld w;
    private final Vector3f jumpForce;

    private Vector3f forward;

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
            // calc forward vector (ignoring vertical part)
            forward = rigidBody.getLinearVelocity();
            forward.y = 0;
            forward.normalizeLocal();

            float projVel = super.getGroundNormal().dot(forward);
            //System.out.println(System.currentTimeMillis() + " " + projVel);

            // combine direction, Mass, gravity and time step to make gravity force
            // rigidBody.getLinearVelocity() is wrong here, but to help the physics
            rigidBody.applyCentralImpulse(rigidBody.getLinearVelocity().normalizeLocal().mult(9.81f * rigidBody.getMass() * projVel * tpf));
        } else {
            //System.out.println(System.currentTimeMillis() + " " + -.1f);
        }
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        super.physicsTick(space, tpf);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        var normal = this.getGroundNormal();
        if (normal != null && forward != null) {
            // update char based on ground direction
            // the char wants to be down the wrong X or Z axis, so rotate
            var side = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y).mult(forward);
            this.charSpatial.setLocalRotation(new Quaternion().lookAt(side, normal));
        }
    }
}
