package snower.player;

import snower.player.GrabMapper.GrabEnum;
import snower.player.TrickDetector.TrickList;

public interface ISnowControl {
    boolean isOnGround();
    boolean isSwitch();
    boolean isCrashing();
    boolean isDucked();
    boolean isSlowing();
    boolean isGrabbing();
    GrabEnum getGrab();
    TrickList getTrick();

    void turn(float amount);
    void jump(float amount);
    void slow(float amount);
    void flip(float amount);
    void reset();

    void setDucked(boolean value);
    void finishRail(boolean value);

    void grab(GrabEnum type);
    String getDebugStr();
}
