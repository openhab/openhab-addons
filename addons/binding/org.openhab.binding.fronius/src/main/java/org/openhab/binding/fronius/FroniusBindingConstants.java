/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link FroniusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusBindingConstants {

    private static final String BINDING_ID = "fronius";

    // List of all Thing Type UIDs
    public static final ThingTypeUID FRONIUS_SYMO_BRIDGE = new ThingTypeUID(BINDING_ID, "symo");
    public static final ThingTypeUID ACTIVE_DEVICE_INFO_THING = new ThingTypeUID(BINDING_ID, "device_info");
    public static final ThingTypeUID INVERTER_REALTIME_DATA_THING = new ThingTypeUID(BINDING_ID, "inverter_data");
    public static final ThingTypeUID METER_REALTIME_DATA_THING = new ThingTypeUID(BINDING_ID, "meter_data");
    public static final ThingTypeUID STORAGE_REALTIME_DATA_THING = new ThingTypeUID(BINDING_ID, "storage_data");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(FRONIUS_SYMO_BRIDGE,
            ACTIVE_DEVICE_INFO_THING, INVERTER_REALTIME_DATA_THING, METER_REALTIME_DATA_THING,
            STORAGE_REALTIME_DATA_THING);

    // List of all Channel ids
    public static final String CHANNEL_INVERTER_DAY_ENERGY = "day_energy";
    public static final String CHANNEL_INVERTER_YEAR_ENERGY = "year_energy";
    public static final String CHANNEL_INVERTER_TOTAL_ENERGY = "total_energy";
    public static final String CHANNEL_INVERTER_PAC = "pac";
    public static final String CHANNEL_INVERTER_IAC = "iac";
    public static final String CHANNEL_INVERTER_UAC = "uac";
    public static final String CHANNEL_INVERTER_FAC = "fac";
    public static final String CHANNEL_INVERTER_IDC = "idc";
    public static final String CHANNEL_INVERTER_UDC = "udc";
    public static final String CHANNEL_INVERTER_ERROR_CODE = "error_code";
    public static final String CHANNEL_INVERTER_LED_COLOR = "led_color";
    public static final String CHANNEL_INVERTER_LED_STATE = "led_state";
    public static final String CHANNEL_INVERTER_MGMT_TIMER_REMAINING_TIME = "mgmt_timer_remaining_time";
    public static final String CHANNEL_INVERTER_STATUS_CODE = "status_code";
    public static final String CHANNEL_INVERTER_STATE_TO_RESET = "state_to_reset";
    public static final String CHANNEL_STORAGE_CURRENT = "current";
    public static final String CHANNEL_STORAGE_VOLTAGE = "voltage";
    public static final String CHANNEL_STORAGE_CHARGE = "charge";
    public static final String CHANNEL_STORAGE_CAPACITY = "capacity";
    public static final String CHANNEL_STORAGE_TEMPERATURE = "temperature";
    public static final String CHANNEL_STORAGE_DESIGNED_CAPACITY = "designed_capacity";
    public static final String CHANNEL_STORAGE_MANUFACTURER = "manufacturer";
    public static final String CHANNEL_STORAGE_MODEL = "model";
    public static final String CHANNEL_STORAGE_SERIAL = "serial";
    public static final String CHANNEL_STORAGE_ENABLE = "enable";
    public static final String CHANNEL_STORAGE_MAX_VOLTAGE = "max_voltage";
    public static final String CHANNEL_STORAGE_MIN_VOLTAGE = "min_voltage";
    public static final String CHANNEL_STATUS_CODE = "status_code";
    public static final String CHANNEL_TIMESTAMP = "timestamp";

    // List of thing parameters names
    public static final String PARAMETER_DEVICE = "device";
    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_REFRESH_INTERVAL = "refresh_interval";

    // List of all API URLs
    public static final String ACTIVE_DEVICE_INFO_URL = "/solar_api/v1/GetActiveDeviceInfo.cgi";
    public static final String INVERTER_INFO_URL = "/solar_api/v1/GetInverterInfo.cgi";
    public static final String INVERTER_REALTIME_DATA_URL = "/solar_api/v1/GetInverterRealtimeData.cgi";
    public static final String LOGGER_INFO_URL = "/solar_api/v1/GetLoggerInfo.cgi";
    public static final String LOGGER_LED_INFO_URL = "/solar_api/v1/GetLoggerLEDInfo.cgi";
    public static final String METER_REALTIME_DATA_URL = "/solar_api/v1/GetMeterRealtimeData.cgi";
    public static final String OHM_PILOT_REALTIME_DATA_URL = "/solar_api/v1/GetOhmPilotRealtimeData.cgi";
    public static final String POWER_FLOW_REALTIME_DATA_URL = "/solar_api/v1/GetPowerFlowRealtimeData.fcgi";
    public static final String SENSOR_REALTIME_DATA_URL = "/solar_api/v1/GetSensorRealtimeData.cgi";
    public static final String STORAGE_REALTIME_DATA_URL = "/solar_api/v1/GetStorageRealtimeData.cgi";
    public static final String STRING_REALTIME_DATA_URL = "/solar_api/v1/GetStringRealtimeData.cgi";

    // List of all service descriptions
    public static final String ACTIVE_DEVICE_INFO_DESCRIPTION = "Active Device Info";
    public static final String INVERTER_REALTIME_DATA_DESCRIPTION = "Inverter Realtime Data";
    public static final String METER_REALTIME_DATA_DESCRIPTION = "Meter Realtime Data";
    public static final String STORAGE_REALTIME_DATA_DESCRIPTION = "Storage Realtime Data";
}
