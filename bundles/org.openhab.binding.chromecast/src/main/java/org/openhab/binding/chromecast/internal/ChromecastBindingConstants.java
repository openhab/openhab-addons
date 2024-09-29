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
package org.openhab.binding.chromecast.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ChromecastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jason Holmes - Additional channels
 */
@NonNullByDefault
public class ChromecastBindingConstants {
    public static final String BINDING_ID = "chromecast";
    public static final String MEDIA_PLAYER = "CC1AD845";

    public static final ThingTypeUID THING_TYPE_CHROMECAST = new ThingTypeUID(BINDING_ID, "chromecast");
    public static final ThingTypeUID THING_TYPE_AUDIO = new ThingTypeUID(BINDING_ID, "audio");
    public static final ThingTypeUID THING_TYPE_AUDIOGROUP = new ThingTypeUID(BINDING_ID, "audiogroup");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_AUDIO, THING_TYPE_AUDIOGROUP, THING_TYPE_CHROMECAST).collect(Collectors.toSet()));

    // Config Parameters
    public static final String HOST = "ipAddress";
    public static final String PORT = "port";
    public static final String DEVICE_ID = "deviceId";

    // Channel IDs
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_PLAY_URI = "playuri";

    public static final String CHANNEL_APP_NAME = "appName";
    public static final String CHANNEL_APP_ID = "appId";
    public static final String CHANNEL_IDLING = "idling";
    public static final String CHANNEL_STATUS_TEXT = "statustext";

    public static final String CHANNEL_CURRENT_TIME = "currentTime";
    public static final String CHANNEL_DURATION = "duration";

    public static final String CHANNEL_METADATA_TYPE = "metadataType";

    public static final String CHANNEL_ALBUM_ARTIST = "albumArtist";
    public static final String CHANNEL_ALBUM_NAME = "albumName";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_BROADCAST_DATE = "broadcastDate";
    public static final String CHANNEL_COMPOSER = "composer";
    public static final String CHANNEL_CREATION_DATE = "creationDate";
    public static final String CHANNEL_DISC_NUMBER = "discNumber";
    public static final String CHANNEL_EPISODE_NUMBER = "episodeNumber";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_IMAGE_SRC = "imageSrc";
    public static final String CHANNEL_LOCATION_NAME = "locationName";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_RELEASE_DATE = "releaseDate";
    public static final String CHANNEL_SEASON_NUMBER = "seasonNumber";
    public static final String CHANNEL_SERIES_TITLE = "seriesTitle";
    public static final String CHANNEL_STUDIO = "studio";
    public static final String CHANNEL_SUBTITLE = "subtitle";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_TRACK_NUMBER = "trackNumber";

    /**
     * These are channels that map directly. Images and location are unique channels that
     * don't fit this description.
     */
    public static final Set<String> METADATA_SIMPLE_CHANNELS = Collections
            .unmodifiableSet(Stream
                    .of(CHANNEL_ALBUM_ARTIST, CHANNEL_ALBUM_NAME, CHANNEL_ARTIST, CHANNEL_BROADCAST_DATE,
                            CHANNEL_COMPOSER, CHANNEL_CREATION_DATE, CHANNEL_DISC_NUMBER, CHANNEL_EPISODE_NUMBER,
                            CHANNEL_LOCATION_NAME, CHANNEL_RELEASE_DATE, CHANNEL_SEASON_NUMBER, CHANNEL_SERIES_TITLE,
                            CHANNEL_STUDIO, CHANNEL_SUBTITLE, CHANNEL_TITLE, CHANNEL_TRACK_NUMBER)
                    .collect(Collectors.toSet()));

    // We don't key these metadata keys directly to a channel, they get linked together
    // into a Location channel.
    public static final String LOCATION_METADATA_LATITUDE = "locationLatitude";
    public static final String LOCATION_METADATA_LONGITUDE = "locationLongitude";
}
