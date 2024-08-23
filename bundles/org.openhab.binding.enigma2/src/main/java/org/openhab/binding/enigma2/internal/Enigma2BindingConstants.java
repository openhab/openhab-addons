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
package org.openhab.binding.enigma2.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Enigma2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2BindingConstants {

    private static final String BINDING_ID = "enigma2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE);

    // List of all Channel ids
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_MEDIA_PLAYER = "mediaPlayer";
    public static final String CHANNEL_MEDIA_STOP = "mediaStop";
    public static final String CHANNEL_ANSWER = "answer";

    // List of all configuration parameters
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_USER = "user";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH = "refreshInterval";
    public static final String CONFIG_TIMEOUT = "timeout";

    public static final int MESSAGE_TIMEOUT = 30;
}
