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
package org.openhab.binding.solax.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolaxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxBindingConstants {

    private static final String BINDING_ID = "solax";
    private static final String BRIDGE_LOCAL_CONNECT_ID = "localConnect";
    private static final String THING_INVERTER_ID = "inverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LOCAL_CONNECT_BRIDGE = new ThingTypeUID(BINDING_ID,
            BRIDGE_LOCAL_CONNECT_ID);
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, THING_INVERTER_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_LOCAL_CONNECT_BRIDGE,
            THING_TYPE_INVERTER);

    // List of all Channel ids
    public static final String SERIAL_NUMBER = "serialWifi";
    public static final String INVERTER_TYPE = "inverterType";

    public static final String INVERTER_OUTPUT_POWER = "inverterOutputPower";
    public static final String INVERTER_OUTPUT_CURRENT = "inverterCurrent";
    public static final String INVERTER_OUTPUT_VOLTAGE = "inverterVoltage";
    public static final String INVERTER_OUTPUT_FREQUENCY = "inverterFrequency";

    public static final String INVERTER_PV1_POWER = "pv1Power";
    public static final String INVERTER_PV1_VOLTAGE = "pv1Voltage";
    public static final String INVERTER_PV1_CURRENT = "pv1Current";

    public static final String INVERTER_PV2_POWER = "pv2Power";
    public static final String INVERTER_PV2_VOLTAGE = "pv2Voltage";
    public static final String INVERTER_PV2_CURRENT = "pv2Current";

    public static final String INVERTER_PV_TOTAL_POWER = "pvTotalPower";
    public static final String INVERTER_PV_TOTAL_CURRENT = "pvTotalCurrent";

    public static final String BATTERY_POWER = "batteryPower";
    public static final String BATTERY_VOLTAGE = "batteryVoltage";
    public static final String BATTERY_CURRENT = "batteryCurrent";
    public static final String BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String BATTERY_STATE_OF_CHARGE = "batteryStateOfCharge";

    public static final String FEED_IN_POWER = "feedInPower";

    public static final String TIMESTAMP = "lastUpdateTime";
    public static final String RAW_DATA = "rawData";
}
