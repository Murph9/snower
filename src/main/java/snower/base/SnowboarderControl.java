package snower.base;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class SnowboarderControl extends BetterCharacterControl {

    private static final float ROT_SPEED = 2.5f;
    
    private float tempRotAmount;
    private float rotAmount;
    
    private float speed;
    private Quaternion viewRot;

    public SnowboarderControl() {
        super(0.5f, 1.8f, 75);

        viewRot = new Quaternion();
    }

    @Override
    public void update(float tpf) {
        rotAmount += tempRotAmount*tpf*ROT_SPEED;

        var dir = Quaternion.IDENTITY.fromAngles(0, rotAmount, 0).mult(Vector3f.UNIT_X);
        setViewDirection(dir);

        // calc angle of ground
        var groundAngle = calcCharAngle();
        if (Float.isNaN(groundAngle)) //i.e. too far from slope to find it
            groundAngle = 0; // TODO smooth this and predict landing slope after jump
        
        // set angle of character Node based on the floor angle
        this.viewRot = new Quaternion().fromAngleAxis(-groundAngle, Vector3f.UNIT_X);
        ((Node)getSpatial()).getChild(0).setLocalRotation(viewRot); // hack to get the physical char
    
        // calc acceleration
        var grav = this.getGravity(null);
        
        speed += tpf*-FastMath.sin(groundAngle)*grav.length();
        System.out.println(groundAngle + " | " + speed);
        // calc drag
        speed = applyDrag(speed);

        // apply speed
        this.setWalkDirection(dir.mult(speed));

        super.update(tpf);
    }


    public void turn(float amount) {
        tempRotAmount = amount;
    }

    public void stop(float amount) {

        // TODO set for higher drag
        speed = 0;
    }

    public Quaternion getViewRot() {
        return viewRot;
    }

    private float calcCharAngle() {
        var pos = getSpatial().getLocalTranslation();
        var dir = this.getWalkDirection(null).normalizeLocal();

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

    
    private static float applyDrag(float speed) {
        // TODO this is terrible
        if (speed > 30) {
            speed = 30;
        }
        // TODO this is even more terrible
        if (speed < 1) {
            speed = 1;
        }

        return speed;
    }
}
