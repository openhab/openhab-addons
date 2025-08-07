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
import java.util.Objects;
import java.util.UUID;

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
    @JsonProperty(JSON_PROPERTY_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BaseItemDto getItem() {
        return item;
    }

    @JsonProperty(JSON_PROPERTY_ITEM)
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
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
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
    @JsonProperty(JSON_PROPERTY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_ID)
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
    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMediaSourceId() {
        return mediaSourceId;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE_ID)
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
    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
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
    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
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
    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
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
    @JsonProperty(JSON_PROPERTY_FAILED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getFailed() {
        return failed;
    }

    @JsonProperty(JSON_PROPERTY_FAILED)
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
    @JsonProperty(JSON_PROPERTY_NEXT_MEDIA_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNextMediaType() {
        return nextMediaType;
    }

    @JsonProperty(JSON_PROPERTY_NEXT_MEDIA_TYPE)
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
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
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
    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
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
}
