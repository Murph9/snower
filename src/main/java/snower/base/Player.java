package snower.base;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Player extends AbstractAppState implements ActionListener {

    private final Main m;
    private CharacterControl player;
    private Node playerNode;
    
    private float dirAngle = -FastMath.HALF_PI;
    private float speed;
    private static final float ROT_SPEED = 2.5f;
    private boolean left=false,right=false,up=false,down=false;

    public Player(Main m) {
        this.m = m;
        this.player = new CharacterControl(new CapsuleCollisionShape(0.5f, 1.8f), 0.6f); // number here is the ground difference required to 'jump'
    }

    public CharacterControl getChar() {
        return player;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        // add player
        playerNode = new Node("character node");
        Spatial playerG = m.getAssetManager().loadModel("models/stick_snowboarder.obj");
        Material baseMat = new Material(m.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        baseMat.setColor("Color", ColorRGBA.Blue);
        playerG.setMaterial(baseMat);
        playerNode.attachChild(playerG);
        
        player.setJumpSpeed(15);
        player.setFallSpeed(145);
        player.setGravity(30);
        resetPos();
        
        playerNode.addControl(player);
        playerNode.setShadowMode(ShadowMode.Cast);
        
        Main.physicsSpace.add(player);
        m.getRootNode().attachChild(playerNode);

        setupKeys();
        super.initialize(stateManager, app);
    }
    
    
    @Override
    public void update(float tpf) {
        //TODO push down hill

        super.update(tpf);
        if (left)
            dirAngle += tpf*ROT_SPEED;
        if (right)
            dirAngle -= tpf*ROT_SPEED;
        if (up)
            speed = 0.7f;
        else 
            speed -= 3*tpf;
        speed = Math.max(speed, 0);
        
        // if (down)
            // walkDirection.addLocal(camDir.negate()); //TODO slow down
        Vector3f dir = Quaternion.IDENTITY.fromAngles(0, dirAngle, 0).mult(Vector3f.UNIT_X);

        player.setWalkDirection(dir.mult(speed));
        player.setViewDirection(dir);
        
        DebugAppState debug = m.getStateManager().getState(DebugAppState.class);
        
        // calc angle of char
        Vector3f frontPos = player.getPhysicsLocation().add(dir.normalize()).add(0, 0, 0);
        Vector3f rearPos = player.getPhysicsLocation().subtract(dir.normalize()).add(0, 0, 0);
        float frontHeight = findHeight(frontPos, 2);
        float rearHeight = findHeight(rearPos, 2);
        if (frontHeight != -1 && rearHeight != -1) {
            float diffheight = rearHeight - frontHeight;
            float angle = FastMath.atan2(diffheight, 2);
            
            // set angle of character Node based on the floor angle
            Quaternion q = new Quaternion().fromAngleAxis(-angle, Vector3f.UNIT_X);
            playerNode.getChild(0).setLocalRotation(q); // hack to get the physical char
        }

        debug.drawBox("front ray", ColorRGBA.Pink, frontPos, 0.1f);
        debug.drawBox("rear ray", ColorRGBA.White, rearPos, 0.1f);
    }

    private void setupKeys() {
        InputManager im = m.getInputManager();
        im.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));

        im.addListener(this,"Lefts");
        im.addListener(this,"Rights");
        im.addListener(this,"Ups");
        im.addListener(this,"Downs");
        im.addListener(this,"Space");
        im.addListener(this,"Reset");
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {

        if (binding.equals("Lefts")) {
            if(value)
                left=true;
            else
                left=false;
        } else if (binding.equals("Rights")) {
            if(value)
                right=true;
            else
                right=false;
        } else if (binding.equals("Ups")) {
            if(value)
                up=true;
            else
                up=false;
        } else if (binding.equals("Downs")) {
            if(value)
                down=true;
            else
                down=false;
        } else if (binding.equals("Space")) {
            player.jump();
        } else if (binding.equals("Reset")) {
            resetPos();
        }
    }

    private void resetPos() {
        player.setPhysicsLocation(new Vector3f(0, 150/2, -360/2));
    }
    

    private float findHeight(final Vector3f from, float rayLength) {
        Vector3f to = new Vector3f(from.x, from.y - rayLength, from.z);
        List<PhysicsRayTestResult> results = new ArrayList<>();
        Main.physicsSpace.rayTest(from, to, results);
        float height = rayLength;
        for (PhysicsRayTestResult result : results) {
            if (result.getCollisionObject().getObjectId() == player.getCharacter().getObjectId())
                continue; // no self collision please
            if (!(result.getCollisionObject() instanceof PhysicsRigidBody))
                continue;

            float intersection = result.getHitFraction() * rayLength;
            return Math.min(height, intersection);
        }

        return -1;
    }
}
