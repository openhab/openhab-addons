/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smaenergymeter.internal;

import static org.openhab.binding.smaenergymeter.internal.handler.MeasuredUnit.*;
import static org.openhab.binding.smaenergymeter.internal.handler.MeasuredUnit.NONE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeterChannel;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SMAEnergyMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class SMAEnergyMeterBindingConstants {

    private static final Set<EnergyMeterChannel> ENERGY_METER_CHANNELS;

    static {
        ENERGY_METER_CHANNELS = new HashSet<>();
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(1, "CONSUME", W, kWh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(2, "SUPPLY", W, kWh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(3, "QCONSUM", VAr, kVArh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(4, "QSUPPLY", VAr, kVArh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(9, "SCONSUME", VA, kVAh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(10, "SSUPPLY", VA, kVAh, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(13, "COSPHI", DEG, DEG, 0));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(14, "FREQUENCE", Hz, Hz, 0));

        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(21, "CONSUME", W, kWh, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(22, "SUPPLY", W, kWh, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(23, "QCONSUM", VAr, kVArh, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(24, "QSUPPLY", VAr, kVArh, 1));

        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(29, "SCONSUME", VA, kVAh, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(30, "SSUPPLY", VA, kVAh, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(31, "CURRENT", A, A, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(32, "VOLTAGE", V, V, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(33, "COSPHI", DEG, DEG, 1));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(34, "FREQUENCE", Hz, Hz, 1));

        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(41, "CONSUME", W, kWh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(42, "SUPPLY", W, kWh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(43, "QCONSUM", VAr, kVArh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(44, "QSUPPLY", VAr, kVArh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(49, "SCONSUME", VA, kVAh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(50, "SSUPPLY", VA, kVAh, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(51, "CURRENT", A, A, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(52, "VOLTAGE", V, V, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(53, "COSPHI", DEG, DEG, 2));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(54, "FREQUENCE", Hz, Hz, 2));

        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(61, "CONSUME", W, kWh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(62, "SUPPLY", W, kWh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(63, "QCONSUM", VAr, kVArh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(64, "QSUPPLY", VAr, kVArh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(69, "SCONSUME", VA, kVAh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(70, "SSUPPLY", VA, kVAh, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(71, "CURRENT", A, A, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(72, "VOLTAGE", V, V, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(73, "COSPHI", DEG, DEG, 3));
        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(74, "FREQUENCE", Hz, Hz, 3));

        ENERGY_METER_CHANNELS.add(new EnergyMeterChannel(36864, "SPEEDWIRE", NONE, NONE, 0));
    }

    public static final String BINDING_ID = "smaenergymeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_METER = new ThingTypeUID(BINDING_ID, "energymeter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ENERGY_METER);

    public static final int DATA_HEADER_SIZE = 4;

    public static EnergyMeterChannel getEnergyMeterValueForChannel(int channel) {
        return ENERGY_METER_CHANNELS.stream().filter(v -> v.getChannel() == channel).findFirst().orElse(null);
    }
}
