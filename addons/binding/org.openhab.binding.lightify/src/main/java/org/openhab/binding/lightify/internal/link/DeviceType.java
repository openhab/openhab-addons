package org.openhab.binding.lightify.internal.link;

public enum DeviceType {
    Unknown(-1),
    Blub(2, 10),
    PlugSocket(16),
    MotionSensor(32),
    Switch(64, 65);

    private static final DeviceType[] DEVICE_TYPES = DeviceType.values();

    private final int type1, type2;

    DeviceType(int type) {
        this(type, -1);
    }

    DeviceType(int type1, int type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    public static DeviceType findByTypeId(int type) {
        for (DeviceType deviceType : DEVICE_TYPES) {
            if (type == deviceType.type1
                    || (deviceType.type2 != -1 && deviceType.type2 == type)) {
                return deviceType;
            }
        }
        return Unknown;
    }
}
