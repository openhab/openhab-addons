package org.openhab.binding.lightify.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.HashSet;
import java.util.Set;

public final class LightifyConstants {

    private LightifyConstants() {
    }

    public static final String BINDING_ID = "lightify";

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_BULB = new ThingTypeUID(BINDING_ID, "bulb");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_ZONE = new ThingTypeUID(BINDING_ID, "zone");

    public static final String PROPERTY_ADDRESS = "ipAddress";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_DEVICE_NAME = "device-name";
    public static final String PROPERTY_DEVICE_ADDRESS = "device-address";
    public static final String PROPERTY_ZONE_ID = "zone-id";

    public static final String CHANNEL_ID_POWER = "rgbw#power";
    public static final String CHANNEL_ID_TEMPERATURE = "rgbw#temperature";
    public static final String CHANNEL_ID_DIMMER = "rgbw#dimmer";
    public static final String CHANNEL_ID_COLOR = "rgbw#color";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_GATEWAY);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_BULB);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_ZONE);
    }

}
