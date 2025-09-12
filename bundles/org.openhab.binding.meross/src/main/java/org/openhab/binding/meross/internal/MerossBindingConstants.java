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
package org.openhab.binding.meross.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MerossBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Added garage door support
 *
 */
@NonNullByDefault
public class MerossBindingConstants {

    static final String BINDING_ID = "meross";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final Set<ThingTypeUID> LIGHT_THING_TYPES = Set.of(THING_TYPE_LIGHT);

    public static final ThingTypeUID THING_TYPE_DOOR = new ThingTypeUID(BINDING_ID, "door");
    public static final ThingTypeUID THING_TYPE_TRIPLE_DOOR = new ThingTypeUID(BINDING_ID, "tripleDoor");
    public static final Set<ThingTypeUID> DOOR_THING_TYPES = Set.of(THING_TYPE_DOOR, THING_TYPE_TRIPLE_DOOR);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .copyOf(Stream.of(Collections.singleton(THING_TYPE_GATEWAY), LIGHT_THING_TYPES, DOOR_THING_TYPES)
                    .flatMap(Set::stream).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set
            .copyOf(Stream.of(LIGHT_THING_TYPES, DOOR_THING_TYPES).flatMap(Set::stream).collect(Collectors.toSet()));

    public static final String PROPERTY_LIGHT_DEVICE_NAME = "lightName"; // Deprecated, name used instead
    public static final String PROPERTY_DEVICE_NAME = "name";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_DEVICE_UUID = "uuid";

    // List of all known Meross light hardware types
    public static final Set<String> DISCOVERABLE_LIGHT_HARDWARE_TYPES = Set.of("msl", "mss");
    // List of all Channel ids
    public static final String CHANNEL_LIGHT_POWER = "power";

    // List of all known Meross garage door hardware types
    public static final Set<String> DISCOVERABLE_DOOR_HARDWARE_TYPES = Set.of("msg");
    public static final String MSG100 = "msg100";
    public static final String MSG200 = "msg200";
    // List of all Channel ids
    public static final String CHANNEL_DOOR_STATE = "doorState";
    public static final String CHANNEL_DOOR_STATE_0 = "doorState0";
    public static final String CHANNEL_DOOR_STATE_1 = "doorState1";
    public static final String CHANNEL_DOOR_STATE_2 = "doorState2";
}
