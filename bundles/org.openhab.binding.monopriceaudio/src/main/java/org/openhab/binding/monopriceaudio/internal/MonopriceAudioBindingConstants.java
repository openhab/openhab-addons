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
package org.openhab.binding.monopriceaudio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MonopriceAudioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 * @author Michael Lobstein - Add support for additional amplifier types
 */
@NonNullByDefault
public class MonopriceAudioBindingConstants {

    public static final String BINDING_ID = "monopriceaudio";

    // List of all Thing Type UIDs
    // to avoid breaking existing installations, the 10761/DAX66 will still be known as 'amplifier'
    public static final ThingTypeUID THING_TYPE_MP = new ThingTypeUID(BINDING_ID, "amplifier");
    public static final ThingTypeUID THING_TYPE_MP70 = new ThingTypeUID(BINDING_ID, "monoprice70");
    public static final ThingTypeUID THING_TYPE_DAX88 = new ThingTypeUID(BINDING_ID, "dax88");
    public static final ThingTypeUID THING_TYPE_XT = new ThingTypeUID(BINDING_ID, "xantech");

    // List of all Channel types
    public static final String CHANNEL_TYPE_POWER = "power";
    public static final String CHANNEL_TYPE_SOURCE = "source";
    public static final String CHANNEL_TYPE_VOLUME = "volume";
    public static final String CHANNEL_TYPE_MUTE = "mute";
    public static final String CHANNEL_TYPE_TREBLE = "treble";
    public static final String CHANNEL_TYPE_BASS = "bass";
    public static final String CHANNEL_TYPE_BALANCE = "balance";
    public static final String CHANNEL_TYPE_DND = "dnd";
    public static final String CHANNEL_TYPE_PAGE = "page";
    public static final String CHANNEL_TYPE_KEYPAD = "keypad";
    public static final String CHANNEL_TYPE_ALLPOWER = "allpower";
    public static final String CHANNEL_TYPE_ALLSOURCE = "allsource";
    public static final String CHANNEL_TYPE_ALLVOLUME = "allvolume";
    public static final String CHANNEL_TYPE_ALLMUTE = "allmute";

    // misc
    public static final String ONE = "1";
    public static final String ZERO = "0";
    public static final String EMPTY = "";
    public static final int NIL = -1;
}
