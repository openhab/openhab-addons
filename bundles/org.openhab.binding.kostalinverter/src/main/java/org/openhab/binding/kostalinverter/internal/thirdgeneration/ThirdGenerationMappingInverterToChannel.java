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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

import static org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link ThirdGenerationMappingInverterToChannel} is responsible for the management of the channels
 * available to inverters
 *
 * @author René Stakemeier - Initial contribution
 */
class ThirdGenerationMappingInverterToChannel {

    private static final Map<ThirdGenerationInverterTypes, List<ThirdGenerationChannelMappingToWebApi>> CHANNEL_MAPPING = new HashMap<>();

    /*
     * Assign the channels to the devices.
     */
    static {
        List<ThirdGenerationInverterTypes> allInvertersList = Arrays.asList(ThirdGenerationInverterTypes.values());
        List<ThirdGenerationInverterTypes> allBatteryInvertersList = Stream
                .of(ThirdGenerationInverterTypes.PLENTICORE_PLUS_42_WITH_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_55_WITH_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_70_WITH_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_85_WITH_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_100_WITH_BATTERY)
                .collect(Collectors.toList());
        List<ThirdGenerationInverterTypes> allInvertersWithThreeStringsList = Stream
                .of(ThirdGenerationInverterTypes.PLENTICORE_PLUS_42_WITHOUT_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_55_WITHOUT_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_70_WITHOUT_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_85_WITHOUT_BATTERY,
                        ThirdGenerationInverterTypes.PLENTICORE_PLUS_100_WITHOUT_BATTERY)
                .collect(Collectors.toList());

        // Channels available on all devices
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_DC_POWER, "devices:local", "Dc_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID, "devices:local",
                "HomeGrid_P", ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_OWNCONSUMPTION, "devices:local", "HomeOwn_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV, "devices:local", "HomePv_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL, "devices:local", "Home_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE, "devices:local", "LimitEvuAbs",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_LIMIT_EVU_RELATIV, "devices:local", "LimitEvuRel",
                ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_WORKTIME, "devices:local", "WorkTime",
                ThirdGenerationChannelDatatypes.SECONDS);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_COS_PHI, "devices:local:ac", "CosPhi",
                ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_FREQUENCY, "devices:local:ac", "Frequency",
                ThirdGenerationChannelDatatypes.HERTZ);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE, "devices:local:ac",
                "L1_I", ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER, "devices:local:ac", "L1_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE, "devices:local:ac",
                "L1_U", ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE, "devices:local:ac",
                "L2_I", ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER, "devices:local:ac", "L2_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE, "devices:local:ac",
                "L2_U", ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE, "devices:local:ac",
                "L3_I", ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER, "devices:local:ac", "L3_P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE, "devices:local:ac",
                "L3_U", ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_AC_CURRENT_POWER, "devices:local:ac", "P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_1_AMPERAGE, "devices:local:pv1", "I",
                ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_1_POWER, "devices:local:pv1", "P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_1_VOLTAGE, "devices:local:pv1", "U",
                ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_2_AMPERAGE, "devices:local:pv2", "I",
                ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_2_POWER, "devices:local:pv2", "P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersList, CHANNEL_DEVICE_LOCAL_PVSTRING_2_VOLTAGE, "devices:local:pv2", "U",
                ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allInvertersList, CHANNEL_SCB_EVENT_ERROR_COUNT_MC, "scb:event", "ErrMc",
                ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allInvertersList, CHANNEL_SCB_EVENT_ERROR_COUNT_SFH, "scb:event", "ErrSFH",
                ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allInvertersList, CHANNEL_SCB_EVENT_ERROR_COUNT_SCB, "scb:event", "Event:ActiveErrorCnt",
                ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allInvertersList, CHANNEL_SCB_EVENT_WARNING_COUNT_SCB, "scb:event", "Event:ActiveWarningCnt",
                ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_AUTARKY_DAY, "scb:statistic:EnergyFlow",
                "Statistic:Autarky:Day", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_AUTARKY_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:Autarky:Month", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_AUTARKY_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:Autarky:Total", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_AUTARKY_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:Autarky:Year", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_CO2SAVING_DAY, "scb:statistic:EnergyFlow",
                "Statistic:CO2Saving:Day", ThirdGenerationChannelDatatypes.KILOGRAM);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_CO2SAVING_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:CO2Saving:Month", ThirdGenerationChannelDatatypes.KILOGRAM);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_CO2SAVING_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:CO2Saving:Total", ThirdGenerationChannelDatatypes.KILOGRAM);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_CO2SAVING_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:CO2Saving:Year", ThirdGenerationChannelDatatypes.KILOGRAM);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_DAY, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHome:Day", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHome:Month", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHome:Total", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHome:Year", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeGrid:Day",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeGrid:Month",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeGrid:Total",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeGrid:Year",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomePv:Day", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomePv:Month",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomePv:Total",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:EnergyHomePv:Year", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_DAY, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Day", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Month", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Total", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:OwnConsumptionRate:Year", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_YIELD_DAY, "scb:statistic:EnergyFlow",
                "Statistic:Yield:Day", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_YIELD_MONTH, "scb:statistic:EnergyFlow",
                "Statistic:Yield:Month", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_YIELD_TOTAL, "scb:statistic:EnergyFlow",
                "Statistic:Yield:Total", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allInvertersList, CHANNEL_STATISTIC_YIELD_YEAR, "scb:statistic:EnergyFlow",
                "Statistic:Yield:Year", ThirdGenerationChannelDatatypes.KILOWATT_HOUR);

        // Plenticore Plus devices can be expanded with a battery.
        // Additional channels become available, but the pv3 information are hidden (since the battery is attached
        // there)
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_LOADING_CYCLES,
                "devices:local:battery", "Cycles", ThirdGenerationChannelDatatypes.INTEGER);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY,
                "devices:local:battery", "FullChargeCap_E", ThirdGenerationChannelDatatypes.AMPERE_HOUR);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_AMPERAGE, "devices:local:battery", "I",
                ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_POWER, "devices:local:battery", "P",
                ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE,
                "devices:local:battery", "SoC", ThirdGenerationChannelDatatypes.PERCEMTAGE);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_BATTERY_VOLTAGE, "devices:local:battery", "U",
                ThirdGenerationChannelDatatypes.VOLT);
        addInverterChannel(allBatteryInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeBat:Day",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allBatteryInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeBat:Month",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allBatteryInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeBat:Total",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allBatteryInvertersList, CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR,
                "scb:statistic:EnergyFlow", "Statistic:EnergyHomeBat:Year",
                ThirdGenerationChannelDatatypes.KILOWATT_HOUR);
        addInverterChannel(allBatteryInvertersList, CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY, "devices:local",
                "HomeBat_P", ThirdGenerationChannelDatatypes.WATT);
        // Plenticore devices without battery have the pv3 settings available
        addInverterChannel(allInvertersWithThreeStringsList, CHANNEL_DEVICE_LOCAL_PVSTRING_3_AMPERAGE,
                "devices:local:pv3", "I", ThirdGenerationChannelDatatypes.AMPERE);
        addInverterChannel(allInvertersWithThreeStringsList, CHANNEL_DEVICE_LOCAL_PVSTRING_3_POWER, "devices:local:pv3",
                "P", ThirdGenerationChannelDatatypes.WATT);
        addInverterChannel(allInvertersWithThreeStringsList, CHANNEL_DEVICE_LOCAL_PVSTRING_3_VOLTAGE,
                "devices:local:pv3", "U", ThirdGenerationChannelDatatypes.VOLT);
    }

    static Map<String, List<ThirdGenerationChannelMappingToWebApi>> getModuleToChannelsMappingForInverter(
            ThirdGenerationInverterTypes inverter) {
        Map<String, List<ThirdGenerationChannelMappingToWebApi>> results = new HashMap<>();
        for (ThirdGenerationChannelMappingToWebApi mapping : CHANNEL_MAPPING.get(inverter)) {
            List<ThirdGenerationChannelMappingToWebApi> channelList = null;
            if (results.containsKey(mapping.moduleId)) {
                channelList = results.get(mapping.moduleId);
            } else {
                channelList = new ArrayList<>();
                results.put(mapping.moduleId, channelList);
            }
            channelList.add(mapping);
        }
        return results;
    }

    private static void addInverterChannel(ThirdGenerationInverterTypes inverter,
            ThirdGenerationChannelMappingToWebApi mapping) {
        if (!CHANNEL_MAPPING.containsKey(inverter)) {
            CHANNEL_MAPPING.put(inverter, new ArrayList<>());
        }
        CHANNEL_MAPPING.get(inverter).add(mapping);
    }

    private static void addInverterChannel(ThirdGenerationInverterTypes inverter, String channelUID, String moduleId,
            String processdataId, ThirdGenerationChannelDatatypes dataType) {
        addInverterChannel(inverter,
                new ThirdGenerationChannelMappingToWebApi(channelUID, moduleId, processdataId, dataType));
    }

    private static void addInverterChannel(List<ThirdGenerationInverterTypes> inverterList, String channelUID,
            String moduleId, String processdataId, ThirdGenerationChannelDatatypes dataType) {
        for (ThirdGenerationInverterTypes inverter : inverterList) {
            addInverterChannel(inverter, channelUID, moduleId, processdataId, dataType);
        }
    }
}
