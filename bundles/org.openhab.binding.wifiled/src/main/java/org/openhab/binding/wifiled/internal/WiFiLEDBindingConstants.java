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
package org.openhab.binding.wifiled.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WiFiLEDBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class WiFiLEDBindingConstants {

    public static final String BINDING_ID = "wifiled";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WIFILED = new ThingTypeUID(BINDING_ID, "wifiled");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_WIFILED);

    // List of all Channel IDs
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_WHITE = "white";
    public static final String CHANNEL_WHITE2 = "white2";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_PROGRAM_SPEED = "programSpeed";
}
