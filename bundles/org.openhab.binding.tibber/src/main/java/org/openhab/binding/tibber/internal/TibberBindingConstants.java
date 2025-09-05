/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TibberBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Stian Kjoglum - Initial contribution
 * @author Bernd Weymann - Enhance used constants
 */
@NonNullByDefault
public class TibberBindingConstants {

    public static final String BINDING_ID = "tibber";
    public static final String JSON_CONTENT_TYPE = "application/json";

    // Tibber base URL for queries and mutations
    public static final String BASE_URL = "https://api.tibber.com/v1-beta/gql";

    // List of all Thing Type UIDs
    public static final ThingTypeUID TIBBER_THING_TYPE = new ThingTypeUID(BINDING_ID, "tibberapi");

    // Channel groups
    public static final String CHANNEL_GROUP_PRICE = "price";
    public static final String CHANNEL_GROUP_LIVE = "live";
    public static final String CHANNEL_GROUP_STATISTICS = "statistics";

    // price channels
    public static final String CHANNEL_TOTAL_PRICE = "total";
    public static final String CHANNEL_SPOT_PRICE = "spot";
    public static final String CHANNEL_DEPRECATED_SPOT_PRICE = "spot-price";
    public static final String CHANNEL_TAX = "tax";
    public static final String CHANNEL_PRICE_LEVELS = "level";
    public static final String CHANNEL_AVERAGE = "average";

    // live channels
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_MIN_COSNUMPTION = "minimum-consumption";
    public static final String CHANNEL_PEAK_CONSUMPTION = "peak-consumption";
    public static final String CHANNEL_AVERAGE_CONSUMPTION = "average-consumption";
    public static final String CHANNEL_PRODUCTION = "production";
    public static final String CHANNEL_MIN_PRODUCTION = "minimum-production";
    public static final String CHANNEL_PEAK_PRODUCTION = "peak-production";
    public static final String CHANNEL_POWER_BALANCE = "power-balance";
    public static final String CHANNEL_VOLTAGE_1 = "voltage1";
    public static final String CHANNEL_VOLTAGE_2 = "voltage2";
    public static final String CHANNEL_VOLTAGE_3 = "voltage3";
    public static final String CHANNEL_CURRENT_1 = "current1";
    public static final String CHANNEL_CURRENT_2 = "current2";
    public static final String CHANNEL_CURRENT_3 = "current3";

    // statistics channels
    public static final String CHANNEL_TOTAL_CONSUMPTION = "total-consumption";
    public static final String CHANNEL_DAILY_CONSUMPTION = "daily-consumption";
    public static final String CHANNEL_DAILY_COST = "daily-cost";
    public static final String CHANNEL_LAST_HOUR_CONSUMPTION = "last-hour-consumption";
    public static final String CHANNEL_TOTAL_PRODUCTION = "total-production";
    public static final String CHANNEL_DAILY_PRODUCTION = "daily-production";
    public static final String CHANNEL_LAST_HOUR_PRODUCTION = "last-hour-production";

    public static final String CURRENCY_QUERY_RESOURCE_PATH = "/graphql/currency.graphql";
    public static final String PRICE_QUERY_RESOURCE_PATH = "/graphql/prices.graphql";
    public static final String REALTIME_QUERY_RESOURCE_PATH = "/graphql/realtime.graphql";
    public static final String WEBSOCKET_SUBSCRIPTION_RESOURCE_PATH = "/graphql/websocket.graphql";

    public static final String SCHEDULE_CONTAINER = "{\"size\":%s, \"cost\":%s, \"schedule\":%s}";
    public static final String QUERY_CONTAINER = "{\"query\":\"%s\"}";
    public static final String WEBSOCKET_URL_QUERY = "{\"query\":\"{viewer {websocketSubscriptionUrl }}\"}";
    public static final String CONNECT_MESSAGE = "{\"type\":\"connection_init\", \"payload\":{\"token\":\"%s\"}}";
    public static final String DISCONNECT_MESSAGE = "{\"type\":\"connection_terminate\",\"payload\":null}";
    public static final String SUBSCRIPTION_MESSAGE = "{\"id\":\"1\",\"type\":\"subscribe\",\"payload\":{\"variables\":{},\"extensions\":{},\"operationName\":null,\"query\":\"subscription { liveMeasurement(homeId:\\\"%s\\\") { timestamp power lastMeterConsumption lastMeterProduction accumulatedConsumption accumulatedConsumptionLastHour accumulatedCost currency minPower averagePower maxPower voltagePhase1 voltagePhase2 voltagePhase3 currentL1 currentL2 currentL3 powerProduction accumulatedProduction accumulatedProductionLastHour minPowerProduction maxPowerProduction }}\"}}";

    public static final String[] CURRENCY_QUERY_JSON_PATH = new String[] { "data", "viewer", "home",
            "currentSubscription", "priceInfo", "current" };
    public static final String[] PRICE_INFO_JSON_PATH = new String[] { "data", "viewer", "home", "currentSubscription",
            "priceInfo" };
    public static final String[] REALTIME_FEATURE_JSON_PATH = new String[] { "data", "viewer", "home", "features" };
    public static final String[] SOCKET_MESSAGE_JSON_PATH = new String[] { "payload", "data", "liveMeasurement" };

    public static final String EMPTY_VALUE = "";
    public static final String NULL_VALUE = "null-value";

    public static final String CRON_DAILY_AT = "30 0 %s ? * * *";

    public static final String JSON_DATA = "data";
    public static final String JSON_VIEWER = "viewer";
    public static final String JSON_HOME = "home";
    public static final String JSON_SUBSRCIPTION = "currentSubscription";
    public static final String JSON_DAILY = "daily";
    public static final String JSON_HOURLY = "hourly";
    public static final String JSON_NODES = "nodes";

    public static final String PRICE_LEVEL_VERY_CHEAP = "very_cheap";
    public static final String PRICE_LEVEL_CHEAP = "cheap";
    public static final String PRICE_LEVEL_NORMAL = "normal";
    public static final String PRICE_LEVEL_EXPENSIVE = "expensive";
    public static final String PRICE_LEVEL_VERY_EXPENSIVE = "very_expensive";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(TIBBER_THING_TYPE)
            .collect(Collectors.toSet());

    // parameter keys
    public static final String PARAM_EARLIEST_START = "earliestStart";
    public static final String PARAM_LATEST_END = "latestEnd";
    public static final String PARAM_ASCENDING = "ascending";
    public static final String PARAM_POWER = "power";
    public static final String PARAM_DURATION = "duration";
    public static final String PARAM_CURVE = "curve";
    public static final String PARAM_TIMESTAMP = "timestamp";
}
