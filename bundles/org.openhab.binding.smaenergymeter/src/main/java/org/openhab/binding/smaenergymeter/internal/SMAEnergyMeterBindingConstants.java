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
import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeterValue;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SMAEnergyMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class SMAEnergyMeterBindingConstants {

    private static final Set<EnergyMeterValue> energyMeterValues;

    static {
        energyMeterValues = new HashSet<>();
        energyMeterValues.add(new EnergyMeterValue(1, "CONSUME", W, kWh, 0));
        energyMeterValues.add(new EnergyMeterValue(2, "SUPPLY", W, kWh, 0));
        energyMeterValues.add(new EnergyMeterValue(3, "QCONSUM", VAr, kVArh, 0));
        energyMeterValues.add(new EnergyMeterValue(4, "QSUPPLY", VAr, kVArh, 0));
        energyMeterValues.add(new EnergyMeterValue(9, "SCONSUME", VA, kVAh, 0));
        energyMeterValues.add(new EnergyMeterValue(10, "SSUPPLY", VA, kVAh, 0));
        energyMeterValues.add(new EnergyMeterValue(13, "COSPHI", DEG, DEG, 0));
        energyMeterValues.add(new EnergyMeterValue(14, "FREQUENCE", Hz, Hz, 0));

        energyMeterValues.add(new EnergyMeterValue(21, "CONSUME", W, kWh, 1));
        energyMeterValues.add(new EnergyMeterValue(22, "SUPPLY", W, kWh, 1));
        energyMeterValues.add(new EnergyMeterValue(23, "QCONSUM", VAr, kVArh, 1));
        energyMeterValues.add(new EnergyMeterValue(24, "QSUPPLY", VAr, kVArh, 1));

        energyMeterValues.add(new EnergyMeterValue(29, "SCONSUME", VA, kVAh, 1));
        energyMeterValues.add(new EnergyMeterValue(30, "SSUPPLY", VA, kVAh, 1));
        energyMeterValues.add(new EnergyMeterValue(31, "CURRENT", A, A, 1));
        energyMeterValues.add(new EnergyMeterValue(32, "VOLTAGE", V, V, 1));
        energyMeterValues.add(new EnergyMeterValue(33, "COSPHI", DEG, DEG, 1));
        energyMeterValues.add(new EnergyMeterValue(34, "FREQUENCE", Hz, Hz, 1));

        energyMeterValues.add(new EnergyMeterValue(41, "CONSUME", W, kWh, 2));
        energyMeterValues.add(new EnergyMeterValue(42, "SUPPLY", W, kWh, 2));
        energyMeterValues.add(new EnergyMeterValue(43, "QCONSUM", VAr, kVArh, 2));
        energyMeterValues.add(new EnergyMeterValue(44, "QSUPPLY", VAr, kVArh, 2));
        energyMeterValues.add(new EnergyMeterValue(49, "SCONSUME", VA, kVAh, 2));
        energyMeterValues.add(new EnergyMeterValue(50, "SSUPPLY", VA, kVAh, 2));
        energyMeterValues.add(new EnergyMeterValue(51, "CURRENT", A, A, 2));
        energyMeterValues.add(new EnergyMeterValue(52, "VOLTAGE", V, V, 2));
        energyMeterValues.add(new EnergyMeterValue(53, "COSPHI", DEG, DEG, 2));
        energyMeterValues.add(new EnergyMeterValue(54, "FREQUENCE", Hz, Hz, 2));

        energyMeterValues.add(new EnergyMeterValue(61, "CONSUME", W, kWh, 3));
        energyMeterValues.add(new EnergyMeterValue(62, "SUPPLY", W, kWh, 3));
        energyMeterValues.add(new EnergyMeterValue(63, "QCONSUM", VAr, kVArh, 3));
        energyMeterValues.add(new EnergyMeterValue(64, "QSUPPLY", VAr, kVArh, 3));
        energyMeterValues.add(new EnergyMeterValue(69, "SCONSUME", VA, kVAh, 3));
        energyMeterValues.add(new EnergyMeterValue(70, "SSUPPLY", VA, kVAh, 3));
        energyMeterValues.add(new EnergyMeterValue(71, "CURRENT", A, A, 3));
        energyMeterValues.add(new EnergyMeterValue(72, "VOLTAGE", V, V, 3));
        energyMeterValues.add(new EnergyMeterValue(73, "COSPHI", DEG, DEG, 3));
        energyMeterValues.add(new EnergyMeterValue(74, "FREQUENCE", Hz, Hz, 3));

        energyMeterValues.add(new EnergyMeterValue(36864, "SPEEDWIRE", NONE, NONE, 0));
    }

    public static final String BINDING_ID = "smaenergymeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_METER = new ThingTypeUID(BINDING_ID, "energymeter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ENERGY_METER);

    public static EnergyMeterValue getEnergyMeterValueForChannel(int channel) {
        return energyMeterValues.stream().filter(v -> v.getChannel() == channel).findFirst().orElse(null);
    }
}
