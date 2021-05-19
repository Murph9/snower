package snower.base;

import java.util.LinkedList;
import java.util.Queue;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import snower.base.TrickDetector.TrickList;

public class SnowboarderControl extends BetterCharacterControl {

    private static final float MASS = 75;
    private static final float GRAV_FALLING = 20;
    private static final float GRAV_GROUND = 40;

    private static final float CRASH_TIME = 2;

    private static final float ROT_SPEED = 2.5f;
    private static final float SPIN_SPEED = 4.5f;
    private static final float DUCK_MOD = 1.4f;
    
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

    private Queue<TrickList> trickBuffer = new LinkedList<>();
    private TrickDetector detector;
    
    public SnowboarderControl() {
        super(0.5f, 1.8f, MASS);

        setGravity(new Vector3f(0, -GRAV_GROUND, 0));
        setJumpForce(new Vector3f(0, MASS*GRAV_FALLING/2, 0));
    }

    public TrickList getTrick() {
        return this.trickBuffer.poll();
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
                if (result != null && result.hasTricks()) {
                    this.trickBuffer.add(result);
                    if (!result.stillFacingTheSameWay())
                        switchStance = !switchStance;
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
            var dtAirFlip = tempAirFlipAmount*tpf*SPIN_SPEED/2;
            airRotAmount += dtAirRot;
            airFlipAmount += dtAirFlip;
            
            if (detector == null) {
                detector = new TrickDetector();
            }

            detector.update(dtAirRot, dtAirFlip);
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
        

        // calc acceleration
        var grav = this.getGravity(null);
        speed += tpf*-FastMath.sin(groundAngle)*grav.length(); //TODO changing gravity changes this too much
        
        // calc drag
        applyDrag(tpf);

        // apply speed
        if (this.isOnGround()) {
            this.setWalkDirection(dir.mult(speed));
        }
        // else this will just keep the speed from the last call

        super.update(tpf);
    }

    public void reset() {
        this.speed = 0;
        this.rotAmount = 0;
        this.airFlipAmount = 0;
        this.trickBuffer.clear();
    }

    public void turn(float amount) {
        tempRotAmount = amount;
    }

    public void flip(float amount) {
        tempAirFlipAmount = amount;
    }

    public void grab(String name) {
        if (name == null && detector == null)
            return;
        this.detector.grab(name);
    }

    public void stop(float amount) {
        slow = amount;
    }

    public Quaternion getViewRot() {
        return new Quaternion().fromAngleAxis(-groundAngle, Vector3f.UNIT_X);
    }

    public boolean isCrashing() {
        return this.crashing > 0;
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

    private float calcCharAngle() {
        var pos = getSpatial().getLocalTranslation();
        var dir = getSpatial().getWorldTransform().getRotation().mult(Vector3f.UNIT_Z).normalizeLocal();

        // 'predict' the location of the ground
        // TODO this could just happen all the time, just ignore weird large angles (like board height differences of more than 10m)
        if (!isOnGround()) {
            var findPos = Helper.findFirstPosDown(pos, 500, this.getRigidBody());
            if (findPos != null)
                pos = findPos;
        }

        // calc angle of char
        var frontPos = pos.add(dir).add(0, 1, 0);
        var rearPos = pos.subtract(dir).add(0, 1, 0);
        float frontHeight = Helper.findHeight(frontPos, 2, this.getRigidBody());
        float rearHeight = Helper.findHeight(rearPos, 2, this.getRigidBody());
        if (frontHeight != -1 && rearHeight != -1) {
            float diffheight = rearHeight - frontHeight;
            return FastMath.atan2(diffheight, 2);
        }

        return Float.NaN;
    }

    
    private void applyDrag(float tpf) {
        if (isOnGround())
            speed -= slow*tpf;
        
        float duckSpeed = this.isDucked() ? DUCK_MOD : 1;

        // TODO check method tobe 'actually' drag related once we like a speed

        // TODO this needs some better logic, see 'me' notes
        speed = Math.max(speed, 3 * duckSpeed);
        
        // TODO this is even more terrible
        speed = Math.min(speed, 45 * duckSpeed);

        if (crashing > 0) {
            speed = Math.min(speed, 5);
        }

        // TODO change switch stance when losing speed going up hill
    }
}
