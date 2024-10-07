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
package org.openhab.binding.homewizard.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HomeWizardBindingConstants} class defines common constants, which are
 * used across the full binding.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardBindingConstants {

    private static final String BINDING_ID = "homewizard";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_P1_METER = new ThingTypeUID(BINDING_ID, "p1_wifi_meter");
    public static final ThingTypeUID THING_TYPE_ENERGY_SOCKET = new ThingTypeUID(BINDING_ID, "energy_socket");
    public static final ThingTypeUID THING_TYPE_WATERMETER = new ThingTypeUID(BINDING_ID, "watermeter");

    // List of all Channel ids
    public static final String CHANNEL_ACTIVE_CURRENT = "active_current";
    public static final String CHANNEL_ACTIVE_CURRENT_L1 = "active_current_l1";
    public static final String CHANNEL_ACTIVE_CURRENT_L2 = "active_current_l2";
    public static final String CHANNEL_ACTIVE_CURRENT_L3 = "active_current_l3";
    public static final String CHANNEL_ACTIVE_POWER = "active_power";
    public static final String CHANNEL_ACTIVE_POWER_L1 = "active_power_l1";
    public static final String CHANNEL_ACTIVE_POWER_L2 = "active_power_l2";
    public static final String CHANNEL_ACTIVE_POWER_L3 = "active_power_l3";
    public static final String CHANNEL_ACTIVE_VOLTAGE = "active_voltage";
    public static final String CHANNEL_ACTIVE_VOLTAGE_L1 = "active_voltage_l1";
    public static final String CHANNEL_ACTIVE_VOLTAGE_L2 = "active_voltage_l2";
    public static final String CHANNEL_ACTIVE_VOLTAGE_L3 = "active_voltage_l3";
    public static final String CHANNEL_POWER_FAILURES = "power_failures";
    public static final String CHANNEL_LONG_POWER_FAILURES = "long_power_failures";
    public static final String CHANNEL_ENERGY_IMPORT_T1 = "total_energy_import_t1";
    public static final String CHANNEL_ENERGY_IMPORT_T2 = "total_energy_import_t2";
    public static final String CHANNEL_ENERGY_EXPORT_T1 = "total_energy_export_t1";
    public static final String CHANNEL_ENERGY_EXPORT_T2 = "total_energy_export_t2";

    public static final String CHANNEL_GAS_TIMESTAMP = "gas_timestamp";
    public static final String CHANNEL_GAS_TOTAL = "total_gas";
    public static final String CHANNEL_TOTAL_WATER = "total_water";
    public static final String CHANNEL_CURRENT_WATER = "current_water";

    public static final String CHANNEL_POWER_SWITCH = "power_switch";
    public static final String CHANNEL_POWER_LOCK = "power_lock";
    public static final String CHANNEL_RING_BRIGHTNESS = "ring_brightness";

    public static final String PROPERTY_METER_MODEL = "meterModel";
    public static final String PROPERTY_METER_VERSION = "meterVersion";
}
