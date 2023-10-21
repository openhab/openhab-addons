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
package org.openhab.binding.weatherunderground.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WeatherUndergroundBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Theo Giovanna - Added a bridge for the API key
 */
@NonNullByDefault
public class WeatherUndergroundBindingConstants {

    public static final String BINDING_ID = "weatherunderground";

    public static final String LOCAL = "local";

    // List all Thing Type UIDs, related to the WeatherUnderground Binding
    public static final ThingTypeUID THING_TYPE_WEATHER = new ThingTypeUID(BINDING_ID, "weather");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(THING_TYPE_WEATHER));

    // Channel configuration Properties
    public static final String PROPERTY_SOURCE_UNIT = "SourceUnit";

    // Bridge config properties
    public static final String APIKEY = "apikey";
}
