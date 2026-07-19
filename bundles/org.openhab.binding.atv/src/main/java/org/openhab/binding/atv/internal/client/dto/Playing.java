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
package org.openhab.binding.atv.internal.client.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable snapshot of what is currently playing on a device.
 *
 * <p>
 * Instances are created via {@link #builder()}. Equality is based on all content fields (including the derived
 * {@link #hash()}), which is used to deduplicate push updates.
 *
 * @author Dan Cunningham - Initial contribution
 */
public final class Playing {

    private final MediaType mediaType;
    private final DeviceState deviceState;
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final Integer totalTime;
    private final Integer position;
    private final ShuffleState shuffle;
    private final RepeatState repeat;
    private final String hash;
    private final String seriesName;
    private final Integer seasonNumber;
    private final Integer episodeNumber;
    private final String contentIdentifier;
    private final Long iTunesStoreIdentifier;

    private Playing(Builder builder) {
        this.mediaType = builder.mediaType;
        this.deviceState = builder.deviceState;
        this.title = builder.title;
        this.artist = builder.artist;
        this.album = builder.album;
        this.genre = builder.genre;
        this.totalTime = builder.totalTime;
        this.shuffle = builder.shuffle;
        this.repeat = builder.repeat;
        this.hash = builder.hash;
        this.seriesName = builder.seriesName;
        this.seasonNumber = builder.seasonNumber;
        this.episodeNumber = builder.episodeNumber;
        this.contentIdentifier = builder.contentIdentifier;
        this.iTunesStoreIdentifier = builder.iTunesStoreIdentifier;
        // Position is clamped to [0, totalTime], but only when it is set and non-zero.
        Integer pos = builder.position;
        if (pos != null && pos != 0) {
            pos = Math.max(pos, 0);
            if (totalTime != null && totalTime != 0) {
                pos = Math.min(pos, totalTime);
            }
        }
        this.position = pos;
    }

    /**
     * Returns a new builder for creating {@link Playing} instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a unique hash for what is currently playing.
     *
     * <p>
     * The hash is based on title, artist, album and total time (SHA-256, where absent fields render as the string
     * {@code "None"}). It should always be the same for the same content, but that is not guaranteed. An explicit
     * hash set via the builder takes precedence.
     *
     * @return content hash as lower case hex string
     */
    public String hash() {
        String localHash = hash;
        if (localHash != null && !localHash.isEmpty()) {
            return localHash;
        }
        String base = pyStr(title) + pyStr(artist) + pyStr(album) + pyStr(totalTime);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(base.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on all JVMs
            throw new IllegalStateException(e);
        }
    }

    private static String pyStr(Object value) {
        return value == null ? "None" : value.toString();
    }

    /**
     * Returns the type of media currently playing, e.g. video or music.
     *
     * @return media type (never {@code null})
     */
    public MediaType mediaType() {
        return mediaType;
    }

    /**
     * Returns the device state, e.g. playing or paused.
     *
     * @return device state (never {@code null})
     */
    public DeviceState deviceState() {
        return deviceState;
    }

    /**
     * Returns the title of the current media, e.g. movie or song name.
     *
     * @return title if available
     */
    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    /**
     * Returns the artist of the currently playing song.
     *
     * @return artist if available
     */
    public Optional<String> artist() {
        return Optional.ofNullable(artist);
    }

    /**
     * Returns the album of the currently playing song.
     *
     * @return album if available
     */
    public Optional<String> album() {
        return Optional.ofNullable(album);
    }

    /**
     * Returns the genre of the currently playing song.
     *
     * @return genre if available
     */
    public Optional<String> genre() {
        return Optional.ofNullable(genre);
    }

    /**
     * Returns the total play time in seconds.
     *
     * @return total time if available
     */
    public Optional<Integer> totalTime() {
        return Optional.ofNullable(totalTime);
    }

    /**
     * Returns the position in the playing media in seconds.
     *
     * @return position if available
     */
    public Optional<Integer> position() {
        return Optional.ofNullable(position);
    }

    /**
     * Returns whether shuffle is enabled or not.
     *
     * @return shuffle state if available
     */
    public Optional<ShuffleState> shuffle() {
        return Optional.ofNullable(shuffle);
    }

    /**
     * Returns the repeat mode.
     *
     * @return repeat state if available
     */
    public Optional<RepeatState> repeat() {
        return Optional.ofNullable(repeat);
    }

    /**
     * Returns the title of the TV series.
     *
     * @return series name if available
     */
    public Optional<String> seriesName() {
        return Optional.ofNullable(seriesName);
    }

    /**
     * Returns the season number of the TV series.
     *
     * @return season number if available
     */
    public Optional<Integer> seasonNumber() {
        return Optional.ofNullable(seasonNumber);
    }

    /**
     * Returns the episode number of the TV series.
     *
     * @return episode number if available
     */
    public Optional<Integer> episodeNumber() {
        return Optional.ofNullable(episodeNumber);
    }

    /**
     * Returns the content identifier (app specific).
     *
     * @return content identifier if available
     */
    public Optional<String> contentIdentifier() {
        return Optional.ofNullable(contentIdentifier);
    }

    /**
     * Returns the iTunes Store identifier.
     *
     * @return iTunes Store identifier if available
     */
    public Optional<Long> iTunesStoreIdentifier() {
        return Optional.ofNullable(iTunesStoreIdentifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Playing other)) {
            return false;
        }
        return mediaType == other.mediaType && deviceState == other.deviceState && Objects.equals(title, other.title)
                && Objects.equals(artist, other.artist) && Objects.equals(album, other.album)
                && Objects.equals(genre, other.genre) && Objects.equals(totalTime, other.totalTime)
                && Objects.equals(position, other.position) && shuffle == other.shuffle && repeat == other.repeat
                && Objects.equals(hash(), other.hash()) && Objects.equals(seriesName, other.seriesName)
                && Objects.equals(seasonNumber, other.seasonNumber)
                && Objects.equals(episodeNumber, other.episodeNumber)
                && Objects.equals(contentIdentifier, other.contentIdentifier)
                && Objects.equals(iTunesStoreIdentifier, other.iTunesStoreIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaType, deviceState, title, artist, album, genre, totalTime, position, shuffle, repeat,
                hash(), seriesName, seasonNumber, episodeNumber, contentIdentifier, iTunesStoreIdentifier);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("Media type: ").append(mediaType).append(", Device state: ").append(deviceState);
        appendIfPresent(output, "Title", title);
        appendIfPresent(output, "Artist", artist);
        appendIfPresent(output, "Album", album);
        appendIfPresent(output, "Genre", genre);
        appendIfPresent(output, "Series Name", seriesName);
        appendIfPresent(output, "Season", seasonNumber);
        appendIfPresent(output, "Episode", episodeNumber);
        appendIfPresent(output, "Identifier", contentIdentifier);
        appendIfPresent(output, "Position", position);
        appendIfPresent(output, "Total time", totalTime);
        appendIfPresent(output, "Repeat", repeat);
        appendIfPresent(output, "Shuffle", shuffle);
        appendIfPresent(output, "iTunes Store Identifier", iTunesStoreIdentifier);
        return output.toString();
    }

    private static void appendIfPresent(StringBuilder output, String label, Object value) {
        if (value != null) {
            output.append(", ").append(label).append(": ").append(value);
        }
    }

    /**
     * Builder for {@link Playing} instances.
     */
    public static final class Builder {

        private MediaType mediaType = MediaType.Unknown;
        private DeviceState deviceState = DeviceState.Idle;
        private String title;
        private String artist;
        private String album;
        private String genre;
        private Integer totalTime;
        private Integer position;
        private ShuffleState shuffle;
        private RepeatState repeat;
        private String hash;
        private String seriesName;
        private Integer seasonNumber;
        private Integer episodeNumber;
        private String contentIdentifier;
        private Long iTunesStoreIdentifier;

        private Builder() {
        }

        /**
         * Sets the media type (defaults to {@link MediaType#Unknown}).
         *
         * @param mediaType media type
         * @return this builder
         */
        public Builder mediaType(MediaType mediaType) {
            this.mediaType = Objects.requireNonNull(mediaType);
            return this;
        }

        /**
         * Sets the device state (defaults to {@link DeviceState#Idle}).
         *
         * @param deviceState device state
         * @return this builder
         */
        public Builder deviceState(DeviceState deviceState) {
            this.deviceState = Objects.requireNonNull(deviceState);
            return this;
        }

        /**
         * Sets the media title.
         *
         * @param title title or {@code null}
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the artist.
         *
         * @param artist artist or {@code null}
         * @return this builder
         */
        public Builder artist(String artist) {
            this.artist = artist;
            return this;
        }

        /**
         * Sets the album.
         *
         * @param album album or {@code null}
         * @return this builder
         */
        public Builder album(String album) {
            this.album = album;
            return this;
        }

        /**
         * Sets the genre.
         *
         * @param genre genre or {@code null}
         * @return this builder
         */
        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        /**
         * Sets the total play time in seconds.
         *
         * @param totalTime total time or {@code null}
         * @return this builder
         */
        public Builder totalTime(Integer totalTime) {
            this.totalTime = totalTime;
            return this;
        }

        /**
         * Sets the position in seconds.
         *
         * @param position position or {@code null}
         * @return this builder
         */
        public Builder position(Integer position) {
            this.position = position;
            return this;
        }

        /**
         * Sets the shuffle state.
         *
         * @param shuffle shuffle state or {@code null}
         * @return this builder
         */
        public Builder shuffle(ShuffleState shuffle) {
            this.shuffle = shuffle;
            return this;
        }

        /**
         * Sets the repeat state.
         *
         * @param repeat repeat state or {@code null}
         * @return this builder
         */
        public Builder repeat(RepeatState repeat) {
            this.repeat = repeat;
            return this;
        }

        /**
         * Sets an explicit content hash, overriding the derived hash.
         *
         * @param hash explicit hash or {@code null} to derive it
         * @return this builder
         */
        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        /**
         * Sets the TV series name.
         *
         * @param seriesName series name or {@code null}
         * @return this builder
         */
        public Builder seriesName(String seriesName) {
            this.seriesName = seriesName;
            return this;
        }

        /**
         * Sets the TV series season number.
         *
         * @param seasonNumber season number or {@code null}
         * @return this builder
         */
        public Builder seasonNumber(Integer seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        /**
         * Sets the TV series episode number.
         *
         * @param episodeNumber episode number or {@code null}
         * @return this builder
         */
        public Builder episodeNumber(Integer episodeNumber) {
            this.episodeNumber = episodeNumber;
            return this;
        }

        /**
         * Sets the app specific content identifier.
         *
         * @param contentIdentifier content identifier or {@code null}
         * @return this builder
         */
        public Builder contentIdentifier(String contentIdentifier) {
            this.contentIdentifier = contentIdentifier;
            return this;
        }

        /**
         * Sets the iTunes Store identifier.
         *
         * @param iTunesStoreIdentifier identifier or {@code null}
         * @return this builder
         */
        public Builder iTunesStoreIdentifier(Long iTunesStoreIdentifier) {
            this.iTunesStoreIdentifier = iTunesStoreIdentifier;
            return this;
        }

        /**
         * Builds the immutable {@link Playing} instance.
         *
         * @return new instance
         */
        public Playing build() {
            return new Playing(this);
        }
    }
}
