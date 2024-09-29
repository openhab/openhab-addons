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
package org.openhab.binding.sonos.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonos.internal.SonosMetaData;
import org.openhab.binding.sonos.internal.SonosXMLParser;

/**
 * The {@link SonosMediaInformation} is responsible for extracting media information from XML metadata
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonosMediaInformation {

    private @Nullable String artist;
    private @Nullable String album;
    private @Nullable String title;
    private @Nullable String combinedInfo;
    private boolean needsUpdate;

    public SonosMediaInformation() {
        this(false);
    }

    public SonosMediaInformation(boolean needsUpdate) {
        this(null, null, null, null, needsUpdate);
    }

    public SonosMediaInformation(@Nullable String artist, @Nullable String album, @Nullable String title,
            @Nullable String combinedInfo, boolean needsUpdate) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.combinedInfo = combinedInfo;
        this.needsUpdate = needsUpdate;
    }

    public @Nullable String getArtist() {
        return artist;
    }

    public @Nullable String getAlbum() {
        return album;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @Nullable String getCombinedInfo() {
        return combinedInfo;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public static SonosMediaInformation parseTuneInMediaInfo(@Nullable String opmlData, @Nullable String radioTitle,
            @Nullable SonosMetaData trackMetaData) {
        String title = null;
        String combinedInfo = null;
        if (opmlData != null) {
            List<String> fields = SonosXMLParser.getRadioTimeFromXML(opmlData);
            if (!fields.isEmpty()) {
                title = fields.get(0);
                combinedInfo = String.join(" - ", fields);
                return new SonosMediaInformation(null, null, title, combinedInfo, true);
            }
        }
        if (radioTitle != null && !radioTitle.isEmpty()) {
            title = radioTitle;
            combinedInfo = title;
            if (trackMetaData != null && !trackMetaData.getStreamContent().isEmpty()) {
                combinedInfo += " - " + trackMetaData.getStreamContent();
            }
            return new SonosMediaInformation(null, null, title, combinedInfo, true);
        }
        return new SonosMediaInformation(false);
    }

    public static SonosMediaInformation parseRadioAppMediaInfo(@Nullable String radioTitle,
            @Nullable SonosMetaData trackMetaData) {
        if (radioTitle != null && !radioTitle.isEmpty()) {
            String artist = null;
            String album = null;
            String title = radioTitle;
            String combinedInfo = title;
            if (trackMetaData != null) {
                String[] contents = trackMetaData.getStreamContent().split("\\|");
                String contentTitle = null;
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i].startsWith("TITLE ")) {
                        contentTitle = contents[i].substring(6).trim();
                    }
                    if (contents[i].startsWith("ARTIST ")) {
                        artist = contents[i].substring(7).trim();
                    }
                    if (contents[i].startsWith("ALBUM ")) {
                        album = contents[i].substring(6).trim();
                    }
                }
                if ((artist == null || artist.isEmpty()) && contentTitle != null && !contentTitle.isEmpty()
                        && !contentTitle.startsWith("Advertisement_")) {
                    // Try to extract artist and song title from contentTitle
                    int idx = contentTitle.indexOf(" - ");
                    if (idx > 0) {
                        artist = contentTitle.substring(0, idx);
                        title = contentTitle.substring(idx + 3);
                    }
                } else if (artist != null && !artist.isEmpty() && album != null && !album.isEmpty()
                        && contentTitle != null && !contentTitle.isEmpty()) {
                    title = contentTitle;
                }
                if (artist != null && !artist.isEmpty()) {
                    combinedInfo += " - " + artist;
                }
                if (album != null && !album.isEmpty()) {
                    combinedInfo += " - " + album;
                }
                if (!radioTitle.equals(title)) {
                    combinedInfo += " - " + title;
                } else if (contentTitle != null && !contentTitle.isEmpty()
                        && !contentTitle.startsWith("Advertisement_")) {
                    combinedInfo += " - " + contentTitle;
                }
            }
            return new SonosMediaInformation(artist, album, title, combinedInfo, true);
        }
        return new SonosMediaInformation(false);
    }

    public static SonosMediaInformation parseTrack(@Nullable SonosMetaData trackMetaData) {
        if (trackMetaData != null) {
            List<String> infos = new ArrayList<>();
            String artist = !trackMetaData.getAlbumArtist().isEmpty() ? trackMetaData.getAlbumArtist()
                    : trackMetaData.getCreator();
            if (!artist.isEmpty()) {
                infos.add(artist);
            }
            String album = trackMetaData.getAlbum();
            if (!album.isEmpty()) {
                infos.add(album);
            }
            String title = trackMetaData.getTitle();
            if (!title.isEmpty()) {
                infos.add(title);
            }
            return new SonosMediaInformation(artist, album, title, String.join(" - ", infos), true);
        }
        return new SonosMediaInformation(false);
    }

    public static SonosMediaInformation parseTrackTitle(@Nullable SonosMetaData trackMetaData) {
        if (trackMetaData != null) {
            String title = trackMetaData.getTitle();
            return new SonosMediaInformation(null, null, title, title, true);
        }
        return new SonosMediaInformation(false);
    }
}
