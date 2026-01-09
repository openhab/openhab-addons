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
package org.openhab.binding.modbus.stiebeleltron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link StiebelEltronBindingConstants} class defines common
 * constants, which are used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Added constants for WPM compatible heat pumps
 *
 */
@NonNullByDefault
public class StiebelEltronBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heatpump");
    public static final ThingTypeUID THING_TYPE_STIEBELELTRON_HEATPUMP_ALLWPM = new ThingTypeUID(BINDING_ID,
            "stiebeleltron-heatpump-allwpm");

    // Channel group ids
    public static final String GROUP_SYSTEM_INFO = "systemInformation";
    public static final String GROUP_SYSTEM_PARAMETER = "systemParameter";
    public static final String GROUP_SYSTEM_STATE = "systemState";
    public static final String GROUP_ENERGY_INFO = "energyInformation";

    public static final String GROUP_SYSTEM_INFORMATION_ALLWPM = "systemInformationAllWpm";
    public static final String GROUP_SYSTEM_PARAMETER_ALLWPM = "systemParameterAllWpm";
    public static final String GROUP_SYSTEM_STATE_ALLWPM = "systemStateAllWpm";
    public static final String GROUP_ENERGY_RUNTIME_INFO_ALLWPM = "energyRuntimeInformationAllWpm";

    public static final String GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS = "sgReadyEnergyManagementSettings";
    public static final String GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION = "sgReadyEnergyManagementSystemInformation";

    // List of all Channel ids in device information group
    // Block 1 System Values / Information block
    public static final String CHANNEL_FEK_TEMPERATURE = "fek-temperature";
    public static final String CHANNEL_FEK_TEMPERATURE_SETPOINT = "fek-temperature-setpoint";
    public static final String CHANNEL_FEK_HUMIDITY = "fek-humidity";
    public static final String CHANNEL_FEK_DEWPOINT = "fek-dewpoint";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_HK1_TEMPERATURE = "hk1-temperature";
    public static final String CHANNEL_HK1_TEMPERATURE_SETPOINT = "hk1-temperature-setpoint";
    public static final String CHANNEL_SUPPLY_TEMPERATURE = "supply-temperature";
    public static final String CHANNEL_RETURN_TEMPERATURE = "return-temperature";
    public static final String CHANNEL_SOURCE_TEMPERATURE = "source-temperature";
    public static final String CHANNEL_WATER_TEMPERATURE = "water-temperature";
    public static final String CHANNEL_WATER_TEMPERATURE_SETPOINT = "water-temperature-setpoint";

    public static final String CHANNEL_FE7_TEMPERATURE = "fe7-temperature";
    public static final String CHANNEL_FE7_TEMPERATURE_SETPOINT = "fe7-temperature-setpoint";
    public static final String CHANNEL_HC1_TEMPERATURE = "hc1-temperature";
    public static final String CHANNEL_HC1_TEMPERATURE_SETPOINT = "hc1-temperature-setpoint";
    public static final String CHANNEL_HC2_TEMPERATURE = "hc2-temperature";
    public static final String CHANNEL_HC2_TEMPERATURE_SETPOINT = "hc2-temperature-setpoint";

    public static final String CHANNEL_HP_FLOW_TEMPERATURE = "hp-flow-temperature";
    public static final String CHANNEL_NHZ_FLOW_TEMPERATURE = "nhz-flow-temperature";
    public static final String CHANNEL_FLOW_TEMPERATURE = "flow-temperature";
    public static final String CHANNEL_FIXED_TEMPERATURE_SETPOINT = "fixed-temperature-setpoint";

    public static final String CHANNEL_BUFFER_TEMPERATURE = "buffer-temperature";
    public static final String CHANNEL_BUFFER_TEMPERATURE_SETPOINT = "buffer-temperature-setpoint";

    public static final String CHANNEL_HEATING_PRESSURE = "heating-pressure";
    public static final String CHANNEL_FLOW_RATE = "flow-rate";

    public static final String CHANNEL_HOTWATER_TEMPERATURE = "hotwater-temperature";
    public static final String CHANNEL_HOTWATER_TEMPERATURE_SETPOINT = "hotwater-temperature-setpoint";

    public static final String CHANNEL_FAN_COOLING_TEMPERATURE = "fan-cooling-temperature";
    public static final String CHANNEL_FAN_COOLING_TEMPERATURE_SETPOINT = "fan-cooling-temperature-setpoint";
    public static final String CHANNEL_AREA_COOLING_TEMPERATURE = "area-cooling-temperature";
    public static final String CHANNEL_AREA_COOLING_TEMPERATURE_SETPOINT = "area-cooling-temperature-setpoint";

    public static final String CHANNEL_SOLAR_THERMAL_COLLECTOR_TEMPERATURE = "solar-thermal-collector-temperature";
    public static final String CHANNEL_SOLAR_THERMAL_CYLINDER_TEMPERATURE = "solar-thermal-cylinder-temperature";
    public static final String CHANNEL_SOLAR_THERMAL_RUNTIME = "solar-thermal-runtime";

    public static final String CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE = "external-heat-source-temperature";
    public static final String CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE_SETPOINT = "external-heat-source-temperature-setpoint";
    public static final String CHANNEL_LOWER_APPLICATION_LIMIT_HEATING = "lower-application-limit-heating";
    public static final String CHANNEL_LOWER_APPLICATION_LIMIT_HOTWATER = "lower-application-limit-hotwater";
    public static final String CHANNEL_EXT_HEAT_SOURCE_RUNTIME = "external-heat-source-runtime";

    public static final String CHANNEL_MIN_SOURCE_TEMPERATURE = "min-source-temperature";
    public static final String CHANNEL_SOURCE_PRESSURE = "source-pressure";

    public static final String CHANNEL_HOTGAS_TEMPERATURE = "hotgas-temperature";
    public static final String CHANNEL_HIGH_PRESSURE = "high-pressure";
    public static final String CHANNEL_LOW_PRESSURE = "low-pressure";

    // String formats for HPx info blocks
    public static final String CHANNEL_HP_RETURN_TEMPERATURE_FORMAT = "hp%d-return-temperature";
    public static final String CHANNEL_HP_FLOW_TEMPERATURE_FORMAT = "hp%d-flow-temperature";
    public static final String CHANNEL_HP_HOTGAS_TEMPERATURE_FORMAT = "hp%d-hotgas-temperature";
    public static final String CHANNEL_HP_LOW_PRESSURE_FORMAT = "hp%d-low-pressure";
    public static final String CHANNEL_HP_MEAN_PRESSURE_FORMAT = "hp%d-mean-pressure";
    public static final String CHANNEL_HP_HIGH_PRESSURE_FORMAT = "hp%d-high-pressure";
    public static final String CHANNEL_HP_FLOW_RATE_FORMAT = "hp%d-flow-rate";

    // Block 2 System Parameters (Read/write holding register)
    public static final String CHANNEL_OPERATION_MODE = "operation-mode";
    public static final String CHANNEL_COMFORT_TEMPERATURE_HEATING = "comfort-temperature-heating";
    public static final String CHANNEL_ECO_TEMPERATURE_HEATING = "eco-temperature-heating";
    public static final String CHANNEL_COMFORT_TEMPERATURE_WATER = "comfort-temperature-water";
    public static final String CHANNEL_ECO_TEMPERATURE_WATER = "eco-temperature-water";

    public static final String CHANNEL_OPERATING_MODE = "operating-mode";
    public static final String CHANNEL_HC1_COMFORT_TEMPERATURE = "hc1-comfort-temperature";
    public static final String CHANNEL_HC1_ECO_TEMPERATURE = "hc1-eco-temperature";
    public static final String CHANNEL_HC1_HEATING_CURVE_RISE = "hc1-heating-curve-rise";
    public static final String CHANNEL_HC2_COMFORT_TEMPERATURE = "hc2-comfort-temperature";
    public static final String CHANNEL_HC2_ECO_TEMPERATURE = "hc2-eco-temperature";
    public static final String CHANNEL_HC2_HEATING_CURVE_RISE = "hc2-heating-curve-rise";
    public static final String CHANNEL_FIXED_VALUE_OPERATION = "fixed-value-operation";
    public static final String CHANNEL_HEATING_DUAL_MODE_TEMPERATURE = "heating-dual-mode-temperature";
    public static final String CHANNEL_HOTWATER_COMFORT_TEMPERATURE = "hotwater-comfort-temperature";
    public static final String CHANNEL_HOTWATER_ECO_TEMPERATURE = "hotwater-eco-temperature";
    public static final String CHANNEL_HOTWATER_STAGES = "hotwater-stages";
    public static final String CHANNEL_HOTWATER_DUAL_MODE_TEMPERATURE = "hotwater-dual-mode-temperature";

    public static final String CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_SETPOINT = "area-cooling-flow-temperature-setpoint";
    public static final String CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_HYSTERESIS = "area-cooling-flow-temperature-hysteresis";
    public static final String CHANNEL_AREA_COOLING_ROOM_TEMPERATURE_SETPOINT = "area-cooling-room-temperature-setpoint";
    public static final String CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_SETPOINT = "fan-cooling-flow-temperature-setpoint";
    public static final String CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_HYSTERESIS = "fan-cooling-flow-temperature-hysteresis";
    public static final String CHANNEL_FAN_COOLING_ROOM_TEMPERATURE_SETPOINT = "fan-cooling-room-temperature-setpoint";

    public static final String CHANNEL_RESET = "reset";
    // Reset Commands
    public static final short CHANNEL_RESET_CMD_SYSTEM_RESET = 1;
    public static final short CHANNEL_RESET_CMD_FAULT_LIST_RESET = 2;
    public static final short CHANNEL_RESET_CMD_HEATPUMP_RESET = 3;

    public static final String CHANNEL_RESTART_ISG = "restart-isg";
    // Restart Commands
    public static final short CHANNEL_RESTART_ISG_CMD_RESTART = 1;
    public static final short CHANNEL_RESTART_ISG_CMD_SERVICE_KEY = 2;

    // Block 3 System Status (Read input register)
    public static final String CHANNEL_IS_HEATING = "is-heating";
    public static final String CHANNEL_IS_HEATING_WATER = "is-heating-water";
    public static final String CHANNEL_IS_COOLING = "is-cooling";
    public static final String CHANNEL_IS_PUMPING = "is-pumping";
    public static final String CHANNEL_IS_SUMMER = "is-summer";

    public static final String CHANNEL_HC1_PUMP_ACTIVE = "hc1-pump-active";
    public static final String CHANNEL_HC2_PUMP_ACTIVE = "hc2-pump-active";
    public static final String CHANNEL_HEAT_UP_PROGRAM_ACTIVE = "heat-up-program-active";
    public static final String CHANNEL_NHZ_RUNNING = "nhz-stages-running";
    public static final String CHANNEL_HP_IN_HEATING_MODE = "hp-in-heating-mode";
    public static final String CHANNEL_HP_IN_HOTWATER_MODE = "hp-in-hotwater-mode";
    public static final String CHANNEL_COMPRESSOR_RUNNING = "compressor-running";
    public static final String CHANNEL_SUMMER_MODE_ACTIVE = "summer-mode-active";
    public static final String CHANNEL_COOLING_MODE_ACTIVE = "cooling-mode-active";
    public static final String CHANNEL_MIN_ONE_IWS_IN_DEFROSTING_MODE = "min-one-iws-in-defrosting-mode";
    public static final String CHANNEL_SILENT_MODE1_ACTIVE = "silent-mode1-active";
    public static final String CHANNEL_SILENT_MODE2_ACTIVE = "silent-mode2-active";
    public static final String CHANNEL_POWER_OFF = "power-off";

    // WPM3 only
    public static final String CHANNEL_COMPRESSOR1_ACTIVE = "compressor1-active";
    public static final String CHANNEL_COMPRESSOR2_ACTIVE = "compressor2-active";
    public static final String CHANNEL_COMPRESSOR3_ACTIVE = "compressor3-active";
    public static final String CHANNEL_COMPRESSOR4_ACTIVE = "compressor4-active";
    public static final String CHANNEL_COMPRESSOR5_ACTIVE = "compressor5-active";
    public static final String CHANNEL_COMPRESSOR6_ACTIVE = "compressor6-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP1_ACTIVE = "buffer-charging-pump1-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP2_ACTIVE = "buffer-charging-pump2-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP3_ACTIVE = "buffer-charging-pump3-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP4_ACTIVE = "buffer-charging-pump4-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP5_ACTIVE = "buffer-charging-pump5-active";
    public static final String CHANNEL_BUFFER_CHARGING_PUMP6_ACTIVE = "buffer-charging-pump6-active";
    public static final String CHANNEL_NHZ1_ACTIVE = "nhz1-active";
    public static final String CHANNEL_NHZ2_ACTIVE = "nhz2-active";
    // ---------

    public static final String CHANNEL_FAULT_STATUS = "fault-status";
    public static final String CHANNEL_BUS_STATUS = "bus-status";
    public static final String CHANNEL_DEFROST_INITIATED = "defrost-initiated";
    public static final String CHANNEL_ACTIVE_ERROR = "active-error";

    // Block 4 Energy and Runtime data (Read input register)
    public static final String CHANNEL_PRODUCTION_HEAT_TODAY = "production-heat-today";
    public static final String CHANNEL_PRODUCTION_HEAT_TOTAL = "production-heat-total";
    public static final String CHANNEL_PRODUCTION_WATER_TODAY = "production-water-today";
    public static final String CHANNEL_PRODUCTION_WATER_TOTAL = "production-water-total";
    public static final String CHANNEL_CONSUMPTION_HEAT_TODAY = "consumption-heat-today";
    public static final String CHANNEL_CONSUMPTION_HEAT_TOTAL = "consumption-heat-total";
    public static final String CHANNEL_CONSUMPTION_WATER_TODAY = "consumption-water-today";
    public static final String CHANNEL_CONSUMPTION_WATER_TOTAL = "consumption-water-total";

    public static final String CHANNEL_PRODUCTION_NHZ_HEAT_TOTAL = "production-nhz-heat-total";
    public static final String CHANNEL_PRODUCTION_NHZ_WATER_TOTAL = "production-nhz-water-total";

    public static final String CHANNEL_HP_PRODUCTION_HEAT_TODAY_FORMAT = "hp%d-production-heat-today";
    public static final String CHANNEL_HP_PRODUCTION_HEAT_TOTAL_FORMAT = "hp%d-production-heat-total";
    public static final String CHANNEL_HP_PRODUCTION_WATER_TODAY_FORMAT = "hp%d-production-water-today";
    public static final String CHANNEL_HP_PRODUCTION_WATER_TOTAL_FORMAT = "hp%d-production-water-total";
    public static final String CHANNEL_HP_CONSUMPTION_HEAT_TODAY_FORMAT = "hp%d-consumption-heat-today";
    public static final String CHANNEL_HP_CONSUMPTION_HEAT_TOTAL_FORMAT = "hp%d-consumption-heat-total";
    public static final String CHANNEL_HP_CONSUMPTION_WATER_TODAY_FORMAT = "hp%d-consumption-water-today";
    public static final String CHANNEL_HP_CONSUMPTION_WATER_TOTAL_FORMAT = "hp%d-consumption-water-total";

    public static final String CHANNEL_HP_PRODUCTION_NHZ_HEAT_TOTAL_FORMAT = "hp%d-production-nhz-heat-total";
    public static final String CHANNEL_HP_PRODUCTION_NHZ_WATER_TOTAL_FORMAT = "hp%d-production-nhz-water-total";

    public static final String CHANNEL_HP_CP1_HEATING_RUNTIME_FORMAT = "hp%d-cp1-heating-runtime";
    public static final String CHANNEL_HP_CP2_HEATING_RUNTIME_FORMAT = "hp%d-cp2-heating-runtime";
    public static final String CHANNEL_HP_CP12_HEATING_RUNTIME_FORMAT = "hp%d-cp12-heating-runtime";
    public static final String CHANNEL_HP_CP1_HOTWATER_RUNTIME_FORMAT = "hp%d-cp1-hotwater-runtime";
    public static final String CHANNEL_HP_CP2_HOTWATER_RUNTIME_FORMAT = "hp%d-cp2-hotwater-runtime";
    public static final String CHANNEL_HP_CP12_HOTWATER_RUNTIME_FORMAT = "hp%d-cp-12-hotwater-runtime";
    public static final String CHANNEL_HP_COOLING_RUNTIME_FORMAT = "hp%d-cooling-runtime";
    public static final String CHANNEL_HP_NHZ1_RUNTIME_FORMAT = "hp%d-nhz1-runtime";
    public static final String CHANNEL_HP_NHZ2_RUNTIME_FORMAT = "hp%d-nhz2-runtime";
    public static final String CHANNEL_HP_NHZ12_RUNTIME_FORMAT = "hp%d-nhz12-runtime";

    public static final String CHANNEL_HEATING_RUNTIME = "heating-runtime";
    public static final String CHANNEL_HOTWATER_RUNTIME = "hotwater-runtime";
    public static final String CHANNEL_COOLING_RUNTIME = "cooling-runtime";
    public static final String CHANNEL_NHZ1_RUNTIME = "nhz1-runtime";
    public static final String CHANNEL_NHZ2_RUNTIME = "nhz2-runtime";
    public static final String CHANNEL_NHZ12_RUNTIME = "nhz12-runtime";

    // Block 5 + 6: SG Ready - Energy Management Settings + Information
    public static final String CHANNEL_SG_READY_ON_OFF_SWITCH = "sg-ready-on-off-switch";
    public static final String CHANNEL_SG_READY_INPUT_LINES = "sg-ready-input-lines";
    public static final String CHANNEL_SG_READY_OPERATING_STATE = "sg-ready-operating-state";
    public static final String CHANNEL_SG_READY_CONTROLLER_IDENT = "sg-ready-controller-identification";
}
