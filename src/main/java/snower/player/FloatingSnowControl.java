package snower.player;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import snower.player.GrabMapper.GrabEnum;
import snower.player.TrickDetector.TrickList;
import snower.service.Helper;
import snower.world.IWorld;

public class FloatingSnowControl extends FloatingControl implements ISnowControl {
    private static final Quaternion ROT_Y_AXIS = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);

    private static final float MASS = 80;
    private static final float JUMP_FORCE = 5.5f;
    private static final float DUCK_SPEED = 1.2f;
    private static final float SLOPE_TURN_RATE = 0.25f;
    private static final float SLOW_RATE = 12;

    private final IWorld w;
    private final Vector3f jumpForce;
    private final AirSnowControl airControl;

    private Vector3f forward;
    private Vector3f side;

    private float turnAmount;
    private boolean ducked;
    private float slowed;
    
    public FloatingSnowControl(IWorld w) {
        super(MASS);
        this.w = w;
        jumpForce = new Vector3f(0, rigidBody.getMass() * JUMP_FORCE, 0);

        this.airControl = new AirSnowControl(this);
    }

    @Override
    public void setSpatial(Spatial newSpatial) {
        super.setSpatial(newSpatial);

        // if this fails its because the floatingcontrol didn't fix it
        this.airControl.setSpatial(((com.jme3.scene.Node)charSpatial).getChild(0));
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        super.prePhysicsTick(space, tpf);

        var normal = super.getGroundNormal();
        if (normal != null) {
            var mass = rigidBody.getMass();
            var gravityY = Math.abs(rigidBody.getGravity(null).y);
            var speed = velocity.length();

            // calc forward vector (ignoring vertical component)
            forward = rigidBody.getLinearVelocity().normalize();
            side = ROT_Y_AXIS.mult(forward);

            float projVel = super.getGroundNormal().dot(Helper.getXZNormalized(forward));
            float projSlip = super.getGroundNormal().dot(Helper.getXZNormalized(side));

            float duckOffset = ducked ? DUCK_SPEED : 1;

            // a gravity force from the combined direction, Mass, gravity and time step (not technically correct math here)
            rigidBody.applyCentralImpulse(forward.mult(duckOffset * gravityY * mass * projVel * tpf));

            if (speed > 3) {
                // rotate character naturally down the slope
                rigidBody.applyCentralImpulse(side.mult(SLOPE_TURN_RATE * gravityY * mass * projSlip * tpf));
                // NOTE: add turning drag so this doesn't add speed
            }
            
            if (!FastMath.approximateEquals(slowed, 0)) {
                // apply manual drag
                float speedReduction = slowed*SLOW_RATE;
                rigidBody.applyCentralForce(velocity.normalize().multLocal(-gravityY*speed*speedReduction*mass*tpf));
            } else {
                // apply natural drag
                rigidBody.applyCentralForce(velocity.normalize().multLocal(-gravityY*speed*0.75f*mass*tpf));
            }

            if (turnAmount != 0) {
                var vel = Math.min(velocity.length(), 5)/5f; // prevent high speed turning at slow speeds
                rigidBody.applyCentralImpulse(side.mult(-turnAmount * vel * gravityY * mass * tpf));
            }
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

        this.airControl.update(tpf);
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
        this.turnAmount = amount;
        this.airControl.spin(amount);
    }

    @Override
    public void jump(float amount) {
        if (isOnGround())
            this.rigidBody.applyCentralImpulse(jumpForce.mult(amount));
    }

    @Override
    public void slow(float amount) {
        slowed = amount;
    }

    @Override
    public void flip(float amount) {
        if (this.isOnGround())
            this.airControl.flip(0);
        else
            this.airControl.flip(amount);
    }

    @Override
    public void reset() {
        setPhysicsLocation(w.startPos());
        rigidBody.setLinearVelocity(new Vector3f());
    }

    @Override
    public void setDucked(boolean value) {
        if (this.isOnGround())
            ducked = value;
    }

    @Override
    public void finishRail(boolean value) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isSwitch() {
        return this.airControl.isSwitch();
    }

    @Override
    public boolean isCrashing() {
        return this.airControl.isCrashing();
    }

    @Override
    public void grab(GrabEnum type) {
        this.airControl.grab(type);
    }

    @Override
    public String getDebugStr() {
        var sb = new StringBuilder();
        sb.append("On Ground: " + isOnGround() + "\n");
        sb.append("Crashing: " + (this.airControl.isCrashing() ? "true " + this.airControl.crashingReason() : "false") + "\n");
        sb.append("Speed: " + velocity.length() + "\n");
        sb.append("Rot: " + this.turnAmount + "\n");
        sb.append("Position:" + this.getSpatialTranslation() +"\n");
        sb.append("Switch: " + this.airControl.isSwitch() + "\n");
        // sb.append("Air flip: " + this.airControl.??? + "\n");
        // sb.append("Air rot: " + this.airControl.??? + "\n");
        sb.append("Ground angle: " + this.getGroundNormal() + "\n");
        //sb.append("Rail: " + this.curRail + "\n");
        sb.append("Slowing: " + this.slowed + "\n");
        sb.append("Ducked: " + this.ducked + "\n");
        return sb.toString();
    }

    @Override
    public TrickList getTrick() {
        return this.airControl.getTrick();
    }

    @Override
    public boolean isDucked() {
        return this.ducked;
    }

    @Override
    public boolean isSlowing() {
        return !FastMath.approximateEquals(this.slowed, 0);
    }

    @Override
    public boolean isGrabbing() {
        return this.airControl.isGrabbing();
    }

    @Override
    public GrabEnum getGrab() {
        return this.airControl.getGrab();
    }
}
