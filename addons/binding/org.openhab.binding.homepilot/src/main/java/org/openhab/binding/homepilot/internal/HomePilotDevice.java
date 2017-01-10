package org.openhab.binding.homepilot.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public interface HomePilotDevice {

    ThingTypeUID getTypeUID();

    String getDeviceId();

    String getName();

    String getDescription();

    Integer getPosition();
}
