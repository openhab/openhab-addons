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
package org.openhab.binding.airgradient.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirGradientBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientBindingConstants {

    private static final String BINDING_ID = "airgradient";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "airgradientapi");
    public static final ThingTypeUID THING_TYPE_LOCATION = new ThingTypeUID(BINDING_ID, "location");

    // List of all Channel ids
    public static final String CHANNEL_PM_01 = "pm01";
    public static final String CHANNEL_PM_02 = "pm02";
    public static final String CHANNEL_PM_10 = "pm10";
    public static final String CHANNEL_PM_003_COUNT = "pm003Count";
    public static final String CHANNEL_ATMP = "atmp";
    public static final String CHANNEL_RHUM = "rhum";
    public static final String CHANNEL_WIFI = "wifi";
    public static final String CHANNEL_RCO2 = "rco2";
    public static final String CHANNEL_TVOC = "tvoc";

    // List of all properties
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_SERIAL_NO = "serialNo";

    // All configurations
    public static final String CONFIG_LOCATION = "location";

    // URLs for API
    public static final String PING_PATH = "/public/api/v1/ping";
    public static final String CURRENT_MEASURES_PATH = "/public/api/v1/locations/measures/current?token=%s";

    // Discovery
    public static final int SEARCH_TIME = 15;
    public static final boolean BACKGROUND_DISCOVERY = true;
}
