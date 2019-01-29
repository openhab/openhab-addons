/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etherrain.internal;

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
     * Hostname of the OpenSprinkler API.
     */
    public String hostname = null;

    /**
     * The port the OpenSprinkler API is listening on.
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
