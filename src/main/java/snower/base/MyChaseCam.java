package snower.base;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;

public class MyChaseCam extends BaseAppState {
    
    private final Camera c;
    private final CharacterControl player;

    private Vector3f nextPlayerPos;
    private Vector3f nextPos;

    public MyChaseCam(Camera c, CharacterControl player) {
        this.c = c;
        this.player = player;
    }
    @Override
	public void initialize(Application app) {
        nextPos = player.getPhysicsLocation();
    }

    @Override
    public void render(RenderManager rm) {
        c.setLocation(nextPos);
        c.lookAt(nextPlayerPos, Vector3f.UNIT_Y); //TODO not unit y if the char is going down hill

        super.render(rm);
    }
        
    // calculate world pos of a camera
    private Vector3f getWantedPos() {
        // calculate world pos of a camera
        Vector3f localVel = new Vector3f();
        player.getViewDirection(localVel);
        Vector3f position = player.getPhysicsLocation();

        // TODO calc angle of ground

        return position.add(localVel.normalize().negate().add(0, 0.4f, 0).mult(new Vector3f(8, 8, 8)));
	}


    @Override
    public void update(float tpf) {
        super.update(tpf);

        Vector3f wanted = getWantedPos();
        nextPos = wanted.interpolateLocal(nextPos, wanted, 12*tpf);
        nextPlayerPos = player.getPhysicsLocation();
    }

    // #region unused methods
    @Override
	protected void onEnable() {
	}

	@Override
	protected void onDisable() {
	}

	@Override
	protected void cleanup(Application app) {
	}
    // #endregion
}
