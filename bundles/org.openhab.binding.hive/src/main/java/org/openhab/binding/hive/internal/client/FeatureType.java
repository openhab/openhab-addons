/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public enum FeatureType {
    AUTOBOOST_V1,
    BATTERY_DEVICE_V1,
    CHILD_LOCK_V1,
    DEVICE_MANAGEMENT_V1,
    DISPLAY_ORIENTATION_V1,
    ETHERNET_DEVICE_V1,
    FROST_PROTECT_V1,
    GROUP_V1,
    HEATING_TEMPERATURE_CONTROL_DEVICE_V1,
    HEATING_TEMPERATURE_CONTROL_V1,
    HEATING_THERMOSTAT_V1,
    HIVE_HUB_V1,
    LIFECYCLE_STATE_V1,
    LINKS_V1,
    MOUNTING_MODE_V1,
    ON_OFF_DEVICE_V1,
    PI_HEATING_DEMAND_V1,
    PHYSICAL_DEVICE_V1,
    RADIO_DEVICE_V1,
    STANDBY_V1,
    TEMPERATURE_SENSOR_V1,
    TRANSIENT_MODE_V1,
    TRV_CALIBRATION_V1,
    TRV_ERROR_DIAGNOSTICS_V1,
    WATER_HEATER_V1,
    ZIGBEE_DEVICE_V1,
    ZIGBEE_ROUTING_DEVICE_V1,

    UNEXPECTED
}
