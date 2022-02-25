package snower.player;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;

public class PlayerInputs implements ActionListener {

    private static final float MAX_JUMP_TIME = 0.4f;

    private final InputManager im;
    private final ISnowControl snower;
    private final SnowboarderRailDetector railDetector;

    private GrabListener grabListener;
    private long jumpTimer;

    public PlayerInputs(InputManager im, ISnowControl snower, SnowboarderRailDetector railDetector) {
        this.im = im;
        this.snower = snower;
        this.railDetector = railDetector;

        setupKeys();
    }

    private void setupKeys() {
        im.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        im.addMapping("Rail", new KeyTrigger(KeyInput.KEY_E));
        im.addListener(this,"Lefts", "Rights", "Ups", "Downs", "Space", "Reset", "Rail");

        this.grabListener = new GrabListener(this, im, KeyInput.KEY_LSHIFT);
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            snower.turn(value ? 1 : 0);
        } else if (binding.equals("Rights")) {
            snower.turn(value ? -1 : 0);
        } else if (binding.equals("Ups")) {
            if (snower.isOnGround()) {
                snower.setDucked(value);
                snower.flip(0);
            } else {
                snower.flip(value ? 1 : 0);
                snower.setDucked(false);
            }
        } else if (binding.equals("Downs")) {
            if (snower.isOnGround()) {
                snower.slow(value ? 1 : 0);
                snower.flip(0);
            } else {
                snower.flip(value ? -1 : 0);
                snower.slow(0);
            }
        } else if (binding.equals("Space")) {
            if (!value) {
                jumpTimer = System.currentTimeMillis() - jumpTimer;
                // match range [0 - MAX_JUMP_TIME] => [min - 1]
                var scaledValue = Math.min(jumpTimer/1000f, MAX_JUMP_TIME)/MAX_JUMP_TIME;
                snower.jump(FastMath.extrapolateLinear(scaledValue, 0.65f, 1));
                snower.setDucked(false);
            } else {
                jumpTimer = System.currentTimeMillis();
                snower.setDucked(true);
            }
        } else if (binding.equals("Reset")) {
            if (value) snower.reset();
        } else if (binding.equals("Rail")) {
            railDetector.setEnabled(value);
            if (!value)
                this.snower.finishRail(true);
        }
    }

    public void remove() {
        im.removeListener(this);

        grabListener.deregister(im);
    }

    public void grabbed(float ud, float lr) {
        var grabDir = GrabMapper.getGrabFrom(ud, lr, snower.isSwitch());
        snower.grab(grabDir);
    }
}
