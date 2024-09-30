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
    public static final String GROUP_HEATPUMP1 = "Heatpump1";
    public static final String GROUP_BOILER1 = "Boiler1";
    public static final String GROUP_BOILER1MT = "Boiler1Mt";
    public static final String GROUP_BUFFER1 = "Buffer1";
    public static final String GROUP_BUFFER1MT = "Buffer1Mt";

    // List of all Channel ids in device information group
    // General Ambient
    public static final String CHANNEL_AMBIENT_ERROR_NUMBER = "ambient-error-number";
    public static final String CHANNEL_AMBIENT_OPERATOR_STATE = "ambient-operator-state";
    public static final String CHANNEL_ACTUAL_AMBIENT_TEMPERATURE = "actual-ambient-temperature";
    public static final String CHANNEL_AVERAGE_AMBIENT_TEMPERATURE = "average-ambient-temperature";
    public static final String CHANNEL_CALCULATED_AMBIENT_TEMPERATURE = "calculated-ambient-temperature";

    // General E-manager
    public static final String CHANNEL_EMANAGER_ERROR_NUMBER = "emanager-error-number";
    public static final String CHANNEL_EMANAGER_OPERATOR_STATE = "emanager-operator-state";
    public static final String CHANNEL_ACTUAL_POWER = "actual-power";
    public static final String CHANNEL_ACTUAL_POWER_CONSUMPTION = "actual-power-consumption";

    // Heatpump 1
    public static final String CHANNEL_HEATPUMP1_TFLOW = "heatpump1-t-flow";

    // Boiler 1
    public static final String CHANNEL_BOILER1_ACTUAL_HIGH_TEMPERATURE = "boiler1-actual-high-temperature";
    public static final String CHANNEL_BOILER1_ACTUAL_LOW_TEMPERATURE = "boiler1-actual-low-temperature";
    public static final String CHANNEL_BOILER1_MAXIMUM_BOILER_TEMPERATURE = "boiler1-maximum-boiler-temperature";

    // Buffer 1
    public static final String CHANNEL_BUFFER1_ACTUAL_HIGH_TEMPERATURE = "buffer1-actual-high-temperature";
    public static final String CHANNEL_BUFFER1_ACTUAL_LOW_TEMPERATURE = "buffer1-actual-low-temperature";
    public static final String CHANNEL_BUFFER1_MAXIMUM_BOILER_TEMPERATURE = "buffer1-maximum-boiler-temperature";
}
