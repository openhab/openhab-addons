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
package org.openhab.binding.coronastats.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CoronaStatsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsBindingConstants {

    private static final String BINDING_ID = "coronastats";

    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // Bridge channels
    public static final String CHANNEL_REFRESHED = "refreshed";

    // Country
    public static final ThingTypeUID THING_TYPE_WORLD = new ThingTypeUID(BINDING_ID, "world");

    // Country
    public static final ThingTypeUID THING_TYPE_COUNTRY = new ThingTypeUID(BINDING_ID, "country");

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
