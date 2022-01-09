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
package org.openhab.binding.konnected.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KonnectedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class KonnectedBindingConstants {

    public static final String BINDING_ID = "konnected";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MODULE = new ThingTypeUID(BINDING_ID, "module");

    // Thing config properties
    public static final String HOST = "ipAddress";
    public static final String MAC_ADDR = "macAddress";
    public static final String CALLBACK_PATH = "callBackPath";
    public static final String REQUEST_TIMEOUT = "request_timeout";
    public static final String RETRY_COUNT = "retry_count";

    // PIN_TO_ZONE array, this array maps an index location as a zone to the corresponding
    // pin location
    public static final Integer[] PIN_TO_ZONE = { 0, 1, 2, 5, 6, 7, 9, 8 };

    public static final String WEBHOOK_APP = "app_security";

    public static final String CHANNEL_ZONE = "zone";

    // channeltypeids
    public static final String CHANNEL_SWITCH = "konnected:switch";
    public static final String CHANNEL_ACTUATOR = "konnected:actuator";
    public static final String CHANNEL_TEMPERATURE = "konnected:temperature";
    public static final String CHANNEL_HUMIDITY = "konnected:humidity";

    public static final String CHANNEL_TEMPERATURE_TYPE = "tempsensorType";
    public static final String CHANNEL_TEMPERATURE_DS18B20_ADDRESS = "ds18b20_address";
    public static final String CHANNEL_TEMPERATRUE_POLL = "pollinterval";

    public static final String CHANNEL_ACTUATOR_TIMES = "times";
    public static final String CHANNEL_ACTUATOR_MOMENTARY = "momentary";
    public static final String CHANNEL_ACTUATOR_PAUSE = "pause";

    public static final String CHANNEL_ONVALUE = "onvalue";
}
