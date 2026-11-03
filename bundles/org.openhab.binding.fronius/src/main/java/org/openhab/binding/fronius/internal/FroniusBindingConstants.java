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
package org.openhab.binding.fronius.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FroniusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Hannes Spenger - Added ohmpilot and meter power sum
 * @author Jimmy Tanagra - Implement a common url parsing method
 */
@NonNullByDefault
public class FroniusBindingConstants {

    private static final String BINDING_ID = "fronius";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "powerinverter");
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");
    public static final ThingTypeUID THING_TYPE_OHMPILOT = new ThingTypeUID(BINDING_ID, "ohmpilot");
    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");

    // Inverter channels
    public static final String INVERTER_DATA_CHANNEL_DAY_ENERGY = "day-energy";
    public static final String INVERTER_DATA_CHANNEL_PAC = "ac-power";
    public static final String INVERTER_DATA_CHANNEL_TOTAL = "total-energy";
    public static final String INVERTER_DATA_CHANNEL_YEAR = "year-energy";
    public static final String INVERTER_DATA_CHANNEL_FAC = "ac-frequency";
    public static final String INVERTER_DATA_CHANNEL_IAC = "ac-current";
    public static final String INVERTER_DATA_CHANNEL_IDC = "dc-current";
    public static final String INVERTER_DATA_CHANNEL_IDC2 = "dc-current-2";
    public static final String INVERTER_DATA_CHANNEL_IDC3 = "dc-current-3";
    public static final String INVERTER_DATA_CHANNEL_UAC = "ac-voltage";
    public static final String INVERTER_DATA_CHANNEL_UDC = "dc-voltage";
    public static final String INVERTER_DATA_CHANNEL_UDC2 = "dc-voltage-2";
    public static final String INVERTER_DATA_CHANNEL_UDC3 = "dc-voltage-3";
    public static final String INVERTER_DATA_CHANNEL_PDC = "dc-power";
    public static final String INVERTER_DATA_CHANNEL_PDC2 = "dc-power-2";
    public static final String INVERTER_DATA_CHANNEL_PDC3 = "dc-power-3";
    public static final String INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE = "error-code";
    public static final String INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE = "status-code";

    // Power Flow channels
    public static final String POWER_FLOW_P_GRID = "grid-power";
    public static final String POWER_FLOW_P_LOAD = "load-power";
    public static final String POWER_FLOW_P_AKKU = "battery-power";
    public static final String POWER_FLOW_P_PV = "solar-power";
    public static final String POWER_FLOW_AUTONOMY = "autonomy";
    public static final String POWER_FLOW_SELF_CONSUMPTION = "self-consumption";

    public static final String POWER_FLOW_INVERTER_POWER = "inverter-power";
    public static final String POWER_FLOW_INVERTER_SOC = "battery-soc";

    public static final String POWER_FLOW_BACKUP_MODE = "backup-mode";
    public static final String POWER_FLOW_BATTERY_STANDBY = "battery-standby";

    // For backwards compatibility
    public static final String POWER_FLOW_INVERTER_1_POWER = "powerflowinverter1power";
    public static final String POWER_FLOW_INVERTER_1_SOC = "powerflowinverter1soc";

    // Meter channels
    public static final String METER_ENABLE = "enable";
    public static final String METER_LOCATION = "location";
    public static final String METER_CURRENT_AC_PHASE_1 = "ac-current-phase-1";
    public static final String METER_CURRENT_AC_PHASE_2 = "ac-current-phase-2";
    public static final String METER_CURRENT_AC_PHASE_3 = "ac-current-phase-3";
    public static final String METER_VOLTAGE_AC_PHASE_1 = "ac-voltage-phase-1";
    public static final String METER_VOLTAGE_AC_PHASE_2 = "ac-voltage-phase-2";
    public static final String METER_VOLTAGE_AC_PHASE_3 = "ac-voltage-phase-3";
    public static final String METER_POWER_PHASE_1 = "real-power-phase-1";
    public static final String METER_POWER_PHASE_2 = "real-power-phase-2";
    public static final String METER_POWER_PHASE_3 = "real-power-phase-3";
    public static final String METER_POWER_SUM = "real-power-sum";
    public static final String METER_POWER_FACTOR_PHASE_1 = "power-factor-phase-1";
    public static final String METER_POWER_FACTOR_PHASE_2 = "power-factor-phase-2";
    public static final String METER_POWER_FACTOR_PHASE_3 = "power-factor-phase-3";
    public static final String METER_ENERGY_REAL_SUM_CONSUMED = "real-energy-consumed";
    public static final String METER_ENERGY_REAL_SUM_PRODUCED = "real-energy-produced";

    // OhmPilot channels
    public static final String OHMPILOT_POWER_REAL_SUM = "real-power-sum";
    public static final String OHMPILOT_ENERGY_REAL_SUM_CONSUMED = "real-energy-consumed";
    public static final String OHMPILOT_ENERGY_SENSOR_TEMPERATURE_CHANNEL_1 = "temperature-1";
    public static final String OHMPILOT_ERROR_CODE = "error-code";
    public static final String OHMPILOT_STATE_CODE = "status-code";

    // Battery channels
    public static final String BATTERY_CAPACITY_MAXIMUM = "maximum-capacity";
    public static final String BATTERY_DESIGNED_CAPACITY = "designed-capacity";
    public static final String BATTERY_CURRENT_DC = "dc-current";
    public static final String BATTERY_VOLTAGE_DC = "dc-voltage";
    public static final String BATTERY_STATE_OF_CHARGE = "soc";
    public static final String BATTERY_STATUS_BATTERY_CELL = "status";
    public static final String BATTERY_ENABLE = "enable";
    public static final String BATTERY_TEMPERATURE_CELL = "temperature";
    public static final String BATTERY_TIMESTAMP = "timestamp";

    // Battery settings channels, provided through the inverter's config API
    public static final String BATTERY_SOC_MIN_CHANNEL = "soc-min";
    public static final String BATTERY_SOC_MAX_CHANNEL = "soc-max";
    public static final String BATTERY_BACKUP_RESERVED_CHANNEL = "backup-reserved-capacity";
    public static final String BATTERY_BACKUP_CRITICAL_SOC_CHANNEL = "backup-critical-soc";
    public static final String BATTERY_CHARGE_FROM_GRID_CHANNEL = "charge-from-grid";
    public static final String BATTERY_CALIBRATION_CHANNEL = "calibration";
    public static final String BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL = "night-preservation-limit";
    public static final Set<String> BATTERY_SETTINGS_CHANNELS = Set.of(BATTERY_SOC_MIN_CHANNEL, BATTERY_SOC_MAX_CHANNEL,
            BATTERY_BACKUP_RESERVED_CHANNEL, BATTERY_BACKUP_CRITICAL_SOC_CHANNEL, BATTERY_CHARGE_FROM_GRID_CHANNEL,
            BATTERY_CALIBRATION_CHANNEL, BATTERY_NIGHT_PRESERVATION_LIMIT_CHANNEL);

    /**
     * Maps the ids of deprecated channels to the ids of the channels replacing them.
     * The deprecated channels stay functional, so that existing setups keep working.
     */
    public static final Map<String, String> DEPRECATED_CHANNEL_IDS = Map.ofEntries(
            Map.entry("inverterdatachannelpac", INVERTER_DATA_CHANNEL_PAC),
            Map.entry("inverterdatachannelpdc", INVERTER_DATA_CHANNEL_PDC),
            Map.entry("inverterdatachannelpdc2", INVERTER_DATA_CHANNEL_PDC2),
            Map.entry("inverterdatachannelpdc3", INVERTER_DATA_CHANNEL_PDC3),
            Map.entry("inverterdatachanneldayenergy", INVERTER_DATA_CHANNEL_DAY_ENERGY),
            Map.entry("inverterdatachanneltotal", INVERTER_DATA_CHANNEL_TOTAL),
            Map.entry("inverterdatachannelyear", INVERTER_DATA_CHANNEL_YEAR),
            Map.entry("inverterdatachannelfac", INVERTER_DATA_CHANNEL_FAC),
            Map.entry("inverterdatachanneliac", INVERTER_DATA_CHANNEL_IAC),
            Map.entry("inverterdatachannelidc", INVERTER_DATA_CHANNEL_IDC),
            Map.entry("inverterdatachannelidc2", INVERTER_DATA_CHANNEL_IDC2),
            Map.entry("inverterdatachannelidc3", INVERTER_DATA_CHANNEL_IDC3),
            Map.entry("inverterdatachanneluac", INVERTER_DATA_CHANNEL_UAC),
            Map.entry("inverterdatachanneludc", INVERTER_DATA_CHANNEL_UDC),
            Map.entry("inverterdatachanneludc2", INVERTER_DATA_CHANNEL_UDC2),
            Map.entry("inverterdatachanneludc3", INVERTER_DATA_CHANNEL_UDC3),
            Map.entry("inverterdatadevicestatuserrorcode", INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE),
            Map.entry("inverterdatadevicestatusstatuscode", INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE),
            Map.entry("powerflowchannelpgrid", POWER_FLOW_P_GRID),
            Map.entry("powerflowchannelpload", POWER_FLOW_P_LOAD),
            Map.entry("powerflowchannelpakku", POWER_FLOW_P_AKKU), Map.entry("powerflowchannelppv", POWER_FLOW_P_PV),
            Map.entry("powerflowautonomy", POWER_FLOW_AUTONOMY),
            Map.entry("powerflowselfconsumption", POWER_FLOW_SELF_CONSUMPTION),
            Map.entry("powerflowinverterpower", POWER_FLOW_INVERTER_POWER),
            Map.entry("powerflowinvertersoc", POWER_FLOW_INVERTER_SOC),
            Map.entry("maximumCapacity", BATTERY_CAPACITY_MAXIMUM),
            Map.entry("designedCapacity", BATTERY_DESIGNED_CAPACITY), Map.entry("currentDc", BATTERY_CURRENT_DC),
            Map.entry("voltageDc", BATTERY_VOLTAGE_DC), Map.entry("currentacphase1", METER_CURRENT_AC_PHASE_1),
            Map.entry("currentacphase2", METER_CURRENT_AC_PHASE_2),
            Map.entry("currentacphase3", METER_CURRENT_AC_PHASE_3),
            Map.entry("voltageacphase1", METER_VOLTAGE_AC_PHASE_1),
            Map.entry("voltageacphase2", METER_VOLTAGE_AC_PHASE_2),
            Map.entry("voltageacphase3", METER_VOLTAGE_AC_PHASE_3), Map.entry("powerrealphase1", METER_POWER_PHASE_1),
            Map.entry("powerrealphase2", METER_POWER_PHASE_2), Map.entry("powerrealphase3", METER_POWER_PHASE_3),
            Map.entry("powerrealsum", METER_POWER_SUM), Map.entry("powerfactorphase1", METER_POWER_FACTOR_PHASE_1),
            Map.entry("powerfactorphase2", METER_POWER_FACTOR_PHASE_2),
            Map.entry("powerfactorphase3", METER_POWER_FACTOR_PHASE_3),
            Map.entry("energyrealsumconsumed", METER_ENERGY_REAL_SUM_CONSUMED),
            Map.entry("energyrealsumproduced", METER_ENERGY_REAL_SUM_PRODUCED),
            Map.entry("temperaturechannel1", OHMPILOT_ENERGY_SENSOR_TEMPERATURE_CHANNEL_1),
            Map.entry("statecode", OHMPILOT_STATE_CODE), Map.entry("errorcode", OHMPILOT_ERROR_CODE));

    // List of all Urls
    public static final String INVERTER_REALTIME_DATA_URL = "%SCHEME%://%IP%/solar_api/v1/GetInverterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=CommonInverterData";
    public static final String INVERTER_INFO_URL = "%SCHEME%://%IP%/solar_api/v1/GetInverterInfo.cgi";
    public static final String POWERFLOW_REALTIME_DATA_URL = "%SCHEME%://%IP%/solar_api/v1/GetPowerFlowRealtimeData.fcgi";
    public static final String METER_REALTIME_DATA_URL = "%SCHEME%://%IP%/solar_api/v1/GetMeterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=MeterRealtimeData";
    public static final String OHMPILOT_REALTIME_DATA_URL = "%SCHEME%://%IP%/solar_api/v1/GetOhmPilotRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%";
    public static final String BATTERY_REALTIME_DATA_URL = "%SCHEME%://%IP%/solar_api/v1/GetStorageRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=StorageRealtimeData";

    public static final int API_TIMEOUT = 5000;
    public static final int DEFAULT_REFRESH_PERIOD = 10;

    public static String getInverterDataUrl(String scheme, String ip, int deviceId) {
        return parseUrl(INVERTER_REALTIME_DATA_URL, scheme, ip, deviceId);
    }

    public static String getInverterInfoUrl(String scheme, String ip) {
        return parseUrl(INVERTER_INFO_URL, scheme, ip);
    }

    public static String getPowerFlowDataUrl(String scheme, String ip) {
        return parseUrl(POWERFLOW_REALTIME_DATA_URL, scheme, ip);
    }

    public static String getMeterDataUrl(String scheme, String ip, int deviceId) {
        return parseUrl(METER_REALTIME_DATA_URL, scheme, ip, deviceId);
    }

    public static String getOhmPilotDataUrl(String scheme, String ip, int deviceId) {
        return parseUrl(OHMPILOT_REALTIME_DATA_URL, scheme, ip, deviceId);
    }

    public static String getBatteryDataUrl(String scheme, String ip, int deviceId) {
        return parseUrl(BATTERY_REALTIME_DATA_URL, scheme, ip, deviceId);
    }

    public static String parseUrl(String url, String scheme, String ip) {
        return url.replace("%SCHEME%", scheme).replace("%IP%", ip.trim());
    }

    public static String parseUrl(String url, String scheme, String ip, int deviceId) {
        return parseUrl(url, scheme, ip).replace("%DEVICEID%", Integer.toString(deviceId));
    }
}
