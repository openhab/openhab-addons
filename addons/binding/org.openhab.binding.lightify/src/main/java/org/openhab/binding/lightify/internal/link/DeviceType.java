package org.openhab.binding.lightify.internal.link;

public enum DeviceType {
    Unknown(-1),
    Blub(2, 4, 10),
    PlugSocket(16),
    MotionSensor(32),
    Switch(64, 65);

    private static final DeviceType[] DEVICE_TYPES = DeviceType.values();

    private final int[] types;

    DeviceType(int... types) {
        this.types = types;
    }

    public static DeviceType findByTypeId(int type) {
        for (DeviceType deviceType : DEVICE_TYPES) {
            for (int t : deviceType.types) {
                if (t == type) {
                    return deviceType;
                }
            }
        }
        return Unknown;
    }
}
