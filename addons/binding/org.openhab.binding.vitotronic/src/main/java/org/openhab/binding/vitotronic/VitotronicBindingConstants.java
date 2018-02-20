/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitotronic;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link VitotronicBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Andres - Initial contribution
 */
public class VitotronicBindingConstants {

    public static final String BROADCAST_MESSAGE = "@@@@VITOTRONIC@@@@/";
    public static final int BROADCAST_PORT = 31113;

    public static final String BINDING_ID = "vitotronic";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    public static final String ADAPTER_ID = "adapterID";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String DISCOVERY_INTERVAL = "discoveryInterval";

    // List of main device types

    public static final String BRIDGE_VITOTRONIC = "bridge";

    // List of all Thing Type
    public static final String THING_ID_HEATING = "heating";
    public static final String THING_ID_PELLETBURNER = "pelletburner";
    public static final String THING_ID_OILBURNER = "oilburner";
    public static final String THING_ID_STORAGETANK = "storagetank";
    public static final String THING_ID_CIRCUIT = "circuit";
    public static final String THING_ID_SOLAR = "solar";
    public static final String THING_ID_TEMPERATURESENSOR = "temperaturesensor";
    public static final String THING_ID_PUMP = "pump";
    public static final String THING_ID_VALVE = "valve";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UID_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_VITOTRONIC);
    public static final ThingTypeUID THING_TYPE_UID_HEATING = new ThingTypeUID(BINDING_ID, THING_ID_HEATING);
    public static final ThingTypeUID THING_TYPE_UID_PELLETBURNER = new ThingTypeUID(BINDING_ID, THING_ID_PELLETBURNER);
    public static final ThingTypeUID THING_TYPE_UID_OILBURNER = new ThingTypeUID(BINDING_ID, THING_ID_OILBURNER);
    public static final ThingTypeUID THING_TYPE_UID_STORAGETANK = new ThingTypeUID(BINDING_ID, THING_ID_STORAGETANK);
    public static final ThingTypeUID THING_TYPE_UID_CIRCUIT = new ThingTypeUID(BINDING_ID, THING_ID_CIRCUIT);
    public static final ThingTypeUID THING_TYPE_UID_SOLAR = new ThingTypeUID(BINDING_ID, THING_ID_SOLAR);
    public static final ThingTypeUID THING_TYPE_UID_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID,
            THING_ID_TEMPERATURESENSOR);
    public static final ThingTypeUID THING_TYPE_UID_PUMP = new ThingTypeUID(BINDING_ID, THING_ID_PUMP);
    public static final ThingTypeUID THING_TYPE_UID_VALVE = new ThingTypeUID(BINDING_ID, THING_ID_VALVE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE,
            THING_TYPE_UID_HEATING, THING_TYPE_UID_PELLETBURNER, THING_TYPE_UID_OILBURNER, THING_TYPE_UID_STORAGETANK,
            THING_TYPE_UID_CIRCUIT, THING_TYPE_UID_SOLAR, THING_TYPE_UID_TEMPERATURESENSOR, THING_TYPE_UID_PUMP,
            THING_TYPE_UID_VALVE);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE);

}
