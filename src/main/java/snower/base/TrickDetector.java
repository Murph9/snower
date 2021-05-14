package snower.base;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.jme3.math.FastMath;

public class TrickDetector {
    // TODO should this be a spatial control?
    
    private float rotAngle;
    private float flipAngle;

    class TrickList {
        private Trick[] tricks;
        public final boolean failed;

        public TrickList(Trick ...tricks) {
            this(false, tricks);
        }
        public TrickList(boolean failed, Trick ...tricks) {
            this.tricks = tricks;
            this.failed = failed;
        }

        public boolean hasTricks() {
            return tricks.length > 0;
        }
        public Trick[] getTricks() {
            return tricks;
        }

        @Override
        public String toString() {
            if (tricks.length < 1) {
                return "[" + (this.failed ? "Failed" : "") +"]";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(tricks[0]);
            for (int i = 1; i < tricks.length; i++) {
                sb.append(" + ");
                sb.append(tricks[i]);
            }
            
            if (this.failed) {
                sb.append(": Failed");
            }
            return sb.toString();
        }
    }

    class Trick {
        public int spins;
        public int frontFlips;
        public int backFlips;
        public String grab;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (spins > 0) {
                sb.append(spins*180 + "' ");
            }
            if (frontFlips > 0) {
                sb.append(frontFlips + " frontflip ");
            }
            if (backFlips > 0) {
                sb.append(backFlips + " backflip ");
            }
            if (grab != null) {
                sb.append(grab);
            }
            return sb.toString().trim();
        }
    }
    
    private boolean inTrick;
    private Trick curTrick;
    private List<Trick> tricks;

    public TrickDetector() {
        curTrick = new Trick();
        tricks = new LinkedList<>();
        tricks.add(curTrick);
    }

    public void update(float dRotAngle, float dFlipAngle) {
        rotAngle += dRotAngle;
        flipAngle += dFlipAngle;

        if (rotAngle > FastMath.PI) {
            curTrick.spins++;
            rotAngle -= FastMath.PI;
        }

        if (rotAngle < -FastMath.PI) {
            curTrick.spins++;
            rotAngle += FastMath.PI;
        }

        if (flipAngle > FastMath.TWO_PI) {
            curTrick.frontFlips++;
            flipAngle -= FastMath.TWO_PI;
        }

        if (flipAngle < -FastMath.TWO_PI) {
            curTrick.backFlips++;
            flipAngle += FastMath.TWO_PI;
        }
    }
    public void grab(String name) {
        inTrick = name != null;

        if (curTrick.grab == null) {
            curTrick.grab = name;
        } else {
            if (curTrick.grab.equals(name))
                return; //don't update the same grab
            if (name == null)
                return; //don't void the previous trick's grab
            
            curTrick = new Trick();
            curTrick.grab = name;
            tricks.add(curTrick);
        }
    }

    public TrickList stop() {
        // some leniency, and set to 0 to show we landed correctly (on the last trick only)
        final float FRACTION = 0.2f; // within 10 percent
        final float LENIENCY = 1 - FRACTION;
        if (Math.abs(rotAngle) > FastMath.PI*LENIENCY) {
            curTrick.spins++;
            rotAngle = 0;
        }
        if (flipAngle > FastMath.TWO_PI*LENIENCY) {
            curTrick.frontFlips++;
            flipAngle = 0;
        }
        if (flipAngle < -FastMath.TWO_PI*LENIENCY) {
            curTrick.backFlips++;
            flipAngle = 0;
        }
        var failed = inTrick || Math.abs(rotAngle) > FRACTION || Math.abs(flipAngle) > FRACTION;
        
        var validTricks = tricks.stream().filter(x -> x.backFlips != 0 || x.frontFlips != 0 || x.grab != null || x.spins != 0).collect(Collectors.toList());
        var trickList = new TrickList(failed, validTricks.toArray(new Trick[0]));
        System.out.println(trickList);
        return trickList;
    }
}
