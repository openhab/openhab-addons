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
package org.openhab.binding.modbus.ankersolix.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AnkerSolixBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixBindingConstants {

    public static final ThingTypeUID THING_TYPE_SOLARBANK_4 = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ankersolix-solarbank4");
    public static final ThingTypeUID THING_TYPE_SOLARBANK_AC = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ankersolix-solarbank-ac");
    public static final ThingTypeUID THING_TYPE_SMART_METER_GEN2 = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ankersolix-smartmeter-gen2");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ankersolix-smartplug");
    public static final ThingTypeUID THING_TYPE_EV_CHARGER = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ankersolix-ev-charger");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SOLARBANK_4,
            THING_TYPE_SOLARBANK_AC, THING_TYPE_SMART_METER_GEN2, THING_TYPE_SMART_PLUG, THING_TYPE_EV_CHARGER);

    public static final String DISCOVERY_PROPERTY_UNIQUE_ADDRESS = "uniqueAddress";
    public static final String DISCOVERY_PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String DISCOVERY_PROPERTY_MODEL = "model";
    public static final String DISCOVERY_PROPERTY_DEVICE_FAMILY = "deviceFamily";

    public static final String DEVICE_FAMILY_SOLARBANK = "solarbank";
    public static final String DEVICE_FAMILY_SMART_METER_GEN2 = "smartmeter-gen2";
    public static final String DEVICE_FAMILY_SMART_PLUG = "smartplug";
    public static final String DEVICE_FAMILY_EV_CHARGER = "ev-charger";

    public static final Map<String, String> PRODUCT_CODE_TO_MODEL = Map.ofEntries(
            Map.entry("DN7M", "Anker SOLIX Solarbank 4 E5000 Pro"),
            Map.entry("DPM4", "Anker SOLIX Solarbank 4 E5000 Pro"), Map.entry("DMWH", "Anker SOLIX Solarbank Max AC"),
            Map.entry("DMXU", "Anker SOLIX Solarbank Max AC"), Map.entry("E25H", "Anker SOLIX Solarbank Max AC"),
            Map.entry("QNA", "Anker SOLIX Smart Plug"), Map.entry("DNSL", "Anker SOLIX Smart Meter Gen 2"),
            Map.entry("DNSM", "Anker SOLIX Smart Meter Gen 2"), Map.entry("DNMS", "Anker SOLIX XE AC"),
            Map.entry("DPP4", "Anker SOLIX XE AC"), Map.entry("DNN3", "Anker SOLIX XE AC"),
            Map.entry("A519", "Anker SOLIX V1 Smart EV Charger"));

    public static final String CHANNEL_DEVICE_MODEL = "device-model";
    public static final String CHANNEL_DEVICE_SERIAL_NUMBER = "device-serial-number";
    public static final String CHANNEL_DEVICE_SW_VERSION = "device-sw-version";
    public static final String CHANNEL_BATTERY_SOC = "battery-soc";
    public static final String CHANNEL_PV_POWER = "pv-power";
    public static final String CHANNEL_BATTERY_CHARGING_POWER = "battery-charging-power";
    public static final String CHANNEL_BATTERY_DISCHARGING_POWER = "battery-discharging-power";
    public static final String CHANNEL_LOAD_POWER = "load-power";
    public static final String CHANNEL_GRID_IMPORT_POWER = "grid-import-power";
    public static final String CHANNEL_GRID_EXPORT_POWER = "grid-export-power";
    public static final String CHANNEL_AC_GRID_OUTPUT_POWER = "ac-grid-output-power";
    public static final String CHANNEL_PV_TOTAL_GENERATION = "pv-total-generation";
    public static final String CHANNEL_CUMULATIVE_CHARGE_ENERGY = "cumulative-charge-energy";
    public static final String CHANNEL_CUMULATIVE_DISCHARGE_ENERGY = "cumulative-discharge-energy";
    public static final String CHANNEL_OPERATING_MODE = "operating-mode";
    public static final String CHANNEL_BATTERY_POWER_DIRECTION = "battery-power-direction";
    public static final String CHANNEL_BATTERY_POWER_SETPOINT = "battery-power-setpoint";

    public static final String CHANNEL_METER_TYPE = "meter-type";
    public static final String CHANNEL_PRIMARY_TOTAL_ACTIVE_POWER = "primary-total-active-power";
    public static final String CHANNEL_PRIMARY_PHASE_1_ACTIVE_POWER = "primary-phase-1-active-power";
    public static final String CHANNEL_PRIMARY_PHASE_1_CURRENT = "primary-phase-1-current";
    public static final String CHANNEL_PRIMARY_PHASE_1_VOLTAGE = "primary-phase-1-voltage";
    public static final String CHANNEL_PRIMARY_PHASE_2_ACTIVE_POWER = "primary-phase-2-active-power";
    public static final String CHANNEL_PRIMARY_PHASE_2_CURRENT = "primary-phase-2-current";
    public static final String CHANNEL_PRIMARY_PHASE_2_VOLTAGE = "primary-phase-2-voltage";
    public static final String CHANNEL_PRIMARY_PHASE_3_ACTIVE_POWER = "primary-phase-3-active-power";
    public static final String CHANNEL_PRIMARY_PHASE_3_CURRENT = "primary-phase-3-current";
    public static final String CHANNEL_PRIMARY_PHASE_3_VOLTAGE = "primary-phase-3-voltage";
    public static final String CHANNEL_SECONDARY_TOTAL_ACTIVE_POWER = "secondary-total-active-power";
    public static final String CHANNEL_SECONDARY_PHASE_1_ACTIVE_POWER = "secondary-phase-1-active-power";
    public static final String CHANNEL_SECONDARY_PHASE_1_CURRENT = "secondary-phase-1-current";
    public static final String CHANNEL_SECONDARY_PHASE_1_VOLTAGE = "secondary-phase-1-voltage";
    public static final String CHANNEL_SECONDARY_PHASE_2_ACTIVE_POWER = "secondary-phase-2-active-power";
    public static final String CHANNEL_SECONDARY_PHASE_2_CURRENT = "secondary-phase-2-current";
    public static final String CHANNEL_SECONDARY_PHASE_2_VOLTAGE = "secondary-phase-2-voltage";
    public static final String CHANNEL_SECONDARY_PHASE_3_ACTIVE_POWER = "secondary-phase-3-active-power";
    public static final String CHANNEL_SECONDARY_PHASE_3_CURRENT = "secondary-phase-3-current";
    public static final String CHANNEL_SECONDARY_PHASE_3_VOLTAGE = "secondary-phase-3-voltage";

    public static final String CHANNEL_REAL_TIME_POWER = "real-time-power";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_SWITCH_STATUS = "switch-status";
    public static final String CHANNEL_POWER_SWITCH = "power-switch";

    public static final String CHANNEL_PRODUCT_NUMBER = "product-number";
    public static final String CHANNEL_DEVICE_HW_VERSION = "device-hw-version";
    public static final String CHANNEL_RATED_POWER = "rated-power";
    public static final String CHANNEL_MINIMUM_OUTPUT_CURRENT = "minimum-output-current";
    public static final String CHANNEL_MAXIMUM_OUTPUT_CURRENT = "maximum-output-current";
    public static final String CHANNEL_ALARM_INFORMATION_1 = "alarm-information-1";
    public static final String CHANNEL_ALARM_INFORMATION_2 = "alarm-information-2";
    public static final String CHANNEL_ALARM_INFORMATION_3 = "alarm-information-3";
    public static final String CHANNEL_ALARM_INFORMATION_4 = "alarm-information-4";
    public static final String CHANNEL_ALARM_INFORMATION_5 = "alarm-information-5";
    public static final String CHANNEL_ALARM_INFORMATION_6 = "alarm-information-6";
    public static final String CHANNEL_ALARM_INFORMATION_7 = "alarm-information-7";
    public static final String CHANNEL_ALARM_INFORMATION_8 = "alarm-information-8";
    public static final String CHANNEL_ALARM_INFORMATION_9 = "alarm-information-9";
    public static final String CHANNEL_ALARM_INFORMATION_10 = "alarm-information-10";
    public static final String CHANNEL_ALARM_INFORMATION_11 = "alarm-information-11";
    public static final String CHANNEL_ALARM_INFORMATION_12 = "alarm-information-12";
    public static final String CHANNEL_L1_N_VOLTAGE = "l1-n-voltage";
    public static final String CHANNEL_L2_N_VOLTAGE = "l2-n-voltage";
    public static final String CHANNEL_L3_N_VOLTAGE = "l3-n-voltage";
    public static final String CHANNEL_L1_L2_VOLTAGE = "l1-l2-voltage";
    public static final String CHANNEL_L2_L3_VOLTAGE = "l2-l3-voltage";
    public static final String CHANNEL_L3_L1_VOLTAGE = "l3-l1-voltage";
    public static final String CHANNEL_L1_CURRENT = "l1-current";
    public static final String CHANNEL_L2_CURRENT = "l2-current";
    public static final String CHANNEL_L3_CURRENT = "l3-current";
    public static final String CHANNEL_L1_ACTIVE_POWER = "l1-active-power";
    public static final String CHANNEL_L2_ACTIVE_POWER = "l2-active-power";
    public static final String CHANNEL_L3_ACTIVE_POWER = "l3-active-power";
    public static final String CHANNEL_TOTAL_CHARGING_ACTIVE_POWER = "total-charging-active-power";
    public static final String CHANNEL_L1_REACTIVE_POWER = "l1-reactive-power";
    public static final String CHANNEL_L2_REACTIVE_POWER = "l2-reactive-power";
    public static final String CHANNEL_L3_REACTIVE_POWER = "l3-reactive-power";
    public static final String CHANNEL_L1_APPARENT_POWER = "l1-apparent-power";
    public static final String CHANNEL_L2_APPARENT_POWER = "l2-apparent-power";
    public static final String CHANNEL_L3_APPARENT_POWER = "l3-apparent-power";
    public static final String CHANNEL_CURRENT_CHARGING_SESSION_DURATION = "current-charging-session-duration";
    public static final String CHANNEL_CURRENT_CHARGING_CAPACITY = "current-charging-capacity";
    public static final String CHANNEL_PWM_ENABLED_STATUS = "pwm-enabled-status";
    public static final String CHANNEL_SINGLE_THREE_PHASE_OPERATING_MODE = "single-three-phase-operating-mode";
    public static final String CHANNEL_CHARGING_MODE = "charging-mode";
    public static final String CHANNEL_LOAD_BALANCING_ENABLED_STATUS = "load-balancing-enabled-status";
    public static final String CHANNEL_SOLAR_POWER_BALANCING_ENABLED_STATUS = "solar-power-balancing-enabled-status";
    public static final String CHANNEL_CP_ACQUISITION_VOLTAGE = "cp-acquisition-voltage";
    public static final String CHANNEL_CP_SIGNAL_STATUS = "cp-signal-status";
    public static final String CHANNEL_RELAY_1_TEMPERATURE = "relay-1-temperature";
    public static final String CHANNEL_RELAY_2_TEMPERATURE = "relay-2-temperature";
    public static final String CHANNEL_BOOST_MODE = "boost-mode";
    public static final String CHANNEL_LED_LIGHT_BRIGHTNESS = "led-light-brightness";
    public static final String CHANNEL_CHARGING_STATUS = "charging-status";
    public static final String CHANNEL_OCPP_CONNECTION_STATUS = "ocpp-connection-status";
    public static final String CHANNEL_MQTT_CONNECTION_STATUS = "mqtt-connection-status";
    public static final String CHANNEL_CHARGING_COMMAND = "charging-command";
    public static final String CHANNEL_MAXIMUM_CURRENT_SETTING = "maximum-current-setting";
    public static final String CHANNEL_BOOST_MODE_COMMAND = "boost-mode-command";
    public static final String CHANNEL_SET_TIMEOUT = "set-timeout";
    public static final String CHANNEL_SET_NUMBER_OF_CHARGING_PHASES = "set-number-of-charging-phases";

    public static @Nullable String resolveModelFromSerial(@Nullable String serialNumber) {
        if (serialNumber == null || serialNumber.length() < 3) {
            return null;
        }

        String upperSerial = serialNumber.toUpperCase(Locale.ROOT);
        if (upperSerial.length() >= 4) {
            String productCode4 = upperSerial.substring(0, 4);
            String mapped4 = PRODUCT_CODE_TO_MODEL.get(productCode4);
            if (mapped4 != null) {
                return mapped4;
            }
        }

        String productCode3 = upperSerial.substring(0, 3);
        return PRODUCT_CODE_TO_MODEL.get(productCode3);
    }
}
