package snower.base;

import java.util.LinkedList;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;

import snower.service.Grab;
import snower.service.GrabListener;

public class PlayerInputs implements ActionListener {

    private final InputManager im;
    private final SnowboarderControl snower;
    private final Node playerNode;

    private GrabListener grabListener;

    public PlayerInputs(InputManager im, SnowboarderControl snower) {
        this.im = im;
        this.snower = snower;

        this.playerNode = new Node("character node");
        this.playerNode.setShadowMode(ShadowMode.Cast);

        setupKeys();
    }

    private void setupKeys() {
        im.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));

        im.addListener(this,"Lefts", "Rights", "Ups", "Downs", "Space", "Reset");

        var grabs = new LinkedList<Grab>();
        grabs.add(new Grab(KeyInput.KEY_1, "DownGrab"));
        grabs.add(new Grab(KeyInput.KEY_2, "UpGrab"));
        this.grabListener = new GrabListener(this, im, KeyInput.KEY_LSHIFT, grabs.toArray(new Grab[0]));
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
            if (value) snower.jump();
        } else if (binding.equals("Reset")) {
            if (value) snower.resetPos();
        }
    }

    public void remove() {
        im.removeListener(this);

        grabListener.deregister(im);
    }

    public void grabbed(Grab action) {
        var grabName = action == null ? null : action.name;
        snower.grab(grabName);
    }
}