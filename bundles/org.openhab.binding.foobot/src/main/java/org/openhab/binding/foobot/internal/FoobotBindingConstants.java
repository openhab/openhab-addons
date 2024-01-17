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
package org.openhab.binding.foobot.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FoobotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Divya Chauhan - Initial contribution
 */
@NonNullByDefault
public class FoobotBindingConstants {

    // List Foobot URLs
    private static final String URL_FOOBOT_API_V2 = "https://api.foobot.io/v2/";
    public static final String URL_TO_FETCH_DEVICES = URL_FOOBOT_API_V2 + "owner/%username%/device/";
    public static final String URL_TO_FETCH_SENSOR_DATA = URL_FOOBOT_API_V2 + "device/%uuid%/datapoint/0/last/0/";

    private static final String BINDING_ID = "foobot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_FOOBOTACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_FOOBOT = new ThingTypeUID(BINDING_ID, "device");

    // Bridge channel
    public static final String CHANNEL_APIKEY_LIMIT_REMAINING = "apiKeyLimitRemaining";

    // List Foobot configuration attributes
    public static final String CONFIG_APIKEY = "apiKey";
    public static final String CONFIG_UUID = "uuid";
    public static final String CONFIG_MAC = "mac";

    public static final String PROPERTY_NAME = "name";

    public static final int MINIMUM_REFRESH_PERIOD_MINUTES = 5;
    public static final int DEFAULT_REFRESH_PERIOD_MINUTES = 8;
}
