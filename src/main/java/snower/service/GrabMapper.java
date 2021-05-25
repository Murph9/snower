package snower.service;

public class GrabMapper {
    
    private static final float DEADZONE = 0.3f;
    public enum GrabEnum {
        UP("nose"),
        DOWN("tail"),
        
        LEFT("mute"),
        RIGHT("method"),
        
        UP_RIGHT("lien"),
        UP_LEFT("crail"),

        DOWN_RIGHT("melon"),
        DOWN_LEFT("iguana"),
        ;

        private String name;
        GrabEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name + " grab";
        }
    }
    public static GrabEnum getGrabFrom(float upDown, float leftRight, boolean isSwitch) {
        upDown = checkDeadZone(upDown);
        leftRight = checkDeadZone(leftRight);
        leftRight = isSwitch ? -leftRight : leftRight; //switch only affects left and right
        
        // check all 8 directions
        if (upDown > 0 && leftRight == 0) {
            return GrabEnum.UP;
        }
        if (upDown < 0 && leftRight == 0) {
            return GrabEnum.DOWN;
        }

        if (leftRight > 0 && upDown == 0) {
            return GrabEnum.LEFT;
        }
        if (leftRight < 0 && upDown == 0) {
            return GrabEnum.RIGHT;
        }

        if (leftRight > 0 && upDown > 0) {
            return GrabEnum.UP_LEFT;
        }
        if (leftRight < 0 && upDown > 0) {
            return GrabEnum.UP_RIGHT;
        }

        if (leftRight > 0 && upDown < 0) {
            return GrabEnum.DOWN_LEFT;
        }
        if (leftRight < 0 && upDown < 0) {
            return GrabEnum.DOWN_RIGHT;
        }

        return null;
    }

    private static float checkDeadZone(float input) {
        if (input > 0 && input < DEADZONE) {
            return 0;
        }
        if (input < 0 && input > -DEADZONE) {
            return 0;
        }

        return input;
    }
}
