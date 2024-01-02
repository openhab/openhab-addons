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
package org.openhab.binding.coronastats.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CoronaStatsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsBindingConstants {
    public static final String BINDING_ID = "coronastats";

    // World
    public static final ThingTypeUID THING_TYPE_WORLD = new ThingTypeUID(BINDING_ID, "world");
    public static final String STATS = "stats";
    public static final String WORLD_LABEL = "Corona Statistics (World)";

    // Country
    public static final ThingTypeUID THING_TYPE_COUNTRY = new ThingTypeUID(BINDING_ID, "country");

    // @formatter:off
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = 
        Collections.unmodifiableSet(Stream
            .of(THING_TYPE_WORLD, THING_TYPE_COUNTRY)
            .collect(Collectors.toSet())
        );
    // @formatter:on

    // Properties country
    public static final String PROPERTY_COUNTRY = "country";

    // Channels world/country
    public static final String CHANNEL_CASES = "cases";
    public static final String CHANNEL_NEW_CASES = "today_cases";
    public static final String CHANNEL_DEATHS = "deaths";
    public static final String CHANNEL_NEW_DEATHS = "today_deaths";
    public static final String CHANNEL_RECOVERED = "recovered";
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_CRITICAL = "critical";
    public static final String CHANNEL_TESTS = "tests";
    public static final String CHANNEL_UPDATED = "updated";
}
