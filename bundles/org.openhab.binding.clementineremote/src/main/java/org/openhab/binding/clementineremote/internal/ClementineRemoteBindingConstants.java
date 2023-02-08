/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.clementineremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ClementineRemoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stephan Richter - Initial contribution
 */
@NonNullByDefault
public class ClementineRemoteBindingConstants {

    private static final String BINDING_ID = "clementineremote";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CLEMENTINE = new ThingTypeUID(BINDING_ID, "clementine");

    // List of all Channel ids
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_COVER = "cover";
    public static final String CHANNEL_PLAYBACK = "playback-control";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_VOLUME = "volume-control";

    // List of commands
    public static final String CMD_FORWARD = "FASTFORWARD";
    public static final String CMD_NEXT = "NEXT";
    public static final String CMD_PAUSE = "PAUSE";
    public static final String CMD_PLAY = "PLAY";
    public static final String CMD_PREVIOUS = "PREVIOUS";
    public static final String CMD_REWIND = "REWIND";
    public static final String CMD_STOP = "STOP";

    // remote config
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5500;
    public static final int MAX_SIZE = 52428800;
}
