/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.solaxx3mic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolaxX3MicBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
@NonNullByDefault
public class SolaxX3MicBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOLAX_X3_MIC = new ThingTypeUID(BINDING_ID, "inverter-solax-x3-mic");

    // properties
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_UNIQUE_ADDRESS = "uniqueAddress";

    // Channel group ids
    public static final String GROUP_DEVICE_INFO = "deviceInformation";
    public static final String GROUP_AC_GENERAL = "acGeneral";
    public static final String GROUP_AC_PHASE_A = "acPhaseA";
    public static final String GROUP_AC_PHASE_B = "acPhaseB";
    public static final String GROUP_AC_PHASE_C = "acPhaseC";
    public static final String GROUP_DC_GENERAL_1 = "dcGeneral1";
    public static final String GROUP_DC_GENERAL_2 = "dcGeneral2";

    // List of all Channel ids in device information group
    public static final String CHANNEL_CABINET_TEMPERATURE = "cabinet-temperature";
    public static final String CHANNEL_HEATSINK_TEMPERATURE = "heatsink-temperature";
    public static final String CHANNEL_STATUS = "status";

    // List of channel ids in AC general group for inverter
    public static final String CHANNEL_AC_POWER = "ac-power";
    public static final String CHANNEL_AC_DAYLY_ENERGY = "ac-dayly-energy";
    public static final String CHANNEL_AC_LIFETIME_ENERGY = "ac-lifetime-energy";

    // List of channel ids in AC phase group for inverter
    public static final String CHANNEL_AC_PHASE_VOLTAGE_TO_N = "ac-phase-voltage-to-n";
    public static final String CHANNEL_AC_PHASE_CURRENT = "ac-phase-current";
    public static final String CHANNEL_AC_PHASE_FREQUENCY = "ac-phase-frequency";
    public static final String CHANNEL_AC_PHASE_POWER = "ac-phase-power";

    // List of channel ids in DC group for inverter
    public static final String CHANNEL_DC_CURRENT = "dc-current";
    public static final String CHANNEL_DC_VOLTAGE = "dc-voltage";
    public static final String CHANNEL_DC_POWER = "dc-power";
}
