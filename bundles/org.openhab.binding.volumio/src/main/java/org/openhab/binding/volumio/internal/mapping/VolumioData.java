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
package org.openhab.binding.volumio.internal.mapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.volumio.internal.VolumioBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VolumioData} class defines state data of volumio.
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioData {

    private final Logger logger = LoggerFactory.getLogger(VolumioData.class);

    private String title = "";
    private boolean titleDirty;

    private String album = "";
    private boolean albumDirty;

    private String artist = "";
    private boolean artistDirty;

    private int volume = 0;
    private boolean volumeDirty;

    private String state = "";
    private boolean stateDirty;

    private String trackType = "";
    private boolean trackTypeDirty;

    private String position = "";
    private boolean positionDirty;

    private byte @Nullable [] coverArt = null;
    private String coverArtUrl = "";
    private boolean coverArtDirty;

    private boolean repeat = false;
    private boolean repeatDirty;

    private boolean random = false;
    private boolean randomDirty;

    public void update(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(VolumioBindingConstants.CHANNEL_TITLE)) {
            setTitle(jsonObject.getString(VolumioBindingConstants.CHANNEL_TITLE));
        } else {
            setTitle("");
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_ALBUM)
                && !jsonObject.isNull(VolumioBindingConstants.CHANNEL_ALBUM)) {
            setAlbum(jsonObject.getString(VolumioBindingConstants.CHANNEL_ALBUM));
        } else {
            setAlbum("");
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_VOLUME)) {
            setVolume(jsonObject.getInt(VolumioBindingConstants.CHANNEL_VOLUME));
        } else {
            setVolume(0);
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_ARTIST)) {
            setArtist(jsonObject.getString(VolumioBindingConstants.CHANNEL_ARTIST));
        } else {
            setArtist("");
        }

        /* Special */
        if (jsonObject.has("status")) {
            setState(jsonObject.getString("status"));
        } else {
            setState("pause");
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_TRACK_TYPE)) {
            setTrackType(jsonObject.getString(VolumioBindingConstants.CHANNEL_TRACK_TYPE));
        } else {
            setTrackType("");
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_COVER_ART)
                && !jsonObject.isNull(VolumioBindingConstants.CHANNEL_COVER_ART)) {
            setCoverArt(jsonObject.getString(VolumioBindingConstants.CHANNEL_COVER_ART));
        } else {
            setCoverArt(null);
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_PLAY_RANDOM)
                && !jsonObject.isNull(VolumioBindingConstants.CHANNEL_PLAY_RANDOM)) {
            setRandom(jsonObject.getBoolean(VolumioBindingConstants.CHANNEL_PLAY_RANDOM));
        } else {
            setRandom(false);
        }

        if (jsonObject.has(VolumioBindingConstants.CHANNEL_PLAY_REPEAT)
                && !jsonObject.isNull(VolumioBindingConstants.CHANNEL_PLAY_REPEAT)) {
            setRepeat(jsonObject.getBoolean(VolumioBindingConstants.CHANNEL_PLAY_REPEAT));
        } else {
            setRepeat(false);
        }
    }

    public StringType getTitle() {
        return new StringType(title);
    }

    public void setTitle(String title) {
        if (!title.equals(this.title)) {
            this.title = title;
            this.titleDirty = true;
        } else {
            this.titleDirty = false;
        }
    }

    public StringType getAlbum() {
        return new StringType(album);
    }

    public void setAlbum(String album) {
        if ("null".equals(album)) {
            album = "";
        }

        if (!album.equals(this.album)) {
            this.album = album;
            this.albumDirty = true;
        } else {
            this.albumDirty = false;
        }
    }

    public StringType getArtist() {
        return new StringType(artist);
    }

    public void setArtist(String artist) {
        if ("null".equals(artist)) {
            this.artist = "";
        }

        if (!artist.equals(this.artist)) {
            this.artist = artist;
            this.artistDirty = true;
        } else {
            this.artistDirty = false;
        }
    }

    public PercentType getVolume() {
        return new PercentType(volume);
    }

    public void setVolume(int volume) {
        if (volume != this.volume) {
            this.volume = volume;
            this.volumeDirty = true;
        } else {
            this.volumeDirty = false;
        }
    }

    public void setState(String state) {
        if (!state.equals(this.state)) {
            this.state = state;
            this.stateDirty = true;
        } else {
            this.stateDirty = false;
        }
    }

    public PlayPauseType getState() {
        PlayPauseType playPauseStatus;

        if ("play".equals(state)) {
            playPauseStatus = PlayPauseType.PLAY;
        } else {
            playPauseStatus = PlayPauseType.PAUSE;
        }

        return playPauseStatus;
    }

    public void setTrackType(String trackType) {
        if (!trackType.equals(this.trackType)) {
            this.trackType = trackType;
            this.trackTypeDirty = true;
        } else {
            this.trackTypeDirty = false;
        }
    }

    public StringType getTrackType() {
        return new StringType(trackType);
    }

    public void setPosition(String position) {
        if (!position.equals(this.position)) {
            this.position = position;
            this.positionDirty = true;
        } else {
            this.positionDirty = false;
        }
    }

    public void setCoverArt(@Nullable String coverArtUrl) {
        if (coverArtUrl != null) {
            if (!Objects.equals(coverArtUrl, this.coverArtUrl)) {
                if (!coverArtUrl.startsWith("http")) {
                    return;
                }

                try {
                    URL url = new URL(coverArtUrl);
                    URLConnection connection = url.openConnection();
                    InputStream inStream = null;
                    inStream = connection.getInputStream();
                    coverArt = inputStreamToByte(inStream);
                } catch (IOException ioe) {
                    coverArt = null;
                }
                this.coverArtDirty = true;
            } else {
                this.coverArtDirty = false;
            }
        } else {
            coverArt = null;
        }
    }

    private byte @Nullable [] inputStreamToByte(InputStream is) {
        byte @Nullable [] imgdata = null;
        try (ByteArrayOutputStream bytestream = new ByteArrayOutputStream()) {
            int ch;
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
            imgdata = bytestream.toByteArray();
            return imgdata;
        } catch (Exception e) {
            logger.error("Could not open or read input stream {}", e.getMessage());
        }

        return imgdata;
    }

    public @Nullable RawType getCoverArt() {
        byte[] localCoverArt = coverArt;
        return localCoverArt == null ? null : new RawType(localCoverArt, "image/jpeg");
    }

    public OnOffType getRandom() {
        return OnOffType.from(random);
    }

    public void setRandom(boolean val) {
        if (val != this.random) {
            this.random = val;
            this.randomDirty = true;
        } else {
            this.randomDirty = false;
        }
    }

    public OnOffType getRepeat() {
        return OnOffType.from(repeat);
    }

    public void setRepeat(boolean val) {
        if (val != this.repeat) {
            this.repeat = val;
            this.repeatDirty = true;
        } else {
            this.repeatDirty = false;
        }
    }

    public StringType getPosition() {
        return new StringType(position);
    }

    public boolean isPositionDirty() {
        return positionDirty;
    }

    public boolean isStateDirty() {
        return stateDirty;
    }

    public boolean isTitleDirty() {
        return titleDirty;
    }

    public boolean isAlbumDirty() {
        return albumDirty;
    }

    public boolean isArtistDirty() {
        return artistDirty;
    }

    public boolean isVolumeDirty() {
        return volumeDirty;
    }

    public boolean isTrackTypeDirty() {
        return trackTypeDirty;
    }

    public boolean isCoverArtDirty() {
        return coverArtDirty;
    }

    public boolean isRandomDirty() {
        return randomDirty;
    }

    public boolean isRepeatDirty() {
        return repeatDirty;
    }
}
