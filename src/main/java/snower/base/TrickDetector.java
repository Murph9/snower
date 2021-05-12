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
            if (tricks.length < 1) {
                return "";
            }
            sb.append(tricks[0]);
            for (int i = 1; i < tricks.length; i++) {
                sb.append(" + ");
                sb.append(tricks[i]);
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
        if (curTrick.grab == null) {
            curTrick.grab = name;
        } else {
            curTrick = new Trick();
            curTrick.grab = name;
            tricks.add(curTrick);
        }
    }

    public TrickList stop() {
        // some leniency
        // within 10 percent
        final float LENIENCY = 1 - 0.1f;
        if (Math.abs(rotAngle) > FastMath.PI*LENIENCY)
            curTrick.spins++;
        if (flipAngle > FastMath.TWO_PI*LENIENCY)
            curTrick.frontFlips++;
        if (flipAngle < -FastMath.TWO_PI*LENIENCY)
            curTrick.backFlips++;

        System.out.println(tricks);
        var validTricks = tricks.stream().filter(x -> x.backFlips != 0 || x.frontFlips != 0 || x.grab != null || x.spins != 0).collect(Collectors.toList());
        return new TrickList(validTricks.toArray(new Trick[0]));
    }
}
