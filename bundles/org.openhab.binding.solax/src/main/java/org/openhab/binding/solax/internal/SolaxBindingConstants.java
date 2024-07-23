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
package org.openhab.binding.solax.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolaxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxBindingConstants {

    public static final String BINDING_ID = "solax";
    private static final String THING_LOCAL_CONNECT_INVERTER_ID = "local-connect-inverter";
    private static final String THING_LOCAL_CONNECT_CHARGER_ID = "local-connect-charger";
    private static final String THING_CLOUD_CONNECT_INVERTER_ID = "cloud-connect-inverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LOCAL_CONNECT_INVERTER = new ThingTypeUID(BINDING_ID,
            THING_LOCAL_CONNECT_INVERTER_ID);
    public static final ThingTypeUID THING_TYPE_LOCAL_CONNECT_CHARGER = new ThingTypeUID(BINDING_ID,
            THING_LOCAL_CONNECT_CHARGER_ID);
    public static final ThingTypeUID THING_TYPE_CLOUD_CONNECT_INVERTER = new ThingTypeUID(BINDING_ID,
            THING_CLOUD_CONNECT_INVERTER_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_LOCAL_CONNECT_INVERTER,
            THING_TYPE_LOCAL_CONNECT_CHARGER, THING_TYPE_CLOUD_CONNECT_INVERTER);

    // List of properties
    public static final String PROPERTY_INVERTER_TYPE = "inverterType";

    // List of all Channel ids
    // Single phase specific
    public static final String CHANNEL_INVERTER_OUTPUT_POWER = "inverter-output-power";
    public static final String CHANNEL_INVERTER_OUTPUT_CURRENT = "inverter-current";
    public static final String CHANNEL_INVERTER_OUTPUT_VOLTAGE = "inverter-voltage";
    public static final String CHANNEL_INVERTER_OUTPUT_FREQUENCY = "inverter-frequency";
    public static final Set<String> SINGLE_CHANNEL_SPECIFIC_CHANNEL_IDS = Set.of(CHANNEL_INVERTER_OUTPUT_POWER,
            CHANNEL_INVERTER_OUTPUT_CURRENT, CHANNEL_INVERTER_OUTPUT_VOLTAGE, CHANNEL_INVERTER_OUTPUT_FREQUENCY);

    // Three phase specific
    public static final String CHANNEL_INVERTER_OUTPUT_POWER_PHASE1 = "inverter-output-power-phase1";
    public static final String CHANNEL_INVERTER_OUTPUT_POWER_PHASE2 = "inverter-output-power-phase2";
    public static final String CHANNEL_INVERTER_OUTPUT_POWER_PHASE3 = "inverter-output-power-phase3";
    public static final String CHANNEL_INVERTER_TOTAL_OUTPUT_POWER = "inverter-total-output-power";
    public static final String CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1 = "inverter-current-phase1";
    public static final String CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2 = "inverter-current-phase2";
    public static final String CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3 = "inverter-current-phase3";
    public static final String CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1 = "inverter-voltage-phase1";
    public static final String CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2 = "inverter-voltage-phase2";
    public static final String CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3 = "inverter-voltage-phase3";
    public static final String CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1 = "inverter-frequency-phase1";
    public static final String CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2 = "inverter-frequency-phase2";
    public static final String CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3 = "inverter-frequency-phase3";
    public static final String CHANNEL_INVERTER_TEMPERATURE1 = "inverter-temperature1";
    public static final String CHANNEL_INVERTER_TEMPERATURE2 = "inverter-temperature2";
    public static final String CHANNEL_INVERTER_WORKMODE = "inverter-workmode";

    // Generic
    public static final String CHANNEL_INVERTER_PV1_POWER = "pv1-power";
    public static final String CHANNEL_INVERTER_PV1_VOLTAGE = "pv1-voltage";
    public static final String CHANNEL_INVERTER_PV1_CURRENT = "pv1-current";

    public static final String CHANNEL_INVERTER_PV2_POWER = "pv2-power";
    public static final String CHANNEL_INVERTER_PV2_VOLTAGE = "pv2-voltage";
    public static final String CHANNEL_INVERTER_PV2_CURRENT = "pv2-current";

    public static final String CHANNEL_INVERTER_PV_TOTAL_POWER = "pv-total-power";
    public static final String CHANNEL_INVERTER_PV_TOTAL_CURRENT = "pv-total-current";

    public static final String CHANNEL_BATTERY_POWER = "battery-power";
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";
    public static final String CHANNEL_BATTERY_CURRENT = "battery-current";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "battery-temperature";
    public static final String CHANNEL_BATTERY_STATE_OF_CHARGE = "battery-level";

    public static final String CHANNEL_FEED_IN_POWER = "feed-in-power";

    public static final String CHANNEL_TIMESTAMP = "last-update-time";
    public static final String CHANNEL_RAW_DATA = "raw-data";

    // Totals
    public static final String CHANNEL_POWER_USAGE = "power-usage";
    public static final String CHANNEL_TOTAL_ENERGY = "total-energy";
    public static final String CHANNEL_TOTAL_BATTERY_DISCHARGE_ENERGY = "total-battery-discharge-energy";
    public static final String CHANNEL_TOTAL_BATTERY_CHARGE_ENERGY = "total-battery-charge-energy";
    public static final String CHANNEL_TOTAL_PV_ENERGY = "total-pv-energy";
    public static final String CHANNEL_TOTAL_FEED_IN_ENERGY = "total-feed-in-energy";
    public static final String CHANNEL_TOTAL_CONSUMPTION = "total-consumption";

    // Today totals
    public static final String CHANNEL_TODAY_ENERGY = "today-energy";
    public static final String CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY = "today-battery-discharge-energy";
    public static final String CHANNEL_TODAY_BATTERY_CHARGE_ENERGY = "today-battery-charge-energy";
    public static final String CHANNEL_TODAY_FEED_IN_ENERGY = "today-feed-in-energy";
    public static final String CHANNEL_TODAY_CONSUMPTION = "today-consumption";

    // Cloud specific channels
    public static final String CHANNEL_INVERTER_PV3_POWER = "pv3-power";
    public static final String CHANNEL_INVERTER_PV4_POWER = "pv4-power";
    public static final String CHANNEL_INVERTER_OUTPUT_POWER_METER2 = "inverter-meter2-power";
    public static final String CHANNEL_INVERTER_EPS_POWER_R = "inverter-eps-power-r";
    public static final String CHANNEL_INVERTER_EPS_POWER_S = "inverter-eps-power-s";
    public static final String CHANNEL_INVERTER_EPS_POWER_T = "inverter-eps-power-t";

    // EV Charger channels
    public static final String CHANNEL_CHARGER_MODE = "charger-mode";
    public static final String CHANNEL_CHARGER_STATE = "charger-state";
    public static final String CHANNEL_CHARGER_EQ_SINGLE_SESSION = "eq-single-session";
    public static final String CHANNEL_CHARGER_EQ_TOTAL = "eq-total";
    public static final String CHANNEL_CHARGER_OUTPUT_POWER_PHASE1 = "charger-output-power-phase1";
    public static final String CHANNEL_CHARGER_OUTPUT_POWER_PHASE2 = "charger-output-power-phase2";
    public static final String CHANNEL_CHARGER_OUTPUT_POWER_PHASE3 = "charger-output-power-phase3";
    public static final String CHANNEL_CHARGER_TOTAL_OUTPUT_POWER = "charger-total-output-power";
    public static final String CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE1 = "charger-current-phase1";
    public static final String CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE2 = "charger-current-phase2";
    public static final String CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE3 = "charger-current-phase3";
    public static final String CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE1 = "charger-voltage-phase1";
    public static final String CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE2 = "charger-voltage-phase2";
    public static final String CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE3 = "charger-voltage-phase3";
    public static final String CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE1 = "charger-external-current-phase1";
    public static final String CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE2 = "charger-external-current-phase2";
    public static final String CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE3 = "charger-external-current-phase3";
    public static final String CHANNEL_CHARGER_EXTERNAL_POWER_PHASE1 = "charger-external-power-phase1";
    public static final String CHANNEL_CHARGER_EXTERNAL_POWER_PHASE2 = "charger-external-power-phase2";
    public static final String CHANNEL_CHARGER_EXTERNAL_POWER_PHASE3 = "charger-external-power-phase3";
    public static final String CHANNEL_CHARGER_TOTAL_EXTERNAL_POWER = "charger-external-total-power";
    public static final String CHANNEL_CHARGER_PLUG_TEMPERATURE = "charger-plug-temperature";
    public static final String CHANNEL_CHARGER_INTERNAL_TEMPERATURE = "charger-internal-temperature";

    // I18N Keys
    public static final String I18N_KEY_OFFLINE_COMMUNICATION_ERROR_JSON_CANNOT_BE_RETRIEVED = "@text/offline.communication-error.json-cannot-be-retrieved";
    public static final String I18N_KEY_OFFLINE_CONFIGURATION_ERROR_JSON_CANNOT_BE_PARSED = "@text/offline.configuration-error.json-cannot-be-parsed";
}
