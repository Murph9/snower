package snower.base;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import snower.base.TrickDetector.TrickList;

public class SnowboarderControl extends BetterCharacterControl {

    // TODO write my own BetterCharacterControl :( sadly its getting in the way of custom stuff

    private static final float MASS = 75;
    private static final float GRAV_FALLING = 20;
    private static final float GRAV_GROUND = 40;

    private static final float CRASH_TIME = 2;
    private static final float SLOW_SPEED = 4;

    private static final float ROT_SPEED = 2.5f;
    private static final float SPIN_SPEED = 4.5f;
    private static final float DUCK_MOD = 1.4f;
    
    private final WorldState w;

    private float tempRotAmount;
    private float rotAmount;

    private float tempAirFlipAmount;
    private float airRotAmount;
    private float airFlipAmount;

    private float speed;
    private float slow;

    private float groundAngle;
    private boolean switchStance;
    private float crashing;

    private TrickList curTrick; // stores the current trick, or the last trick
    private TrickDetector detector;
    
    public SnowboarderControl(WorldState w) {
        super(0.5f, 1.8f, MASS);

        this.w = w;

        setGravity(new Vector3f(0, -GRAV_GROUND, 0));
        setJumpForce(new Vector3f(0, MASS*GRAV_FALLING/2, 0));
        setPhysicsDamping(0.2f);
    }

    @Override
    public void setSpatial(Spatial newSpatial) {
        super.setSpatial(newSpatial);
        resetPos();
    }
    
    public void resetPos() {
        warp(w.startPos());
    }

    public TrickList getTrick() {
        return curTrick;
    }

    public boolean isSwitch() {
        return this.switchStance;
    }

    @Override
    public void jump() {
        if (!isCrashing())
            super.jump();
    }

    private void toggleSwitch() {
        this.switchStance = !this.switchStance;
        this.groundAngle = -this.groundAngle;
    }

    @Override
    public void update(float tpf) {
        if (isOnGround()) {
            // stick character to the ground to prevent annoying jumping
            // also ignore the BetterCharacterControl gravity setting, its confusingly broken here
            getRigidBody().setGravity(new Vector3f(0, -GRAV_GROUND, 0));
            
            rotAmount += tempRotAmount*tpf*ROT_SPEED;
            
            if (detector != null) {
                var result = detector.stop();
                if (result.hasTricks()) {
                    this.curTrick = result;
                    if (!result.landedSwitch()) {
                        toggleSwitch();
                    }
                }
                detector = null;

                // trigger landing things
                airRotAmount = 0;
                airFlipAmount = 0;
                
                if (result.failed) {
                    crashing = CRASH_TIME;
                }
            }
        } else {
            getRigidBody().setGravity(new Vector3f(0, -GRAV_FALLING, 0));

            var dtAirRot = tempRotAmount*tpf*SPIN_SPEED;
            var dtAirFlip = tempAirFlipAmount*tpf*SPIN_SPEED;
            airRotAmount += dtAirRot;
            airFlipAmount += dtAirFlip;
            
            if (detector == null) {
                detector = new TrickDetector();
            }

            detector.update(dtAirRot, dtAirFlip);

            var result = detector.progress();
            if (result != null & result.hasTricks()) {
                curTrick = result;
            }
        }
        
        if (crashing > 0)
            crashing -= tpf;

        var dir = Quaternion.IDENTITY.fromAngles(0, rotAmount+airRotAmount, 0).mult(Vector3f.UNIT_X);
        setViewDirection(dir);
 
        // calc angle of ground
        var newGroundAngle = calcCharAngle();
        if (Float.isNaN(newGroundAngle)) //i.e. too far from slope to find it
            newGroundAngle = 0;
        groundAngle = FastMath.interpolateLinear(10*tpf, groundAngle, newGroundAngle);
        
        // set angle of character Node based on the floor angle
        Quaternion rot;
        if (this.switchStance) {
            rot = new Quaternion().fromAngles(- airFlipAmount + groundAngle, FastMath.PI, 0);
        } else {
            rot = new Quaternion().fromAngles(airFlipAmount - groundAngle, 0, 0);
        }
        ((Node)getSpatial()).getChild(0).setLocalRotation(rot); // TODO hack to get the physical char rotated to match slope
        
        // calc drag
        applyDrag(tpf);

        // calc acceleration
        speed += tpf * accelFromSlope();

        // apply speed
        if (this.isOnGround()) {
            // swap direction if trying to go up a slope
            if (speed < 0) {
                speed = 0;
                this.rotAmount += FastMath.PI;
                toggleSwitch();
            }

            this.setWalkDirection(dir.mult(speed));
        }
        // else this will just keep the speed from the last call

        super.update(tpf);
    }

    public void reset() {
        this.speed = 0;
        this.rotAmount = 0;
        this.airFlipAmount = 0;
        this.curTrick = null;
    }

    public void turn(float amount) {
        tempRotAmount = amount;
    }

    public void flip(float amount) {
        tempAirFlipAmount = amount;
    }

    public void grab(String name) {
        if (detector == null)
            return;
        this.detector.grab(name);
    }

    public void slow(float amount) {
        slow = amount;
    }

    public Quaternion getViewRot() {
        return new Quaternion().fromAngleAxis(-groundAngle, Vector3f.UNIT_X);
    }

    public boolean isCrashing() {
        return this.crashing > 0;
    }
    public boolean isSlowing() {
        return this.slow > 0.2f;
    }
    public boolean isGrabbing() {
        return this.detector.inGrab();
    }
    public String getGrab() {
        return this.detector.curGrab();
    }

    public Vector3f[] getBoardExtents() {
        final float BOARD_WIDTH = 0.15f;
        final float BOARD_LENGTH = 1.55f;

        var dir = this.getViewDirection().normalize();
        dir.multLocal(BOARD_LENGTH/2);
        var pos = this.getSpatialTranslation();
        var worldRot = ((Node)this.getSpatial()).getChild(0).getWorldRotation(); //TODO any getChild(0) reference is a hack
        var extents = new Vector3f[] {
            worldRot.mult(Vector3f.UNIT_Z.clone().addLocal(BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().addLocal(-BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().negateLocal().addLocal(BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().negateLocal().addLocal(-BOARD_WIDTH, 0, 0)).addLocal(pos)
        };
        return extents;
    }

    private float accelFromSlope() {
        var grav = this.getGravity(null);
        return -FastMath.sin(groundAngle)*grav.length(); // TODO changing gravity changes this too much
    }

    private float calcCharAngle() {
        var dir = getSpatial().getWorldTransform().getRotation().mult(Vector3f.UNIT_Z).normalizeLocal();

        // 'predict' the location of the ground (by doing my own ray cast)
        var pos = getSpatial().getWorldTranslation();
        var findPos = Helper.findFirstPosDown(pos.add(0, 1, 0), 500, this.getRigidBody());
        if (findPos != null)
            pos = findPos;

        // calc angle of char
        var frontPos = pos.add(dir).add(0, 1, 0);
        var rearPos = pos.subtract(dir).add(0, 1, 0);
        float frontHeight = Helper.findHeight(frontPos, 2, this.getRigidBody());
        float rearHeight = Helper.findHeight(rearPos, 2, this.getRigidBody());

        if (frontHeight != -1 && rearHeight != -1) {
            float diffheight = rearHeight - frontHeight;
            if (Math.abs(diffheight) > 4) //ignore weird large angles (like board height differences of more than 4m)
                return Float.NaN;
            return FastMath.atan2(diffheight, 2);
        }

        return Float.NaN;
    }

    
    private void applyDrag(float tpf) {
        if (isOnGround() && slow > 0) {
            speed -= SLOW_SPEED*slow*tpf;
            if (speed < 0) {
                speed = 0.25f; // you can't stop completely, fixes annoying flipping direction issues
            }
            return;
        }

        // TODO do this with drag
        float maxSpeed = 45 * (this.isDucked() ? DUCK_MOD : 1);
        speed = Math.min(speed, maxSpeed);

        if (crashing > 0) {
            speed = Math.min(speed, 5); // while crashing prevent going faster than 5
        }
    }

    public String getDebugStr() {
        var sb = new StringBuilder();
        sb.append("Speed: " + getVelocity().length() + "\n");
        sb.append("Ground char Speed: " + this.speed + "\n");
        return sb.toString();
    }
}
