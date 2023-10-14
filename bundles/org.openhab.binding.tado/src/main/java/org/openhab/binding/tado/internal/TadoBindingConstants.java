/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tado.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TadoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dennis Frommknecht - Initial contribution
 * @author Andrew Fiddian-Green - Added Low Battery Alarm, A/C Power and Open Window channels
 *
 */
@NonNullByDefault
public class TadoBindingConstants {

    public static final String BINDING_ID = "tado";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HOME = new ThingTypeUID(BINDING_ID, "home");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_MOBILE_DEVICE = new ThingTypeUID(BINDING_ID, "mobiledevice");

    // List of all Channel IDs
    public static final String PROPERTY_HOME_TEMPERATURE_UNIT = "temperatureUnit";

    public enum TemperatureUnit {
        CELSIUS,
        FAHRENHEIT
    }

    public static final String CHANNEL_HOME_PRESENCE_MODE = "homePresence";

    public static final String CHANNEL_ZONE_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_ZONE_HUMIDITY = "humidity";

    public static final String CHANNEL_ZONE_HEATING_POWER = "heatingPower";
    // air conditioning power
    public static final String CHANNEL_ZONE_AC_POWER = "acPower";

    public static final String CHANNEL_ZONE_HVAC_MODE = "hvacMode";

    public enum HvacMode {
        OFF,
        HEAT,
        COOL,
        DRY,
        FAN,
        AUTO
    }

    public static final String CHANNEL_ZONE_TARGET_TEMPERATURE = "targetTemperature";

    public static final String CHANNEL_ZONE_SWING = "swing";

    public static final String CHANNEL_ZONE_LIGHT = "light";

    public static final String CHANNEL_ZONE_FAN_SPEED = "fanspeed";

    public enum FanSpeed {
        LOW,
        MIDDLE,
        HIGH,
        AUTO
    }

    public static final String CHANNEL_ZONE_FAN_LEVEL = "fanLevel";

    public enum FanLevel {
        SILENT,
        LEVEL1,
        LEVEL2,
        LEVEL3,
        LEVEL4,
        LEVEL5,
        AUTO
    }

    public static final String CHANNEL_ZONE_HORIZONTAL_SWING = "horizontalSwing";

    public enum HorizontalSwing {
        OFF,
        ON,
        LEFT,
        MID_LEFT,
        MID,
        MID_RIGHT,
        RIGHT,
        AUTO
    }

    public static final String CHANNEL_ZONE_VERTICAL_SWING = "verticalSwing";

    public enum VerticalSwing {
        OFF,
        ON,
        UP,
        MID_UP,
        MID,
        MID_DOWN,
        DOWN,
        AUTO
    }

    public static final String CHANNEL_ZONE_OPERATION_MODE = "operationMode";

    public enum OperationMode {
        SCHEDULE,
        TIMER,
        MANUAL,
        UNTIL_CHANGE
    }

    public static final String CHANNEL_ZONE_TIMER_DURATION = "timerDuration";
    public static final String CHANNEL_ZONE_OVERLAY_EXPIRY = "overlayExpiry";

    // battery low alarm channel
    public static final String CHANNEL_ZONE_BATTERY_LOW_ALARM = "batteryLowAlarm";
    // open window detected channel
    public static final String CHANNEL_ZONE_OPEN_WINDOW_DETECTED = "openWindowDetected";

    public static final String CHANNEL_MOBILE_DEVICE_AT_HOME = "atHome";

    // Configuration
    public static final String CONFIG_ZONE_ID = "id";
    public static final String CONFIG_MOBILE_DEVICE_ID = "id";

    // Properties
    public static final String PROPERTY_ZONE_NAME = "name";
    public static final String PROPERTY_ZONE_TYPE = "type";

    public enum ZoneType {
        HEATING,
        AIR_CONDITIONING,
        HOT_WATER
    }

    public static final String PROPERTY_MOBILE_DEVICE_NAME = "name";
}
