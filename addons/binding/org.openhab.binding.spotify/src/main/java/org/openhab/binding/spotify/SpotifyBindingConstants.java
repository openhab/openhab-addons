/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SpotifyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andreas Stenlund - Initial contribution
 */
public class SpotifyBindingConstants {

    private static final String BINDING_ID = "spotify";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_REFRESHTOKEN = "refreshToken";

    public static final String CHANNEL_TRACKID = "trackId";
    public static final String CHANNEL_TRACKHREF = "trackHref";
    public static final String CHANNEL_TRACKPLAYER = "trackPlayer";
    public static final String CHANNEL_TRACKSHUFFLE = "trackShuffle";
    public static final String CHANNEL_TRACKREPEAT = "trackRepeat";

    public static final String CHANNEL_PLAYED_TRACKID = "trackId";
    public static final String CHANNEL_PLAYED_TRACKURI = "trackUri";
    public static final String CHANNEL_PLAYED_TRACKHREF = "trackHref";
    public static final String CHANNEL_PLAYED_TRACKNAME = "trackName";
    public static final String CHANNEL_PLAYED_TRACKTYPE = "trackType";
    public static final String CHANNEL_PLAYED_TRACKNUMBER = "trackNumber";
    public static final String CHANNEL_PLAYED_TRACKDISCNUMBER = "trackDiscNumber";
    public static final String CHANNEL_PLAYED_TRACKPOPULARITY = "trackPopularity";
    public static final String CHANNEL_PLAYED_TRACKDURATION = "trackDuration";
    public static final String CHANNEL_PLAYED_TRACKPROGRESS = "trackProgress";
    public static final String CHANNEL_PLAYED_TRACKDURATIONFMT = "trackDurationFmt";
    public static final String CHANNEL_PLAYED_TRACKPROGRESSFMT = "trackProgressFmt";

    public static final String CHANNEL_PLAYED_ALBUMID = "albumId";
    public static final String CHANNEL_PLAYED_ALBUMURI = "albumUri";
    public static final String CHANNEL_PLAYED_ALBUMHREF = "albumHref";
    public static final String CHANNEL_PLAYED_ALBUMNAME = "albumName";
    public static final String CHANNEL_PLAYED_ALBUMTYPE = "albumType";

    public static final String CHANNEL_PLAYED_ARTISTID = "artistId";
    public static final String CHANNEL_PLAYED_ARTISTURI = "artistUri";
    public static final String CHANNEL_PLAYED_ARTISTHREF = "artistHref";
    public static final String CHANNEL_PLAYED_ARTISTNAME = "artistName";
    public static final String CHANNEL_PLAYED_ARTISTTYPE = "artistType";

    public static final String CHANNEL_DEVICEID = "deviceId";
    public static final String CHANNEL_DEVICENAME = "deviceName";
    public static final String CHANNEL_DEVICETYPE = "deviceType";
    public static final String CHANNEL_DEVICEACTIVE = "deviceActive";
    public static final String CHANNEL_DEVICEVOLUME = "deviceVolume";
    public static final String CHANNEL_DEVICESHUFFLE = "deviceShuffle";
    public static final String CHANNEL_DEVICEPLAY = "devicePlay";

}
