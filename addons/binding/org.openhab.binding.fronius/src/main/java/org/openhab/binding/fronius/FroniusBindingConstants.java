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
    public static final ThingTypeUID FRONIUS_SYMO = new ThingTypeUID(BINDING_ID, "symo");
    public static final ThingTypeUID FRONIUS_SYMO_HYBRID = new ThingTypeUID(BINDING_ID, "symo_hybrid");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(FRONIUS_SYMO,
            FRONIUS_SYMO_HYBRID);

    // List of all Channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_LASTSEEN = "lastseen";
    public static final String CHANNEL_DAY_ENERGY = "day_energy";
    public static final String CHANNEL_YEAR_ENERGY = "year_energy";
    public static final String CHANNEL_TOTAL_ENERGY = "total_energy";
    public static final String CHANNEL_PAC = "pac";
    public static final String CHANNEL_IAC = "iac";
    public static final String CHANNEL_UAC = "uac";
    public static final String CHANNEL_FAC = "fac";
    public static final String CHANNEL_IDC = "idc";
    public static final String CHANNEL_UDC = "udc";
    public static final String CHANNEL_STATUS_CODE = "status_code";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_STORAGE_CURRENT = "storage_current";
    public static final String CHANNEL_STORAGE_VOLTAGE = "storage_voltage";
    public static final String CHANNEL_STORAGE_CHARGE = "storage_charge";
    public static final String CHANNEL_STORAGE_CAPACITY = "storage_capacity";
    public static final String CHANNEL_STORAGE_TEMPERATURE = "storage_temperature";
    public static final String CHANNEL_STORAGE_CODE = "storage_code";
    public static final String CHANNEL_STORAGE_TIMESTAMP = "storage_timestamp";

    // List of thing parameters names
    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_DEVICE = "device";
    public static final String PARAMETER_REFRESH_INTERVAL = "refreshInterval";

    // List of all API URLs
    public static final String INVERTER_REALTIME_DATA_URL = "/solar_api/v1/GetInverterRealtimeData.cgi";
    public static final String SENSOR_REALTIME_DATA_URL = "/solar_api/v1/GetSensorRealtimeData.cgi";
    public static final String STRING_REALTIME_DATA_URL = "/solar_api/v1/GetStringRealtimeData.cgi";
    public static final String LOGGER_INFO_URL = "/solar_api/v1/GetLoggerInfo.cgi";
    public static final String LOGGER_LED_INFO_URL = "/solar_api/v1/GetLoggerLEDInfo.cgi";
    public static final String INVERTER_INFO_URL = "/solar_api/v1/GetInverterInfo.cgi";
    public static final String ACTIVE_DEVICE_INFO_URL = "/solar_api/v1/GetActiveDeviceInfo.cgi";
    public static final String METER_REALTIME_DATA_URL = "/solar_api/v1/GetMeterRealtimeData.cgi";
    public static final String STORAGE_REALTIME_DATA_URL = "/solar_api/v1/GetStorageRealtimeData.cgi";
    public static final String OHM_PILOT_REALTIME_DATA_URL = "/solar_api/v1/GetOhmPilotRealtimeData.cgi";
    public static final String POWER_FLOW_REALTIME_DATA_URL = "/solar_api/v1/GetPowerFlowRealtimeData.fcgi";

    public static final String INVERTER_REALTIME_DATA_COLLECTION = "CommonInverterData";
}
