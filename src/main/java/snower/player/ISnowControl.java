package snower.player;

import snower.player.GrabMapper.GrabEnum;

public interface ISnowControl {
    boolean isOnGround();
    boolean isSwitch();

    void turn(float amount);
    void jump(float amount);
    void slow(float amount);
    void flip(float amount);
    void reset();

    void setDucked(boolean value);
    void finishRail(boolean value);

    void grab(GrabEnum type);
}
