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
package org.openhab.binding.solaredge.internal;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarEdgeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeBindingConstants {

    private static final String BINDING_ID = "solaredge";

    // List of main device types
    public static final String DEVICE_GENERIC = "generic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC);

    // List of all channel groups
    public static final String CHANNEL_GROUP_LIVE = "live";
    public static final String CHANNEL_GROUP_AGGREGATE_DAY = "aggregate_day";
    public static final String CHANNEL_GROUP_AGGREGATE_WEEK = "aggregate_week";
    public static final String CHANNEL_GROUP_AGGREGATE_MONTH = "aggregate_month";
    public static final String CHANNEL_GROUP_AGGREGATE_YEAR = "aggregate_year";

    // List of all channel ids
    public static final String CHANNEL_ID_PRODUCTION = "production";
    public static final String CHANNEL_ID_PV_STATUS = "pv_status";
    public static final String CHANNEL_ID_CONSUMPTION = "consumption";
    public static final String CHANNEL_ID_LOAD_STATUS = "load_status";
    public static final String CHANNEL_ID_BATTERY_STATUS = "battery_status";
    public static final String CHANNEL_ID_BATTERY_CRITICAL = "battery_critical";
    public static final String CHANNEL_ID_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_ID_GRID_STATUS = "grid_status";
    public static final String CHANNEL_ID_IMPORT = "import";
    public static final String CHANNEL_ID_EXPORT = "export";
    public static final String CHANNEL_ID_BATTERY_CHARGE = "battery_charge";
    public static final String CHANNEL_ID_BATTERY_DISCHARGE = "battery_discharge";
    public static final String CHANNEL_ID_BATTERY_CHARGE_DISCHARGE = "battery_charge_discharge";
    public static final String CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION = "selfConsumptionForConsumption";
    public static final String CHANNEL_ID_SELF_CONSUMPTION_COVERAGE = "selfConsumptionCoverage";
    public static final String CHANNEL_ID_BATTERY_SELF_CONSUMPTION = "batterySelfConsumption";

    // PRIVATE API CONSTANTS
    // URLs
    public static final String PRIVATE_DATA_API_URL = "https://monitoring.solaredge.com/solaredge-apigw/api/site/";
    public static final String PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX = "/powerDashboardChart";
    public static final String PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX = "/energyDashboardChart";
    public static final String PRIVATE_DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow.json";

    // field names
    public static final String PRIVATE_API_TOKEN_COOKIE_NAME = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
    public static final String PRIVATE_API_TOKEN_COOKIE_DOMAIN = "monitoring.solaredge.com";
    public static final String PRIVATE_API_TOKEN_COOKIE_PATH = "/";
    public static final String PRIVATE_DATA_API_AGGREGATE_DATA_CHARTFIELD_FIELD = "chartField";

    //
    //
    // PRIVATE API CONSTANTS
    // URLs
    public static final String PUBLIC_DATA_API_URL = "https://monitoringapi.solaredge.com/site/";
    public static final String PUBLIC_DATA_API_URL_AGGREGATE_DATA_SUFFIX = "/energyDetails";
    public static final String PUBLIC_DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow";
    public static final String PUBLIC_DATA_API_URL_LIVE_DATA_METERLESS_SUFFIX = "/overview";

    // field names
    public static final String PUBLIC_DATA_API_KEY_FIELD = "api_key";
    public static final String PUBLIC_DATA_API_START_TIME_FIELD = "startTime";
    public static final String PUBLIC_DATA_API_END_TIME_FIELD = "endTime";
    public static final String PUBLIC_DATA_API_TIME_UNIT_FIELD = "timeUnit";

    // constants
    public static final String BEGIN_OF_DAY_TIME = "00:00:00";
    public static final String END_OF_DAY_TIME = "23:59:59";
    public static final long MINUTES_PER_DAY = 1440;

    // web request constants
    public static final long WEB_REQUEST_PUBLIC_API_DAY_LIMIT = 300;
    public static final long WEB_REQUEST_INITIAL_DELAY = TimeUnit.SECONDS.toMillis(30);
    public static final long WEB_REQUEST_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;

    // Status Keys
    public static final String STATUS_INVALID_SOLAR_ID = "@text/status.invalid.solarId";
    public static final String STATUS_INVALID_TOKEN = "@text/status.invalid.token";
    public static final String STATUS_UNKNOWN_ERROR = "@text/status.unknown.error";
    public static final String STATUS_INVALID_TOKEN_LENGTH = "@text/status.invalid.token.length";
    public static final String STATUS_INVALID_API_KEY_LENGTH = "@text/status.invalid.api.key.length";
    public static final String STATUS_REQUEST_LIMIT_EXCEEDED = "@text/status.request.limit.exceeded [\""
            + WEB_REQUEST_PUBLIC_API_DAY_LIMIT + "\"]";
    public static final String STATUS_NO_METER_CONFIGURED = "@text/status.no.meter.configured";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GENERIC);
}
