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
package org.openhab.binding.mpd.internal.protocol;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for representing a song.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDSong {

    private final Logger logger = LoggerFactory.getLogger(MPDSong.class);

    private final String filename;
    private final String album;
    private final String artist;
    private final String name;
    private final int song;
    private final int songId;
    private final String title;
    private final int track;

    public MPDSong(MPDResponse response) {
        Map<String, String> values = MPDResponseParser.responseToMap(response);
        filename = values.getOrDefault("file", "");
        album = values.getOrDefault("Album", "");
        artist = values.getOrDefault("Artist", "");
        name = values.getOrDefault("Name", "");
        song = parseInteger(values.getOrDefault("Pos", "0"), 0);
        songId = parseInteger(values.getOrDefault("Id", "0"), 0);
        title = values.getOrDefault("Title", "");
        track = parseInteger(values.getOrDefault("Track", "-1"), -1);
    }

    public String getFilename() {
        return filename;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public int getSong() {
        return song;
    }

    public int getSongId() {
        return songId;
    }

    public String getTitle() {
        return title;
    }

    public int getTrack() {
        return track;
    }

    private int parseInteger(String value, int aDefault) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.debug("parseInt of {} failed", value);
        }
        return aDefault;
    }
}
