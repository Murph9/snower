package snower.service;

import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Panel;

public class Screen {
    
    // https://github.com/Murph9/RallyGame/blob/master/src/main/java/rallygame/service/Screen.java

    private final int width;
    private final int height;

    public Screen(AppSettings set) {
        this.height = set.getHeight();
        this.width = set.getWidth();
    }

    public void bottomCenterMe(Panel c) {
        var size = c.getPreferredSize();
        Vector3f pos = new Vector3f(width / 2 - size.x / 2, size.y, 0);
        c.setLocalTranslation(pos);
    }

    public void topLeftMe(Panel c) {
        Vector3f pos = new Vector3f(0, height, 0);
        c.setLocalTranslation(pos);
    }
}
