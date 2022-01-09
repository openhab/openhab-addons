package org.openhab.binding.lgthinq.lgapi.model;

public enum DeviceTypes {
    AIR_CONDITIONER(401), UNKNOWN(-1);

    private final int deviceTypeId;

    public int deviceTypeId() {
        return deviceTypeId;
    }

    public static DeviceTypes fromDeviceTypeId(int deviceTypeId) {
        switch (deviceTypeId) {
            case 401:
                return AIR_CONDITIONER;
            default:
                return UNKNOWN;
        }
    }

    DeviceTypes(int i) {
        this.deviceTypeId = i;
    }
}
