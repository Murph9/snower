package snower.player;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import snower.world.IWorld;
import snower.world.RailPath;
import snower.player.GrabMapper.GrabEnum;
import snower.player.TrickDetector.TrickList;
import snower.service.Helper;

public class SnowboarderControl extends ControlBase implements ISnowControl {

    private static final float MASS = 75;
    private static final float GRAV_FALLING = 15;
    private static final float GRAV_GROUND = 20;

    private static final float DRAG_CONST = 0.015f;
    private static final float DUCK_DRAG_CONST = 0.004f;
    private final float CalculatedMaxSpeed;

    private static final float CRASH_TIME = 2;
    private static final float RAIL_NO_TIME = 2;

    private static final float ROT_SPEED = 2.5f;
    private static final float SLOW_DOWN_SPEED = 50;

    private static final float SPIN_SPEED = 4.5f;
    private static final float FLIP_SPEED = 3.5f;

    private final IWorld w;

    private float tempRotAmount;
    private float rotAmount;

    private float tempAirFlipAmount;
    private float airRotAmount;
    private float airFlipAmount;

    private float speed;
    private float slow;

    private final Vector3f groundAngles;

    private float switchStanceTimeout;
    private boolean switchStance;
    
    private float crashing;
    private String crashedReason;

    private TrickList curTrick; // stores the current trick, or the last trick
    private TrickDetector detector;
    
    private float railTimeout;
    private RailPath curRail;
    private float railRotAmount;

    public SnowboarderControl(IWorld w) {
        super(0.5f, 1.8f, MASS);

        this.w = w;
        this.groundAngles = new Vector3f();

        setGravity(new Vector3f(0, -GRAV_GROUND, 0));
        setJumpForce(new Vector3f(0, MASS*GRAV_FALLING/2, 0));
        setPhysicsDamping(0.2f);

        this.CalculatedMaxSpeed = FastMath.sqrt(FastMath.sin(FastMath.DEG_TO_RAD*30)*GRAV_GROUND/(DRAG_CONST-DUCK_DRAG_CONST));
    }

    @Override
    public void setSpatial(Spatial newSpatial) {
        super.setSpatial(newSpatial);

        // validate some assumptions made in the class, to protect all the uses of .getChild(0)
        assert newSpatial instanceof Node;
        var children = ((Node)newSpatial).getChildren();
        assert children.size() == 1;
        assert children.get(0) instanceof Spatial;

        reset();
    }
    
    @Override
    public void reset() {
        warp(w.startPos());
        
        this.getRigidBody().setLinearVelocity(new Vector3f());
        this.getRigidBody().setAngularVelocity(new Vector3f());
        
        this.speed = 0;
        this.rotAmount = 0;
        this.crashing = 0;
        this.airRotAmount = 0;
        this.airFlipAmount = 0;
        this.curTrick = null;
        this.curRail = null; // ignore any active rail
        this.detector = null; //ignore any active trick
    }

    public TrickList getTrick() {
        return curTrick;
    }

    public boolean isSwitch() {
        return this.switchStance;
    }

    @Override
    public void jump(float amount) {
        if (!isCrashing() || curRail != null)
            super.jump();
    }

    public void finishRail(boolean withJump) {
        if (this.curRail != null) {
            this.curRail = null;
            this.railTimeout = RAIL_NO_TIME;

            this.airRotAmount = railRotAmount;
            railRotAmount = 0;

            if (withJump)
                this.jump();

            detector.finishRail();
        }
    }

    private void landTrick() {
        if (detector == null)
            return;

        this.curTrick = detector.stop();
        if (curTrick.switchedStance()) {
            toggleSwitch();
        }
        detector = null;

        // trigger landing things
        airRotAmount = 0;
        airFlipAmount = 0;

        if (this.curTrick.failed()) {
            crashing = CRASH_TIME;
            crashedReason = this.curTrick.failedReason;
        }
    }

    /**Toggle riding switch, returns if success */
    private boolean toggleSwitch() {
        if (this.switchStanceTimeout > 0)
            return false;
        
        this.switchStance = !this.switchStance;
        this.switchStanceTimeout = 1;
        return true;
    }

    @Override
    protected void checkOnGround() {
        if (curRail == null) // no ground checks while on a rail
            super.checkOnGround();
    }

    @Override
    public void update(float tpf) {
        if (switchStanceTimeout > 0)
            switchStanceTimeout -= tpf;

        if (railTimeout > 0)
            railTimeout -= tpf;

        if (curRail != null) {
            getRigidBody().setGravity(new Vector3f(0, 0, 0));
            
            var pos = this.getRigidBody().getPhysicsLocation();
            this.getRigidBody().setLinearVelocity(new Vector3f());

            // calc position based on location
            var newPos = curRail.getClosestPos(pos);
            if (newPos == null || newPos == curRail.end) {
                this.finishRail(false);
            } else {
                // set rot to be the direction of the rail
                this.setWalkDirection(curRail.end.subtract(curRail.start).normalize().mult(speed));
                setPhysicsLocation(newPos);
            }
        
        } else if (isOnGround()) {
            // stick character to the ground to prevent annoying jumping
            // also ignore the BetterCharacterControl gravity setting, its confusingly broken here
            getRigidBody().setGravity(new Vector3f(0, -GRAV_GROUND, 0));
            
            rotAmount += tempRotAmount*tpf*ROT_SPEED;
            
            landTrick();
        } else {
            getRigidBody().setGravity(new Vector3f(0, -GRAV_FALLING, 0));

            if (detector == null) {
                //i.e. just got off the ground
                detector = new TrickDetector();
            }

            var dtAirRot = tempRotAmount*tpf*SPIN_SPEED;
            var dtAirFlip = tempAirFlipAmount*tpf*FLIP_SPEED;

            if (tempRotAmount == 0) {
                // i.e. no input, attempt to snap to a multiple of PI to land successfully if its close to one
                var closest = Math.round(airRotAmount/FastMath.PI) * FastMath.PI;
                var diff = closest - airRotAmount;
                if (Math.abs(diff) < FastMath.PI*3/8) {
                    var sign = Math.signum(diff);
                    if (sign*diff > sign*tpf*SPIN_SPEED)
                        dtAirRot += sign*tpf*SPIN_SPEED;
                }
            }

            if (tempAirFlipAmount == 0) {
                var closest = Math.round(airFlipAmount/FastMath.TWO_PI) * FastMath.TWO_PI;
                var diff = closest - airFlipAmount;
                if (Math.abs(diff) < FastMath.TWO_PI*3/8) {
                    var sign = Math.signum(diff);
                    if (sign*diff > sign*tpf*FLIP_SPEED)
                        dtAirFlip += sign*tpf*FLIP_SPEED;
                }
            }

            airRotAmount += dtAirRot;
            airFlipAmount += dtAirFlip;
            
            var result = detector.update(dtAirRot, dtAirFlip);
            if (result != null & result.hasTricks()) {
                curTrick = result;
            }
        }
        
        if (crashing > 0)
            crashing -= tpf;

        wallCollisionDetection();

        var dir = Quaternion.IDENTITY.fromAngles(0, rotAmount + airRotAmount + railRotAmount, 0).mult(Vector3f.UNIT_X);
        setViewDirection(dir);
 
        // calc angle of ground
        var newGroundAngles = getCharAngles();
        groundAngles.interpolateLocal(newGroundAngles, 10*tpf);

        // char center pos is at feet - prevent weird rotation by moving the center of the character dynamically while in air
        if (airFlipAmount != 0) {
            var height = this.getFinalHeight();
            var diffheight = height * (-FastMath.cos(airFlipAmount) + 1) / 2;
            var diffZ = height * -FastMath.sin(airFlipAmount) / 2;
            ((Node)getSpatial()).getChild(0).setLocalTranslation(0, diffheight, diffZ);
        } else {
            ((Node)getSpatial()).getChild(0).setLocalTranslation(0, 0, 0);
        }
        
        // set angle of character Node based on the floor angle
        var rot = new Quaternion().fromAngles(airFlipAmount - groundAngles.x, 0, groundAngles.z);
        ((Node)getSpatial()).getChild(0).setLocalRotation(rot);
        
        ((Node)getSpatial()).getChild(0).setLocalScale(1, 1, this.switchStance ? 1 : -1); // switch allows mirroring of moves
        
        // calc drag
        applyDrag(tpf);

        // calc acceleration
        if (isOnGround())
            speed += tpf * FastMath.sin(groundAngles.x)*this.getGravity(null).y;

        // apply speed
        if (this.isOnGround()) {
            // swap direction if trying to go up a slope
            if (speed < 0) {
                if (toggleSwitch()) {
                    speed = 0;
                    this.groundAngles.setZ(-this.groundAngles.z);
                    this.rotAmount += FastMath.PI;
                }
            }

            this.setWalkDirection(dir.mult(speed));
        } else if (this.isCrashing()) {
            this.setWalkDirection(dir.mult(speed));
        }   
        // else this will just keep the speed from the last call

        super.update(tpf);
    }

    public void turn(float amount) {
        tempRotAmount = amount;
    }

    public void flip(float amount) {
        tempAirFlipAmount = amount;
    }

    public void grab(GrabMapper.GrabEnum grabDir) {
        if (detector == null)
            return;
        this.detector.grab(grabDir);
    }

    public void slow(float amount) {
        slow = amount;
    }

    public boolean isCrashing() {
        return this.crashing > 0;
    }
    public boolean isSlowing() {
        return this.slow > 0.2f;
    }
    public boolean isGrabbing() {
        if (this.detector == null)
            return false;
        return this.detector.inGrab();
    }
    public GrabEnum getGrab() {
        return this.detector.curGrab();
    }

    public Vector3f[] getExpectedBoardExtents() {
        // TODO calc this in anim control with nodes on the actual character

        final float BOARD_WIDTH = 0.15f;
        final float BOARD_LENGTH = 1.55f;

        var dir = this.getViewDirection().normalize();
        dir.multLocal(BOARD_LENGTH/2);
        var pos = this.getSpatialTranslation();
        var worldRot = ((Node)this.getSpatial()).getChild(0).getWorldRotation();
        var extents = new Vector3f[] {
            worldRot.mult(Vector3f.UNIT_Z.clone().addLocal(BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().addLocal(-BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().negateLocal().addLocal(BOARD_WIDTH, 0, 0)).addLocal(pos),
            worldRot.mult(Vector3f.UNIT_Z.clone().negateLocal().addLocal(-BOARD_WIDTH, 0, 0)).addLocal(pos)
        };
        return extents;
    }

    private Vector3f getCharAngles() {
        // 'predict' the location of the ground (by doing my own ray cast)
        var pos = getSpatial().getWorldTranslation();
        var findPos = Helper.findFirstPosDown(pos.add(0, 1, 0), 500, this.getRigidBody());
        if (findPos != null)
            pos = findPos;

        // get the angle in both x and z directions
        var rot = getSpatial().getWorldTransform().getRotation();
        var angle = calcCharAngle(pos, rot.mult(Vector3f.UNIT_Z));
        if (Float.isNaN(angle))
            angle = 0;
        var latAngle = calcCharAngle(pos, rot.mult(Vector3f.UNIT_X));
        if (Float.isNaN(latAngle))
            latAngle = 0;

        return new Vector3f(angle, 0, latAngle);
    }

    private float calcCharAngle(Vector3f pos, Vector3f dir) {
        // calc angle of char
        var frontPos = pos.add(dir).add(0, 1, 0);
        var rearPos = pos.subtract(dir).add(0, 1, 0);
        float frontHeight = Helper.findHeight(frontPos, 2, this.getRigidBody());
        float rearHeight = Helper.findHeight(rearPos, 2, this.getRigidBody());

        if (frontHeight != -1 && rearHeight != -1) {
            float diffheight = rearHeight - frontHeight;
            if (Math.abs(diffheight) > 2) //ignore weird large angles (like board height differences of more than 4m)
                return Float.NaN;
            return FastMath.atan2(diffheight, 2);
        }

        return Float.NaN;
    }

    
    private void applyDrag(float tpf) {
        if (isOnGround() && slow > 0) {
            speed -= SLOW_DOWN_SPEED*slow*tpf;
            if (speed < 0.25f) {
                speed = 0.25f; // you can't stop completely, fixes annoying flipping direction issues
            }
            return;
        }

        // apply drag (quadratic version) but as a ground related drag (making jumping faster?)
        if (isOnGround()) {
            var duckDrag = this.isDucked() ? DUCK_DRAG_CONST : 0;
            var drag = speed*speed*(DRAG_CONST - duckDrag)*tpf;
            if (drag > speed) {
                speed = 0;
            } else {
                speed -= drag/2;
            }

            speed = Math.max(speed, 2);
        }

        if (isCrashing()) {
            speed = Math.min(speed, 5); // while crashing prevent going faster than 5
        }
    }

    private void wallCollisionDetection() {
        // TODO calc collision direction from walkdir and difference in speed
        
        var vel = getVelocity();
        if (!isCrashing() && vel.length() < speed*0.5f) {
            speed = 1;
            rotAmount += FastMath.PI;
            crashing = CRASH_TIME;
            crashedReason = "Hit wall";
            // TODO minor crashing

            if (!isOnGround()) {
                // TODO bad crashing
                
            }
        }

        // very primitive, need to 'bounce' off of walls
    }

    public String getDebugStr() {
        var sb = new StringBuilder();
        sb.append("On Ground: " + isOnGround() + "\n");
        sb.append("Crashing: " + (isCrashing() ? "true " + crashedReason : "false") + "\n");
        sb.append("(Max:" + Math.round(CalculatedMaxSpeed*100f)/100 + ") Ground speed: " + this.speed + "\n");
        sb.append("Speed: " + getVelocity().length() + ", walk dir: " + getWalkDirection(null) + "\n");
        sb.append("Rot: " + this.rotAmount + "\n");
        sb.append("Position:" + this.getSpatialTranslation() +"\n");
        sb.append("Switch: " + this.switchStance + "\n");
        sb.append("Air flip: " + this.airFlipAmount + "\n");
        sb.append("Air rot: " + this.airRotAmount + "\n");
        sb.append("Ground angle: " + this.groundAngles + "\n");
        sb.append("Rail: " + this.curRail + "\n");
        return sb.toString();
    }

    public void getOnRail(RailPath path) {
        if (isCrashing())
            return; //can't rail while crashing
        if (railTimeout > 0)
            return; // still jumping from the previous rail
        if (this.curRail == path)
            return;

        if (detector != null && detector.inGrab()) {
            this.crashing = CRASH_TIME;
            this.crashedReason = "In-grab and hit rail";
            return;
        }

        System.out.println("A rail: " + path);

        curRail = path;

        railRotAmount = this.airRotAmount;
        this.airRotAmount = 0;

        // calculate rail direction and set that to my current direction
        Vector2f railDirXZ = new Vector2f(path.getRailDirection().x, path.getRailDirection().z);
        this.rotAmount = -railDirXZ.getAngle();

        if (detector == null)
            detector = new TrickDetector();
        
        detector.startRail();
    }
}
