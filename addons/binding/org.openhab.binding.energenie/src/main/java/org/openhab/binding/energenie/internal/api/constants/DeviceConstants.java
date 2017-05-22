/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.constants;

/**
 * Contains all Energenie Mi|Home JSON keys associated with the devices.
 * They are used in the communication with the server
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class DeviceConstants {

    // Common constants for all devices (gateways and subdevices)
    public static final String DEVICE_TYPE_KEY = "device_type";
    public static final String DEVICE_ID_KEY = "id";
    public static final String DEVICE_LABEL_KEY = "label";

    // Common constants for the gateways
    public static final String USER_ID_KEY = "user_id";
    public static final String GATEWAY_MAC_ADDRESS_KEY = "mac_address";
    public static final String GATEWAY_IP_ADDRESS_KEY = "ip_address";
    public static final String GATEWAY_PORT_KEY = "port";
    public static final String GATEWAY_FIRMWARE_VERSION_KEY = "firmware_version_id";
    public static final String GATEWAY_AUTH_CODE_KEY = "auth_code";
    public static final String GATEWAY_LAST_SEEN_KEY = "last_seen_at";

    // Common constants for the subdevices
    public static final String SUBDEVICE_PARENT_ID_KEY = "device_id";
    public static final String SUBDEVICE_INCLUDE_USAGE_DATA = "include_usage_data";

    // Common constants for the sensors
    public static final String SENSOR_STATE_KEY = "sensor_state";

    // Common constants for the house monitors
    public static final String MONITOR_POWER_STATE_KEY = "power_state";
    public static final String MONITOR_VOLTAGE_KEY = "voltage";
    public static final String MONITOR_REAL_POWER_KEY = "real_power";
    public static final String MONITOR_TODAY_CONSUMPTION_KEY = "today_wh";
}
