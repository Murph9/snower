package snower.base;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;


public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.setDisplayStatView(false); //defaults to on, shows the triangle count and stuff
        app.start();
    }
    
    public static PhysicsSpace physicsSpace;
    private GameState gameState;

    @Override
    public void simpleInitApp() {
        BulletAppState bullet = new BulletAppState();
        getStateManager().attach(bullet);
        physicsSpace = bullet.getPhysicsSpace();

        //remove the default keys
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_CAMERA_POS); //TODO doesn't work
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_MEMORY);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        
        //initialize Lemur (GUI thing)
        GuiGlobals.initialize(this);
        //Load my style
        LemurGuiStyle.load(assetManager);
        //Init the lemur mouse listener
        getStateManager().attach(new MouseAppState(this));

        //some camera state stuff
        flyCam.setEnabled(false);
        getViewPort().setBackgroundColor(ColorRGBA.Black);

        // TODO static class stuff
        getStateManager().attach(new DebugAppState());

        //start game
        gameState = new GameState(this);
        getStateManager().attach(gameState);

        // TODO stuff that should in another file
        DirectionalLight l = new DirectionalLight();
        l.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        rootNode.addLight(l);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.02f));
        rootNode.addLight(al);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 4096, 3);
        dlsr.setLight(l);
        dlsr.setLambda(0.55f);
        dlsr.setShadowIntensity(0.8f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        viewPort.addProcessor(dlsr);
    }
}
