/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfosshrv;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DanfossHRVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ralf Duckstein - Initial contribution
 */
@NonNullByDefault
public class DanfossHRVBindingConstants {

    private static final String BINDING_ID = "danfosshrv";

    // The only thing type UIDs
    public static final ThingTypeUID THING_TYPE_HRV = new ThingTypeUID(BINDING_ID, "hrv");

    // The thing type as a set
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_HRV);

    // Properties
    public static final String PROPERTY_UNIT_NAME = "Unit Name";
    public static final String PROPERTY_SERIAL = "Serial Number";

    // Main Channels
    public static final String GROUP_MAIN = "main";
    public static final String CHANNEL_CURRENT_TIME = "current_time";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_FAN_SPEED = "fan_speed";
    public static final String CHANNEL_BOOST = "boost";

    // Main Temperature Channels
    public static final String GROUP_TEMPS = "temps";
    public static final String CHANNEL_ROOM_TEMP = "room_temp";
    public static final String CHANNEL_OUTDOOR_TEMP = "outdoor_temp";

    // Humidity Channel
    public static final String GROUP_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY = "humidity";

    // recuperator channels
    public static final String GROUP_RECUPERATOR = "recuperator";
    public static final String CHANNEL_BYPASS = "bypass";
    public static final String CHANNEL_SUPPLY_TEMP = "supply_temp";
    public static final String CHANNEL_EXTRACT_TEMP = "extract_temp";
    public static final String CHANNEL_EXHAUST_TEMP = "exhaust_temp";

    // service channels
    public static final String GROUP_SERVICE = "service";
    public static final String CHANNEL_BATTERY_LIFE = "battery_life";
    public static final String CHANNEL_FILTER_LIFE = "filter_life";

}
