package snower.player;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;

import snower.player.GrabMapper.GrabEnum;
import snower.player.TrickDetector.TrickList;

public class AirSnowControl {

    private static final float SPIN_SPEED = 4.5f;
    private static final float FLIP_SPEED = 3.5f;
    private static final float CRASH_TIME = 1.5f;

    private final FloatingSnowControl control;
    private final SnowboarderAnimControl animControl;
    private Spatial controlNode;

    private TrickDetector trickDetector;
    private TrickList curTrick;

    private float dtSpinAmount;
    private float spinAmount;
    private float dtFlipAmount;
    private float flipAmount;

    private boolean isSwitch;
    private float crashingTimer;
    private String crashingReason; // Enum?

    public AirSnowControl(FloatingSnowControl control) {
        this.control = control;

        animControl = new SnowboarderAnimControl(FloatingSnowControl.class);
    }

    public void setSpatial(Spatial child) {
        this.controlNode = child;

        controlNode.addControl(animControl);
    }

    public boolean isCrashing() {
        return crashingTimer > 0;
    }

    public String crashingReason() {
        return crashingReason;
    }

    public boolean isSwitch() {
        return isSwitch;
    }

    public void update(float tpf) {
        if (crashingTimer > 0)
            crashingTimer -= tpf;

        if (control.isOnGround()) {
            // end any trick
            if (trickDetector != null) {
                this.curTrick = trickDetector.stop();
                if (curTrick.switchedStance()) {
                    this.isSwitch = !this.isSwitch;
                }

                trickDetector = null;
                flipAmount = 0;
                spinAmount = 0;

                if (this.curTrick.failed()) {
                    crashingTimer = CRASH_TIME;
                    crashingReason = this.curTrick.failedReason;
                }
            }
            
            this.spinAmount = 0;
            this.flipAmount = 0;
        } else {
            // start tricks
            if (trickDetector == null)
                trickDetector = new TrickDetector();

            var dtAirRot = dtSpinAmount * tpf * SPIN_SPEED;
            var dtAirFlip = dtFlipAmount * tpf * FLIP_SPEED;
            spinAmount += dtAirRot;
            flipAmount += dtAirFlip;

            curTrick = trickDetector.update(dtAirRot, dtAirFlip);
        }

        var dir = Quaternion.IDENTITY.fromAngles(0, spinAmount, -flipAmount);
        this.controlNode.setLocalRotation(dir);

        if (flipAmount != 0) {
            var height = 1.8f; // TODO do we have a height?
            var diffheight = height * (-FastMath.cos(flipAmount) + 1) / 2;
            this.controlNode.setLocalTranslation(0, diffheight, 0);
        } else {
            this.controlNode.setLocalTranslation(0, 0, 0);
        }

        this.controlNode.setLocalScale(1,1,this.isSwitch ? -1 : 1);
    }

    public void grab(GrabEnum type) {
        if (trickDetector == null)
            return;
        trickDetector.grab(type);
    }

    public void spin(float amount) {
        dtSpinAmount = amount;
        // TODO only allow spinning while we are 'some' distance off the ground
        // rational is that you as the player can't really tell how far it is
    }

    public void flip(float amount) {
        dtFlipAmount = amount;
        // TODO only allow flipping while we are 'some' distance off the ground
    }

    public TrickList getTrick() {
        return curTrick;
    }

    public boolean isGrabbing() {
        return this.trickDetector.inGrab();
    }

    public GrabEnum getGrab() {
        return this.trickDetector.curGrab();
    }
}