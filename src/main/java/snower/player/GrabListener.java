package snower.player;

import java.util.HashSet;
import java.util.Set;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GrabListener implements ActionListener {
    
    private static final String BASE_ACTION = "BaseAction";

    private final PlayerInputs player;
    private final String prefix;

    private final Set<String> curDirection = new HashSet<>();

    public GrabListener(PlayerInputs player, InputManager im, int baseKey) {
        this.player = player;
        this.prefix = player.toString()+"_Grab_";

        im.addMapping(prefix+BASE_ACTION, new KeyTrigger(baseKey));
        im.addListener(this, prefix+BASE_ACTION);
        
        im.addMapping(prefix+"Up", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping(prefix+"Left", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping(prefix+"Down", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping(prefix+"Right", new KeyTrigger(KeyInput.KEY_D));
        im.addListener(this, prefix+"Up", prefix+"Down", prefix+"Left", prefix+"Right");
    }

    public void deregister(InputManager im) {
        im.removeListener(this);

        im.deleteMapping(prefix+"Up");
        im.deleteMapping(prefix+"Left");
        im.deleteMapping(prefix+"Down");
        im.deleteMapping(prefix+"Right");
        im.deleteMapping(prefix+BASE_ACTION);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        // detect the direction being pressed
        if (name.equals(prefix+"Up")) {
            addOrRemove("Up", isPressed);
        }
        if (name.equals(prefix+"Down")) {
            addOrRemove("Down", isPressed);
        }
        if (name.equals(prefix+"Left")) {
            addOrRemove("Left", isPressed);
        }
        if (name.equals(prefix+"Right")) {
            addOrRemove("Right", isPressed);
        }

        if (name.equals(prefix+BASE_ACTION)) {
            if (isPressed && curDirection.size() > 0) {
                this.player.grabbed(upDownFromCurrent(), leftRightFromCurrent());
            }
            if (!isPressed) {
                this.player.grabbed(0, 0);
            }
        }
    }

    private void addOrRemove(String name, boolean isPressed) {
        if (isPressed)
            curDirection.add(name);
        else
            curDirection.remove(name);
    }

    private float leftRightFromCurrent() {
        float output = 0;
        if (curDirection.contains("Left")) {
            output += 1;
        }
        if (curDirection.contains("Right")) {
            output -= 1;
        }
        return output;
    }

    private float upDownFromCurrent() {
        float output = 0;
        if (curDirection.contains("Up")) {
            output += 1;
        }
        if (curDirection.contains("Down")) {
            output -= 1;
        }
        return output;
    }
}
