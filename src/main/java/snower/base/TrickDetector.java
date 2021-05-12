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

        public TrickList(Trick ...tricks) {
            this.tricks = tricks;
        }

        public boolean hasTricks() {
            return tricks.length > 0;
        }
        public Trick[] getTricks() {
            return tricks;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Trick t: tricks) {
                sb.append(t);
                sb.append(", ");
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
            return sb.toString();
        }
    }
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

        if (rotAngle > FastMath.HALF_PI) {
            curTrick.spins++;
            rotAngle -= FastMath.HALF_PI;
        }

        if (rotAngle < -FastMath.HALF_PI) {
            curTrick.spins++;
            rotAngle += FastMath.HALF_PI;
        }

        if (flipAngle > FastMath.PI) {
            curTrick.frontFlips++;
            flipAngle -= FastMath.PI;
        }

        if (flipAngle < -FastMath.PI) {
            curTrick.backFlips++;
            flipAngle += FastMath.PI;
        }
    }
    public void grab(String name) {
        if (curTrick.grab == null) {
            curTrick.grab = name;
        } else {
            curTrick = new Trick();
            curTrick.grab = name;
            tricks.add(curTrick);
        }
    }

    public TrickList stop() {
        var validTricks = tricks.stream().filter(x -> x.backFlips != 0 || x.frontFlips != 0 || x.grab != null || x.spins != 0).collect(Collectors.toList());
        return new TrickList(validTricks.toArray(new Trick[0]));
    }
}
