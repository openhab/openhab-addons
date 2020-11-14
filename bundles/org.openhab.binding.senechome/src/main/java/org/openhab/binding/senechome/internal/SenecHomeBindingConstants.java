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
package org.openhab.binding.senechome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SenecHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
public class SenecHomeBindingConstants {
    private static final String BINDING_ID = "senechome";
    private static final String THING_BASE_ID = "senechome";
    public static final ThingTypeUID THING_TYPE_SENEC_HOME_BATTERY = new ThingTypeUID(BINDING_ID, THING_BASE_ID);

    public static final String CHANNEL_SENEC_POWER_LIMITATION = "powerLimitation";
    public static final String CHANNEL_SENEC_POWER_LIMITATION_STATE = "powerLimitationState";
    public static final String CHANNEL_SENEC_BATTERY_STATE = "batteryState";
    public static final String CHANNEL_SENEC_BATTERY_STATE_VALUE = "batteryStateValue";
    public static final String CHANNEL_SENEC_POWER_CONSUMPTION = "houseConsumption";
    public static final String CHANNEL_SENEC_ENERGY_PRODUCTION = "energyProduction";
    public static final String CHANNEL_SENEC_BATTERY_POWER = "batteryPower";
    public static final String CHANNEL_SENEC_BATTERY_FUEL_CHARGE = "batteryFuelCharge";
    public static final String CHANNEL_SENEC_GRID_POWER = "gridPower";
    public static final String CHANNEL_SENEC_GRID_POWER_SUPPLY = "gridPowerSupply";
    public static final String CHANNEL_SENEC_GRID_POWER_DRAW = "gridPowerDraw";
    public static final String CHANNEL_SENEC_GRID_POWER_PH1 = "gridPowerPhase1";
    public static final String CHANNEL_SENEC_GRID_POWER_PH2 = "gridPowerPhase2";
    public static final String CHANNEL_SENEC_GRID_POWER_PH3 = "gridPowerPhase3";
    public static final String CHANNEL_SENEC_GRID_CURRENT_PH1 = "gridCurrentPhase1";
    public static final String CHANNEL_SENEC_GRID_CURRENT_PH2 = "gridCurrentPhase2";
    public static final String CHANNEL_SENEC_GRID_CURRENT_PH3 = "gridCurrentPhase3";
    public static final String CHANNEL_SENEC_GRID_VOLTAGE_PH1 = "gridVoltagePhase1";
    public static final String CHANNEL_SENEC_GRID_VOLTAGE_PH2 = "gridVoltagePhase2";
    public static final String CHANNEL_SENEC_GRID_VOLTAGE_PH3 = "gridVoltagePhase3";
    public static final String CHANNEL_SENEC_GRID_FREQUENCY = "gridFrequency";
    public static final String CHANNEL_SENEC_LIVE_BAT_CHARGE = "liveBatCharge";
    public static final String CHANNEL_SENEC_LIVE_BAT_DISCHARGE = "liveBatDischarge";
    public static final String CHANNEL_SENEC_LIVE_GRID_IMPORT = "liveGridImport";
    public static final String CHANNEL_SENEC_LIVE_GRID_EXPORT = "liveGridExport";
    public static final String CHANNEL_SENEC_BATTERY_VOLTAGE = "batteryVoltage";
}
