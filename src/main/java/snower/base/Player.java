package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Player extends AbstractAppState implements ActionListener {

    private final Main m;
    private final SnowboarderControl snower;
    private final Node playerNode;

    private final SnowTrail trail;

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
    public void initialize(AppStateManager stateManager, Application app) {
        // add player
        Spatial playerG = m.getAssetManager().loadModel("models/stick_snowboarder.obj");
        Material baseMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        baseMat.setColor("Color", ColorRGBA.Blue);
        playerG.setMaterial(baseMat);
        playerNode.attachChild(playerG);
        resetPos();
        
        playerNode.addControl(snower);
        playerNode.setShadowMode(ShadowMode.Cast);
        
        Main.physicsSpace.add(snower);
        m.getRootNode().attachChild(playerNode);

        m.getRootNode().attachChild(trail.getGeom());

        setupKeys();
        super.initialize(stateManager, app);
    }

    private void setupKeys() {
        InputManager im = m.getInputManager();
        im.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));

        im.addListener(this,"Lefts");
        im.addListener(this,"Rights");
        im.addListener(this,"Ups");
        im.addListener(this,"Downs");
        im.addListener(this,"Space");
        im.addListener(this,"Reset");
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            snower.turn(value ? 1 : 0);
        } else if (binding.equals("Rights")) {
            snower.turn(value ? -1 : 0);
        } else if (binding.equals("Ups")) {
            snower.setDucked(value);
        } else if (binding.equals("Downs")) {
            snower.stop(value ? 1 : 0);
        } else if (binding.equals("Space")) {
            if (value) snower.jump();
        } else if (binding.equals("Reset")) {
            if (value) resetPos();
        }
    }

    private void resetPos() {
        snower.warp(new Vector3f(0, 150/2, -360/2));
    }

    //TODO dispose

    @Override
    public void update(float tpf) {
        if (snower.isOnGround()) {
            var extents = snower.getBoardExtents();
            trail.viewUpdate(tpf, extents);

            var points = Helper.getMinMaxX(extents);
            var debug = m.getStateManager().getState(DebugAppState.class);
            debug.drawBox("a0", ColorRGBA.Orange, points[0], 0.1f);
            debug.drawBox("a1", ColorRGBA.Orange, points[1], 0.1f);
        } else {
            trail.viewUpdate(tpf, null);
        }

        super.update(tpf);
    }
}
