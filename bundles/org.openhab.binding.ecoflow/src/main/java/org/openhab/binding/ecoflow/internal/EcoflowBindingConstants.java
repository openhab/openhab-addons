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
package org.openhab.binding.ecoflow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EcoflowBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcoflowBindingConstants {
    private static final String BINDING_ID = "ecoflow";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "ecoflow-api");
    public static final ThingTypeUID THING_TYPE_DELTA2 = new ThingTypeUID(BINDING_ID, "delta2");
    public static final ThingTypeUID THING_TYPE_DELTA2MAX = new ThingTypeUID(BINDING_ID, "delta2-max");
    public static final ThingTypeUID THING_TYPE_POWERSTREAM = new ThingTypeUID(BINDING_ID, "powerstream");

    public class DeltaChannels {
        public static final String CHANNEL_ID_BATTERY_SOC = "status#battery-level";
        public static final String CHANNEL_ID_INPUT_POWER = "status#input-power";
        public static final String CHANNEL_ID_OUTPUT_POWER = "status#output-power";
        public static final String CHANNEL_ID_REMAINING_CHARGE_TIME = "status#remaining-charge-time";
        public static final String CHANNEL_ID_REMAINING_DISCHARGE_TIME = "status#remaining-discharge-time";

        public static final String CHANNEL_ID_BATTERY_TEMPERATURE = "battery#temperature";
        public static final String CHANNEL_ID_BATTERY_VOLTAGE = "battery#voltage";
        public static final String CHANNEL_ID_BATTERY_CURRENT = "battery#current";
        public static final String CHANNEL_ID_BATTERY_CHARGER_TYPE = "battery#charger-type";
        public static final String CHANNEL_ID_BATTERY_CHARGE_LIMIT = "battery#charge-limit";
        public static final String CHANNEL_ID_BATTERY_DISCHARGE_LIMIT = "battery#discharge-limit";

        public static final String CHANNEL_ID_AC_IN_CHARGING_POWER = "ac-input#set-charging-power";
        public static final String CHANNEL_ID_AC_IN_VOLTAGE = "ac-input#voltage";
        public static final String CHANNEL_ID_AC_IN_CURRENT = "ac-input#current";
        public static final String CHANNEL_ID_AC_IN_POWER = "ac-input#power";
        public static final String CHANNEL_ID_AC_IN_FREQUENCY = "ac-input#frequency";
        public static final String CHANNEL_ID_AC_IN_ENERGY = "ac-input#total-energy";

        public static final String CHANNEL_ID_AC_OUT_ENABLED = "ac-output#enabled";
        public static final String CHANNEL_ID_AC_OUT_XBOOST_ENABLED = "ac-output#xboost-enabled";
        public static final String CHANNEL_ID_AC_OUT_VOLTAGE = "ac-output#voltage";
        public static final String CHANNEL_ID_AC_OUT_CURRENT = "ac-output#current";
        public static final String CHANNEL_ID_AC_OUT_POWER = "ac-output#power";
        public static final String CHANNEL_ID_AC_OUT_FREQUENCY = "ac-output#frequency";
        public static final String CHANNEL_ID_AC_OUT_ENERGY = "ac-output#total-energy";
        public static final String CHANNEL_ID_AC_OUT_TEMPERATURE = "ac-output#temperature";

        public static final String CHANNEL_ID_USB_OUT_ENABLED = "dc-output#usb-enabled";
        public static final String CHANNEL_ID_12V_OUT_ENABLED = "dc-output#12v-enabled";
        public static final String CHANNEL_ID_USB1_OUTPUT_POWER = "dc-output#usb1-power";
        public static final String CHANNEL_ID_USB2_OUTPUT_POWER = "dc-output#usb2-power";
        public static final String CHANNEL_ID_QCUSB1_OUTPUT_POWER = "dc-output#qc-usb1-power";
        public static final String CHANNEL_ID_QCUSB2_OUTPUT_POWER = "dc-output#qc-usb2-power";
        public static final String CHANNEL_ID_USBC1_OUTPUT_POWER = "dc-output#usbc1-power";
        public static final String CHANNEL_ID_USBC2_OUTPUT_POWER = "dc-output#usbc2-power";
        public static final String CHANNEL_ID_12V_OUT_VOLTAGE = "dc-output#12v-out-voltage";
        public static final String CHANNEL_ID_12V_OUT_CURRENT = "dc-output#12v-out-current";
        public static final String CHANNEL_ID_12V_OUT_POWER = "dc-output#12v-out-power";
        public static final String CHANNEL_ID_DC_OUT_ENERGY = "dc-output#total-energy";

        public static final String CHANNEL_ID_PV_IN_VOLTAGE = "solar-input#voltage";
        public static final String CHANNEL_ID_PV_IN_CURRENT = "solar-input#current";
        public static final String CHANNEL_ID_PV_IN_POWER = "solar-input#power";
        public static final String CHANNEL_ID_PV_IN_STATE = "solar-input#charge-state";
        public static final String CHANNEL_ID_PV_IN_TYPE = "solar-input#input-type";
        public static final String CHANNEL_ID_SOLAR_ENERGY = "solar-input#total-energy";
        public static final String CHANNEL_ID_EXTRA_BATTERY_POWER = "battery#extra-battery-power";

        public static final String CHANNEL_ID_MAX_PV2_IN_VOLTAGE = "solar-input#voltage2";
        public static final String CHANNEL_ID_MAX_PV2_IN_CURRENT = "solar-input#current2";
        public static final String CHANNEL_ID_MAX_PV2_IN_POWER = "solar-input#power2";
        public static final String CHANNEL_ID_MAX_PV2_IN_STATE = "solar-input#charge-state2";
        public static final String CHANNEL_ID_MAX_PV2_IN_TYPE = "solar-input#input-type2";
        public static final String CHANNEL_ID_MAX_EXTRA_BATTERY2_POWER = "battery#extra-battery2-power";
    }

    // PowerStream channels
    public class PowerStreamChannels {
        public static final String CHANNEL_ID_INV_STATUS = "inverter#status";
        public static final String CHANNEL_ID_AC_IN_VOLTAGE = "inverter#input-voltage";
        public static final String CHANNEL_ID_AC_IN_FREQUENCY = "inverter#input-frequency";
        public static final String CHANNEL_ID_AC_OUT_POWER = "inverter#output-power";
        public static final String CHANNEL_ID_AC_OUT_TARGET_POWER = "inverter#output-target-power";
        public static final String CHANNEL_ID_SUPPLY_PRIORITY = "inverter#supply-priority";
        public static final String CHANNEL_ID_BATTERY_VOLTAGE = "battery-input#voltage";
        public static final String CHANNEL_ID_BATTERY_CURRENT = "battery-input#current";
        public static final String CHANNEL_ID_BATTERY_POWER = "battery-input#power";
        public static final String CHANNEL_ID_BATTERY_TEMPERATURE = "battery-input#temperature";
        public static final String CHANNEL_ID_BATTERY_ACTIVE = "battery-input#active";
        public static final String CHANNEL_ID_BATTERY_LEVEL = "battery-input#battery-level";
        public static final String CHANNEL_ID_BATTERY_CHARGE_LIMIT = "battery-input#charge-limit";
        public static final String CHANNEL_ID_BATTERY_DISCHARGE_LIMIT = "battery-input#discharge-limit";
        public static final String CHANNEL_ID_PV1_IN_VOLTAGE = "pv1-input#voltage";
        public static final String CHANNEL_ID_PV1_IN_TARGET_VOLTAGE = "pv1-input#voltage-target";
        public static final String CHANNEL_ID_PV1_IN_CURRENT = "pv1-input#current";
        public static final String CHANNEL_ID_PV1_IN_POWER = "pv1-input#power";
        public static final String CHANNEL_ID_PV1_MPPT_ACTIVE = "pv1-input#mppt-active";
        public static final String CHANNEL_ID_PV2_IN_VOLTAGE = "pv2-input#voltage";
        public static final String CHANNEL_ID_PV2_IN_TARGET_VOLTAGE = "pv2-input#voltage-target";
        public static final String CHANNEL_ID_PV2_IN_CURRENT = "pv2-input#current";
        public static final String CHANNEL_ID_PV2_IN_POWER = "pv2-input#power";
        public static final String CHANNEL_ID_PV2_MPPT_ACTIVE = "pv2-input#mppt-active";
    }
}
