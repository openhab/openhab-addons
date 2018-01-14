/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link PentairBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
public class PentairBindingConstants {

    public static final String BINDING_ID = "pentair";

    // List of Bridge Types
    public static final String IP_BRIDGE = "ip_bridge";
    public static final String SERIAL_BRIDGE = "serial_bridge";

    // List of all Device Types
    public static final String EASYTOUCH = "easytouch";
    public static final String INTELLIFLO = "intelliflo";
    public static final String INTELLICHLOR = "intellichlor";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID IP_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, IP_BRIDGE);
    public static final ThingTypeUID SERIAL_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, SERIAL_BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID INTELLIFLO_THING_TYPE = new ThingTypeUID(BINDING_ID, INTELLIFLO);
    public static final ThingTypeUID EASYTOUCH_THING_TYPE = new ThingTypeUID(BINDING_ID, EASYTOUCH);
    public static final ThingTypeUID INTELLICHLOR_THING_TYPE = new ThingTypeUID(BINDING_ID, INTELLICHLOR);

    // List of all Channel ids
    public static final String EASYTOUCH_POOLTEMP = "pooltemp";
    public static final String EASYTOUCH_SPATEMP = "spatemp";
    public static final String EASYTOUCH_AIRTEMP = "airtemp";
    public static final String EASYTOUCH_SOLARTEMP = "solartemp";

    public static final String EASYTOUCH_SPAHEATMODE = "spaheatmode";
    public static final String EASYTOUCH_SPAHEATMODESTR = "spaheatmodestr";
    public static final String EASYTOUCH_POOLHEATMODE = "poolheatmode";
    public static final String EASYTOUCH_POOLHEATMODESTR = "poolheatmodestr";
    public static final String EASYTOUCH_HEATACTIVE = "heatactive";

    public static final String EASYTOUCH_POOLSETPOINT = "poolsetpoint";
    public static final String EASYTOUCH_SPASETPOINT = "spasetpoint";

    public static final String EASYTOUCH_POOL = "pool";
    public static final String EASYTOUCH_SPA = "spa";
    public static final String EASYTOUCH_AUX1 = "aux1";
    public static final String EASYTOUCH_AUX2 = "aux2";
    public static final String EASYTOUCH_AUX3 = "aux3";
    public static final String EASYTOUCH_AUX4 = "aux4";
    public static final String EASYTOUCH_AUX5 = "aux5";
    public static final String EASYTOUCH_AUX6 = "aux6";
    public static final String EASYTOUCH_AUX7 = "aux7";

    public static final String INTELLICHLOR_SALTOUTPUT = "saltoutput";
    public static final String INTELLICHLOR_SALINITY = "salinity";

    public static final String INTELLIFLO_RUN = "run";
    public static final String INTELLIFLO_MODE = "mode";
    public static final String INTELLIFLO_DRIVESTATE = "drivestate";
    public static final String INTELLIFLO_POWER = "power";
    public static final String INTELLIFLO_RPM = "rpm";
    public static final String INTELLIFLO_PPC = "ppc";
    public static final String INTELLIFLO_ERROR = "error";
    public static final String INTELLIFLO_TIMER = "timer";

    public static final String DIAG = "diag";

    // Custom Properties
    public static final String PROPERTY_ADDRESS = "localhost";
    public static final Integer PROPERTY_PORT = 10000;

    // Set of all supported Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(IP_BRIDGE_THING_TYPE,
            SERIAL_BRIDGE_THING_TYPE, EASYTOUCH_THING_TYPE, INTELLIFLO_THING_TYPE, INTELLICHLOR_THING_TYPE);
}

