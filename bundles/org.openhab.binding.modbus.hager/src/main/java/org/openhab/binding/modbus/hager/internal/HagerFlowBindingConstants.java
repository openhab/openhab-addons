/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.hager.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HagerFlowBindingConstants} defines the constants used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HagerFlowBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    public static final ThingTypeUID THING_TYPE_EMS = new ThingTypeUID(BINDING_ID, "hager-flow-ems");
    public static final ThingTypeUID THING_TYPE_EVSE = new ThingTypeUID(BINDING_ID, "hager-flow-evse");
    public static final ThingTypeUID THING_TYPE_STORAGE = new ThingTypeUID(BINDING_ID, "hager-flow-storage");
    public static final ThingTypeUID THING_TYPE_POWERMETER = new ThingTypeUID(BINDING_ID, "hager-flow-powermeter");
    public static final ThingTypeUID THING_TYPE_SGREADY = new ThingTypeUID(BINDING_ID, "hager-flow-sgready");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_EMS, THING_TYPE_EVSE,
            THING_TYPE_STORAGE, THING_TYPE_POWERMETER, THING_TYPE_SGREADY);

    public static final String CONFIG_SLAVE_ID = "slaveId";
    public static final String CONFIG_REFRESH = "refresh";

    /** Shortest poll interval accepted, matching the minimum declared in the thing types. */
    public static final int MIN_REFRESH_MS = 1000;

    public static final String READ_ERROR = "Modbus read error";

    /** Raw value switching a boolean register on. */
    public static final int ON = 1;

    /** Raw value switching a boolean register off. */
    public static final int OFF = 0;
}
