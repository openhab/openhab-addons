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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
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

    public static final String BINDING_ID = "meross";

    // List of all supported device types

    // Meross light hardware types
    public static final Set<String> LIGHT_HARDWARE_TYPES = Set.of("msl", "mss");

    // Meross garage door openers hardware types
    public static final String MSG100 = "msg100";
    public static final String MSG200 = "msg200";
    public static final Set<String> GARAGE_DOOR_HARDWARE_TYPES = Set.of("msg");

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light"); // catch all when not
                                                                                               // explicitly defined

    public static final ThingTypeUID THING_TYPE_MSG100 = new ThingTypeUID(BINDING_ID, MSG100);
    public static final ThingTypeUID THING_TYPE_MSG200 = new ThingTypeUID(BINDING_ID, MSG200);
    public static final ThingTypeUID THING_TYPE_GARAGE_DOOR = new ThingTypeUID(BINDING_ID, "garage-door"); // catch all
                                                                                                           // when not
                                                                                                           // explicitly
                                                                                                           // defined

    public static final Set<ThingTypeUID> DEVICE_THING_TYPES_UIDS = Set.of( //
            THING_TYPE_LIGHT, //
            THING_TYPE_MSG100, //
            THING_TYPE_MSG200, //
            THING_TYPE_GARAGE_DOOR);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .copyOf(Stream.of(Collections.singleton(THING_TYPE_GATEWAY), DEVICE_THING_TYPES_UIDS).flatMap(Set::stream)
                    .collect(Collectors.toSet()));

    // Hardware types to thing types, specific hardware thing types will have an ID equal to the name of the hardware
    // type
    public static final Map<String, ThingTypeUID> HARDWARE_THING_TYPE_MAP = DEVICE_THING_TYPES_UIDS.stream()
            .collect(Collectors.toMap(ThingTypeUID::getId, Function.identity()));

    // Thing properties
    public static final String PROPERTY_LIGHT_DEVICE_NAME = "lightName"; // Deprecated, name used instead
    public static final String PROPERTY_DEVICE_NAME = "name";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_DEVICE_UUID = "uuid";

    // List of all channel types, different thing types will support different channels types
    public static final String CHANNEL_TOGGLEX = "power";
    public static final String CHANNEL_DOOR_STATE = "door-state";

    // Map of channel types to Meross namespaces
    public static final Map<String, Namespace> CHANNEL_NAMESPACE_MAP = Map.of( //
            CHANNEL_TOGGLEX, Namespace.CONTROL_TOGGLEX, //
            CHANNEL_DOOR_STATE, Namespace.GARAGE_DOOR_STATE);

    public static final Map<Namespace, String> NAMESPACE_CHANNEL_MAP = CHANNEL_NAMESPACE_MAP.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
}
