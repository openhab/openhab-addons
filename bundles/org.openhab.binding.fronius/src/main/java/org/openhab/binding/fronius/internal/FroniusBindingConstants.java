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
package org.openhab.binding.fronius.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FroniusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Hannes Spenger - Added ohmpilot & meter power sum
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

    // Inverter channels
    public static final String INVERTER_DATA_CHANNEL_DAY_ENERGY = "inverterdatachanneldayenergy";
    public static final String INVERTER_DATA_CHANNEL_PAC = "inverterdatachannelpac";
    public static final String INVERTER_DATA_CHANNEL_TOTAL = "inverterdatachanneltotal";
    public static final String INVERTER_DATA_CHANNEL_YEAR = "inverterdatachannelyear";
    public static final String INVERTER_DATA_CHANNEL_FAC = "inverterdatachannelfac";
    public static final String INVERTER_DATA_CHANNEL_IAC = "inverterdatachanneliac";
    public static final String INVERTER_DATA_CHANNEL_IDC = "inverterdatachannelidc";
    public static final String INVERTER_DATA_CHANNEL_IDC2 = "inverterdatachannelidc2";
    public static final String INVERTER_DATA_CHANNEL_IDC3 = "inverterdatachannelidc3";
    public static final String INVERTER_DATA_CHANNEL_UAC = "inverterdatachanneluac";
    public static final String INVERTER_DATA_CHANNEL_UDC = "inverterdatachanneludc";
    public static final String INVERTER_DATA_CHANNEL_UDC2 = "inverterdatachanneludc2";
    public static final String INVERTER_DATA_CHANNEL_UDC3 = "inverterdatachanneludc3";
    public static final String INVERTER_DATA_CHANNEL_PDC = "inverterdatachannelpdc";
    public static final String INVERTER_DATA_CHANNEL_PDC2 = "inverterdatachannelpdc2";
    public static final String INVERTER_DATA_CHANNEL_PDC3 = "inverterdatachannelpdc3";
    public static final String INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE = "inverterdatadevicestatuserrorcode";
    public static final String INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE = "inverterdatadevicestatusstatuscode";

    // Power Flow channels
    public static final String POWER_FLOW_P_GRID = "powerflowchannelpgrid";
    public static final String POWER_FLOW_P_LOAD = "powerflowchannelpload";
    public static final String POWER_FLOW_P_AKKU = "powerflowchannelpakku";
    public static final String POWER_FLOW_P_PV = "powerflowchannelppv";
    public static final String POWER_FLOW_AUTONOMY = "powerflowautonomy";
    public static final String POWER_FLOW_SELF_CONSUMPTION = "powerflowselfconsumption";

    public static final String POWER_FLOW_INVERTER_1_POWER = "powerflowinverter1power";
    public static final String POWER_FLOW_INVERTER_1_SOC = "powerflowinverter1soc";

    // Meter channels
    public static final String METER_ENABLE = "enable";
    public static final String METER_LOCATION = "location";
    public static final String METER_CURRENT_AC_PHASE_1 = "currentacphase1";
    public static final String METER_CURRENT_AC_PHASE_2 = "currentacphase2";
    public static final String METER_CURRENT_AC_PHASE_3 = "currentacphase3";
    public static final String METER_VOLTAGE_AC_PHASE_1 = "voltageacphase1";
    public static final String METER_VOLTAGE_AC_PHASE_2 = "voltageacphase2";
    public static final String METER_VOLTAGE_AC_PHASE_3 = "voltageacphase3";
    public static final String METER_POWER_PHASE_1 = "powerrealphase1";
    public static final String METER_POWER_PHASE_2 = "powerrealphase2";
    public static final String METER_POWER_PHASE_3 = "powerrealphase3";
    public static final String METER_POWER_SUM = "powerrealsum";
    public static final String METER_POWER_FACTOR_PHASE_1 = "powerfactorphase1";
    public static final String METER_POWER_FACTOR_PHASE_2 = "powerfactorphase2";
    public static final String METER_POWER_FACTOR_PHASE_3 = "powerfactorphase3";
    public static final String METER_ENERGY_REAL_SUM_CONSUMED = "energyrealsumconsumed";
    public static final String METER_ENERGY_REAL_SUM_PRODUCED = "energyrealsumproduced";

    // OhmPilot channels
    public static final String OHMPILOT_POWER_REAL_SUM = "powerrealsum";
    public static final String OHMPILOT_ENERGY_REAL_SUM_CONSUMED = "energyrealsumconsumed";
    public static final String OHMPILOT_ENERGY_SENSOR_TEMPERATURE_CHANNEL_1 = "temperaturechannel1";
    public static final String OHMPILOT_ERROR_CODE = "errorcode";
    public static final String OHMPILOT_STATE_CODE = "statecode";

    // List of all Urls
    public static final String INVERTER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetInverterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=CommonInverterData";
    public static final String POWERFLOW_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetPowerFlowRealtimeData.fcgi";
    public static final String METER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetMeterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=MeterRealtimeData";
    public static final String OHMPILOT_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetOhmPilotRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%";

    public static String getInverterDataUrl(String ip, int deviceId) {
        return parseUrl(INVERTER_REALTIME_DATA_URL, ip, deviceId);
    }

    public static String getPowerFlowDataUrl(String ip) {
        return parseUrl(POWERFLOW_REALTIME_DATA_URL, ip);
    }

    public static String getMeterDataUrl(String ip, int deviceId) {
        return parseUrl(METER_REALTIME_DATA_URL, ip, deviceId);
    }

    public static String getOhmPilotDataUrl(String ip, int deviceId) {
        return parseUrl(OHMPILOT_REALTIME_DATA_URL, ip, deviceId);
    }

    public static String parseUrl(String url, String ip) {
        return url.replace("%IP%", ip == null ? "" : ip.trim());
    }

    public static String parseUrl(String url, String ip, int deviceId) {
        return parseUrl(url, ip).replace("%DEVICEID%", Integer.toString(deviceId));
    }
}
