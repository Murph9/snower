package snower.base;

import com.jme3.anim.AnimComposer;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import snower.service.GrabMapper.GrabEnum;

public class SnowboarderAnimControl extends AbstractControl {

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial == null) {
            return;
        }

        var anim = spatial.getControl(AnimComposer.class);
        for (var actionName : anim.getAnimClipsNames()) {
            var a = anim.makeAction(actionName);
            a.setLength(0.6f); // allow smooth animation transitions
            anim.addAction(actionName, a);
        }
        System.out.println("Player has these animations: " + String.join(", ", anim.getAnimClipsNames()));

        ((Node)spatial).getChild(0).setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
    }

    @Override
    protected void controlUpdate(float tpf) {
        var snower = spatial.getParent().getControl(SnowboarderControl.class);

        if (!snower.isOnGround() && snower.isGrabbing())
            setPose(PlayerState.Grabbing, snower.getGrab());
        else if (snower.isCrashing())
            setPose(PlayerState.Crashing);
        else if (snower.isDucked())
            setPose(PlayerState.Ducked);
        else if (snower.isSlowing())
            setPose(PlayerState.Slowing);
        else
            setPose(PlayerState.Nothing);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // nothing
    }
    

    enum PlayerState {
        Nothing,
        Ducked,
        TurningLeft,
        TurningRight,
        Slowing,
        Crashing,
        Grabbing,;
    }
    private PlayerState curState;
    private void setPose(PlayerState type) {
        setPose(type, null);
    }
    private void setPose(PlayerState type, GrabEnum grab) {
        if (curState == type)
            return;
        curState = type;
        
        System.out.println("Updating animation with: " + type + " " + grab);
        
        var anim = spatial.getControl(AnimComposer.class);
        String newAction = null;
        switch(type) {
            case Crashing:
                newAction = "crashing";
                break;
            case Grabbing:
                newAction = getGrabAction(grab);
                break;
            case Nothing:
                newAction = "boarding_normal";
                break;
            default:
                newAction = "0TPose";
                System.out.println("Warning: unknown animation state: " + type);
        }

        anim.setCurrentAction(newAction);
    }

    private String getGrabAction(GrabEnum grab) {
        switch(grab) {
            case DOWN:
                return "tail_grab";
            case UP:
                return "nose_grab";
            case LEFT:
                return "mute_grab";
            case RIGHT:
                return "method_grab";
            default:
                System.out.println("Unknown grab type: " + grab.getName());
                return "0TPose";
        }
    }
}
