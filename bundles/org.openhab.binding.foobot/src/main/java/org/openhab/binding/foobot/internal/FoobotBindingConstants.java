/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FoobotBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Divya Chauhan - Initial contribution
 */
@NonNullByDefault
public class FoobotBindingConstants {

    private static final String BINDING_ID = "foobot";

    // List Foobot URLs
    public static final String URL_TO_FETCH_UUID = "https://api.foobot.io/v2/owner/%username%/device/";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_FOOBOTACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_FOOBOT = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String TMP = "temperature";
    public static final String HUM = "humidity";
    public static final String PM = "pm";
    public static final String VOC = "voc";
    public static final String CO2 = "co2";
    public static final String GPI = "gpi";

    public static final Map<String, String> SENSOR_MAP = new HashMap<>();
    static {
        SENSOR_MAP.put(TMP, "tmp");
        SENSOR_MAP.put(HUM, "hum");
        SENSOR_MAP.put(PM, "pm");
        SENSOR_MAP.put(VOC, "voc");
        SENSOR_MAP.put(CO2, "co2");
        SENSOR_MAP.put(GPI, "allpollu");
    }

    // List Foobot configuration attributes
    public static final String CONFIG_APIKEY = "apiKey";
    public static final String CONFIG_UUID = "uuid";
    public static final String CONFIG_MAC = "mac";
    public static final String CONFIG_NAME_ = "name";
    public static final String CONFIG_REFRESHTIME = "refreshIntervalInMinutes";

    public static final String PROPERTY_NAME = "foobot-name";

    public static final int DEFAULT_REFRESH_PERIOD_MINUTES = 7;
}
