/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link TibberBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stian Kjoglum - Initial contribution
 */
@NonNullByDefault
public class TibberBindingConstants {

    public static final String BINDING_ID = "tibber";
    public static final String JSON_CONTENT_TYPE = "application/json";

    // Tibber base URL for queries and mutations
    public static final String BASE_URL = "https://api.tibber.com/v1-beta/gql";

    // Tibber driver version
    public static final String TIBBER_DRIVER = "com.tibber/1.8.3";

    // List of all Thing Type UIDs
    public static final ThingTypeUID TIBBER_THING_TYPE = new ThingTypeUID(BINDING_ID, "tibberapi");

    // List of all Channel ids - Used for queries
    public static final String CURRENT_TOTAL = "current_total";
    public static final String CURRENT_STARTSAT = "current_startsAt";
    public static final String CURRENT_LEVEL = "current_level";

    public static final String TODAY_PRICES = "today_prices";
    public static final String TOMORROW_PRICES = "tomorrow_prices";
    public static final String DAILY_FROM = "daily_from";
    public static final String DAILY_TO = "daily_to";
    public static final String DAILY_COST = "daily_cost";
    public static final String DAILY_CONSUMPTION = "daily_consumption";
    public static final String HOURLY_FROM = "hourly_from";
    public static final String HOURLY_TO = "hourly_to";
    public static final String HOURLY_COST = "hourly_cost";
    public static final String HOURLY_CONSUMPTION = "hourly_consumption";
    public static final String LIVE_TIMESTAMP = "live_timestamp";
    public static final String LIVE_POWER = "live_power";
    public static final String LIVE_LASTMETERCONSUMPTION = "live_lastMeterConsumption";
    public static final String LIVE_LASTMETERPRODUCTION = "live_lastMeterProduction";
    public static final String LIVE_ACCUMULATEDCONSUMPTION = "live_accumulatedConsumption";
    public static final String LIVE_ACCUMULATEDCOST = "live_accumulatedCost";
    public static final String LIVE_ACCUMULATEREWARD = "live_accumulatedReward";
    public static final String LIVE_CURRENCY = "live_currency";
    public static final String LIVE_MINPOWER = "live_minPower";
    public static final String LIVE_AVERAGEPOWER = "live_averagePower";
    public static final String LIVE_MAXPOWER = "live_maxPower";
    public static final String LIVE_VOLTAGE1 = "live_voltage1";
    public static final String LIVE_VOLTAGE2 = "live_voltage2";
    public static final String LIVE_VOLTAGE3 = "live_voltage3";
    public static final String LIVE_CURRENT1 = "live_current1";
    public static final String LIVE_CURRENT2 = "live_current2";
    public static final String LIVE_CURRENT3 = "live_current3";
    public static final String LIVE_POWERPRODUCTION = "live_powerProduction";
    public static final String LIVE_ACCUMULATEDPRODUCTION = "live_accumulatedProduction";
    public static final String LIVE_MINPOWERPRODUCTION = "live_minPowerproduction";
    public static final String LIVE_MAXPOWERPRODUCTION = "live_maxPowerproduction";

    // Lift of all config ids
    public static final String CONFIG_BRIDGE_TOKEN = "token";
    public static final String CONFIG_BRIDGE_REFRESH = "refresh";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(TIBBER_THING_TYPE)
            .collect(Collectors.toSet());
}
