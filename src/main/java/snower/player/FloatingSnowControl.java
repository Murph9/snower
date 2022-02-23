package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import snower.player.GrabMapper.GrabEnum;
import snower.player.TrickDetector.TrickList;
import snower.service.Helper;
import snower.world.IWorld;

public class FloatingSnowControl extends FloatingControl implements ISnowControl {
    private static final Quaternion Rotate_About_Y = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);

    private static final float MASS = 80;

    private final IWorld w;
    private final Vector3f jumpForce;

    private Vector3f forward;
    private Vector3f side;

    private float turnAmount;
    
    private float spinAmount;
    private float flipAmount;

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

            float projVel = super.getGroundNormal().dot(Helper.getXZNormalized(forward));
            float projSlip = super.getGroundNormal().dot(Helper.getXZNormalized(side));

            // a gravity force from the combined direction, Mass, gravity and time step
            // rigidBody.getLinearVelocity() is wrong here, but to help falling physics its here
            rigidBody.applyCentralImpulse(forward.mult(9.81f * rigidBody.getMass() * projVel * tpf));

            // rotate character naturally down the slope
            this.rigidBody.applyCentralImpulse(side.mult(9.81f * rigidBody.getMass() * projSlip * tpf));
            // NOTE: add drag so this doesn't add speed

            if (turnAmount != 0) {
                this.rigidBody.applyCentralImpulse(side.mult(-turnAmount * 10 * rigidBody.getMass() * tpf));
            }
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
            this.charSpatial.setLocalRotation(new Quaternion().lookAt(Helper.getXZNormalized(side), normal));
        }
    }


    @Override
    public boolean isOnGround() {
        if (super.getGroundLocation() != null)
            return true;
        
        // TODO debounce raycasts which aren't perfect
        return false;
    }

    @Override
    public void turn(float amount) {
        if (this.isOnGround()) {
            this.turnAmount = amount;
            this.spinAmount = 0;
        } else {
            this.turnAmount = 0;
            this.spinAmount = amount;
        }
        //TODO only allow spinning while we are 'some' distance off the ground
    }

    @Override
    public void jump(float amount) {
        this.rigidBody.applyCentralImpulse(jumpForce);
    }

    @Override
    public void slow(float amount) {
        // TODO maybe change speed factor?
    }

    @Override
    public void flip(float amount) {
        if (this.isOnGround())
            this.flipAmount = 0;
        else
            this.flipAmount = 1;
        
    }

    @Override
    public void reset() {
        setPhysicsLocation(w.startPos());
    }

    @Override
    public void setDucked(boolean value) {
        // TODO Auto-generated method stub
    }

    @Override
    public void finishRail(boolean value) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isSwitch() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grab(GrabEnum type) {
    }

    @Override
    public String getDebugStr() {
        var sb = new StringBuilder();
        sb.append("On Ground: " + isOnGround() + "\n");
        sb.append("Crashing: " + (this.charControl.isCrashing() ? "true " + this.charControl.crashingReason() : "false") + "\n");
        sb.append("Speed: " + velocity.length() + "\n");
        sb.append("Rot: " + this.turnAmount + "\n");
        sb.append("Position:" + this.getSpatialTranslation() +"\n");
        sb.append("Switch: " + this.charControl.isSwitch() + "\n");
        // sb.append("Air flip: " + this.charControl.??? + "\n");
        // sb.append("Air rot: " + this.charControl.??? + "\n");
        sb.append("Ground angle: " + this.getGroundNormal() + "\n");
        //sb.append("Rail: " + this.curRail + "\n");
        return sb.toString();
    }

    @Override
    public TrickList getTrick() {
        return this.charControl.getTrick();
    }
}
