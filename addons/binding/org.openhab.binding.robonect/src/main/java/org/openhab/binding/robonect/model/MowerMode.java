package org.openhab.binding.robonect.model;

public enum MowerMode {

    AUTO(0),
    MANUAL(1),
    HOME(2),
    DEMO(3),
    UNKNOWN(99);

    private int code;

    MowerMode(int code) {
        this.code = code;
    }

    public static MowerMode fromMode(int mode) {
        for (MowerMode mowerMode : MowerMode.values()) {
            if (mowerMode.code == mode) {
                return mowerMode;
            }
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }
    
    /*
    0: Auto
    1: Manuell
    2: Home
    3: Demo
     */
}
