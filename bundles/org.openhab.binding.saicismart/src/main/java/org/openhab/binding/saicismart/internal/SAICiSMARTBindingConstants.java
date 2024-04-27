/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.saicismart.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SAICiSMARTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class SAICiSMARTBindingConstants {

    private static final String BINDING_ID = "saicismart";

    /**
     * Interval in seconds between polls of API.
     */
    public static final int REFRESH_INTERVAL = 10;

    /**
     * Active polling period in minutes
     */
    public static final int POLLING_ACTIVE_MINS = 10;

    /**
     * URL of the SAIC API Host.
     */
    private static final String API_HOST_URL = "https://tap-eu.soimt.com";

    /**
     * https://github.com/SAIC-iSmart-API/documentation?tab=readme-ov-file#api-v11
     */
    public static final String API_ENDPOINT_V11 = API_HOST_URL + "/TAP.Web/ota.mp";

    /**
     * https://github.com/SAIC-iSmart-API/documentation?tab=readme-ov-file#api-v21
     */
    public static final String API_ENDPOINT_V21 = API_HOST_URL + "/TAP.Web/ota.mpv21";

    /**
     * https://github.com/SAIC-iSmart-API/documentation?tab=readme-ov-file#api-v30
     */
    public static final String API_ENDPOINT_V30 = API_HOST_URL + "/TAP.Web/ota.mpv30";

    public static final String ABRP_API_KEY = "8cfc314b-03cd-4efe-ab7d-4431cd8f2e2d";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of all Channel ids
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_RANGE_ELECTRIC = "range-electric";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_ENGINE = "engine";
    public static final String CHANNEL_CHARGING = "charging";
    public static final String CHANNEL_TYRE_PRESSURE_FRONT_LEFT = "tyre-pressure-front-left";
    public static final String CHANNEL_TYRE_PRESSURE_FRONT_RIGHT = "tyre-pressure-front-right";
    public static final String CHANNEL_TYRE_PRESSURE_REAR_LEFT = "tyre-pressure-rear-left";
    public static final String CHANNEL_TYRE_PRESSURE_REAR_RIGHT = "tyre-pressure-rear-right";
    public static final String CHANNEL_INTERIOR_TEMPERATURE = "interior-temperature";
    public static final String CHANNEL_EXTERIOR_TEMPERATURE = "exterior-temperature";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_AUXILIARY_BATTERY_VOLTAGE = "auxiliary-battery-voltage";
    public static final String CHANNEL_DOOR_DRIVER = "door-driver";
    public static final String CHANNEL_DOOR_PASSENGER = "door-passenger";
    public static final String CHANNEL_DOOR_REAR_LEFT = "door-rear-left";
    public static final String CHANNEL_DOOR_REAR_RIGHT = "door-rear-right";
    public static final String CHANNEL_WINDOW_DRIVER = "window-driver";
    public static final String CHANNEL_WINDOW_PASSENGER = "window-passenger";
    public static final String CHANNEL_WINDOW_REAR_LEFT = "window-rear-left";
    public static final String CHANNEL_WINDOW_REAR_RIGHT = "window-rear-right";
    public static final String CHANNEL_WINDOW_SUN_ROOF = "window-sun-roof";
    public static final String CHANNEL_LAST_ACTIVITY = "last-activity";
    public static final String CHANNEL_FORCE_REFRESH = "force-refresh";
    public static final String CHANNEL_REMOTE_AC_STATUS = "remote-ac-status";
    public static final String CHANNEL_SWITCH_AC = "switch-ac";
    public static final String CHANNEL_LAST_POSITION_UPDATE = "last-position-update";
    public static final String CHANNEL_LAST_CHARGE_STATE_UPDATE = "last-charge-state-update";
    public static final String CHANNEL_ALARM_MESSAGE_DATE = "last-alarm-message-date";
    public static final String CHANNEL_ALARM_MESSAGE_CONTENT = "last-alarm-message-content";
}
