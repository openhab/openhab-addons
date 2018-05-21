/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TadoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoBindingConstants {

    public static final String BINDING_ID = "tado";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_HOME = new ThingTypeUID(BINDING_ID, "home");
    public final static ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public final static ThingTypeUID THING_TYPE_MOBILE_DEVICE = new ThingTypeUID(BINDING_ID, "mobiledevice");

    // List of all Channel IDs
    public final static String PROPERTY_HOME_TEMPERATURE_UNIT = "temperatureUnit";

    public static enum TemperatureUnit {
        CELSIUS,
        FAHRENHEIT
    }

    public final static String CHANNEL_ZONE_CURRENT_TEMPERATURE = "currentTemperature";
    public final static String CHANNEL_ZONE_HUMIDITY = "humidity";

    public final static String CHANNEL_ZONE_HEATING_POWER = "heatingPower";

    public final static String CHANNEL_ZONE_HVAC_MODE = "hvacMode";

    public static enum HvacMode {
        OFF,
        HEAT,
        COOL,
        DRY,
        FAN,
        AUTO
    }

    public final static String CHANNEL_ZONE_TARGET_TEMPERATURE = "targetTemperature";

    public final static String CHANNEL_ZONE_SWING = "swing";

    public final static String CHANNEL_ZONE_FAN_SPEED = "fanspeed";

    public static enum FanSpeed {
        LOW,
        MIDDLE,
        HIGH,
        AUTO
    }

    public final static String CHANNEL_ZONE_OPERATION_MODE = "operationMode";

    public static enum OperationMode {
        SCHEDULE,
        TIMER,
        MANUAL,
        UNTIL_CHANGE
    }

    public final static String CHANNEL_ZONE_TIMER_DURATION = "timerDuration";
    public final static String CHANNEL_ZONE_OVERLAY_EXPIRY = "overlayExpiry";

    public final static String CHANNEL_MOBILE_DEVICE_AT_HOME = "atHome";

    // Configuration
    public final static String CONFIG_ZONE_ID = "id";
    public final static String CONFIG_MOBILE_DEVICE_ID = "id";

    // Properties
    public final static String PROPERTY_ZONE_NAME = "name";
    public final static String PROPERTY_ZONE_TYPE = "type";

    public static enum ZoneType {
        HEATING,
        AIR_CONDITIONING,
        HOT_WATER
    }

    public final static String PROPERTY_MOBILE_DEVICE_NAME = "name";
}
