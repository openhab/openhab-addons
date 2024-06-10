/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.teslascope.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TeslascopeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslascopeBindingConstants {

    private static final String BINDING_ID = "teslascope";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "service");

    // List of all Channel ids
    public static final String CHANNEL_VIN = "vin";
    public static final String CHANNEL_VEHICLENAME = "vehiclename";
    public static final String CHANNEL_VEHICLESTATE = "vehiclestate";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_BATTERYLEVEL = "batterylevel";
    public static final String CHANNEL_CHARGINGSTATE = "chargingstate";
    public static final String CHANNEL_TPMSFL = "tpms_pressure_fl";
    public static final String CHANNEL_TPMSFR = "tpms_pressure_fr";
    public static final String CHANNEL_TPMSRL = "tpms_pressure_rl";
    public static final String CHANNEL_TPMSRR = "tpms_pressure_rr";
}
