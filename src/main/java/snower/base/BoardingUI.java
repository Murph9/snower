package snower.base;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import snower.service.Screen;

public class BoardingUI extends BaseAppState {

    private static final float TRICK_MESSAGE_TIMEOUT = 5; //seconds

    private final SnowboarderControl control;

    private Container trickView;
    private Label trickText;
    private Container debugView;
    private Label debugText;

    private float trickTextTimer;

    public BoardingUI(SnowboarderControl control) {
        this.control = control;
    }

    @Override
    public void initialize(Application app) {
        this.trickView = new Container();
        this.trickText = this.trickView.addChild(new Label(""));
		((SimpleApplication)app).getGuiNode().attachChild(this.trickView);

        this.debugView = new Container();
        this.debugText = this.debugView.addChild(new Label(""));
        ((SimpleApplication)app).getGuiNode().attachChild(this.debugView);
    }

    @Override
    public void update(float tpf) {
        trickTextTimer -= tpf;
        if (trickTextTimer < 0) {
            this.trickText.setText("");

            // buffer calling the tricks to display them
            var trick = control.getTrick();
            if (trick != null) {
                this.trickText.setText(trick.toString());
                this.trickTextTimer = TRICK_MESSAGE_TIMEOUT;
            }
        }

        Screen screen = new Screen(getApplication().getContext().getSettings());
        screen.bottomCenterMe(this.trickView);

        var debugAppState = getState(DebugAppState.class);
        if (debugAppState.DEBUG()) {
            this.debugText.setText(control.getDebugStr());
            screen.topLeftMe(this.debugView);
        } else {
            this.debugText.setText("");
        }

        super.update(tpf);
    }

    @Override
    public void cleanup(Application app) {
        this.debugView.removeFromParent();
        this.trickView.removeFromParent();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
}
