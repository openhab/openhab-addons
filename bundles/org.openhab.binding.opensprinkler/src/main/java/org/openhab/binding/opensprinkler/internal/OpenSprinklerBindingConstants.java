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
package org.openhab.binding.opensprinkler.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenSprinklerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Split channels to their own things
 */
@NonNullByDefault
public class OpenSprinklerBindingConstants {
    public static final String BINDING_ID = "opensprinkler";

    // List of all Thing ids
    public static final String HTTP_BRIDGE = "http";
    public static final String PI_BRIDGE = "pi";
    public static final String STATION_THING = "station";
    public static final String DEVICE_THING = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID OPENSPRINKLER_HTTP_BRIDGE = new ThingTypeUID(BINDING_ID, HTTP_BRIDGE);
    public static final ThingTypeUID OPENSPRINKLER_STATION = new ThingTypeUID(BINDING_ID, STATION_THING);
    public static final ThingTypeUID OPENSPRINKLER_DEVICE = new ThingTypeUID(BINDING_ID, DEVICE_THING);
    public static final ThingTypeUID OPENSPRINKLER_PI_BRIDGE = new ThingTypeUID(BINDING_ID, PI_BRIDGE);

    public static final int DEFAULT_WAIT_BEFORE_INITIAL_REFRESH = 30;
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;

    // List of all Channel ids
    public static final String SENSOR_RAIN = "rainsensor";
    public static final String STATION_STATE = "stationState";
    public static final String REMAINING_WATER_TIME = "remainingWaterTime";
}
