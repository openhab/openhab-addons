/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Enigma2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2BindingConstants {
    public static final String BINDING_ID = "enigma2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // all thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_DEVICE));

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_PLAYER_CONTROL = "playerControl";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_REMOTE_KEY = "remoteKey";
    public static final String CHANNEL_SEND_MESSAGE = "sendMessage";
    public static final String CHANNEL_SEND_WARNING = "sendWarning";
    public static final String CHANNEL_SEND_QUESTION = "sendQuestion";
    public static final String CHANNEL_GET_ANSWER = "getAnswer";

    public static final String CHANNEL_NOW_PLAYING_TITLE = "nowPlayingTitle";
    public static final String CHANNEL_NOW_PLAYING_DESCRIPTION = "nowPlayingDescription";
    public static final String CHANNEL_NOW_PLAYING_DESCRIPTION_EXTENDED = "nowPlayingDescriptionExtended";

    public static final String DEVICE_PARAMETER_HOST = "hostName";
    public static final String DEVICE_PARAMETER_USER = "user";
    public static final String DEVICE_PARAMETER_PASSWORD = "password";
    public static final String DEVICE_PARAMETER_REFRESH = "refreshInterval";
}
