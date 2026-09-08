/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SMAEnergyMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class SMAEnergyMeterBindingConstants {

    public static final String BINDING_ID = "smaenergymeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_METER = new ThingTypeUID(BINDING_ID, "energymeter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGY_METER);

    // List of all Channel IDs
    public static final String CHANNEL_POWER_IN = "powerIn";
    public static final String CHANNEL_POWER_OUT = "powerOut";
    public static final String CHANNEL_ENERGY_IN = "energyIn";
    public static final String CHANNEL_ENERGY_OUT = "energyOut";
    public static final String CHANNEL_POWER_IN_L1 = "powerInL1";
    public static final String CHANNEL_POWER_OUT_L1 = "powerOutL1";
    public static final String CHANNEL_ENERGY_IN_L1 = "energyInL1";
    public static final String CHANNEL_ENERGY_OUT_L1 = "energyOutL1";
    public static final String CHANNEL_POWER_IN_L2 = "powerInL2";
    public static final String CHANNEL_POWER_OUT_L2 = "powerOutL2";
    public static final String CHANNEL_ENERGY_IN_L2 = "energyInL2";
    public static final String CHANNEL_ENERGY_OUT_L2 = "energyOutL2";
    public static final String CHANNEL_POWER_IN_L3 = "powerInL3";
    public static final String CHANNEL_POWER_OUT_L3 = "powerOutL3";
    public static final String CHANNEL_ENERGY_IN_L3 = "energyInL3";
    public static final String CHANNEL_ENERGY_OUT_L3 = "energyOutL3";
    public static final String CHANNEL_REACTIVE_POWER_IN = "reactivePowerIn";
    public static final String CHANNEL_REACTIVE_POWER_OUT = "reactivePowerOut";
    public static final String CHANNEL_REACTIVE_ENERGY_IN = "reactiveEnergyIn";
    public static final String CHANNEL_REACTIVE_ENERGY_OUT = "reactiveEnergyOut";
    public static final String CHANNEL_REACTIVE_POWER_IN_L1 = "reactivePowerInL1";
    public static final String CHANNEL_REACTIVE_POWER_OUT_L1 = "reactivePowerOutL1";
    public static final String CHANNEL_REACTIVE_ENERGY_IN_L1 = "reactiveEnergyInL1";
    public static final String CHANNEL_REACTIVE_ENERGY_OUT_L1 = "reactiveEnergyOutL1";
    public static final String CHANNEL_REACTIVE_POWER_IN_L2 = "reactivePowerInL2";
    public static final String CHANNEL_REACTIVE_POWER_OUT_L2 = "reactivePowerOutL2";
    public static final String CHANNEL_REACTIVE_ENERGY_IN_L2 = "reactiveEnergyInL2";
    public static final String CHANNEL_REACTIVE_ENERGY_OUT_L2 = "reactiveEnergyOutL2";
    public static final String CHANNEL_REACTIVE_POWER_IN_L3 = "reactivePowerInL3";
    public static final String CHANNEL_REACTIVE_POWER_OUT_L3 = "reactivePowerOutL3";
    public static final String CHANNEL_REACTIVE_ENERGY_IN_L3 = "reactiveEnergyInL3";
    public static final String CHANNEL_REACTIVE_ENERGY_OUT_L3 = "reactiveEnergyOutL3";
    public static final String CHANNEL_APPARENT_POWER_IN = "apparentPowerIn";
    public static final String CHANNEL_APPARENT_POWER_OUT = "apparentPowerOut";
    public static final String CHANNEL_APPARENT_ENERGY_IN = "apparentEnergyIn";
    public static final String CHANNEL_APPARENT_ENERGY_OUT = "apparentEnergyOut";
    public static final String CHANNEL_APPARENT_POWER_IN_L1 = "apparentPowerInL1";
    public static final String CHANNEL_APPARENT_POWER_OUT_L1 = "apparentPowerOutL1";
    public static final String CHANNEL_APPARENT_ENERGY_IN_L1 = "apparentEnergyInL1";
    public static final String CHANNEL_APPARENT_ENERGY_OUT_L1 = "apparentEnergyOutL1";
    public static final String CHANNEL_APPARENT_POWER_IN_L2 = "apparentPowerInL2";
    public static final String CHANNEL_APPARENT_POWER_OUT_L2 = "apparentPowerOutL2";
    public static final String CHANNEL_APPARENT_ENERGY_IN_L2 = "apparentEnergyInL2";
    public static final String CHANNEL_APPARENT_ENERGY_OUT_L2 = "apparentEnergyOutL2";
    public static final String CHANNEL_APPARENT_POWER_IN_L3 = "apparentPowerInL3";
    public static final String CHANNEL_APPARENT_POWER_OUT_L3 = "apparentPowerOutL3";
    public static final String CHANNEL_APPARENT_ENERGY_IN_L3 = "apparentEnergyInL3";
    public static final String CHANNEL_APPARENT_ENERGY_OUT_L3 = "apparentEnergyOutL3";
    public static final String CHANNEL_POWER_FACTOR = "powerFactor";
    public static final String CHANNEL_POWER_FACTOR_L1 = "powerFactorL1";
    public static final String CHANNEL_POWER_FACTOR_L2 = "powerFactorL2";
    public static final String CHANNEL_POWER_FACTOR_L3 = "powerFactorL3";
    public static final String CHANNEL_CURRENT_L1 = "currentL1";
    public static final String CHANNEL_CURRENT_L2 = "currentL2";
    public static final String CHANNEL_CURRENT_L3 = "currentL3";
    public static final String CHANNEL_VOLTAGE_L1 = "voltageL1";
    public static final String CHANNEL_VOLTAGE_L2 = "voltageL2";
    public static final String CHANNEL_VOLTAGE_L3 = "voltageL3";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_VERSION = "version";
}
