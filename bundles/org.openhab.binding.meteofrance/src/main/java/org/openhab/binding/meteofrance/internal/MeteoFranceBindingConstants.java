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
package org.openhab.binding.meteofrance.internal;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MeteoFranceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoFranceBindingConstants {
    public static final String BINDING_ID = "meteofrance";
    public static final String LOCAL = "local";
    public static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(45);

    // List of Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_API = new ThingTypeUID(BINDING_ID, "api");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VIGILANCE = new ThingTypeUID(BINDING_ID, "vigilance");
    public static final ThingTypeUID THING_TYPE_RAIN_FORECAST = new ThingTypeUID(BINDING_ID, "rain-forecast");

    // List of all Channel IDs

    public static final String OBSERVATION_TIME = "observation-time";
    public static final String UPDATE_TIME = "update-time";
    public static final String INTENSITY = "intensity";

    public static final String END_TIME = "end-time";
    public static final String COMMENT = "comment";

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_VIGILANCE,
            THING_TYPE_RAIN_FORECAST);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(Set.of(BRIDGE_TYPE_API), DISCOVERABLE_THING_TYPES_UIDS).flatMap(Set::stream)
            .collect(Collectors.toSet());
}
