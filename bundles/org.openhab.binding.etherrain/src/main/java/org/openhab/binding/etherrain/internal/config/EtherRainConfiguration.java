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
package org.openhab.binding.etherrain.internal.config;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EtherRainConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public class EtherRainConfiguration {

    public static final String BINDING_ID = "etherrrain";

    public static final ThingTypeUID ETHERRAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "etherrain");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(ETHERRAIN_THING_TYPE);

    /**
     * Hostname of the EtherRain API.
     */
    public String host = null;

    /**
     * The port the EtherRain API is listening on.
     * Default: 80 per specification
     */
    public int port = 80;

    /**
     * The password to connect to the EtherRain API.
     * Default: pw per specification
     */
    public String password = "pw";

    /**
     * Number of seconds in between refreshes from the EtherRain device.
     */
    public int refresh = 60;

    /**
     * Default Delay for Program Timer
     */
    public int programDelay = 0;

    /**
     * Default Zone on times
     */
    public static int zoneOnTime1 = 0;
    public static int zoneOnTime2 = 0;
    public static int zoneOnTime3 = 0;
    public static int zoneOnTime4 = 0;
    public static int zoneOnTime5 = 0;
    public static int zoneOnTime6 = 0;
    public static int zoneOnTime7 = 0;
    public static int zoneOnTime8 = 0;

}
