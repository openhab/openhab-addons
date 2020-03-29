/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DWDPollenflugBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBindingConstants {

    public static final long INITIAL_DELAY = TimeUnit.SECONDS.toSeconds(1);

    public static final long SECONDS_PER_MINUTE = 60;

    private static final String BINDING_ID = "dwdpollenflug";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_REGION = new ThingTypeUID(BINDING_ID, "region");

    // Region channels Mapping
    public static final Map<String, String> CHANNELS_POLLEN_MAP = initChannelMap();

    private static Map<String, String> initChannelMap() {
        Map<String, String> map = new HashMap<>();
        map.put("Ambrosia", "ambrosia");
        map.put("Beifuss", "mugwort");
        map.put("Birke", "birch");
        map.put("Erle", "alder");
        map.put("Esche", "ash");
        map.put("Graeser", "grasses");
        map.put("Hasel", "hazel");
        map.put("Roggen", "rye");
        return Collections.unmodifiableMap(map);
    }

    // Channels of Pollen groups
    public static final String CHANNEL_TODAY = "today";
    public static final String CHANNEL_TOMORROW = "tomorrow";
    public static final String CHANNEL_DAYAFTER_TO = "dayafter_to";

    // Bridge config properties
    public static final String REFRESH = "refresh";

    // Region config properties
    public static final String REGION_ID = "regionID";

    // Region properties
    public static final String PROPERTY_REGION_ID = "region_id";
    public static final String PROPERTY_REGION_NAME = "region_name";
    public static final String PROPERTY_PARTREGION_ID = "partregion_id";
    public static final String PROPERTY_PARTREGION_NAME = "partregion_name";
}
