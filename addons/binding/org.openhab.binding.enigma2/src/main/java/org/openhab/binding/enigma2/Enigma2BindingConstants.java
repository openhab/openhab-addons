/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
    public final static ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // all thing types
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_DEVICE));

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_MUTE = "mute";
    public final static String CHANNEL_PLAYER_CONTROL = "playerControl";
    public final static String CHANNEL_CHANNEL = "channel";

    public final static String CHANNEL_NOW_PLAYING_TITLE = "nowPlayingTitle";
    public final static String CHANNEL_NOW_PLAYING_DESCRIPTION = "nowPlayingDescription";
    public final static String CHANNEL_NOW_PLAYING_DESCRIPTION_EXTENDED = "nowPlayingDescriptionExtended";

    public final static String DEVICE_PARAMETER_HOST = "DEVICE_HOST";

    public final static String DEVICE_PARAMETER_USER = "USER";
    public final static String DEVICE_PARAMETER_PASSWORD = "PASSWORD";
    public final static String DEVICE_PARAMETER_REFRESH = "REFRESH";

}
