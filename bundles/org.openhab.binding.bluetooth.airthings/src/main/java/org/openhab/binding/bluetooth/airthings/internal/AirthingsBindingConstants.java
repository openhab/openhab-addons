/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.math.BigInteger;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.dimension.Density;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;

import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.RationalConverter;
import tec.uom.se.unit.ProductUnit;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

/**
 * The {@link AirthingsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AIRTHINGS_WAVE_PLUS = new ThingTypeUID(
            BluetoothBindingConstants.BINDING_ID, "airthings_wave_plus");

    // Channel IDs
    public static final String CHANNEL_ID_HUMIDITY = "humidity";
    public static final String CHANNEL_ID_TEMPERATURE = "temperature";
    public static final String CHANNEL_ID_PRESSURE = "pressure";
    public static final String CHANNEL_ID_CO2 = "co2";
    public static final String CHANNEL_ID_TVOC = "tvoc";
    public static final String CHANNEL_ID_RADON_ST_AVG = "radon_st_avg";
    public static final String CHANNEL_ID_RADON_LT_AVG = "radon_lt_avg";

    public static final Unit<Dimensionless> PARTS_PER_BILLION = new TransformedUnit<>(SmartHomeUnits.ONE,
            new RationalConverter(BigInteger.ONE, BigInteger.valueOf(1000000000)));
    public static final Unit<Density> BECQUEREL_PER_CUBIC_METRE = new ProductUnit<>(
            Units.BECQUEREL.divide(Units.CUBIC_METRE));

    static {
        SimpleUnitFormat.getInstance().label(PARTS_PER_BILLION, "ppb");
        SimpleUnitFormat.getInstance().label(BECQUEREL_PER_CUBIC_METRE, "Bq/m³");
    }
}
