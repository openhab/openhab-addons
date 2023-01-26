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
package org.openhab.binding.androidtv.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AndroidTVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class AndroidTVBindingConstants {

    private static final String BINDING_ID = "androidtv";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GOOGLETV = new ThingTypeUID(BINDING_ID, "googletv");
    public static final ThingTypeUID THING_TYPE_SHIELDTV = new ThingTypeUID(BINDING_ID, "shieldtv");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_GOOGLETV, THING_TYPE_SHIELDTV).collect(Collectors.toSet()));

    // List of all Channel ids
    public static final String CHANNEL_RAW = "raw";
    public static final String CHANNEL_RAWMSG = "rawmsg";
    public static final String CHANNEL_KEYPRESS = "keypress";
    public static final String CHANNEL_PINCODE = "pincode";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_APPNAME = "appname";
    public static final String CHANNEL_APPURL = "appurl";

    // List of all config properties
    public static final String IPADDRESS = "ipAddress";
    public static final String PORT = "port";
}
