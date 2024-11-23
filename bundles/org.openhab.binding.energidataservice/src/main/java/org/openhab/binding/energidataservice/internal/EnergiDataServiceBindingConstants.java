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
package org.openhab.binding.energidataservice.internal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnergiDataServiceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class EnergiDataServiceBindingConstants {

    public static final String BINDING_ID = "energidataservice";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVICE = new ThingTypeUID(BINDING_ID, "service");

    // List of all Channel Group ids
    public static final String CHANNEL_GROUP_ELECTRICITY = "electricity";

    // List of all Channel ids
    public static final String CHANNEL_SPOT_PRICE = CHANNEL_GROUP_ELECTRICITY + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + "spot-price";
    public static final String CHANNEL_GRID_TARIFF = CHANNEL_GROUP_ELECTRICITY + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + "grid-tariff";
    public static final String CHANNEL_SYSTEM_TARIFF = CHANNEL_GROUP_ELECTRICITY + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + "system-tariff";
    public static final String CHANNEL_ELECTRICITY_TAX = CHANNEL_GROUP_ELECTRICITY + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + "electricity-tax";
    public static final String CHANNEL_REDUCED_ELECTRICITY_TAX = CHANNEL_GROUP_ELECTRICITY
            + ChannelUID.CHANNEL_GROUP_SEPARATOR + "reduced-electricity-tax";
    public static final String CHANNEL_TRANSMISSION_GRID_TARIFF = CHANNEL_GROUP_ELECTRICITY
            + ChannelUID.CHANNEL_GROUP_SEPARATOR + "transmission-grid-tariff";
    public static final String CHANNEL_CO2_EMISSION_PROGNOSIS = CHANNEL_GROUP_ELECTRICITY
            + ChannelUID.CHANNEL_GROUP_SEPARATOR + "co2-emission-prognosis";
    public static final String CHANNEL_CO2_EMISSION_REALTIME = CHANNEL_GROUP_ELECTRICITY
            + ChannelUID.CHANNEL_GROUP_SEPARATOR + "co2-emission-realtime";
    public static final String CHANNEL_EVENT = CHANNEL_GROUP_ELECTRICITY + ChannelUID.CHANNEL_GROUP_SEPARATOR + "event";

    public static final Set<String> ELECTRICITY_CHANNELS = Set.of(CHANNEL_SPOT_PRICE, CHANNEL_GRID_TARIFF,
            CHANNEL_SYSTEM_TARIFF, CHANNEL_TRANSMISSION_GRID_TARIFF, CHANNEL_ELECTRICITY_TAX,
            CHANNEL_REDUCED_ELECTRICITY_TAX);

    public static final Set<String> CO2_EMISSION_CHANNELS = Set.of(CHANNEL_CO2_EMISSION_PROGNOSIS,
            CHANNEL_CO2_EMISSION_REALTIME);

    public static final Set<String> SUBSCRIPTION_CHANNELS = Stream
            .concat(ELECTRICITY_CHANNELS.stream(), CO2_EMISSION_CHANNELS.stream()).collect(Collectors.toSet());

    // List of all properties
    public static final String PROPERTY_REMAINING_CALLS = "remainingCalls";
    public static final String PROPERTY_TOTAL_CALLS = "totalCalls";
    public static final String PROPERTY_LAST_CALL = "lastCall";
    public static final String PROPERTY_NEXT_CALL = "nextCall";

    // List of all events
    public static final String EVENT_DAY_AHEAD_AVAILABLE = "DAY_AHEAD_AVAILABLE";

    // List of supported currencies
    public static final Currency CURRENCY_DKK = Currency.getInstance("DKK");
    public static final Currency CURRENCY_EUR = Currency.getInstance("EUR");

    public static final Set<Currency> SUPPORTED_CURRENCIES = Set.of(CURRENCY_DKK, CURRENCY_EUR);

    // Time-zone of Datahub
    public static final ZoneId DATAHUB_TIMEZONE = ZoneId.of("CET");
    public static final ZoneId NORD_POOL_TIMEZONE = ZoneId.of("CET");

    // Other
    public static final LocalTime DAILY_REFRESH_TIME_CET = LocalTime.of(13, 0);
    public static final LocalDate ENERGINET_CUTOFF_DATE = LocalDate.of(2023, 1, 1);
    public static final String PROPERTY_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
