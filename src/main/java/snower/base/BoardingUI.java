package snower.base;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;

public class BoardingUI extends AbstractAppState {

    private static final float TRICK_MESSAGE_TIMEOUT = 5; //seconds

    private final SnowboarderControl control;
    private Node rootNode;
    private BitmapText speedText;
    private BitmapText trickText;

    private float trickTextTimer;

    public BoardingUI(SnowboarderControl control) {
        this.control = control;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        this.rootNode = new Node("game node");
		this.rootNode.setQueueBucket(Bucket.Gui);
		((SimpleApplication)app).getRootNode().attachChild(rootNode);

        int screenHeight = ((SimpleApplication)app).getCamera().getHeight();
		int screenWidth = ((SimpleApplication)app).getCamera().getWidth();

        BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        int fontSize = font.getCharSet().getRenderedSize();

        this.speedText = new BitmapText(font, false);
		this.speedText.setLocalTranslation(screenWidth - 150, screenHeight - fontSize, 0);
		this.speedText.setText("Speed: 0");
		this.speedText.setColor(ColorRGBA.White);
		this.speedText.setSize(fontSize);
		this.rootNode.attachChild(this.speedText);

        this.trickText = new BitmapText(font, false);
		this.trickText.setLocalTranslation(50, screenHeight - fontSize, 0);
		this.trickText.setText("");
		this.trickText.setColor(ColorRGBA.White);
		this.trickText.setSize(fontSize);
		this.rootNode.attachChild(this.trickText);

        super.initialize(stateManager, app);
    }

    @Override
    public void update(float tpf) {
        this.speedText.setText("Speed: " + control.getVelocity().length());

        trickTextTimer -= tpf;
        if (trickTextTimer < 0) {
            this.trickText.setText("");

             // buffer calling the tricks to display them
            var trick = control.getTrick();
            if (trick != null) {
                System.out.println("Got trick " + trick);
                this.trickText.setText(trick.toString());
                this.trickTextTimer = TRICK_MESSAGE_TIMEOUT;
            }
        }

        super.update(tpf);
    }

    @Override
    public void cleanup() {
        this.trickText.getParent().removeFromParent(); // hack to remove the rootnode
        super.cleanup();
    }
}
