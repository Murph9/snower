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
        MyChaseCam camera = new MyChaseCam(cam, player.getChar(), player.getCharNode());
        stateManager.attach(camera);

        BoardingUI ui = new BoardingUI(player.getChar());
        stateManager.attach(ui);
        
        super.stateAttached(stateManager);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        
        stateManager.detach(stateManager.getState(BoardingUI.class));
        stateManager.detach(stateManager.getState(WorldState.class));
        stateManager.detach(stateManager.getState(Player.class));
        stateManager.detach(stateManager.getState(MyChaseCam.class));
    }
}
