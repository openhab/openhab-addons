/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.volumio.internal.VolumioBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the last known player state, as delivered by the Volumio {@code pushState} event, and the size of the
 * play queue, as delivered by {@code pushQueue}.
 * <p>
 * The JSON keys used here follow the Volumio WebSocket API documentation ("Get Player State").
 * <p>
 * Each setter remembers whether the value changed, so that the handler only publishes real changes.
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioData {

    private boolean initialized;

    /** Fields that have received a value from the device at least once; the first value is always published. */
    private final Set<String> seen = new HashSet<>();

    private String title = "";
    private boolean titleDirty;

    private String album = "";
    private boolean albumDirty;

    private String artist = "";
    private boolean artistDirty;

    private int volume = 0;
    private boolean volumeDirty;

    private boolean mute = false;
    private boolean muteDirty;

    private String state = "";
    private boolean stateDirty;

    private String trackType = "";
    private boolean trackTypeDirty;

    private String coverArtUrl = "";
    private boolean coverArtDirty;

    private boolean repeat = false;
    private boolean repeatDirty;

    private boolean repeatSingle = false;
    private boolean repeatSingleDirty;

    private boolean random = false;
    private boolean randomDirty;

    private int duration = 0;
    private boolean durationDirty;

    private int position = 0;
    private boolean positionDirty;

    private String uri = "";
    private boolean uriDirty;

    private String service = "";
    private boolean serviceDirty;

    private String sampleRate = "";
    private boolean sampleRateDirty;

    private String bitDepth = "";
    private boolean bitDepthDirty;

    private String audioChannels = "";
    private boolean audioChannelsDirty;

    private boolean stream = false;
    private boolean streamDirty;

    private boolean updateDb = false;
    private boolean updateDbDirty;

    private int queueSize = 0;
    private boolean queueSizeDirty;
    private boolean queueInitialized;

    /**
     * Update the state from a {@code pushState} payload. Text fields and the duration that are missing or null are
     * reset (for example a web radio without artist), numeric and boolean fields keep their previous value if they
     * are missing.
     */
    public synchronized void update(JSONObject json) {
        // Optional fields that are missing in this payload must not keep a stale dirty flag from the previous one.
        stateDirty = volumeDirty = positionDirty = muteDirty = randomDirty = repeatDirty = repeatSingleDirty = false;
        streamDirty = updateDbDirty = false;
        setTitle(text(json, VolumioBindingConstants.STATE_TITLE));
        setAlbum(text(json, VolumioBindingConstants.STATE_ALBUM));
        setArtist(text(json, VolumioBindingConstants.STATE_ARTIST));
        setTrackType(text(json, VolumioBindingConstants.STATE_TRACK_TYPE));
        setCoverArtUrl(text(json, VolumioBindingConstants.STATE_ALBUM_ART));
        setUri(text(json, VolumioBindingConstants.STATE_URI));
        setService(text(json, VolumioBindingConstants.STATE_SERVICE));
        setSampleRate(text(json, VolumioBindingConstants.STATE_SAMPLE_RATE));
        setBitDepth(text(json, VolumioBindingConstants.STATE_BIT_DEPTH));
        setAudioChannels(text(json, VolumioBindingConstants.STATE_CHANNELS));
        setDuration(integer(json, VolumioBindingConstants.STATE_DURATION, 0));

        if (present(json, VolumioBindingConstants.STATE_STATUS)) {
            setState(json.optString(VolumioBindingConstants.STATE_STATUS, ""));
        }
        if (present(json, VolumioBindingConstants.STATE_VOLUME)) {
            setVolume(integer(json, VolumioBindingConstants.STATE_VOLUME, volume));
        }
        if (present(json, VolumioBindingConstants.STATE_POSITION)) {
            setPosition(integer(json, VolumioBindingConstants.STATE_POSITION, position));
        }
        if (present(json, VolumioBindingConstants.STATE_MUTE)) {
            setMute(json.optBoolean(VolumioBindingConstants.STATE_MUTE, mute));
        }
        if (present(json, VolumioBindingConstants.STATE_RANDOM)) {
            setRandom(json.optBoolean(VolumioBindingConstants.STATE_RANDOM, random));
        }
        if (present(json, VolumioBindingConstants.STATE_REPEAT)) {
            setRepeat(json.optBoolean(VolumioBindingConstants.STATE_REPEAT, repeat));
        }
        if (present(json, VolumioBindingConstants.STATE_REPEAT_SINGLE)) {
            setRepeatSingle(json.optBoolean(VolumioBindingConstants.STATE_REPEAT_SINGLE, repeatSingle));
        }
        if (present(json, VolumioBindingConstants.STATE_STREAM)) {
            setStream(json.optBoolean(VolumioBindingConstants.STATE_STREAM, stream));
        }
        if (present(json, VolumioBindingConstants.STATE_UPDATE_DB)) {
            setUpdateDb(json.optBoolean(VolumioBindingConstants.STATE_UPDATE_DB, updateDb));
        }
        initialized = true;
    }

    private static boolean present(JSONObject json, String key) {
        return json.has(key) && !json.isNull(key);
    }

    private static String text(JSONObject json, String key) {
        if (!present(json, key)) {
            return "";
        }
        String value = json.optString(key, "");
        return "null".equals(value) ? "" : value;
    }

    private static int integer(JSONObject json, String key, int defaultValue) {
        return present(json, key) ? json.optInt(key, defaultValue) : defaultValue;
    }

    /** @return true once at least one {@code pushState} payload has been processed */
    public synchronized boolean isInitialized() {
        return initialized;
    }

    /** Record the number of tracks in the play queue, as delivered by {@code pushQueue}. */
    public synchronized void setQueueSize(int size) {
        queueSizeDirty = !queueInitialized || size != this.queueSize;
        queueInitialized = true;
        this.queueSize = size;
    }

    public synchronized boolean isQueueInitialized() {
        return queueInitialized;
    }

    public synchronized DecimalType getQueueSize() {
        return new DecimalType(queueSize);
    }

    public synchronized boolean isQueueSizeDirty() {
        return queueSizeDirty;
    }

    public synchronized StringType getTitle() {
        return new StringType(title);
    }

    /** @return the plain title, empty if nothing is playing */
    public synchronized String getTitleText() {
        return title;
    }

    private void setTitle(String title) {
        titleDirty = !seen.add("title") || !title.equals(this.title);
        this.title = title;
    }

    public synchronized StringType getAlbum() {
        return new StringType(album);
    }

    private void setAlbum(String album) {
        albumDirty = !seen.add("album") || !album.equals(this.album);
        this.album = album;
    }

    public synchronized StringType getArtist() {
        return new StringType(artist);
    }

    private void setArtist(String artist) {
        artistDirty = !seen.add("artist") || !artist.equals(this.artist);
        this.artist = artist;
    }

    public synchronized PercentType getVolume() {
        return new PercentType(volume);
    }

    private void setVolume(int volume) {
        int clamped = Math.max(0, Math.min(100, volume));
        volumeDirty = !seen.add("volume") || clamped != this.volume;
        this.volume = clamped;
    }

    public synchronized OnOffType getMute() {
        return OnOffType.from(mute);
    }

    private void setMute(boolean mute) {
        muteDirty = !seen.add("mute") || mute != this.mute;
        this.mute = mute;
    }

    private void setState(String state) {
        stateDirty = !seen.add("state") || !state.equals(this.state);
        this.state = state;
    }

    public synchronized PlayPauseType getState() {
        return "play".equals(state) ? PlayPauseType.PLAY : PlayPauseType.PAUSE;
    }

    private void setTrackType(String trackType) {
        trackTypeDirty = !seen.add("trackType") || !trackType.equals(this.trackType);
        this.trackType = trackType;
    }

    public synchronized StringType getTrackType() {
        return new StringType(trackType);
    }

    private void setCoverArtUrl(String coverArtUrl) {
        coverArtDirty = !seen.add("coverArt") || !coverArtUrl.equals(this.coverArtUrl);
        this.coverArtUrl = coverArtUrl;
    }

    /**
     * @return the raw {@code albumart} value, which is either an absolute URL or a path relative to the Volumio
     *         host (for example {@code /albumart?web=...}); empty if there is none
     */
    public synchronized String getCoverArtUrl() {
        return coverArtUrl;
    }

    public synchronized OnOffType getRandom() {
        return OnOffType.from(random);
    }

    private void setRandom(boolean val) {
        randomDirty = !seen.add("random") || val != this.random;
        this.random = val;
    }

    public synchronized OnOffType getRepeat() {
        return OnOffType.from(repeat);
    }

    private void setRepeat(boolean val) {
        repeatDirty = !seen.add("repeat") || val != this.repeat;
        this.repeat = val;
    }

    public synchronized OnOffType getRepeatSingle() {
        return OnOffType.from(repeatSingle);
    }

    private void setRepeatSingle(boolean val) {
        repeatSingleDirty = !seen.add("repeatSingle") || val != this.repeatSingle;
        this.repeatSingle = val;
    }

    /** @return the track duration in seconds; 0 if the item has none (for example a web radio) */
    public synchronized QuantityType<Time> getDuration() {
        return new QuantityType<>(duration, Units.SECOND);
    }

    private void setDuration(int seconds) {
        int value = Math.max(0, seconds);
        durationDirty = !seen.add("duration") || value != this.duration;
        this.duration = value;
    }

    /** @return the position of the current track in the play queue, 0 for the first track */
    public synchronized DecimalType getPosition() {
        return new DecimalType(position);
    }

    private void setPosition(int position) {
        positionDirty = !seen.add("position") || position != this.position;
        this.position = position;
    }

    public synchronized StringType getUri() {
        return new StringType(uri);
    }

    /** @return the plain URI of the current item, empty if nothing is playing */
    public synchronized String getUriText() {
        return uri;
    }

    private void setUri(String uri) {
        uriDirty = !seen.add("uri") || !uri.equals(this.uri);
        this.uri = uri;
    }

    public synchronized StringType getService() {
        return new StringType(service);
    }

    /** @return the playback service of the current item (for example mpd), empty if unknown */
    public synchronized String getServiceText() {
        return service;
    }

    private void setService(String service) {
        serviceDirty = !seen.add("service") || !service.equals(this.service);
        this.service = service;
    }

    public synchronized StringType getSampleRate() {
        return new StringType(sampleRate);
    }

    private void setSampleRate(String sampleRate) {
        sampleRateDirty = !seen.add("sampleRate") || !sampleRate.equals(this.sampleRate);
        this.sampleRate = sampleRate;
    }

    public synchronized StringType getBitDepth() {
        return new StringType(bitDepth);
    }

    private void setBitDepth(String bitDepth) {
        bitDepthDirty = !seen.add("bitDepth") || !bitDepth.equals(this.bitDepth);
        this.bitDepth = bitDepth;
    }

    public synchronized StringType getAudioChannels() {
        return new StringType(audioChannels);
    }

    private void setAudioChannels(String audioChannels) {
        audioChannelsDirty = !seen.add("audioChannels") || !audioChannels.equals(this.audioChannels);
        this.audioChannels = audioChannels;
    }

    public synchronized OnOffType getStream() {
        return OnOffType.from(stream);
    }

    private void setStream(boolean val) {
        streamDirty = !seen.add("stream") || val != this.stream;
        this.stream = val;
    }

    public synchronized OnOffType getUpdateDb() {
        return OnOffType.from(updateDb);
    }

    private void setUpdateDb(boolean val) {
        updateDbDirty = !seen.add("updateDb") || val != this.updateDb;
        this.updateDb = val;
    }

    public synchronized boolean isStateDirty() {
        return stateDirty;
    }

    public synchronized boolean isTitleDirty() {
        return titleDirty;
    }

    public synchronized boolean isAlbumDirty() {
        return albumDirty;
    }

    public synchronized boolean isArtistDirty() {
        return artistDirty;
    }

    public synchronized boolean isVolumeDirty() {
        return volumeDirty;
    }

    public synchronized boolean isMuteDirty() {
        return muteDirty;
    }

    public synchronized boolean isTrackTypeDirty() {
        return trackTypeDirty;
    }

    public synchronized boolean isCoverArtDirty() {
        return coverArtDirty;
    }

    public synchronized boolean isRandomDirty() {
        return randomDirty;
    }

    public synchronized boolean isRepeatDirty() {
        return repeatDirty;
    }

    public synchronized boolean isRepeatSingleDirty() {
        return repeatSingleDirty;
    }

    public synchronized boolean isDurationDirty() {
        return durationDirty;
    }

    public synchronized boolean isPositionDirty() {
        return positionDirty;
    }

    public synchronized boolean isUriDirty() {
        return uriDirty;
    }

    public synchronized boolean isServiceDirty() {
        return serviceDirty;
    }

    public synchronized boolean isSampleRateDirty() {
        return sampleRateDirty;
    }

    public synchronized boolean isBitDepthDirty() {
        return bitDepthDirty;
    }

    public synchronized boolean isAudioChannelsDirty() {
        return audioChannelsDirty;
    }

    public synchronized boolean isStreamDirty() {
        return streamDirty;
    }

    public synchronized boolean isUpdateDbDirty() {
        return updateDbDirty;
    }
}
