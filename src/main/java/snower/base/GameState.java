package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;

import snower.player.BoardingUI;
import snower.player.FloatingSnowControl;
import snower.player.PlayerInputs;
import snower.player.SnowTrail;
import snower.player.SnowboarderAnimControl;
import snower.player.SnowboarderControl;
import snower.player.SnowboarderRailDetector;
import snower.service.DebugAppState;
import snower.service.Helper;
import snower.world.StaticWorldState;

public class GameState extends BaseAppState {
    
    //https://github.com/stephengold/Minie/blob/master/Jme3Examples/src/main/java/jme3test/bullet/TestQ3.java

    private Main m;
    private SnowTrail trail;
    private SnowboarderControl snower;
    private FloatingSnowControl newSnower;

    private PlayerInputs inputs;

    public GameState(Main m) {
        this.m = m;
    }

    @Override
    protected void initialize(Application app) {
        var world = new StaticWorldState();
        app.getStateManager().attach(world);

        // new char
        var controlNode2 = ((Node)m.getAssetManager().loadModel("models/tinybuttanimate.gltf"));
        controlNode2.setLocalTranslation(world.startPos());
        controlNode2.setShadowMode(ShadowMode.CastAndReceive);
        
        newSnower = new FloatingSnowControl(world);
        controlNode2.addControl(newSnower);
        this.m.getRootNode().attachChild(controlNode2);
        Main.physicsSpace.add(newSnower);

        // add player model
        var controlNode = ((Node)m.getAssetManager().loadModel("models/tinybuttanimate.gltf"));
        var playerNode = ((Node)controlNode).getChild(0);
        playerNode.setShadowMode(ShadowMode.CastAndReceive);
        snower = new SnowboarderControl(world);
        controlNode.addControl(snower);
        Main.physicsSpace.add(snower);
        this.m.getRootNode().attachChild(controlNode);

        // setup rail detection
        var railDetector = new SnowboarderRailDetector(app.getAssetManager(), Main.physicsSpace, snower);
        
        // setup player inputs
        this.inputs = new PlayerInputs(app.getInputManager(), snower, railDetector);

        // setup animation control
        var viewControl = new SnowboarderAnimControl();
        playerNode.addControl(viewControl);

        trail = new SnowTrail(m.getAssetManager());
        m.getRootNode().attachChild(trail.getGeom());

        // camera
        Camera cam = m.getCamera();
        PullCam camera = new PullCam(cam, controlNode);
        app.getStateManager().attach(camera);

        BoardingUI ui = new BoardingUI(snower);
        app.getStateManager().attach(ui);
    }

    @Override
    protected void cleanup(Application app) {
        snower.getSpatial().removeFromParent();

        app.getStateManager().detach(app.getStateManager().getState(BoardingUI.class));
        app.getStateManager().detach(app.getStateManager().getState(StaticWorldState.class));
        app.getStateManager().detach(app.getStateManager().getState(PullCam.class));

        inputs.remove();
    }

    @Override
    public void update(float tpf) {
        var debug = m.getStateManager().getState(DebugAppState.class);

        var extents = snower.getExpectedBoardExtents();
        if (snower.isOnGround()) {
            trail.viewUpdate(tpf, extents);
        } else {
            trail.viewUpdate(tpf, null);
        }

        if (debug.DEBUG()) {
            if (snower.isOnGround()) {
                var points = Helper.getMinMaxX(extents);
                debug.drawBox("a0", ColorRGBA.Orange, points[0], 0.1f);
                debug.drawBox("a1", ColorRGBA.Orange, points[1], 0.1f);
            }
            debug.drawBox("player pos", ColorRGBA.Black, snower.getRigidBody().getPhysicsLocation(), 0.1f);
        }
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onEnable() {
    }
}
