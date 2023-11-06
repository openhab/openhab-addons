/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnphaseBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnphaseBindingConstants {

    private static final String BINDING_ID = "enphase";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENPHASE_ENVOY = new ThingTypeUID(BINDING_ID, "envoy");
    public static final ThingTypeUID THING_TYPE_ENPHASE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");
    public static final ThingTypeUID THING_TYPE_ENPHASE_RELAY = new ThingTypeUID(BINDING_ID, "relay");

    // Configuration parameters
    public static final String CONFIG_SERIAL_NUMBER = "serialNumber";
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH = "refresh";
    public static final String PROPERTY_VERSION = "version";

    // Envoy gateway channels
    public static final String ENVOY_CHANNELGROUP_CONSUMPTION = "consumption";
    public static final String ENVOY_WATT_HOURS_TODAY = "wattHoursToday";
    public static final String ENVOY_WATT_HOURS_SEVEN_DAYS = "wattHoursSevenDays";
    public static final String ENVOY_WATT_HOURS_LIFETIME = "wattHoursLifetime";
    public static final String ENVOY_WATTS_NOW = "wattsNow";

    // Device channels
    public static final String DEVICE_CHANNEL_STATUS = "status";
    public static final String DEVICE_CHANNEL_PRODUCING = "producing";
    public static final String DEVICE_CHANNEL_COMMUNICATING = "communicating";
    public static final String DEVICE_CHANNEL_PROVISIONED = "provisioned";
    public static final String DEVICE_CHANNEL_OPERATING = "operating";

    // Inverter channels
    public static final String INVERTER_CHANNEL_LAST_REPORT_WATTS = "lastReportWatts";
    public static final String INVERTER_CHANNEL_MAX_REPORT_WATTS = "maxReportWatts";
    public static final String INVERTER_CHANNEL_LAST_REPORT_DATE = "lastReportDate";

    // Relay channels
    public static final String RELAY_CHANNEL_RELAY = "relay";
    public static final String RELAY_CHANNEL_LINE_1_CONNECTED = "line1Connected";
    public static final String RELAY_CHANNEL_LINE_2_CONNECTED = "line2Connected";
    public static final String RELAY_CHANNEL_LINE_3_CONNECTED = "line3Connected";

    public static final String RELAY_STATUS_CLOSED = "closed";

    // Properties
    public static final String DEVICE_PROPERTY_PART_NUMBER = "partNumber";

    // Discovery constants
    public static final String DISCOVERY_SERIAL = "serialnum";
    public static final String DISCOVERY_VERSION = "protovers";

    // Status messages
    public static final String DEVICE_STATUS_OK = "envoy.global.ok";
    public static final String ERROR_NODATA = "error.nodata";

    public enum EnphaseDeviceType {
        ACB, // AC Battery
        PSU, // Inverter
        NSRB; // Network system relay controller

        public static @Nullable EnphaseDeviceType safeValueOf(final String type) {
            try {
                return valueOf(type);
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
    }

    /**
     * Derives the default password from the serial number.
     *
     * @param serialNumber serial number to use
     * @return the default password or empty string if serial number is to short.
     */
    public static String defaultPassword(final String serialNumber) {
        return isValidSerial(serialNumber) ? serialNumber.substring(serialNumber.length() - 6) : "";
    }

    /**
     * Checks if the serial number is at least long enough to contain the default password.
     *
     * @param serialNumber serial number to check
     * @return true if not null and at least 6 characters long.
     */
    public static boolean isValidSerial(@Nullable final String serialNumber) {
        return serialNumber != null && serialNumber.length() > 6;
    }
}
