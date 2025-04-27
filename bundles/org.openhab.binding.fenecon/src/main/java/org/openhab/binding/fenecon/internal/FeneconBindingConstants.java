/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fenecon.internal.api.Address;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FeneconBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconBindingConstants {

    private static final String BINDING_ID = "fenecon";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HOME_DEVICE = new ThingTypeUID(BINDING_ID, "home-device");

    // List of all FENECON Addresses
    // Group: _sum/...
    public static final String STATE_ADDRESS = "_sum/State";
    public static final String ESS_SOC_ADDRESS = "_sum/EssSoc";
    public static final String CONSUMPTION_ACTIVE_POWER_ADDRESS = "_sum/ConsumptionActivePower";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE1_ADDRESS = "_sum/ConsumptionActivePowerL1";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE2_ADDRESS = "_sum/ConsumptionActivePowerL2";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE3_ADDRESS = "_sum/ConsumptionActivePowerL3";
    public static final String CONSUMPTION_MAX_ACTIVE_POWER_ADDRESS = "_sum/ConsumptionMaxActivePower";
    public static final String PRODUCTION_MAX_ACTIVE_POWER_ADDRESS = "_sum/ProductionMaxActivePower";
    public static final String PRODUCTION_ACTIVE_POWER_ADDRESS = "_sum/ProductionActivePower";
    public static final String GRID_ACTIVE_POWER_ADDRESS = "_sum/GridActivePower";
    public static final String ESS_DISCHARGE_POWER_ADDRESS = "_sum/EssDischargePower";
    public static final String GRID_MODE_ADDRESS = "_sum/GridMode";
    public static final String GRID_SELL_ACTIVE_ENERGY_ADDRESS = "_sum/GridSellActiveEnergy";
    public static final String GRID_BUY_ACTIVE_ENERGY_ADDRESS = "_sum/GridBuyActiveEnergy";
    // Group: _meta/...
    public static final String FEMS_VERSION_ADDRESS = "_meta/Version";
    // Group: batteryInverter0/...
    public static final String BATT_INVERTER_AIR_TEMP_ADDRESS = "batteryInverter0/AirTemperature";
    public static final String BATT_INVERTER_RADIATOR_TEMP_ADDRESS = "batteryInverter0/RadiatorTemperature";
    public static final String BATT_INVERTER_BMS_PACK_TEMP_ADDRESS = "batteryInverter0/BmsPackTemperature";
    // Group: battery0/...
    public static final String BATT_TOWER_PACK_VOLTAGE_ADDRESS = "battery0/Tower0PackVoltage";
    public static final String BATT_TOWER_CURRENT_ADDRESS = "battery0/Current";
    public static final String BATT_SOH_ADDRESS = "battery0/Soh";
    // Group: charger0/...
    public static final String CHARGER0_ACTUAL_POWER_ADDRESS = "charger0/ActualPower";
    public static final String CHARGER0_VOLTAGE_ADDRESS = "charger0/Voltage";
    public static final String CHARGER0_CURRENT_ADDRESS = "charger0/Current";
    // Group: charger1/...
    public static final String CHARGER1_ACTUAL_POWER_ADDRESS = "charger1/ActualPower";
    public static final String CHARGER1_VOLTAGE_ADDRESS = "charger1/Voltage";
    public static final String CHARGER1_CURRENT_ADDRESS = "charger1/Current";
    // Group: charger2/...
    public static final String CHARGER2_ACTUAL_POWER_ADDRESS = "charger2/ActualPower";
    public static final String CHARGER2_VOLTAGE_ADDRESS = "charger2/Voltage";
    public static final String CHARGER2_CURRENT_ADDRESS = "charger2/Current";

    // Group of all FENECON Addresses
    public static final List<Address> ADDRESSES = List.of(new Address(STATE_ADDRESS), new Address(GRID_MODE_ADDRESS),
            new Address(CONSUMPTION_ACTIVE_POWER_ADDRESS), new Address(CONSUMPTION_ACTIVE_POWER_PHASE1_ADDRESS),
            new Address(CONSUMPTION_ACTIVE_POWER_PHASE2_ADDRESS), new Address(CONSUMPTION_ACTIVE_POWER_PHASE3_ADDRESS),
            new Address(CONSUMPTION_MAX_ACTIVE_POWER_ADDRESS), new Address(PRODUCTION_MAX_ACTIVE_POWER_ADDRESS),
            new Address(PRODUCTION_ACTIVE_POWER_ADDRESS), new Address(GRID_ACTIVE_POWER_ADDRESS),
            new Address(GRID_BUY_ACTIVE_ENERGY_ADDRESS), new Address(GRID_SELL_ACTIVE_ENERGY_ADDRESS),
            new Address(ESS_SOC_ADDRESS), new Address(ESS_DISCHARGE_POWER_ADDRESS), new Address(FEMS_VERSION_ADDRESS),
            new Address(BATT_INVERTER_AIR_TEMP_ADDRESS), new Address(BATT_INVERTER_RADIATOR_TEMP_ADDRESS),
            new Address(BATT_INVERTER_BMS_PACK_TEMP_ADDRESS), new Address(BATT_TOWER_PACK_VOLTAGE_ADDRESS),
            new Address(BATT_TOWER_CURRENT_ADDRESS), new Address(BATT_SOH_ADDRESS),
            new Address(CHARGER0_ACTUAL_POWER_ADDRESS), new Address(CHARGER1_ACTUAL_POWER_ADDRESS),
            new Address(CHARGER2_ACTUAL_POWER_ADDRESS), new Address(CHARGER0_VOLTAGE_ADDRESS),
            new Address(CHARGER1_VOLTAGE_ADDRESS), new Address(CHARGER2_VOLTAGE_ADDRESS),
            new Address(CHARGER0_CURRENT_ADDRESS), new Address(CHARGER1_CURRENT_ADDRESS),
            new Address(CHARGER2_CURRENT_ADDRESS));

    // List of all Channel IDs
    public static final String STATE_CHANNEL = "state";
    public static final String ESS_SOC_CHANNEL = "ess-soc";
    public static final String CONSUMPTION_ACTIVE_POWER_CHANNEL = "consumption-active-power";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE1_CHANNEL = "consumption-active-power-l1";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE2_CHANNEL = "consumption-active-power-l2";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE3_CHANNEL = "consumption-active-power-l3";
    public static final String CONSUMPTION_MAX_ACTIVE_POWER_CHANNEL = "consumption-max-active-power";
    public static final String PRODUCTION_MAX_ACTIVE_POWER_CHANNEL = "production-max-active-power";
    public static final String PRODUCTION_ACTIVE_POWER_CHANNEL = "production-active-power";
    public static final String EXPORT_TO_GRID_POWER_CHANNEL = "export-to-grid-power";
    public static final String IMPORT_FROM_GRID_POWER_CHANNEL = "import-from-grid-power";
    public static final String ESS_CHARGER_POWER_CHANNEL = "charger-power";
    public static final String ESS_DISCHARGER_POWER_CHANNEL = "discharger-power";
    public static final String EMERGENCY_POWER_MODE_CHANNEL = "emergency-power-mode";
    public static final String EXPORTED_TO_GRID_ENERGY_CHANNEL = "exported-to-grid-energy";
    public static final String IMPORTED_FROM_GRID_ENERGY_CHANNEL = "imported-from-grid-energy";
    public static final String LAST_UPDATE_CHANNEL = "last-update";
    public static final String FEMS_VERSION_CHANNEL = "fems-version";
    public static final String BATT_INVERTER_AIR_TEMP_CHANNEL = "inverter-air-temperature";
    public static final String BATT_INVERTER_RADIATOR_TEMP_CHANNEL = "inverter-radiator-temperature";
    public static final String BATT_INVERTER_BMS_PACK_TEMP_CHANNEL = "bms-pack-temperature";
    public static final String BATT_TOWER_PACK_VOLTAGE_CHANNEL = "batt-tower-voltage";
    public static final String BATT_TOWER_CURRENT_CHANNEL = "batt-tower-current";
    public static final String BATT_SOH_CHANNEL = "batt-tower-soh";
    public static final String CHARGER0_ACTUAL_POWER_CHANNEL = "charger0-actual-power";
    public static final String CHARGER1_ACTUAL_POWER_CHANNEL = "charger1-actual-power";
    public static final String CHARGER2_ACTUAL_POWER_CHANNEL = "charger2-actual-power";
    public static final String CHARGER0_VOLTAGE_CHANNEL = "charger0-voltage";
    public static final String CHARGER1_VOLTAGE_CHANNEL = "charger1-voltage";
    public static final String CHARGER2_VOLTAGE_CHANNEL = "charger2-voltage";
    public static final String CHARGER0_CURRENT_CHANNEL = "charger0-current";
    public static final String CHARGER1_CURRENT_CHANNEL = "charger1-current";
    public static final String CHARGER2_CURRENT_CHANNEL = "charger2-current";
}
