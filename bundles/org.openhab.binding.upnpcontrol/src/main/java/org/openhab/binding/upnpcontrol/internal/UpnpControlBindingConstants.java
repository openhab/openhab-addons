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
package org.openhab.binding.upnpcontrol.internal;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link UpnpControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpControlBindingConstants {

    public static final String BINDING_ID = "upnpcontrol";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RENDERER = new ThingTypeUID(BINDING_ID, "upnprenderer");
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "upnpserver");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_RENDERER, THING_TYPE_SERVER)
            .collect(Collectors.toSet());

    // Binding config parameters
    public static final String PATH = "path";

    // Thing config parameters
    public static final String UDN_PARAMETER = "udn";
    public static final String REFRESH_INTERVAL = "refresh";
    public static final String RESPONSE_TIMEOUT = "responsetimeout";
    // Server thing only config parameters
    public static final String CONFIG_FILTER = "filter";
    public static final String SORT_CRITERIA = "sortcriteria";
    public static final String BROWSE_DOWN = "browsedown";
    public static final String SEARCH_FROM_ROOT = "searchfromroot";
    // Renderer thing only config parameters
    public static final String NOTIFICATION_VOLUME_ADJUSTMENT = "notificationvolumeadjustment";
    public static final String MAX_NOTIFICATION_DURATION = "maxnotificationduration";
    public static final String SEEK_STEP = "seekstep";

    // List of all Channel ids
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String CONTROL = "control";
    public static final String STOP = "stop";
    public static final String REPEAT = "repeat";
    public static final String SHUFFLE = "shuffle";
    public static final String ONLY_PLAY_ONE = "onlyplayone";
    public static final String URI = "uri";
    public static final String FAVORITE_SELECT = "favoriteselect";
    public static final String FAVORITE = "favorite";
    public static final String FAVORITE_ACTION = "favoriteaction";
    public static final String TITLE = "title";
    public static final String ALBUM = "album";
    public static final String ALBUM_ART = "albumart";
    public static final String CREATOR = "creator";
    public static final String ARTIST = "artist";
    public static final String PUBLISHER = "publisher";
    public static final String GENRE = "genre";
    public static final String TRACK_NUMBER = "tracknumber";
    public static final String TRACK_DURATION = "trackduration";
    public static final String TRACK_POSITION = "trackposition";
    public static final String REL_TRACK_POSITION = "reltrackposition";

    public static final String UPNPRENDERER = "upnprenderer";
    public static final String CURRENTTITLE = "currenttitle";
    public static final String BROWSE = "browse";
    public static final String SEARCH = "search";
    public static final String SERVE = "serve";
    public static final String PLAYLIST_SELECT = "playlistselect";
    public static final String PLAYLIST = "playlist";
    public static final String PLAYLIST_ACTION = "playlistaction";

    // Type constants for dynamic renderer channels
    public static final String CHANNEL_TYPE_VOLUME = DefaultSystemChannelTypeProvider.SYSTEM_VOLUME.toString();
    public static final String CHANNEL_TYPE_MUTE = DefaultSystemChannelTypeProvider.SYSTEM_MUTE.toString();
    public static final String CHANNEL_TYPE_LOUDNESS = (new ChannelTypeUID(BINDING_ID, "loudness")).toString();

    public static final String ITEM_TYPE_VOLUME = "Dimmer";
    public static final String ITEM_TYPE_MUTE = "Switch";
    public static final String ITEM_TYPE_LOUDNESS = "Switch";

    // Command options for playlist and favorite actions
    public static final String RESTORE = "RESTORE";
    public static final String SAVE = "SAVE";
    public static final String APPEND = "APPEND";
    public static final String DELETE = "DELETE";

    // Channels that are duplicated on server to control current renderer
    public static final Set<String> SERVER_CONTROL_CHANNELS = Set.of(VOLUME, MUTE, CONTROL, STOP);

    // Master volume and mute identifier
    public static final String UPNP_MASTER = "Master";

    // Filepath and extension defaults and constants for playlists and favorites
    public static final String DEFAULT_PATH = OpenHAB.getUserDataFolder() + File.separator + BINDING_ID
            + File.separator;
    public static final String PLAYLIST_FILE_EXTENSION = ".lst";
    public static final String FAVORITE_FILE_EXTENSION = ".fav";

    // Notification audio sink name extension
    public static final String NOTIFICATION_AUDIOSINK_EXTENSION = "-notify";
}
