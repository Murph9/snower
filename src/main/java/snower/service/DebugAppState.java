package snower.service;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.HashMap;
import java.util.Map;

public class DebugAppState extends BaseAppState {

    // https://github.com/Murph9/RallyGame/blob/master/src/main/java/rallygame/game/DebugAppState.java

    private boolean debug;
    private boolean slowMotion;
    private boolean showWireframes;

    private Node node;
    private Map<String, Geometry> thingSet;

    public DebugAppState() {
        this.thingSet = new HashMap<String, Geometry>();
    }

    @Override
    protected void initialize(Application app) {
        this.node = new Node("DebugState");
        ((SimpleApplication)app).getRootNode().attachChild(node);

        app.getInputManager().addRawInputListener(new DebugListener(this));
    }

    @Override
    protected void cleanup(Application app) {
        node.removeFromParent();
        this.node = null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    public boolean DEBUG() {
        return this.debug;
    }

    public void drawBox(String key, ColorRGBA colour, Vector3f pos, float size) {
        if (!debug)
            return;

        addThing(key, DebugAppState.makeShapeBox(getApplication().getAssetManager(), colour, pos, size));
    }

    private void addThing(String key, Geometry thing) {
        if (thing == null)
            return; //not a valid object, then not a vaild thing

        Application app = getApplication();
        app.enqueue(() -> {
            // check if key exists, if yes remove it (from view)
            if (thingSet.containsKey(key)) {
                Geometry g = thingSet.get(key);
                this.node.detachChild(g);
                thingSet.remove(key);
            }

            // add obj with key
            this.node.attachChild(thing);
            thingSet.put(key, thing);
        });
    }

    public void toggleDebug() {
        this.debug = !this.debug;
    }
    private void toggleSlowMotion() {
        this.slowMotion = !this.slowMotion;
        BulletAppState bullet = getState(BulletAppState.class);
        // physics per second rate
        if (slowMotion) {
            bullet.setSpeed(0.1f);
        } else {
            bullet.setSpeed(1f);
        }
    }
    private void toggleShowWireframes() {
        this.showWireframes = !this.showWireframes;
        BulletAppState bullet = getState(BulletAppState.class);
        bullet.setDebugEnabled(this.showWireframes);
    }

    class DebugListener implements RawInputListener {
        
        private DebugAppState state;
        public DebugListener(DebugAppState state) {
            this.state = state;
        }

        public void beginInput() {}
        public void endInput() {}

        public void onKeyEvent(KeyInputEvent arg0) {
            if (arg0.isPressed() && arg0.getKeyCode() == KeyInput.KEY_GRAVE) 
                state.toggleDebug();

            if (arg0.isPressed() && arg0.getKeyCode() == KeyInput.KEY_PAUSE)
                state.toggleSlowMotion();

            if (arg0.isPressed() && arg0.getKeyCode() == KeyInput.KEY_PGUP)
                state.toggleShowWireframes();
        }

        public void onMouseButtonEvent(MouseButtonEvent arg0) {}
        public void onMouseMotionEvent(MouseMotionEvent arg0) {}
        public void onTouchEvent(TouchEvent arg0) {}
        public void onJoyAxisEvent(JoyAxisEvent arg0) {}
        public void onJoyButtonEvent(JoyButtonEvent arg0) {}
    }

    private static Geometry makeShapeBox(AssetManager am, ColorRGBA color, Vector3f pos, float size) {
        if (!Vector3f.isValidVector(pos)) {
            System.out.println("not valid position");
            return null;
        }

        Box box = new Box(size, size, size);
        Geometry boxG = DebugAppState.createShape(am, box, color, pos, "a box");
        return boxG;
    }

    private static Geometry createShape(AssetManager am, Mesh shape, ColorRGBA color, Vector3f pos, String name) {
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setWireframe(true);

        Geometry g = new Geometry(name, shape);
        g.setMaterial(mat);
        g.setLocalTranslation(pos);
        return g;
    }
}
