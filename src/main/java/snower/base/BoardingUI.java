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

    private final SnowboarderControl control;
    private Node rootNode;
    private BitmapText scoreText;

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
        this.scoreText = new BitmapText(font, false);
		this.scoreText.setLocalTranslation(screenWidth - 150, screenHeight - fontSize, 0);
		this.scoreText.setText("Speed: 0");
		this.scoreText.setColor(ColorRGBA.White);
		this.scoreText.setSize(fontSize);
		this.rootNode.attachChild(this.scoreText);

        super.initialize(stateManager, app);
    }

    @Override
    public void update(float tpf) {
        this.scoreText.setText("Speed: " + control.getVelocity().length());

        super.update(tpf);
    }
}
