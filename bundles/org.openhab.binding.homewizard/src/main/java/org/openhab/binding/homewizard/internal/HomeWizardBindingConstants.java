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
package org.openhab.binding.homewizard.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HomeWizardBindingConstants} class defines common constants, which are
 * used across the full binding.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Added constants
 *
 */
@NonNullByDefault
public class HomeWizardBindingConstants {

    private static final String BINDING_ID = "homewizard";

    // List of supported devices
    public static final String HWE_P1 = "hwe-p1";
    public static final String HWE_SKT = "hwe-skt";
    public static final String HWE_WTR = "hwe-wtr";
    public static final String HWE_KWH = "hwe-kwh";
    public static final String HWE_KWH1 = "hwe-kwh1";
    public static final String HWE_KWH3 = "hwe-kwh3";
    public static final String SDM230_WIFI = "sdm230-wifi";
    public static final String SDM630_WIFI = "sdm630-wifi";
    public static final String HWE_BAT = "hwe-bat";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HWE_P1 = new ThingTypeUID(BINDING_ID, HWE_P1);
    public static final ThingTypeUID THING_TYPE_HWE_SKT = new ThingTypeUID(BINDING_ID, HWE_SKT);
    public static final ThingTypeUID THING_TYPE_HWE_WTR = new ThingTypeUID(BINDING_ID, HWE_WTR);
    public static final ThingTypeUID THING_TYPE_HWE_KWH = new ThingTypeUID(BINDING_ID, HWE_KWH);
    public static final ThingTypeUID THING_TYPE_HWE_BAT = new ThingTypeUID(BINDING_ID, HWE_BAT);

    public static final ThingTypeUID THING_TYPE_P1_METER = new ThingTypeUID(BINDING_ID, "p1_wifi_meter");
    public static final ThingTypeUID THING_TYPE_ENERGY_SOCKET = new ThingTypeUID(BINDING_ID, "energy_socket");
    public static final ThingTypeUID THING_TYPE_WATERMETER = new ThingTypeUID(BINDING_ID, "watermeter");

    // Channel Groups
    public static final String CHANNEL_GROUP_ENERGY = "energy";
    public static final String CHANNEL_GROUP_WATER = "water";
    public static final String CHANNEL_GROUP_SKT_CONTROL = "control";

    // Energy Meter Channels
    public static final String CHANNEL_ENERGY_IMPORT = "energy_import";
    public static final String CHANNEL_ENERGY_IMPORT_T1 = "energy_import_t1";
    public static final String CHANNEL_ENERGY_IMPORT_T2 = "energy_import_t2";
    public static final String LEGACY_CHANNEL_ENERGY_IMPORT_T1 = "total_energy_import_t1";
    public static final String LEGACY_CHANNEL_ENERGY_IMPORT_T2 = "total_energy_import_t2";

    public static final String CHANNEL_ENERGY_EXPORT = "energy_export";
    public static final String CHANNEL_ENERGY_EXPORT_T1 = "energy_export_t1";
    public static final String CHANNEL_ENERGY_EXPORT_T2 = "energy_export_t2";
    public static final String LEGACY_CHANNEL_ENERGY_EXPORT_T1 = "total_energy_export_t1";
    public static final String LEGACY_CHANNEL_ENERGY_EXPORT_T2 = "total_energy_export_t2";

    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_POWER_L1 = "power_l1";
    public static final String CHANNEL_POWER_L2 = "power_l2";
    public static final String CHANNEL_POWER_L3 = "power_l3";
    public static final String LEGACY_CHANNEL_POWER = "active_power";
    public static final String LEGACY_CHANNEL_POWER_L1 = "active_power_l1";
    public static final String LEGACY_CHANNEL_POWER_L2 = "active_power_l2";
    public static final String LEGACY_CHANNEL_POWER_L3 = "active_power_l3";

    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_VOLTAGE_L1 = "voltage_l1";
    public static final String CHANNEL_VOLTAGE_L2 = "voltage_l2";
    public static final String CHANNEL_VOLTAGE_L3 = "voltage_l3";
    public static final String LEGACY_CHANNEL_VOLTAGE = "active_voltage";
    public static final String LEGACY_CHANNEL_VOLTAGE_L1 = "active_voltage_l1";
    public static final String LEGACY_CHANNEL_VOLTAGE_L2 = "active_voltage_l2";
    public static final String LEGACY_CHANNEL_VOLTAGE_L3 = "active_voltage_l3";

    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_CURRENT_L1 = "current_l1";
    public static final String CHANNEL_CURRENT_L2 = "current_l2";
    public static final String CHANNEL_CURRENT_L3 = "current_l3";
    public static final String LEGACY_CHANNEL_CURRENT = "active_current";
    public static final String LEGACY_CHANNEL_CURRENT_L1 = "active_current_l1";
    public static final String LEGACY_CHANNEL_CURRENT_L2 = "active_current_l2";
    public static final String LEGACY_CHANNEL_CURRENT_L3 = "active_current_l3";

    public static final String CHANNEL_FREQUENCY = "frequency";

    // P1 Meter Channels
    public static final String CHANNEL_POWER_FAILURES = "power_failures";
    public static final String CHANNEL_LONG_POWER_FAILURES = "long_power_failures";

    public static final String CHANNEL_GAS_TIMESTAMP = "gas_timestamp";
    public static final String CHANNEL_GAS_TOTAL = "total_gas";

    // Energy Socket And kWh Meter Channels
    public static final String CHANNEL_REACTIVE_POWER = "reactive_power";
    public static final String CHANNEL_APPARENT_POWER = "apparent_power";
    public static final String CHANNEL_POWER_FACTOR = "power_factor";

    // Water Meter Channels
    public static final String CHANNEL_ACTIVE_LITER = "active_liter";
    public static final String CHANNEL_TOTAL_LITER = "total_liter";
    public static final String LEGACY_CHANNEL_ACTIVE_LITER = "active_water";
    public static final String LEGACY_CHANNEL_TOTAL_LITER = "total_water";

    // Plug-In Battery Channels
    public static final String CHANNEL_STATE_OF_CHARGE = "state_of_charge";
    public static final String CHANNEL_CYCLES = "cycles";

    // Energy Socket Control Channels
    public static final String CHANNEL_POWER_SWITCH = "power_switch";
    public static final String CHANNEL_POWER_LOCK = "power_lock";
    public static final String CHANNEL_RING_BRIGHTNESS = "ring_brightness";

    //
    public static final String PROPERTY_METER_MODEL = "meterModel";
    public static final String PROPERTY_METER_VERSION = "meterVersion";
}
