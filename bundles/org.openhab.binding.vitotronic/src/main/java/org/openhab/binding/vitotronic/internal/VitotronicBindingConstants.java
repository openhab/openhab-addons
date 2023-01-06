/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vitotronic.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VitotronicBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Andres - Initial contribution
 */
@NonNullByDefault
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
    public static final String THING_ID_GASBURNER = "gasburner";
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
    public static final ThingTypeUID THING_TYPE_UID_GASBURNER = new ThingTypeUID(BINDING_ID, THING_ID_GASBURNER);
    public static final ThingTypeUID THING_TYPE_UID_PELLETBURNER = new ThingTypeUID(BINDING_ID, THING_ID_PELLETBURNER);
    public static final ThingTypeUID THING_TYPE_UID_OILBURNER = new ThingTypeUID(BINDING_ID, THING_ID_OILBURNER);
    public static final ThingTypeUID THING_TYPE_UID_STORAGETANK = new ThingTypeUID(BINDING_ID, THING_ID_STORAGETANK);
    public static final ThingTypeUID THING_TYPE_UID_CIRCUIT = new ThingTypeUID(BINDING_ID, THING_ID_CIRCUIT);
    public static final ThingTypeUID THING_TYPE_UID_SOLAR = new ThingTypeUID(BINDING_ID, THING_ID_SOLAR);
    public static final ThingTypeUID THING_TYPE_UID_TEMPERATURESENSOR = new ThingTypeUID(BINDING_ID,
            THING_ID_TEMPERATURESENSOR);
    public static final ThingTypeUID THING_TYPE_UID_PUMP = new ThingTypeUID(BINDING_ID, THING_ID_PUMP);
    public static final ThingTypeUID THING_TYPE_UID_VALVE = new ThingTypeUID(BINDING_ID, THING_ID_VALVE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_UID_BRIDGE, THING_TYPE_UID_GASBURNER, THING_TYPE_UID_HEATING, THING_TYPE_UID_PELLETBURNER,
                    THING_TYPE_UID_OILBURNER, THING_TYPE_UID_STORAGETANK, THING_TYPE_UID_CIRCUIT, THING_TYPE_UID_SOLAR,
                    THING_TYPE_UID_TEMPERATURESENSOR, THING_TYPE_UID_PUMP, THING_TYPE_UID_VALVE)
            .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_UID_BRIDGE);
}
