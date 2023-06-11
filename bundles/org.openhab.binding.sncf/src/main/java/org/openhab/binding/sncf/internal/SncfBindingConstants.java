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
package org.openhab.binding.sncf.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SncfBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SncfBindingConstants {

    public static final String BINDING_ID = "sncf";

    // Station properties
    public static final String STOP_POINT_ID = "stopPointId";
    public static final String DISTANCE = "Distance";
    public static final String LOCATION = "Location";
    public static final String TIMEZONE = "Timezone";

    // List of Channel groups
    public static final String GROUP_ARRIVAL = "arrivals";
    public static final String GROUP_DEPARTURE = "departures";

    // List of Channel id's
    public static final String DIRECTION = "direction";
    public static final String LINE_NAME = "lineName";
    public static final String NAME = "name";
    public static final String NETWORK = "network";
    public static final String TIMESTAMP = "timestamp";

    // List of Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "api");
    public static final ThingTypeUID STATION_THING_TYPE = new ThingTypeUID(BINDING_ID, "station");

    // List of all adressable things
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(APIBRIDGE_THING_TYPE, STATION_THING_TYPE);
}
