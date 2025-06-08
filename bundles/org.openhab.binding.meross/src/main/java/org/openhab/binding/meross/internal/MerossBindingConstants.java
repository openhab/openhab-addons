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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MerossBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossBindingConstants {

    private static final String BINDING_ID = "meross";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY, THING_TYPE_LIGHT);

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_LIGHT);

    // List of all known Meross light hardware types
    public static final Set<String> DISCOVERABLE_LIGHT_HARDWARE_TYPES = Set.of("msl", "mss");
    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String PROPERTY_DEVICE_NAME = "lightName";
}
