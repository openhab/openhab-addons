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
package org.openhab.binding.modbus.lambda.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LambdaBindingConstants} class defines common
 * constants, which are used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class LambdaBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LAMBDAHP = new ThingTypeUID(BINDING_ID, "lambdahp");

    // Channel group ids
    public static final String GROUP_GENERAL_AMBIENT = "generalAmbient";
    public static final String GROUP_GENERAL_EMANAGER = "generalEManager";
    public static final String GROUP_BOILER1 = "boiler1";
    public static final String GROUP_BOILER150 = "boiler150";

    /**
     * public static final String GROUP_HEAT_PUMP1 = "HeatPump1";
     * public static final String GROUP_BUFFER1 = "Buffer1";
     * public static final String GROUP_SOLAR1 = "Solar1";
     * public static final String GROUP_HEATING_CIRCUIT1 = "HeatingCircuit1";
     */
    // List of all Channel ids in device information group
    public static final String CHANNEL_ACTUAL_AMBIENT_TEMPERATURE = "actual-ambient-temperature";
    public static final String CHANNEL_ACTUAL_POWER = "actual-power";
    public static final String CHANNEL_ACTUAL_POWER_CONSUMPTION = "actual-power-consumption";
    public static final String CHANNEL_BOILER1_ACTUAL_HIGH_TEMPERATURE = "boiler1-actual-high-temperature";
    public static final String CHANNEL_BOILER150_MAXIMUM_BOILER_TEMPERATURE = "boiler150-maximum-boiler-temperature";
}
