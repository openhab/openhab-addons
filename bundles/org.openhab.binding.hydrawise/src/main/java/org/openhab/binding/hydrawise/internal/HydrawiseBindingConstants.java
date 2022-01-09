/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HydrawiseBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseBindingConstants {
    private static final String BINDING_ID = "hydrawise";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_LOCAL = new ThingTypeUID(BINDING_ID, "local");
    public static final String BASE_IMAGE_URL = "https://app.hydrawise.com/config/images/";

    public static final String CONFIG_USERNAME = "userName";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESHTOKEN = "refreshToken";
    public static final String CONFIG_CONTROLLER_ID = "controllerId";

    public static final String CHANNEL_GROUP_CONTROLLER_SYSTEM = "system";
    public static final String CHANNEL_CONTROLLER_NAME = "name";
    public static final String CHANNEL_CONTROLLER_LAST_CONTACT = "lastcontact";
    public static final String CHANNEL_CONTROLLER_STATUS = "status";
    public static final String CHANNEL_CONTROLLER_SUMMARY = "summary";
    public static final String CHANNEL_CONTROLLER_ONLINE = "online";
    public static final String CHANNEL_GROUP_ALLZONES = "allzones";
    public static final String CHANNEL_ZONE_NAME = "name";
    public static final String CHANNEL_ZONE_ICON = "icon";
    public static final String CHANNEL_ZONE_STARTTIME = "starttime";
    public static final String CHANNEL_ZONE_DURATION = "duration";
    public static final String CHANNEL_ZONE_TYPE = "type";
    public static final String CHANNEL_ZONE_RUN = "run";
    public static final String CHANNEL_ZONE_RUN_CUSTOM = "runcustom";
    public static final String CHANNEL_ZONE_NEXT_RUN_TIME_TIME = "nextruntime";
    public static final String CHANNEL_ZONE_SUSPEND = "suspend";
    public static final String CHANNEL_ZONE_SUSPENDUNTIL = "suspenduntil";
    public static final String CHANNEL_ZONE_SUMMARY = "summary";
    public static final String CHANNEL_ZONE_TIME_LEFT = "timeleft";
    public static final String CHANNEL_SENSOR_NAME = "name";
    public static final String CHANNEL_SENSOR_INPUT = "input";
    public static final String CHANNEL_SENSOR_MODE = "mode";
    public static final String CHANNEL_SENSOR_DELAY = "delay";
    public static final String CHANNEL_SENSOR_OFFTIMER = "offtimer";
    public static final String CHANNEL_SENSOR_OFFLEVEL = "offlevel";
    public static final String CHANNEL_SENSOR_ACTIVE = "active";
    public static final String CHANNEL_SENSOR_WATERFLOW = "waterflow";
    public static final String CHANNEL_FORECAST_TEMPERATURE_HIGH = "temperaturehigh";
    public static final String CHANNEL_FORECAST_TEMPERATURE_LOW = "temperaturelow";
    public static final String CHANNEL_FORECAST_CONDITIONS = "conditions";
    public static final String CHANNEL_FORECAST_TIME = "time";
    public static final String CHANNEL_FORECAST_HUMIDITY = "humidity";
    public static final String CHANNEL_FORECAST_WIND = "wind";
    public static final String CHANNEL_FORECAST_ICON = "icon";
    public static final String CHANNEL_FORECAST_EVAPOTRANSPRIATION = "evapotranspiration";
    public static final String CHANNEL_FORECAST_PRECIPITATION = "precipitation";
    public static final String CHANNEL_FORECAST_PROBABILITYOFPRECIPITATION = "probabilityofprecipitation";

    public static final String PROPERTY_CONTROLLER_ID = "controller";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_ADDRESS = "address";
}
