package org.openhab.binding.homepilot.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class HomePilotDeviceImpl implements HomePilotDevice {

    private ThingTypeUID thingTypeUID;
    private String deviceId;
    private String name;
    private String description;
    private Integer position;

    public HomePilotDeviceImpl(ThingTypeUID thingTypeUID, Integer deviceId, String name, String description,
            Integer position) {
        this.position = position;
        this.thingTypeUID = thingTypeUID;
        this.deviceId = Integer.toString(deviceId);
        this.name = name;
        this.description = description;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public ThingTypeUID getTypeUID() {
        return thingTypeUID;
    }
}
