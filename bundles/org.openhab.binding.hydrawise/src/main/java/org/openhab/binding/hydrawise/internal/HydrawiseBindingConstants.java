/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public static final ThingTypeUID THING_TYPE_CLOUD = new ThingTypeUID(BINDING_ID, "cloud");
    public static final ThingTypeUID THING_TYPE_LOCAL = new ThingTypeUID(BINDING_ID, "local");

    public static final String BASE_IMAGE_URL = "https://app.hydrawise.com/config/images/";

    public static final String CHANNEL_ZONE_RUN_CUSTOM = "runcustom";
    public static final String CHANNEL_ZONE_RUN = "run";
    public static final String CHANNEL_ZONE_STOP = "stop";
    public static final String CHANNEL_ZONE_SUSPEND = "suspend";
    public static final String CHANNEL_ZONE_NAME = "name";
    public static final String CHANNEL_ZONE_ICON = "icon";
    public static final String CHANNEL_ZONE_LAST_WATER = "lastwater";
    public static final String CHANNEL_ZONE_TIME = "time";
    public static final String CHANNEL_ZONE_TYPE = "type";
    public static final String CHANNEL_ZONE_NEXT_RUN_TIME_TIME = "nextruntime";
    public static final String CHANNEL_ZONE_TIME_LEFT = "timeleft";
    public static final String CHANNEL_RUN_ALL_ZONES = "runall";
    public static final String CHANNEL_STOP_ALL_ZONES = "stopall";
    public static final String CHANNEL_SUSPEND_ALL_ZONES = "suspendall";
    public static final String CHANNEL_SENSOR_NAME = "name";
    public static final String CHANNEL_SENSOR_INPUT = "input";
    public static final String CHANNEL_SENSOR_MODE = "mode";
    public static final String CHANNEL_SENSOR_TIMER = "timer";
    public static final String CHANNEL_SENSOR_OFFTIMER = "offtimer";
    public static final String CHANNEL_SENSOR_OFFLEVEL = "offlevel";
    public static final String CHANNEL_SENSOR_ACTIVE = "active";

    // <channel id="name" typeId="name" />
    // <channel id="lastContact" typeId="lastContact" />
    // <channel id="description" typeId="description" />
    // <channel id="location" typeId="location" />
    // <channel id="lastContactReadable" typeId="lastContactReadable" />
    // <channel id="status" typeId="status" />
    // <channel id="online" typeId="online" />

    public static final String CHANNEL_SYSTEM_LASTCONTACT = "lastContact";
    public static final String CHANNEL_SYSTEM_LASTCONTACT_READABLE = "lastContactReadable";
    public static final String CHANNEL_SYSTEM_STATUS = "status";
    public static final String CHANNEL_SYSTEM_ONLINE = "online";

    public static final String PROPERTY_CONTROLLER_ID = "controller";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_ADDRESS = "address";

}
