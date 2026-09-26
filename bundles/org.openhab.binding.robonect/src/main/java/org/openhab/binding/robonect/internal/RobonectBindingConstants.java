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
package org.openhab.binding.robonect.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RobonectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectBindingConstants {

    public static final String BINDING_ID = "robonect";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "mower");

    // List of all Channel ids
    public static final String CHANNEL_MOWER_NAME = "name";
    public static final String CHANNEL_MOWER_ID = "id";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_SERVICE_REBOOT = "service-reboot";
    public static final String CHANNEL_SERVICE_SHUTDOWN = "service-shutdown";
    public static final String CHANNEL_SERVICE_SLEEP = "service-sleep";
    public static final String CHANNEL_DOOR_ENABLED = "door-enabled";
    public static final String CHANNEL_DOOR_STATE = "door-state";
    public static final String CHANNEL_DOOR_ARRESTED = "door-arrested";
    public static final String CHANNEL_STATUS_BATTERY = "battery";
    public static final String CHANNEL_STATUS_DURATION = "status-duration";
    public static final String CHANNEL_STATUS_DISTANCE = "status-distance";
    public static final String CHANNEL_STATUS_HOURS = "mowing-hours";
    public static final String CHANNEL_STATUS_MODE = "mode";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_MOWER_START = "start";
    public static final String CHANNEL_MOWER_STATUS_OFFLINE_TRIGGER = "offlineTrigger";
    public static final String CHANNEL_TIMER_STATUS = "timer-status";
    public static final String CHANNEL_TIMER_NEXT_TIMER = "timer-next";
    public static final String CHANNEL_WLAN_SIGNAL = "wlan-signal";
    public static final String CHANNEL_WLAN_SIGNAL_STRENGTH = "wlan-signal-strength";
    public static final String CHANNEL_WLAN_ENABLED = "wlan-enabled";
    public static final String CHANNEL_WLAN_SSID = "wlan-ssid";
    public static final String CHANNEL_WLAN_IP = "wlan-ip";
    public static final String CHANNEL_WLAN_AP_ENABLED = "wlan-ap-enabled";
    public static final String CHANNEL_WLAN_AP_SSID = "wlan-ap-ssid";
    public static final String CHANNEL_WLAN_MAC = "wlan-mac";
    public static final String CHANNEL_WLAN_DHCP = "wlan-dhcp";
    public static final String CHANNEL_WLAN_PING = "wlan-ping";
    public static final String CHANNEL_WLAN_PING_TIME = "wlan-ping-time";
    public static final String CHANNEL_WLAN_NETMASK = "wlan-netmask";
    public static final String CHANNEL_WLAN_GATEWAY = "wlan-gateway";
    public static final String CHANNEL_WLAN_AP_MAC = "wlan-ap-mac";
    public static final String CHANNEL_WLAN_AP_HIDDEN = "wlan-ap-hidden";
    public static final String CHANNEL_WLAN_AP_CHANNEL = "wlan-ap-channel";
    public static final String CHANNEL_WLAN_AP_ENCRYPTION = "wlan-ap-encryption";
    public static final String CHANNEL_WLAN_AP_MAX_CONNECTIONS = "wlan-ap-max-connections";
    public static final String CHANNEL_WLAN_AP_IP = "wlan-ap-ip";
    public static final String CHANNEL_WLAN_AP_NETMASK = "wlan-ap-netmask";
    public static final String CHANNEL_WLAN_AP_GATEWAY = "wlan-ap-gateway";
    public static final String CHANNEL_WLAN_AP_DHCP = "wlan-ap-dhcp";
    public static final String CHANNEL_WLAN_AP_DHCP_START = "wlan-ap-dhcp-start";
    public static final String CHANNEL_WLAN_AP_DHCP_END = "wlan-ap-dhcp-end";
    public static final String CHANNEL_BATTERY_ID = "battery-id";
    public static final String CHANNEL_BATTERY_CHARGE = "battery-charge";
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";
    public static final String CHANNEL_BATTERY_CURRENT = "battery-current";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "battery-temperature";
    public static final String CHANNEL_BATTERY_CAPACITY = "battery-capacity";
    public static final String CHANNEL_BATTERY_REMAINING_CAPACITY = "battery-remaining-capacity";
    public static final String CHANNEL_LEFT_DRIVE_POWER = "left-drive-power";
    public static final String CHANNEL_LEFT_DRIVE_SPEED = "left-drive-speed";
    public static final String CHANNEL_LEFT_DRIVE_CURRENT = "left-drive-current";
    public static final String CHANNEL_RIGHT_DRIVE_POWER = "right-drive-power";
    public static final String CHANNEL_RIGHT_DRIVE_SPEED = "right-drive-speed";
    public static final String CHANNEL_RIGHT_DRIVE_CURRENT = "right-drive-current";
    public static final String CHANNEL_BLADE_MOTOR_AVERAGE = "blade-motor-average";
    public static final String CHANNEL_BLADE_MOTOR_SPEED = "blade-motor-speed";
    public static final String CHANNEL_BLADE_MOTOR_CURRENT = "blade-motor-current";
    public static final String CHANNEL_STATISTICS_RUNNING_HOURS = "statistics-running-hours";
    public static final String CHANNEL_STATISTICS_MOWING_HOURS = "statistics-mowing-hours";
    public static final String CHANNEL_STATISTICS_SEARCHING_HOURS = "statistics-searching-hours";
    public static final String CHANNEL_STATISTICS_CHARGING_HOURS = "statistics-charging-hours";
    public static final String CHANNEL_STATISTICS_CHARGES = "statistics-charges";
    public static final String CHANNEL_STATISTICS_ERRORS = "statistics-errors";
    public static final String CHANNEL_PORTAL_ENABLED = "portal-enabled";
    public static final String CHANNEL_PORTAL_CONNECTED = "portal-connected";
    public static final String CHANNEL_PORTAL_URL = "portal-url";
    public static final String CHANNEL_PORTAL_HOST = "portal-host";
    public static final String CHANNEL_WEATHER_BREAK = "weather-break";
    public static final String CHANNEL_WEATHER_RAIN = "weather-rain";
    public static final String CHANNEL_WEATHER_TEMPERATURE = "weather-temperature";
    public static final String CHANNEL_WEATHER_HUMIDITY = "weather-humidity";
    public static final String CHANNEL_WEATHER_DAY = "weather-day";
    public static final String CHANNEL_WEATHER_CITY = "weather-city";
    public static final String CHANNEL_WEATHER_REMAINING = "weather-remaining";
    public static final String CHANNEL_WEATHER_ENABLED = "weather-enabled";
    public static final String CHANNEL_WEATHER_INTERVAL = "weather-interval";
    public static final String CHANNEL_WEATHER_MAX_RAIN = "weather-max-rain";
    public static final String CHANNEL_WEATHER_MIN_TEMPERATURE = "weather-min-temperature";
    public static final String CHANNEL_WEATHER_MAX_TEMPERATURE = "weather-max-temperature";
    public static final String CHANNEL_WEATHER_MIN_HUMIDITY = "weather-min-humidity";
    public static final String CHANNEL_WEATHER_MAX_HUMIDITY = "weather-max-humidity";
    public static final String CHANNEL_WEATHER_SKIP_DAY = "weather-skip-day";
    public static final String CHANNEL_WEATHER_SKIP_NIGHT = "weather-skip-night";
    public static final String CHANNEL_EXTENSION_GPIO_1_FLASH = "extension-gpio-1-flash";
    public static final String CHANNEL_EXTENSION_GPIO_1_INVERTED = "extension-gpio-1-inverted";
    public static final String CHANNEL_EXTENSION_GPIO_1_STATUS = "extension-gpio-1-status";
    public static final String CHANNEL_EXTENSION_GPIO_2_FLASH = "extension-gpio-2-flash";
    public static final String CHANNEL_EXTENSION_GPIO_2_INVERTED = "extension-gpio-2-inverted";
    public static final String CHANNEL_EXTENSION_GPIO_2_STATUS = "extension-gpio-2-status";
    public static final String CHANNEL_EXTENSION_OUTPUT_1_FLASH = "extension-output-1-flash";
    public static final String CHANNEL_EXTENSION_OUTPUT_1_INVERTED = "extension-output-1-inverted";
    public static final String CHANNEL_EXTENSION_OUTPUT_1_STATUS = "extension-output-1-status";
    public static final String CHANNEL_EXTENSION_OUTPUT_2_FLASH = "extension-output-2-flash";
    public static final String CHANNEL_EXTENSION_OUTPUT_2_INVERTED = "extension-output-2-inverted";
    public static final String CHANNEL_EXTENSION_OUTPUT_2_STATUS = "extension-output-2-status";
    public static final String CHANNEL_PUSH_URL = "push-url";
    public static final String CHANNEL_PUSH_USERNAME = "push-username";
    public static final String CHANNEL_PUSH_INTERVAL = "push-interval";

    public static final String CHANNEL_JOB = "job";
    public static final String CHANNEL_JOB_START = "job-start";
    public static final String CHANNEL_JOB_END = "job-end";

    public static final String CHANNEL_ERROR_CODE = "error-code";
    public static final String CHANNEL_ERROR_MESSAGE = "error-message";
    public static final String CHANNEL_ERROR_DATE = "error-date";

    public static final String CHANNEL_LAST_ERROR_CODE = "last-error-code";
    public static final String CHANNEL_LAST_ERROR_MESSAGE = "last-error-message";
    public static final String CHANNEL_LAST_ERROR_DATE = "last-error-date";
    public static final String CHANNEL_RESET_ERROR = "reset-error";

    public static final String CHANNEL_HEALTH_TEMP = "health-temperature";
    public static final String CHANNEL_HEALTH_HUM = "health-humidity";

    public static final String CHANNEL_BLADES_QUALITY = "blades-quality";
    public static final String CHANNEL_BLADES_REPL_DAYS = "blades-replacement-days";
    public static final String CHANNEL_BLADES_USAGE_HOURS = "blades-usage-hours";

    public static final String PROPERTY_COMPILED = "compiled";
    public static final String PROPERTY_COMMENT = "comment";
}
