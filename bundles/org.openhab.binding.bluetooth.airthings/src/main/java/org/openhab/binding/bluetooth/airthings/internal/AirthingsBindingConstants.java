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
package org.openhab.binding.bluetooth.airthings.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirthingsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Kai Kreuzer - Added Airthings Wave Mini support
 * @author Davy Wong - Added Airthings Wave Gen 1 support
 */
@NonNullByDefault
public class AirthingsBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AIRTHINGS_WAVE_PLUS = new ThingTypeUID(
            BluetoothBindingConstants.BINDING_ID, "airthings_wave_plus");
    public static final ThingTypeUID THING_TYPE_AIRTHINGS_WAVE_MINI = new ThingTypeUID(
            BluetoothBindingConstants.BINDING_ID, "airthings_wave_mini");
    public static final ThingTypeUID THING_TYPE_AIRTHINGS_WAVE_GEN1 = new ThingTypeUID(
            BluetoothBindingConstants.BINDING_ID, "airthings_wave_gen1");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIRTHINGS_WAVE_PLUS,
            THING_TYPE_AIRTHINGS_WAVE_MINI, THING_TYPE_AIRTHINGS_WAVE_GEN1);

    // Channel IDs
    public static final String CHANNEL_ID_HUMIDITY = "humidity";
    public static final String CHANNEL_ID_TEMPERATURE = "temperature";
    public static final String CHANNEL_ID_PRESSURE = "pressure";
    public static final String CHANNEL_ID_CO2 = "co2";
    public static final String CHANNEL_ID_TVOC = "tvoc";
    public static final String CHANNEL_ID_RADON_ST_AVG = "radon_st_avg";
    public static final String CHANNEL_ID_RADON_LT_AVG = "radon_lt_avg";
}
