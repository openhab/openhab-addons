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
package org.openhab.binding.mffan.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MfFanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class MfFanBindingConstants {

    private static final String BINDING_ID = "mffan";
    private static final String THING_MFFAN_ID = "mffan";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MFFAN = new ThingTypeUID(BINDING_ID, THING_MFFAN_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MFFAN);

    // List of all Channel ids
    public static final String CHANNEL_FAN_ON = "fan-on";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_FAN_DIRECTION = "fan-direction";
    public static final String CHANNEL_WIND_ON = "wind-on";
    public static final String CHANNEL_WIND_LEVEL = "wind-level";
    public static final String CHANNEL_LIGHT_ON = "light-on";
    public static final String CHANNEL_LIGHT_INTENSITY = "light-intensity";
}
