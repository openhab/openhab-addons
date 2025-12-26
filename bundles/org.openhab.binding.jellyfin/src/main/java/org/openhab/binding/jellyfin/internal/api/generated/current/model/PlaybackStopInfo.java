/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlaybackStopInfo.
 */
@JsonPropertyOrder({ PlaybackStopInfo.JSON_PROPERTY_ITEM, PlaybackStopInfo.JSON_PROPERTY_ITEM_ID,
        PlaybackStopInfo.JSON_PROPERTY_SESSION_ID, PlaybackStopInfo.JSON_PROPERTY_MEDIA_SOURCE_ID,
        PlaybackStopInfo.JSON_PROPERTY_POSITION_TICKS, PlaybackStopInfo.JSON_PROPERTY_LIVE_STREAM_ID,
        PlaybackStopInfo.JSON_PROPERTY_PLAY_SESSION_ID, PlaybackStopInfo.JSON_PROPERTY_FAILED,
        PlaybackStopInfo.JSON_PROPERTY_NEXT_MEDIA_TYPE, PlaybackStopInfo.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        PlaybackStopInfo.JSON_PROPERTY_NOW_PLAYING_QUEUE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaybackStopInfo {
    public static final String JSON_PROPERTY_ITEM = "Item";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto item;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_SESSION_ID = "SessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String sessionId;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.NonNull
    private String mediaSourceId;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long positionTicks;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.NonNull
    private String liveStreamId;

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String playSessionId;

    public static final String JSON_PROPERTY_FAILED = "Failed";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean failed;

    public static final String JSON_PROPERTY_NEXT_MEDIA_TYPE = "NextMediaType";
    @org.eclipse.jdt.annotation.NonNull
    private String nextMediaType;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String playlistItemId;

    public static final String JSON_PROPERTY_NOW_PLAYING_QUEUE = "NowPlayingQueue";
    @org.eclipse.jdt.annotation.NonNull
    private List<QueueItem> nowPlayingQueue;

    public PlaybackStopInfo() {
    }

    public PlaybackStopInfo item(@org.eclipse.jdt.annotation.NonNull BaseItemDto item) {
        this.item = item;
        return this;
    }

    /**
     * Gets or sets the item.
     * 
     * @return item
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getItem() {
        return item;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto item) {
        this.item = item;
    }

    public PlaybackStopInfo itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public PlaybackStopInfo sessionId(@org.eclipse.jdt.annotation.NonNull String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Gets or sets the session id.
     * 
     * @return sessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionId(@org.eclipse.jdt.annotation.NonNull String sessionId) {
        this.sessionId = sessionId;
    }

    public PlaybackStopInfo mediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the media version identifier.
     * 
     * @return mediaSourceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMediaSourceId() {
        return mediaSourceId;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public PlaybackStopInfo positionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public PlaybackStopInfo liveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the live stream identifier.
     * 
     * @return liveStreamId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLiveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
    }

    public PlaybackStopInfo playSessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session identifier.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public PlaybackStopInfo failed(@org.eclipse.jdt.annotation.NonNull Boolean failed) {
        this.failed = failed;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Session.PlaybackStopInfo is failed.
     * 
     * @return failed
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FAILED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getFailed() {
        return failed;
    }

    @JsonProperty(value = JSON_PROPERTY_FAILED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFailed(@org.eclipse.jdt.annotation.NonNull Boolean failed) {
        this.failed = failed;
    }

    public PlaybackStopInfo nextMediaType(@org.eclipse.jdt.annotation.NonNull String nextMediaType) {
        this.nextMediaType = nextMediaType;
        return this;
    }

    /**
     * Get nextMediaType
     * 
     * @return nextMediaType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NEXT_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNextMediaType() {
        return nextMediaType;
    }

    @JsonProperty(value = JSON_PROPERTY_NEXT_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNextMediaType(@org.eclipse.jdt.annotation.NonNull String nextMediaType) {
        this.nextMediaType = nextMediaType;
    }

    public PlaybackStopInfo playlistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Get playlistItemId
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public PlaybackStopInfo nowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        return this;
    }

    public PlaybackStopInfo addNowPlayingQueueItem(QueueItem nowPlayingQueueItem) {
        if (this.nowPlayingQueue == null) {
            this.nowPlayingQueue = new ArrayList<>();
        }
        this.nowPlayingQueue.add(nowPlayingQueueItem);
        return this;
    }

    /**
     * Get nowPlayingQueue
     * 
     * @return nowPlayingQueue
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
    }

    /**
     * Return true if this PlaybackStopInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaybackStopInfo playbackStopInfo = (PlaybackStopInfo) o;
        return Objects.equals(this.item, playbackStopInfo.item) && Objects.equals(this.itemId, playbackStopInfo.itemId)
                && Objects.equals(this.sessionId, playbackStopInfo.sessionId)
                && Objects.equals(this.mediaSourceId, playbackStopInfo.mediaSourceId)
                && Objects.equals(this.positionTicks, playbackStopInfo.positionTicks)
                && Objects.equals(this.liveStreamId, playbackStopInfo.liveStreamId)
                && Objects.equals(this.playSessionId, playbackStopInfo.playSessionId)
                && Objects.equals(this.failed, playbackStopInfo.failed)
                && Objects.equals(this.nextMediaType, playbackStopInfo.nextMediaType)
                && Objects.equals(this.playlistItemId, playbackStopInfo.playlistItemId)
                && Objects.equals(this.nowPlayingQueue, playbackStopInfo.nowPlayingQueue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, itemId, sessionId, mediaSourceId, positionTicks, liveStreamId, playSessionId, failed,
                nextMediaType, playlistItemId, nowPlayingQueue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaybackStopInfo {\n");
        sb.append("    item: ").append(toIndentedString(item)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
        sb.append("    mediaSourceId: ").append(toIndentedString(mediaSourceId)).append("\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
        sb.append("    liveStreamId: ").append(toIndentedString(liveStreamId)).append("\n");
        sb.append("    playSessionId: ").append(toIndentedString(playSessionId)).append("\n");
        sb.append("    failed: ").append(toIndentedString(failed)).append("\n");
        sb.append("    nextMediaType: ").append(toIndentedString(nextMediaType)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
        sb.append("    nowPlayingQueue: ").append(toIndentedString(nowPlayingQueue)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Item` to the URL query string
        if (getItem() != null) {
            joiner.add(getItem().toUrlQueryString(prefix + "Item" + suffix));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `SessionId` to the URL query string
        if (getSessionId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSessionId()))));
        }

        // add `MediaSourceId` to the URL query string
        if (getMediaSourceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaSourceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceId()))));
        }

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        // add `LiveStreamId` to the URL query string
        if (getLiveStreamId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLiveStreamId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLiveStreamId()))));
        }

        // add `PlaySessionId` to the URL query string
        if (getPlaySessionId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaySessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaySessionId()))));
        }

        // add `Failed` to the URL query string
        if (getFailed() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFailed%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFailed()))));
        }

        // add `NextMediaType` to the URL query string
        if (getNextMediaType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sNextMediaType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNextMediaType()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        // add `NowPlayingQueue` to the URL query string
        if (getNowPlayingQueue() != null) {
            for (int i = 0; i < getNowPlayingQueue().size(); i++) {
                if (getNowPlayingQueue().get(i) != null) {
                    joiner.add(getNowPlayingQueue().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sNowPlayingQueue%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaybackStopInfo instance;

        public Builder() {
            this(new PlaybackStopInfo());
        }

        protected Builder(PlaybackStopInfo instance) {
            this.instance = instance;
        }

        public PlaybackStopInfo.Builder item(BaseItemDto item) {
            this.instance.item = item;
            return this;
        }

        public PlaybackStopInfo.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public PlaybackStopInfo.Builder sessionId(String sessionId) {
            this.instance.sessionId = sessionId;
            return this;
        }

        public PlaybackStopInfo.Builder mediaSourceId(String mediaSourceId) {
            this.instance.mediaSourceId = mediaSourceId;
            return this;
        }

        public PlaybackStopInfo.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        public PlaybackStopInfo.Builder liveStreamId(String liveStreamId) {
            this.instance.liveStreamId = liveStreamId;
            return this;
        }

        public PlaybackStopInfo.Builder playSessionId(String playSessionId) {
            this.instance.playSessionId = playSessionId;
            return this;
        }

        public PlaybackStopInfo.Builder failed(Boolean failed) {
            this.instance.failed = failed;
            return this;
        }

        public PlaybackStopInfo.Builder nextMediaType(String nextMediaType) {
            this.instance.nextMediaType = nextMediaType;
            return this;
        }

        public PlaybackStopInfo.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public PlaybackStopInfo.Builder nowPlayingQueue(List<QueueItem> nowPlayingQueue) {
            this.instance.nowPlayingQueue = nowPlayingQueue;
            return this;
        }

        /**
         * returns a built PlaybackStopInfo instance.
         *
         * The builder is not reusable.
         */
        public PlaybackStopInfo build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static PlaybackStopInfo.Builder builder() {
        return new PlaybackStopInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaybackStopInfo.Builder toBuilder() {
        return new PlaybackStopInfo.Builder().item(getItem()).itemId(getItemId()).sessionId(getSessionId())
                .mediaSourceId(getMediaSourceId()).positionTicks(getPositionTicks()).liveStreamId(getLiveStreamId())
                .playSessionId(getPlaySessionId()).failed(getFailed()).nextMediaType(getNextMediaType())
                .playlistItemId(getPlaylistItemId()).nowPlayingQueue(getNowPlayingQueue());
    }
}
