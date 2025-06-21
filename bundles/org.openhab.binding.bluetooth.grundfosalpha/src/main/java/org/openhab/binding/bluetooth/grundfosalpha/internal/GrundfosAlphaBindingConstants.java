/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.grundfosalpha.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GrundfosAlphaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class GrundfosAlphaBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ALPHA3 = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "alpha3");
    public static final ThingTypeUID THING_TYPE_MI401 = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, "mi401");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ALPHA3, THING_TYPE_MI401);

    // List of configuration parameters
    public static final String CONFIGURATION_REFRESH_INTERVAL = "refreshInterval";

    // List of all Channel ids
    public static final String CHANNEL_FLOW_RATE = "flow-rate";
    public static final String CHANNEL_PUMP_HEAD = "pump-head";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_PUMP_TEMPERATURE = "pump-temperature";
    public static final String CHANNEL_VOLTAGE_AC = "voltage-ac";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MOTOR_SPEED = "motor-speed";
}
