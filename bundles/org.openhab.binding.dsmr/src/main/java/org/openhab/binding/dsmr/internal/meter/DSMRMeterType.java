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
package org.openhab.binding.dsmr.internal.meter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supported meters
 *
 * @author M. Volaart - Initial contribution
 */
@NonNullByDefault
public enum DSMRMeterType {
    // Don't auto format the enum list. For readability the format for the enum is:
    // First line parameters; DSMRMeterKind and CosemObjectType (identification object type)
    // 2nd line and further required CosemObjectType
    // optional CosemObjectType start on a new line
    //@formatter:off

    /** DSMR V2 / V3 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V2_V3(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN,
            CosemObjectType.P1_TEXT_CODE, CosemObjectType.P1_TEXT_STRING),

    /** DSMR V4 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V4(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN,
            CosemObjectType.P1_TEXT_CODE, CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_VERSION_OUTPUT,
            CosemObjectType.P1_TIMESTAMP),

    /** DSMR V5 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V5(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN,
            new CosemObjectType[] {
                    CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_VERSION_OUTPUT, CosemObjectType.P1_TIMESTAMP },
            new CosemObjectType[] {
                    CosemObjectType.P1_TEXT_STRING_LONG }),

    /** ACE4000 Electricity */
    ELECTRICITY_ACE4000(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF0,
                    CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF0,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_ACTIVE_IMPORT_POWER, CosemObjectType.EMETER_SWITCH_POSITION },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_DELIVERY_TARIFF0_ANTIFRAUD,
                    CosemObjectType.EMETER_DELIVERY_TARIFF1_ANTIFRAUD, CosemObjectType.EMETER_DELIVERY_TARIFF2_ANTIFRAUD,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TRESHOLD_A }),

    /** ACE4000 Gas meter */
    GAS_ACE4000(DSMRMeterKind.GAS, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GMETER_24H_DELIVERY_V2,
            CosemObjectType.GMETER_VALVE_POSITION_V2_2),

    /** ACE4000 Heating meter */
    HEATING_ACE4000(DSMRMeterKind.HEATING, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.HMETER_VALUE_V2),

    /** ACE4000 Cooling meter */
    COOLING_ACE4000(DSMRMeterKind.COOLING, CosemObjectType.UNKNOWN,
            CosemObjectType.CMETER_VALUE_V2),

    /** ACE4000 Water meter */
    WATER_ACE4000(DSMRMeterKind.WATER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.WMETER_VALUE_V2),

    /** ACE4000 first Slave electricity meter */
    SLAVE_ELECTRICITY1_ACE4000(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.UNKNOWN,
            CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1,
            CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF0,
            CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2,
            CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTIVE_IMPORT_POWER,
            CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION),

    /** ACE4000 second Slave electricity meter */
    SLAVE_ELECTRICITY2_ACE4000(DSMRMeterKind.SLAVE_ELECTRICITY2, CosemObjectType.UNKNOWN,
            CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1,
            CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF0,
            CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2,
            CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTIVE_IMPORT_POWER,
            CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION),

    /** DSMR V2.1 Electricity meter */
    ELECTRICITY_V2_1(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X,
            CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_DELIVERY_TARIFF1,
            CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
            CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
            CosemObjectType.EMETER_TRESHOLD_A_V2_1, CosemObjectType.EMETER_SWITCH_POSITION_V2_1,
            CosemObjectType.EMETER_ACTUAL_DELIVERY),

    /** DSMR V2.1 Gas meter */
    GAS_V2_1(DSMRMeterKind.GAS, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2,
            CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_24H_DELIVERY_V2,
            CosemObjectType.GMETER_24H_DELIVERY_COMPENSATED_V2, CosemObjectType.GMETER_VALVE_POSITION_V2_1),

    /** DSMR V2.2 Electricity meter */
    ELECTRICITY_V2_2(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X,
            CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_DELIVERY_TARIFF1,
            CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
            CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
            CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.METER_VALVE_SWITCH_POSITION,
            CosemObjectType.EMETER_ACTUAL_DELIVERY),

    /** DSMR V2.2 Gas meter */
    GAS_V2_2(DSMRMeterKind.GAS, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2,
            CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_24H_DELIVERY_V2,
            CosemObjectType.GMETER_24H_DELIVERY_COMPENSATED_V2, CosemObjectType.GMETER_VALVE_POSITION_V2_2),

    /** DSMR V2.2 Heating meter */
    HEATING_V2_2(DSMRMeterKind.HEATING, CosemObjectType.HMETER_EQUIPMENT_IDENTIFIER_V2_2,
            CosemObjectType.HMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.HMETER_VALUE_V2),

    /** DSMR V2.2 Cooling meter */
    COOLING_V2_2(DSMRMeterKind.COOLING, CosemObjectType.CMETER_EQUIPMENT_IDENTIFIER_V2_2,
            CosemObjectType.CMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.CMETER_VALUE_V2),

    /** DSMR V2.2 Water meter */
    WATER_V2_2(DSMRMeterKind.WATER, CosemObjectType.WMETER_EQUIPMENT_IDENTIFIER_V2_2,
            CosemObjectType.WMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.WMETER_VALUE_V2),

    /** DSMR V3.0 Electricity meter */
    ELECTRICITY_V3_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION},
            new CosemObjectType[] {
                    CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_TRESHOLD_KW,
                    CosemObjectType.EMETER_SWITCH_POSITION}),

    /** DSMR V3.0 Gas meter */
    GAS_V3_0(DSMRMeterKind.GAS, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
                    CosemObjectType.GMETER_VALUE_V3},
            new CosemObjectType[] {
                    CosemObjectType.METER_VALVE_SWITCH_POSITION}),

    /** DSMR V3.0 Water meter */
    WATER_V3_0(DSMRMeterKind.WATER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.WMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V3.0 GJ meter (heating, cooling) */
    GJ_V3_0(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.GJMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V3.0 Generic meter */
    GENERIC_V3_0(DSMRMeterKind.GENERIC, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.GENMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V4.0 Electricity meter */
    ELECTRICITY_V4_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_TRESHOLD_KW, CosemObjectType.EMETER_SWITCH_POSITION,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1 },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L3 }),

    /** DSMR V4 m3 meter (gas, water) */
    M3_V4(DSMRMeterKind.M3, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.M3METER_VALUE, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V4 GJ meter (heating, cooling) */
    GJ_V4(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.GJMETER_VALUE_V4, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V4 Slave Electricity meter */
    SLAVE_ELECTRICITY_V4(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.EMETER_VALUE, CosemObjectType.EMETER_SWITCH_POSITION),

    /** DSMR V4.0.4 Electricity meter */
    ELECTRICITY_V4_0_4(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_TRESHOLD_KW, CosemObjectType.EMETER_SWITCH_POSITION,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1 },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3 }),

    /** DSMR V4.2 Electricity meter (specification not available, implemented by reverse engineering */
    ELECTRICITY_V4_2(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1 },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3 }),

    /** DSMR V5.0 Electricity meter */
    ELECTRICITY_V5_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1, CosemObjectType.EMETER_INSTANT_VOLTAGE_L1 },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3, CosemObjectType.EMETER_INSTANT_VOLTAGE_L2,
                    CosemObjectType.EMETER_INSTANT_VOLTAGE_L3 }),

    /** DSMR V5.0 m3 meter (gas, water) */
    M3_V5_0(DSMRMeterKind.M3, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.M3METER_VALUE),

    /** DSMR V5.0 GJ meter (heating, cooling) */
    GJ_V5_0(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.GJMETER_VALUE_V4),

    /** DSMR V5.0 Slave Electricity meter */
    SLAVE_ELECTRICITY_V5_0(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER,
            CosemObjectType.EMETER_VALUE),

    /** Luxembourg "Smarty" V1.0 Electricity meter */
    ELECTRICITY_SMARTY_V1_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_DELIVERY_TARIFF0,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF0, CosemObjectType.EMETER_TOTAL_IMPORTED_ENERGY_REGISTER_Q,
                    CosemObjectType.EMETER_TOTAL_EXPORTED_ENERGY_REGISTER_Q, CosemObjectType.EMETER_ACTUAL_DELIVERY,
                    CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_ACTUAL_REACTIVE_DELIVERY,
                    CosemObjectType.EMETER_ACTUAL_REACTIVE_PRODUCTION,
                    CosemObjectType.EMETER_SWITCH_POSITION },
            new CosemObjectType[] {
                    CosemObjectType.EMETER_TRESHOLD_KW, CosemObjectType.EMETER_ACTIVE_THRESHOLD_SMAX,
                    CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_VOLTAGE_SAGS_L1,
                    CosemObjectType.EMETER_VOLTAGE_SAGS_L2, CosemObjectType.EMETER_VOLTAGE_SAGS_L3,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2,
                    CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L1,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L2, CosemObjectType.EMETER_INSTANT_CURRENT_L3,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3,
                    CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L1,
                    CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L3,
    }),
    /** Austrian "Smarty" meter */
    ELECTRICITY_SMARTY_V1_0_AUSTRIA(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.UNKNOWN,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF0,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_ACTUAL_REACTIVE_DELIVERY, CosemObjectType.EMETER_ACTUAL_REACTIVE_PRODUCTION,
                    CosemObjectType.EMETER_TOTAL_IMPORTED_ENERGY_REGISTER_Q, CosemObjectType.EMETER_TOTAL_EXPORTED_ENERGY_REGISTER_Q,
                    CosemObjectType.EMETER_TOTAL_IMPORTED_ENERGY_REGISTER_R_RATE1, CosemObjectType.EMETER_TOTAL_IMPORTED_ENERGY_REGISTER_R_RATE2,
                    CosemObjectType.EMETER_TOTAL_EXPORTED_ENERGY_REGISTER_R_RATE1, CosemObjectType.EMETER_TOTAL_EXPORTED_ENERGY_REGISTER_R_RATE2,
    },
            new CosemObjectType[] {
                    CosemObjectType.P1_VERSION_OUTPUT, CosemObjectType.P1_TIMESTAMP,
    }),

    /** Belgium Smart Meter for the e-MUCS specification */
    DEVICE_EMUCS_V1_0(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN,
            CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_EMUCS_VERSION_OUTPUT,
            CosemObjectType.P1_TIMESTAMP),

    /** Belgium Smart Electricity Meter for the e-MUCS specification */
    ELECTRICITY_EMUCS_V1_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1,
                    CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1,
                    CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR,
                    CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION,
                    CosemObjectType.EMETER_TRESHOLD_KW, CosemObjectType.EMETER_FUSE_THRESHOLD_A,
                    CosemObjectType.EMETER_SWITCH_POSITION},
            new CosemObjectType[] {
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2,
                    CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1,
                    CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_CURRENT_L2,
                    CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_VOLTAGE_L1,
                    CosemObjectType.EMETER_INSTANT_VOLTAGE_L2, CosemObjectType.EMETER_INSTANT_VOLTAGE_L3
    }),

    /** Belgium Smart Gas Meter for the e-MUCS specification */
    GAS_EMUCS_V1_0(DSMRMeterKind.GAS, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER,
            new CosemObjectType[] {
                    CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE,
                    CosemObjectType.GMETER_LAST_VALUE, CosemObjectType.METER_VALVE_SWITCH_POSITION });
    // @formatter:on

    public static final Set<ThingTypeUID> METER_THING_TYPES = Arrays.asList(DSMRMeterType.values()).stream()
            .map(DSMRMeterType::getThingTypeUID).collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterType.class);

    /**
     * Meter kind
     */
    public final DSMRMeterKind meterKind;

    /**
     * Required objects for this meter type
     */
    public final CosemObjectType[] requiredCosemObjects;

    /**
     * Additional object this meter type can receive
     */
    public final CosemObjectType[] optionalCosemObjects;

    /**
     * All objects this meter type can receive (convenience for {requiredCosemObjects, optionalCosemObjects})
     */
    public final CosemObjectType[] supportedCosemObjects;

    /**
     * Which CosemObjectType is used to identify this meter
     */
    public final CosemObjectType cosemObjectTypeMeterId;

    /**
     * Creates a new enum
     *
     * @param meterKind kind of meter
     * @param cosemObjectTypeMeterId identifier cosem object
     * @param requiredCosemObjects list of objects that are present in this meter type
     */
    DSMRMeterType(final DSMRMeterKind meterKind, final CosemObjectType cosemObjectTypeMeterId,
            final CosemObjectType... requiredCosemObjects) {
        this(meterKind, cosemObjectTypeMeterId, requiredCosemObjects, new CosemObjectType[0]);
    }

    /**
     * Creates a new enum
     *
     * @param meterKind kind of meter
     * @param cosemObjectTypeMeterId identifier cosem object
     * @param requiredCosemObjects list of objects that are present in this meter type
     * @param optionalCosemObjects list of objects that are optional present in this meter type
     */
    DSMRMeterType(final DSMRMeterKind meterKind, final CosemObjectType cosemObjectTypeMeterId,
            final CosemObjectType[] requiredCosemObjects, final CosemObjectType[] optionalCosemObjects) {
        this.meterKind = meterKind;
        this.cosemObjectTypeMeterId = cosemObjectTypeMeterId;
        this.requiredCosemObjects = requiredCosemObjects;
        this.optionalCosemObjects = optionalCosemObjects;

        supportedCosemObjects = new CosemObjectType[requiredCosemObjects.length + optionalCosemObjects.length];
        System.arraycopy(requiredCosemObjects, 0, supportedCosemObjects, 0, requiredCosemObjects.length);
        System.arraycopy(optionalCosemObjects, 0, supportedCosemObjects, requiredCosemObjects.length,
                optionalCosemObjects.length);
    }

    /**
     * Returns if this DSMRMeterType is compatible for the Cosem Objects.
     *
     * If successful the real OBIS identification message (including the actual channel and identification value)
     * is returned.
     * If the meter is compatible but the meter type has no identification message, a message is created using the
     * UNKNOWN OBISMsgType and no value.
     * If the meter is not compatible, null is returned
     *
     *
     * @param availableCosemObjects the Cosem Objects to detect if the current meter compatible
     * @return {@link DSMRMeterDescriptor} containing the identification of the compatible meter
     */
    public @Nullable DSMRMeterDescriptor findCompatible(final List<CosemObject> availableCosemObjects) {
        final Map<@Nullable Integer, AtomicInteger> channelCounter = new HashMap<>(3);

        for (final CosemObjectType objectType : requiredCosemObjects) {
            final AtomicBoolean match = new AtomicBoolean();
            availableCosemObjects.stream().filter(a -> a.getType() == objectType).forEach(b -> {
                match.set(true);
                final Integer channel = b.getObisIdentifier().getChannel();

                if (channel != null) {
                    channelCounter.computeIfAbsent(channel, t -> new AtomicInteger()).incrementAndGet();
                }
            });
            if (!match.get()) {
                logger.trace("Required objectType {} not found for meter: {}", objectType, this);
                return null;
            }
        }
        DSMRMeterDescriptor meterDescriptor = null;

        if (meterKind.isChannelRelevant()) {
            final Optional<Entry<@Nullable Integer, AtomicInteger>> max = channelCounter.entrySet().stream()
                    .max((e1, e2) -> Integer.compare(e1.getValue().get(), e2.getValue().get()));

            if (max.isPresent()) {
                final Integer channel = max.get().getKey();
                meterDescriptor = new DSMRMeterDescriptor(this,
                        channel == null ? DSMRMeterConstants.UNKNOWN_CHANNEL : channel);
            }
        } else {
            meterDescriptor = new DSMRMeterDescriptor(this, DSMRMeterConstants.UNKNOWN_CHANNEL);
        }

        // Meter type is compatible, check if an identification exists
        if (meterDescriptor == null && cosemObjectTypeMeterId == CosemObjectType.UNKNOWN) {
            logger.trace("Meter type {} has no identification, but is compatible", this);
            meterDescriptor = new DSMRMeterDescriptor(this, DSMRMeterConstants.UNKNOWN_CHANNEL);
        } else if (meterDescriptor != null) {
            logger.trace("Meter type is compatible and has the following meter type:{}", this);
        }
        return meterDescriptor;
    }

    /**
     * Returns the ThingTypeUID for this meterType
     *
     * @return {@link ThingTypeUID} containing the unique identifier for this meter type
     */
    public ThingTypeUID getThingTypeUID() {
        return new ThingTypeUID(DSMRBindingConstants.BINDING_ID, name().toLowerCase());
    }
}
