package snower.player;

import java.util.LinkedList;
import java.util.List;

import com.jme3.math.FastMath;

import snower.player.GrabMapper.GrabEnum;

public class TrickDetector {
    
    private final float TRICK_RAD_FRACTION = 0.2f; // within 10 percent
    private final float TRICK_RAD_LENIENCY = 1 - TRICK_RAD_FRACTION;

    private float rotAngle;
    private float flipAngle;

    class TrickList {
        private final Trick[] tricks;
        public final String failedReason;
        public final boolean completed;

        public TrickList(String failedReason, boolean completed, Trick ...tricks) {
            this.tricks = tricks;
            this.failedReason = failedReason;
            this.completed = completed;
        }

        public boolean failed() {
            return failedReason != null;
        }
        public boolean hasTricks() {
            return tricks.length > 0;
        }
        public Trick[] getTricks() {
            return tricks;
        }
        public boolean switchedStance() {
            int spins = 0;
            for (Trick t: this.tricks) {
                spins += t.spins;
            }
            return spins % 2 == 1;
        }
        public int getComboCount() {
            if (tricks.length < 1)
                return 1;

            int total = 1;
            for (int i = 1; i < tricks.length; i++) {
                var tLast = tricks[i-1];
                var tCur = tricks[i];
                // count changes from: 
                // - air trick to rail
                if (!tLast.rail && tCur.rail)
                    total++;
                // - rail to air trick
                if (tLast.rail && !tCur.rail)
                    total++;
                // - rail to rail
                if (tLast.rail && tCur.rail)
                    total++;
            }
            return total;
        }

        @Override
        public String toString() {
            if (tricks.length < 1) {
                return this.failed() ? "[Failed]" : null;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(tricks[0]);
            for (int i = 1; i < tricks.length; i++) {
                sb.append(" + ");
                sb.append(tricks[i]);
            }
            
            if (this.failed()) {
                sb.append(": Failed");
            }
            return sb.toString();
        }
    }

    class Trick {
        public int spins;
        public int frontFlips;
        public int backFlips;
        public GrabEnum grab;
        public boolean rail;

        public boolean anyValidTrick() {
            return backFlips != 0 || frontFlips != 0 || grab != null || spins != 0 || rail;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (spins > 0)
                sb.append(spins*180 + "' ");
            if (frontFlips > 0)
                sb.append(frontFlips + " frontflip ");
            if (backFlips > 0)
                sb.append(backFlips + " backflip ");
            if (grab != null)
                sb.append(grab.getName());
            if (rail)
                sb.append("Grind");
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

    public boolean inGrab() {
        return inTrick;
    }
    public GrabEnum curGrab() {
        return curTrick.grab;
    }

    public TrickList update(float dRotAngle, float dFlipAngle) {
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

        var validTricks = tricks.stream().filter(x -> x.anyValidTrick()).toArray(Trick[]::new);
        return new TrickList(null, false, validTricks);
    }

    public void grab(GrabMapper.GrabEnum grab) {
        inTrick = grab != null;

        if (curTrick.grab == null) {
            curTrick.grab = grab;
        } else {
            if (grab == null)
                return; //don't void the previous trick's grab with no grab
            
            curTrick = new Trick();
            curTrick.grab = grab;
            tricks.add(curTrick);
        }
    }

    /**Start a rail, returns whether it creates a combo */
    public boolean startRail() {
        if (curTrick.anyValidTrick()) {
            curTrick = new Trick();
            curTrick.rail = true;
            tricks.add(curTrick);
            return true;
        }

        curTrick.rail = true;
        return false;
    }

    public void finishRail() {
        assert !curTrick.rail;
        
        curTrick = new Trick();
        tricks.add(curTrick);
    }

    public TrickList stop() {
        // detect some very close to finishing tricks and set to 0 to show we landed correctly
        if (Math.abs(rotAngle) > FastMath.PI*TRICK_RAD_LENIENCY) {
            curTrick.spins++;
            rotAngle = 0;
        }
        if (flipAngle > FastMath.TWO_PI*TRICK_RAD_LENIENCY) {
            curTrick.frontFlips++;
            flipAngle = 0;
        }
        if (flipAngle < -FastMath.TWO_PI*TRICK_RAD_LENIENCY) {
            curTrick.backFlips++;
            flipAngle = 0;
        }
        String failedReason = null;
        if (inTrick)
            failedReason = "Still grabbing";
        if (Math.abs(rotAngle) > FastMath.PI*TRICK_RAD_FRACTION)
            failedReason = "Still rotating";
        if (Math.abs(flipAngle) > FastMath.TWO_PI*TRICK_RAD_FRACTION)
            failedReason = "Still flipping";

        var validTricks = tricks.stream().filter(x -> x.anyValidTrick()).toArray(Trick[]::new);
        var trickList = new TrickList(failedReason, true, validTricks);
        return trickList;
    }
}
