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
package org.openhab.binding.lirc.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LIRCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCBindingConstants {

    public static final String BINDING_ID = "lirc";
    public static final int DISCOVERY_TIMOUT = 5;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_REMOTE = new ThingTypeUID(BINDING_ID, "remote");

    // List of all channel ids
    public static final String CHANNEL_EVENT = "event";
    public static final String CHANNEL_TRANSMIT = "transmit";

    // List of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_TYPES = Set.of(THING_TYPE_REMOTE);
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES = Set.of(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_REMOTE, THING_TYPE_BRIDGE)
            .collect(Collectors.toSet());

    // List of all properties
    public static final String PROPERTY_REMOTE = "remote";
}
