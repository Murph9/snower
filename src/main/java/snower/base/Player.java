package snower.base;

import java.util.LinkedList;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import snower.service.Grab;
import snower.service.GrabListener;

public class Player extends AbstractAppState implements ActionListener {

    private static final ColorRGBA BASE_COLOUR = ColorRGBA.Blue;

    private final Main m;
    private final SnowboarderControl snower;
    private final Node playerNode;

    private final SnowTrail trail;

    private GrabListener grabListener;

    public Player(Main m) {
        this.m = m;
        this.playerNode = new Node("character node");
        this.snower = new SnowboarderControl();

        this.trail = new SnowTrail(m);
    }

    public SnowboarderControl getChar() {
        return snower;
    }

    public Node getCharNode() {
        return playerNode;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        // add player
        Spatial playerG = m.getAssetManager().loadModel("models/stick_snowboarder.obj");
        Material baseMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        baseMat.setColor("Color", BASE_COLOUR);
        playerG.setMaterial(baseMat);
        playerNode.attachChild(playerG);
        resetPos();
        
        playerNode.addControl(snower);
        playerNode.setShadowMode(ShadowMode.Cast);
        
        Main.physicsSpace.add(snower);
        m.getRootNode().attachChild(playerNode);

        m.getRootNode().attachChild(trail.getGeom());

        setupKeys();
    }

    private void setupKeys() {
        InputManager im = m.getInputManager();
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
        this.grabListener = new GrabListener(im, KeyInput.KEY_LSHIFT, grabs.toArray(new Grab[0]));
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
                snower.stop(value ? 1 : 0);
                snower.flip(0);
            } else {
                snower.flip(value ? -1 : 0);
                snower.stop(0);
            }
        } else if (binding.equals("Space")) {
            if (value) snower.jump();
        } else if (binding.equals("Reset")) {
            if (value) resetPos();
        }
    }

    private void resetPos() {
        var world = m.getStateManager().getState(WorldState.class);
        snower.warp(world.startPos());
        snower.reset();
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        Main.physicsSpace.remove(snower);
        m.getRootNode().detachChild(playerNode);
        m.getRootNode().detachChild(trail.getGeom());

        InputManager im = m.getInputManager();
        im.removeListener(this);

        grabListener.deregister(im);

        super.stateDetached(stateManager);
    }

    @Override
    public void update(float tpf) {
        if (snower.isOnGround()) {
            var extents = snower.getBoardExtents();
            trail.viewUpdate(tpf, extents);

            var points = Helper.getMinMaxX(extents);
            var debug = m.getStateManager().getState(DebugAppState.class);
            debug.drawBox("a0", ColorRGBA.Orange, points[0], 0.1f);
            debug.drawBox("a1", ColorRGBA.Orange, points[1], 0.1f);

            grabListener.landed();
        } else {
            trail.viewUpdate(tpf, null);

            var action = grabListener.getAction();
            if (action != null) {
                this.snower.grab(action.name);
            } else {
                this.snower.grab(null);
            }
        }

        super.update(tpf);
    }
}
