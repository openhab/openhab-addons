/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot;

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

    // List of all Thing Type UIDs
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
}
