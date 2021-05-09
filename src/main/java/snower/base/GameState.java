package snower.base;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.Camera;

public class GameState extends AbstractAppState {
    
    //https://github.com/stephengold/Minie/blob/master/Jme3Examples/src/main/java/jme3test/bullet/TestQ3.java

    private Main m;

    public GameState(Main m) {
        this.m = m;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {

        WorldState world = new WorldState(this.m);
        stateManager.attach(world);

        Player player = new Player(this.m);
        stateManager.attach(player);

        // camera
        Camera cam = m.getCamera();
        MyChaseCam camera = new MyChaseCam(cam, player.getChar());
        stateManager.attach(camera);
        
        super.stateAttached(stateManager);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        
        //TODO remove added states
    }
}
