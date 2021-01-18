/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.util.GsonDurationTypeAdapter;
import org.openhab.binding.octopusenergy.internal.util.GsonLocalDateTypeAdapter;
import org.openhab.binding.octopusenergy.internal.util.GsonLocalTimeTypeAdapter;
import org.openhab.binding.octopusenergy.internal.util.GsonZonedDateTimeTypeAdapter;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link OctopusEnergyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyBindingConstants {

    private static final String BINDING_ID = "octopusenergy";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ELECTRICITY_METER_POINT = new ThingTypeUID(BINDING_ID,
            "electricityMeterPoint");
    public static final ThingTypeUID THING_TYPE_GAS_METER_POINT = new ThingTypeUID(BINDING_ID, "gasMeterPoint");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_ELECTRICITY_METER_POINT, THING_TYPE_GAS_METER_POINT));

    // List of all Channel ids
    public static final String CHANNEL_BRIDGE_REFRESH = "refresh";
    public static final String CHANNEL_BRIDGE_LAST_REFRESH_TIME = "lastRefreshTime";

    public static final String CHANNEL_METERPOINT_MPAN = "mpan";
    public static final String CHANNEL_METERPOINT_MPRN = "mprn";
    public static final String CHANNEL_METERPOINT_CURRENT_TARIFF = "currentTariff";
    public static final String CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_AMOUNT = "mostRecentConsumptionAmount";
    public static final String CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_START_TIME = "mostRecentConsumptionStartTime";
    public static final String CHANNEL_METERPOINT_MOST_RECENT_CONSUPTION_END_TIME = "mostRecentConsumptionEndTime";
    public static final String CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_START_TIME = "unitPriceWindowStartTime";
    public static final String CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_END_TIME = "unitPriceWindowEndTime";
    public static final String CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MIN_AMOUNT = "unitPriceWindowMinAmount";
    public static final String CHANNEL_METERPOINT_UNIT_PRICE_WINDOW_MAX_AMOUNT = "unitPriceWindowMaxAmount";

    // Other Binding constants

    public static final String PROPERTY_NAME_MPAN = "mpan";
    public static final String PROPERTY_NAME_MPRN = "mprn";

    public static final long DEFAULT_REFRESH_INTERVAL_MINS = 15;

    public static final String UNDEFINED_STRING = "UNDEFINED";
    public static final ZonedDateTime UNDEFINED_TIME = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Duration.class, new GsonDurationTypeAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new GsonZonedDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new GsonLocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime.class, new GsonLocalTimeTypeAdapter()).create();
}
