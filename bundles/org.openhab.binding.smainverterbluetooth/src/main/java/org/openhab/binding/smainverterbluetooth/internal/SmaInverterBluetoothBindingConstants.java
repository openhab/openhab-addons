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
package org.openhab.binding.smainverterbluetooth.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link smainverterbluetoothBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lee Charlton - Initial contribution
 */
@NonNullByDefault
public class SmaInverterBluetoothBindingConstants {

    private static final String BINDING_ID = "smainverterbluetooth";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "solar-inverter");

    // List of all Channel ids
    public static final String CHANNEL_INVERTER_DAY_GENERATION = "inverter-day-generation";
    public static final String CHANNEL_INVERTER_TOTAL_GENERATION = "inverter-total-generation";
    public static final String CHANNEL_INVERTER_SPOT_POWER = "inverter-spot-power";
    public static final String CHANNEL_INVERTER_SPOT_AC_VOLTAGE = "inverter-spot-ac-voltage";
    public static final String CHANNEL_INVERTER_SPOT_TEMPERATURE = "inverter-spot-temperature";
    public static final String CHANNEL_INVERTER_STATUS_CODE = "inverter-status-code";
    public static final String CHANNEL_INVERTER_STATUS_MESSAGE = "inverter-status-message";
    public static final String CHANNEL_INVERTER_TIME = "inverter-time";
}
