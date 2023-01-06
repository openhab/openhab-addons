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
package org.openhab.binding.dwdpollenflug.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DWDPollenflugBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBindingConstants {

    private static final String BINDING_ID = "dwdpollenflug";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final String DWD = "dwd";
    public static final String BRIDGE_LABEL = "DWD Pollen Count Index (Bridge)";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_REGION = new ThingTypeUID(BINDING_ID, "region");

    // @formatter:off
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = 
        Collections.unmodifiableSet(Stream
            .of(THING_TYPE_BRIDGE, THING_TYPE_REGION)
            .collect(Collectors.toSet())
        );
    // @formatter:on

    // Channels of pollen groups
    public static final String CHANNEL_TODAY = "today";
    public static final String CHANNEL_TOMORROW = "tomorrow";
    public static final String CHANNEL_DAYAFTER_TO = "dayafter_to";

    // Channels of region update
    public static final String CHANNEL_UPDATES = "updates";
    public static final String CHANNEL_REFRESHED = "refreshed";
    public static final String CHANNEL_NEXT_UPDATE = "next_update";
    public static final String CHANNEL_LAST_UPDATE = "last_update";
    public static final String CHANNEL_UPDATED = "updated";

    public static final String TRIGGER_REFRESHED = "REFRESHED";

    // Bridge config properties
    public static final String REFRESH = "refresh";

    // Bridge properties
    public static final String PROPERTY_SENDER = "sender";
    public static final String PROPERTY_NAME = "name";

    // Region config properties
    public static final String REGION_ID = "regionID";

    // Region properties
    public static final String PROPERTY_REGION_NAME = "region_name";
    public static final String PROPERTY_PARTREGION_NAME = "partregion_name";
}
