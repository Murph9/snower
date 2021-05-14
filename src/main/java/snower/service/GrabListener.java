package snower.service;

import java.util.HashMap;
import java.util.Map.Entry;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GrabListener implements ActionListener {
    
    private static final String BASE_ACTION = "BaseAction";
    private final HashMap<String, Grab> grabKeys;
    private boolean baseKeyPressed;
    private String curAction;

    public GrabListener(InputManager im, int baseKey, Grab ...grabs) {
        im.addMapping(BASE_ACTION, new KeyTrigger(baseKey));
        im.addListener(this, BASE_ACTION);

        grabKeys = new HashMap<>();
        for (Grab g: grabs) {
            grabKeys.put(g.name, g);

            im.addMapping(g.name, new KeyTrigger(g.key));
            im.addListener(this, g.name);
        }
    }

    public void deregister(InputManager im) {
        im.removeListener(this);

        for (String s: grabKeys.keySet()) {
            im.deleteMapping(s);
        }
        im.deleteMapping(BASE_ACTION);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(BASE_ACTION)) {
            baseKeyPressed = isPressed;
            if (!isPressed) {
                curAction = null; //let go when removed
            }
        }
        
        if (baseKeyPressed) { //base key + has stopped pressing base key
            for (Entry<String, Grab> grab: grabKeys.entrySet()) {
                if (isPressed && name.equals(grab.getKey())) {
                    curAction = name;
                    baseKeyPressed = false; // need to let go of the grab button to continue
                    break;
                }
            }
        }
    }

    public void landed() {
        this.curAction = null;
        this.baseKeyPressed = false;
    }

    public Grab getAction() {
        if (curAction == null)
            return null;
        return grabKeys.get(curAction);
    }
}
