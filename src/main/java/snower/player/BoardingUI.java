package snower.player;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

import snower.base.DebugAppState;
import snower.player.TrickDetector.TrickList;
import snower.service.Screen;

public class BoardingUI extends BaseAppState {

    private static final float TRICK_MESSAGE_TIMEOUT = 5; //seconds

    private final SnowboarderControl control;

    private Container trickView;
    private Label comboText;
    private Label trickText;
    private Container debugView;
    private Label debugText;

    private TrickList prevTrick;
    private float trickTextTimer;

    public BoardingUI(SnowboarderControl control) {
        this.control = control;
    }

    @Override
    public void initialize(Application app) {
        this.trickView = new Container();
        this.comboText = this.trickView.addChild(new Label(""));
        this.trickText = this.trickView.addChild(new Label(""));
		((SimpleApplication)app).getGuiNode().attachChild(this.trickView);

        this.debugView = new Container();
        this.debugText = this.debugView.addChild(new Label(""));
        ((SimpleApplication)app).getGuiNode().attachChild(this.debugView);
    }

    @Override
    public void update(float tpf) {
        // get trick, if not completed show it
        var trick = control.getTrick();
        if (trick != null) {
            int comboCount = trick.getComboCount();
            if (comboCount > 1) // woo at least 'a' combo
                this.comboText.setText(comboCount + "x combo!");

            this.trickText.setText(trick.toString());
            if (trick.completed) {
                this.trickText.setText(trick.toString());
                if (prevTrick != trick)
                    trickTextTimer = TRICK_MESSAGE_TIMEOUT;

                trickTextTimer -= tpf;
                if (trickTextTimer < 0) {
                    this.trickText.setText("");
                }
            }
            prevTrick = trick;
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
