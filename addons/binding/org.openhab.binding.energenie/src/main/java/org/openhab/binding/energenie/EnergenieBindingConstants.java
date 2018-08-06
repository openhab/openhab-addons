/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie;

import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;

import com.google.common.collect.ImmutableMap;

/**
 * The {@link EnergenieBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class EnergenieBindingConstants {

    public static final String BINDING_ID = "energenie";

    // List of all thing IDs
    public static final String THING_ID_GATEWAY = "gateway";
    public static final String THING_ID_OPEN_SENSOR = "openSensor";
    public static final String THING_ID_MOTION_SENSOR = "motionSensor";
    public static final String THING_ID_ENERGY_MONITOR = "energyMonitor";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, THING_ID_GATEWAY);
    public static final ThingTypeUID THING_TYPE_ENERGY_MONITOR = new ThingTypeUID(BINDING_ID, THING_ID_ENERGY_MONITOR);
    public static final ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, THING_ID_MOTION_SENSOR);
    public static final ThingTypeUID THING_TYPE_OPEN_SENSOR = new ThingTypeUID(BINDING_ID, THING_ID_OPEN_SENSOR);

    // Mapping from thing type to device type and vice versa
    public static final Map<EnergenieDeviceTypes, ThingTypeUID> DEVICE_TYPE_TO_THING_TYPE = ImmutableMap.of(
            EnergenieDeviceTypes.HOUSE_MONITOR, EnergenieBindingConstants.THING_TYPE_ENERGY_MONITOR,
            EnergenieDeviceTypes.MOTION_SENSOR, EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR,
            EnergenieDeviceTypes.OPEN_SENSOR, EnergenieBindingConstants.THING_TYPE_OPEN_SENSOR);

    public static final Map<ThingTypeUID, EnergenieDeviceTypes> THING_TYPE_TO_DEVICE_TYPE = ImmutableMap.of(
            EnergenieBindingConstants.THING_TYPE_ENERGY_MONITOR, EnergenieDeviceTypes.HOUSE_MONITOR,
            EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR, EnergenieDeviceTypes.MOTION_SENSOR,
            EnergenieBindingConstants.THING_TYPE_OPEN_SENSOR, EnergenieDeviceTypes.OPEN_SENSOR);

    // List of all Channel IDs
    public static final String CHANNEL_LAST_SEEN = "lastSeen";
    // Channel IDs of the House Monitor device
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_REAL_POWER = "realPower";
    public static final String CHANNEL_TODAY_CONSUMPTION = "todayConsumption";
    /**
     * Channel ID used from sensor devices (e.g. "motion", "openClosed" etc)
     */
    public static final String CHANNEL_STATE = "state";

    // List of configuration parameters

    // Configuration parameters shared by all devices
    /**
     * Update interval in seconds
     */
    public static final String CONFIG_UPDATE_INTERVAL = "updateInterval";

    // Gateway specific configuration parameters
    /**
     * Username in the Mi|Home portal. It is the email address used for the user registration.
     */
    public static final String CONFIG_USERNAME = "username";
    /**
     * User's password used for authentication for all requests to the REST API
     */
    public static final String CONFIG_PASSWORD = "password";

    // List of properties

    // Properties shared by all devices
    /**
     * Type of the device (e.g. "motion","house" etc )
     */
    public static final String PROPERTY_TYPE = "type";
    /**
     * ID of the gateway master device (the bridge)
     */
    public static final String PROPERTY_GATEWAY_ID = "gatewayID";
    /**
     * ID of the current device
     */
    public static final String PROPERTY_DEVICE_ID = "deviceID";

    // Gateway specific properties
    /**
     * The email address that is used for authentication from the REST API
     */
    public static final String PROPERTY_USER_ID = "userID";
    /**
     * The current firmware version
     */
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    /**
     * MAC address of the gateway device
     */
    public static final String PROPERTY_MAC_ADDRESS = "mac";
    /**
     * IP address of the gateway device
     */
    public static final String PROPERTY_IP_ADDRESS = "ip";
    /**
     * Port of the gateway device
     */
    public static final String PROPERTY_PORT = "port";
    /**
     * Authentication code of the gateway. It is automatically generated using the
     * hardware code of the device.
     */
    public static final String PROPERTY_AUTH_CODE = "authCode";

    /**
     * Formatting pattern for the date of the 'last_seen' property
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

}
