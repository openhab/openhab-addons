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
package org.openhab.binding.senechome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SenecHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steven Schwarznau - Initial contribution
 * @author Lukas Pindl - Update for writing to chargeMode
 */
@NonNullByDefault
public class SenecHomeBindingConstants {
    protected static final String BINDING_ID = "senechome";
    private static final String THING_BASE_ID = "senechome";
    public static final ThingTypeUID THING_TYPE_SENEC_HOME_BATTERY = new ThingTypeUID(BINDING_ID, THING_BASE_ID);

    // SenecHomePower
    public static final String CHANNEL_SENEC_POWER_LIMITATION = "powerLimitation";
    public static final String CHANNEL_SENEC_POWER_LIMITATION_STATE = "powerLimitationState";
    public static final String CHANNEL_SENEC_CURRENT_MPP1 = "currentMpp1";
    public static final String CHANNEL_SENEC_CURRENT_MPP2 = "currentMpp2";
    public static final String CHANNEL_SENEC_CURRENT_MPP3 = "currentMpp3";
    public static final String CHANNEL_SENEC_POWER_MPP1 = "powerMpp1";
    public static final String CHANNEL_SENEC_POWER_MPP2 = "powerMpp2";
    public static final String CHANNEL_SENEC_POWER_MPP3 = "powerMpp3";
    public static final String CHANNEL_SENEC_VOLTAGE_MPP1 = "voltageMpp1";
    public static final String CHANNEL_SENEC_VOLTAGE_MPP2 = "voltageMpp2";
    public static final String CHANNEL_SENEC_VOLTAGE_MPP3 = "voltageMpp3";

    // SenecHomeEnergy
    public static final String CHANNEL_SENEC_SYSTEM_STATE = "systemState";
    public static final String CHANNEL_SENEC_SYSTEM_STATE_VALUE = "systemStateValue";
    public static final String CHANNEL_SENEC_POWER_CONSUMPTION = "houseConsumption";
    public static final String CHANNEL_SENEC_ENERGY_PRODUCTION = "energyProduction";
    public static final String CHANNEL_SENEC_BATTERY_POWER = "batteryPower";
    public static final String CHANNEL_SENEC_BATTERY_FUEL_CHARGE = "batteryFuelCharge";
    public static final String CHANNEL_SENEC_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String CHANNEL_SENEC_BATTERY_CURRENT = "batteryCurrent";
    public static final String CHANNEL_SENEC_CHARGE_MODE = "chargeMode";

    // SenecHomeGrid
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

    // SenecHomeBattery
    public static final String CHANNEL_SENEC_CHARGED_ENERGY_PACK1 = "chargedEnergyPack1";
    public static final String CHANNEL_SENEC_CHARGED_ENERGY_PACK2 = "chargedEnergyPack2";
    public static final String CHANNEL_SENEC_CHARGED_ENERGY_PACK3 = "chargedEnergyPack3";
    public static final String CHANNEL_SENEC_CHARGED_ENERGY_PACK4 = "chargedEnergyPack4";
    public static final String CHANNEL_SENEC_DISCHARGED_ENERGY_PACK1 = "dischargedEnergyPack1";
    public static final String CHANNEL_SENEC_DISCHARGED_ENERGY_PACK2 = "dischargedEnergyPack2";
    public static final String CHANNEL_SENEC_DISCHARGED_ENERGY_PACK3 = "dischargedEnergyPack3";
    public static final String CHANNEL_SENEC_DISCHARGED_ENERGY_PACK4 = "dischargedEnergyPack4";
    public static final String CHANNEL_SENEC_CYCLES_PACK1 = "cyclesPack1";
    public static final String CHANNEL_SENEC_CYCLES_PACK2 = "cyclesPack2";
    public static final String CHANNEL_SENEC_CYCLES_PACK3 = "cyclesPack3";
    public static final String CHANNEL_SENEC_CYCLES_PACK4 = "cyclesPack4";
    public static final String CHANNEL_SENEC_CURRENT_PACK1 = "currentPack1";
    public static final String CHANNEL_SENEC_CURRENT_PACK2 = "currentPack2";
    public static final String CHANNEL_SENEC_CURRENT_PACK3 = "currentPack3";
    public static final String CHANNEL_SENEC_CURRENT_PACK4 = "currentPack4";
    public static final String CHANNEL_SENEC_VOLTAGE_PACK1 = "voltagePack1";
    public static final String CHANNEL_SENEC_VOLTAGE_PACK2 = "voltagePack2";
    public static final String CHANNEL_SENEC_VOLTAGE_PACK3 = "voltagePack3";
    public static final String CHANNEL_SENEC_VOLTAGE_PACK4 = "voltagePack4";
    public static final String CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK1 = "maxCellVoltagePack1";
    public static final String CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK2 = "maxCellVoltagePack2";
    public static final String CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK3 = "maxCellVoltagePack3";
    public static final String CHANNEL_SENEC_MAX_CELL_VOLTAGE_PACK4 = "maxCellVoltagePack4";
    public static final String CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK1 = "minCellVoltagePack1";
    public static final String CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK2 = "minCellVoltagePack2";
    public static final String CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK3 = "minCellVoltagePack3";
    public static final String CHANNEL_SENEC_MIN_CELL_VOLTAGE_PACK4 = "minCellVoltagePack4";

    // SenecHomeTemperature
    public static final String CHANNEL_SENEC_BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String CHANNEL_SENEC_CASE_TEMPERATURE = "caseTemperature";
    public static final String CHANNEL_SENEC_MCU_TEMPERATURE = "mcuTemperature";

    // SenecHomeWallbox
    public static final String CHANNEL_SENEC_WALLBOX1_STATE = "wallbox1State";
    public static final String CHANNEL_SENEC_WALLBOX1_STATE_VALUE = "wallbox1StateValue";
    public static final String CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH1 = "wallbox1ChargingCurrentPhase1";
    public static final String CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH2 = "wallbox1ChargingCurrentPhase2";
    public static final String CHANNEL_SENEC_WALLBOX1_CHARGING_CURRENT_PH3 = "wallbox1ChargingCurrentPhase3";
    public static final String CHANNEL_SENEC_WALLBOX1_CHARGING_POWER = "wallbox1ChargingPower";

    // Charge Mode Definitions
    public static final String STATE_SENEC_CHARGE_MODE_OFF = "OFF";
    public static final String STATE_SENEC_CHARGE_MODE_CHARGE = "CHARGE";
    public static final String STATE_SENEC_CHARGE_MODE_STORAGE = "STORAGE";
}
