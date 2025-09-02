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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * MediaPlayback
 *
 * @author Dan Cunningham - Initial contribution
 */
public class MediaPlaybackCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0506;
    public static final String CLUSTER_NAME = "MediaPlayback";
    public static final String CLUSTER_PREFIX = "mediaPlayback";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CURRENT_STATE = "currentState";
    public static final String ATTRIBUTE_START_TIME = "startTime";
    public static final String ATTRIBUTE_DURATION = "duration";
    public static final String ATTRIBUTE_SAMPLED_POSITION = "sampledPosition";
    public static final String ATTRIBUTE_PLAYBACK_SPEED = "playbackSpeed";
    public static final String ATTRIBUTE_SEEK_RANGE_END = "seekRangeEnd";
    public static final String ATTRIBUTE_SEEK_RANGE_START = "seekRangeStart";
    public static final String ATTRIBUTE_ACTIVE_AUDIO_TRACK = "activeAudioTrack";
    public static final String ATTRIBUTE_AVAILABLE_AUDIO_TRACKS = "availableAudioTracks";
    public static final String ATTRIBUTE_ACTIVE_TEXT_TRACK = "activeTextTrack";
    public static final String ATTRIBUTE_AVAILABLE_TEXT_TRACKS = "availableTextTracks";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current playback state of media.
     * During fast-forward, rewind, and other seek operations; this attribute shall be set to PLAYING.
     */
    public PlaybackStateEnum currentState; // 0 PlaybackStateEnum R V
    /**
     * Indicates the start time of the media, in case the media has a fixed start time (for example, live stream or
     * television broadcast), or null when start time does not apply to the current media (for example,
     * video-on-demand). This time is a UTC time. The client needs to handle conversion to local time, as required,
     * taking in account time zone and possible local DST offset.
     */
    public BigInteger startTime; // 1 epoch-us R V
    /**
     * Indicates the duration, in milliseconds, of the current media being played back or null when duration is not
     * applicable (for example, in live streaming content with no known duration). This attribute shall never be 0.
     */
    public BigInteger duration; // 2 uint64 R V
    /**
     * Indicates the position of playback (Position field) at the time (UpdateAt field) specified in the attribute. The
     * client may use the SampledPosition attribute to compute the current position within the media stream based on the
     * PlaybackSpeed, PlaybackPositionStruct.UpdatedAt and PlaybackPositionStruct.Position fields. To enable this, the
     * SampledPosition attribute shall be updated whenever a change in either the playback speed or the playback
     * position is triggered outside the normal playback of the media. The events which may cause this to happen
     * include:
     * • Starting or resumption of playback
     * • Seeking
     * • Skipping forward or backward
     * • Fast-forwarding or rewinding
     * • Updating of playback speed as a result of explicit request, or as a result of buffering events
     */
    public PlaybackPositionStruct sampledPosition; // 3 PlaybackPositionStruct R V
    /**
     * Indicates the speed at which the current media is being played. The new PlaybackSpeed shall be reflected in this
     * attribute whenever any of the following occurs:
     * • Starting of playback
     * • Resuming of playback
     * • Fast-forwarding
     * • Rewinding
     * The PlaybackSpeed shall reflect the ratio of time elapsed in the media to the actual time taken for the playback
     * assuming no changes to media playback (for example buffering events or requests to pause/rewind/forward).
     * • A value for PlaybackSpeed of 1 shall indicate normal playback where, for example, playback for 1 second causes
     * the media to advance by 1 second within the duration of the media.
     * • A value for PlaybackSpeed which is greater than 0 shall indicate that as playback is happening the media is
     * currently advancing in time within the duration of the media.
     * • A value for PlaybackSpeed which is less than 0 shall indicate that as playback is happening the media is
     * currently going back in time within the duration of the media.
     * • A value for PlaybackSpeed of 0 shall indicate that the media is currently not playing back. When the
     * CurrentState attribute has the value of PAUSED, NOT_PLAYING or BUFFERING, the PlaybackSpeed shall be set to 0 to
     * reflect that the media is not playing.
     * Following examples illustrate the PlaybackSpeed attribute values in various conditions.
     */
    public Float playbackSpeed; // 4 single R V
    /**
     * Indicates the furthest forward valid position to which a client may seek forward, in milliseconds from the start
     * of the media. When the media has an associated StartTime, a value of null shall indicate that a seek forward is
     * valid only until the current time within the media, using a position computed from the difference between the
     * current time offset and StartTime, in milliseconds from start of the media, truncating fractional milliseconds
     * towards 0. A value of Nas when StartTime is not specified shall indicate that seeking forward is not allowed.
     */
    public BigInteger seekRangeEnd; // 5 uint64 R V
    /**
     * Indicates the earliest valid position to which a client may seek back, in milliseconds from start of the media. A
     * value of Nas shall indicate that seeking backwards is not allowed.
     */
    public BigInteger seekRangeStart; // 6 uint64 R V
    /**
     * ActiveTrack refers to the Audio track currently set and being used for the streaming media. A value of null shall
     * indicate that no Audio Track corresponding to the current media is currently being played.
     */
    public TrackStruct activeAudioTrack; // 7 TrackStruct R V
    /**
     * AvailableAudioTracks refers to the list of Audio tracks available for the current title being played. A value of
     * null shall indicate that no Audio Tracks corresponding to the current media are selectable by the client.
     */
    public List<TrackStruct> availableAudioTracks; // 8 list R V
    /**
     * ActiveTrack refers to the Text track currently set and being used for the streaming media. This can be nil. A
     * value of null shall indicate that no Text Track corresponding to the current media is currently being displayed.
     */
    public TrackStruct activeTextTrack; // 9 TrackStruct R V
    /**
     * AvailableTextTracks refers to the list of Text tracks available for the current title being played. This can be
     * an empty list. A value of null shall indicate that no Text Tracks corresponding to the current media are
     * selectable by the client.
     */
    public List<TrackStruct> availableTextTracks; // 10 list R V

    // Structs
    /**
     * If supported, this event shall be generated when there is a change in any of the supported attributes of the
     * Media Playback cluster.
     */
    public static class StateChanged {
        /**
         * This field shall indicate the updated playback state as defined by the CurrentState attribute, and has the
         * same constraint as that attribute.
         */
        public PlaybackStateEnum currentState; // PlaybackStateEnum
        /**
         * This field shall indicate the updated start time as defined by the StartTime attribute, and has the same
         * constraint as that attribute.
         */
        public BigInteger startTime; // epoch-us
        /**
         * This field shall indicate the updated duration as defined by the Duration attribute, and has the same
         * constraint as that attribute.
         */
        public BigInteger duration; // uint64
        /**
         * This field shall indicate the updated position of playback as defined by the SampledPosition attribute, and
         * has the same constraint as that attribute.
         */
        public PlaybackPositionStruct sampledPosition; // PlaybackPositionStruct
        /**
         * This field shall indicate the updated speed at which the current media is being played as defined by the
         * PlaybackSpeed attribute, and has the same constraint as that attribute.
         */
        public Float playbackSpeed; // single
        /**
         * This field shall indicate the updated start of the seek range end as defined by the SeekRangeEnd attribute,
         * and has the same constraint as that attribute.
         */
        public BigInteger seekRangeEnd; // uint64
        /**
         * This field shall indicate the updated start of the seek range start as defined by the SeekRangeStart
         * attribute, and has the same constraint as that attribute.
         */
        public BigInteger seekRangeStart; // uint64
        /**
         * This field shall indicate Optional app-specific data.
         */
        public OctetString data; // octstr
        /**
         * This field shall indicate whether audio is unmuted by the player due to a FF or REW command. This field is
         * only meaningful when the PlaybackSpeed is present and not equal to 0 (paused) or 1 (normal playback).
         * Typically the value will be false (muted), however, some players will play audio during certain fast forward
         * and rewind speeds, and in these cases, the value will be true (not muted).
         * A value of true does not guarantee that audio can be heard by the user since the speaker may be muted, turned
         * down to a low level and/or unplugged.
         */
        public Boolean audioAdvanceUnmuted; // bool

        public StateChanged(PlaybackStateEnum currentState, BigInteger startTime, BigInteger duration,
                PlaybackPositionStruct sampledPosition, Float playbackSpeed, BigInteger seekRangeEnd,
                BigInteger seekRangeStart, OctetString data, Boolean audioAdvanceUnmuted) {
            this.currentState = currentState;
            this.startTime = startTime;
            this.duration = duration;
            this.sampledPosition = sampledPosition;
            this.playbackSpeed = playbackSpeed;
            this.seekRangeEnd = seekRangeEnd;
            this.seekRangeStart = seekRangeStart;
            this.data = data;
            this.audioAdvanceUnmuted = audioAdvanceUnmuted;
        }
    }

    /**
     * This structure defines a playback position within a media stream being played.
     */
    public static class PlaybackPositionStruct {
        /**
         * This field shall indicate the time when the position was last updated.
         */
        public BigInteger updatedAt; // epoch-us
        /**
         * This field shall indicate the associated discrete position within the media stream, in milliseconds from the
         * beginning of the stream, being associated with the time indicated by the UpdatedAt field. The Position shall
         * NOT be greater than the duration of the media if duration is specified. The Position shall NOT be greater
         * than the time difference between current time and start time of the media when start time is specified.
         * A value of null shall indicate that playback position is not applicable for the current state of the media
         * playback (For example : Live media with no known duration and where seek is not supported).
         */
        public BigInteger position; // uint64

        public PlaybackPositionStruct(BigInteger updatedAt, BigInteger position) {
            this.updatedAt = updatedAt;
            this.position = position;
        }
    }

    /**
     * This structure defines a uniquely identifiable Text Track or Audio Track.
     */
    public static class TrackStruct {
        /**
         * This field shall indicate the Identifier for the Track which is unique within the Track catalog. The Track
         * catalog contains all the Text/Audio tracks corresponding to the main media content.
         */
        public String id; // string
        /**
         * This field shall indicate the Attributes associated to the Track, like languageCode.
         */
        public TrackAttributesStruct trackAttributes; // TrackAttributesStruct

        public TrackStruct(String id, TrackAttributesStruct trackAttributes) {
            this.id = id;
            this.trackAttributes = trackAttributes;
        }
    }

    /**
     * This structure includes the attributes associated with a Text/Audio Track
     */
    public static class TrackAttributesStruct {
        /**
         * The value is a String containing one of the standard Tags for Identifying Languages RFC 5646, which
         * identifies the primary language used in the Track.
         */
        public String languageCode; // string
        /**
         * This is a list of enumerated CharacteristicEnum values that indicate a purpose, trait or feature associated
         * with the Track. A value of null shall indicate that there are no Characteristics corresponding to the Track.
         */
        public List<CharacteristicEnum> characteristics; // list
        /**
         * The value is a String containing a user displayable name for the Track. A value of null shall indicate that
         * there is no DisplayName corresponding to the Track.
         */
        public String displayName; // string

        public TrackAttributesStruct(String languageCode, List<CharacteristicEnum> characteristics,
                String displayName) {
            this.languageCode = languageCode;
            this.characteristics = characteristics;
            this.displayName = displayName;
        }
    }

    // Enums
    public enum PlaybackStateEnum implements MatterEnum {
        PLAYING(0, "Playing"),
        PAUSED(1, "Paused"),
        NOT_PLAYING(2, "Not Playing"),
        BUFFERING(3, "Buffering");

        public final Integer value;
        public final String label;

        private PlaybackStateEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        INVALID_STATE_FOR_COMMAND(1, "Invalid State For Command"),
        NOT_ALLOWED(2, "Not Allowed"),
        NOT_ACTIVE(3, "Not Active"),
        SPEED_OUT_OF_RANGE(4, "Speed Out Of Range"),
        SEEK_OUT_OF_RANGE(5, "Seek Out Of Range");

        public final Integer value;
        public final String label;

        private StatusEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum CharacteristicEnum implements MatterEnum {
        FORCED_SUBTITLES(0, "Forced Subtitles"),
        DESCRIBES_VIDEO(1, "Describes Video"),
        EASY_TO_READ(2, "Easy To Read"),
        FRAME_BASED(3, "Frame Based"),
        MAIN_PROGRAM(4, "Main Program"),
        ORIGINAL_CONTENT(5, "Original Content"),
        VOICE_OVER_TRANSLATION(6, "Voice Over Translation"),
        CAPTION(7, "Caption"),
        SUBTITLE(8, "Subtitle"),
        ALTERNATE(9, "Alternate"),
        SUPPLEMENTARY(10, "Supplementary"),
        COMMENTARY(11, "Commentary"),
        DUBBED_TRANSLATION(12, "Dubbed Translation"),
        DESCRIPTION(13, "Description"),
        METADATA(14, "Metadata"),
        ENHANCED_AUDIO_INTELLIGIBILITY(15, "Enhanced Audio Intelligibility"),
        EMERGENCY(16, "Emergency"),
        KARAOKE(17, "Karaoke");

        public final Integer value;
        public final String label;

        private CharacteristicEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This feature provides access to the time offset location within current playback media and allows for jumping
         * to a specific location using time offsets. This enables clients to implement more advanced media seeking
         * behavior in their user interface, for instance a &quot;seek bar&quot;.
         */
        public boolean advancedSeek;
        /**
         * 
         * This feature is for a device which supports variable speed playback on media that supports it.
         */
        public boolean variableSpeed;
        /**
         * 
         * This feature is for a device or app that supports Text Tracks.
         */
        public boolean textTracks;
        /**
         * 
         * This feature is for a device or app that supports Audio Tracks.
         */
        public boolean audioTracks;
        /**
         * 
         * This feature is for a device or app that supports playing audio during fast and slow advance and rewind
         * (e.g., while playback speed is not 1). A device that supports this feature may only support playing audio
         * during certain speeds.
         * A cluster implementing AA shall implement AS.
         */
        public boolean audioAdvance;

        public FeatureMap(boolean advancedSeek, boolean variableSpeed, boolean textTracks, boolean audioTracks,
                boolean audioAdvance) {
            this.advancedSeek = advancedSeek;
            this.variableSpeed = variableSpeed;
            this.textTracks = textTracks;
            this.audioTracks = audioTracks;
            this.audioAdvance = audioAdvance;
        }
    }

    public MediaPlaybackCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1286, "MediaPlayback");
    }

    protected MediaPlaybackCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall play media. If content is currently in a FastForward or Rewind state. Play shall return
     * media to normal playback speed.
     */
    public static ClusterCommand play() {
        return new ClusterCommand("play");
    }

    /**
     * Upon receipt, this shall pause playback of the media.
     */
    public static ClusterCommand pause() {
        return new ClusterCommand("pause");
    }

    /**
     * Upon receipt, this shall stop playback of the media. User-visible outcome is context-specific. This may navigate
     * the user back to the location from where the media was originally launched.
     */
    public static ClusterCommand stop() {
        return new ClusterCommand("stop");
    }

    /**
     * Upon receipt, this shall Start Over with the current media playback item.
     */
    public static ClusterCommand startOver() {
        return new ClusterCommand("startOver");
    }

    /**
     * Upon receipt, this shall cause the handler to be invoked for &quot;Previous&quot;. User experience is
     * context-specific. This will often Go back to the previous media playback item.
     */
    public static ClusterCommand previous() {
        return new ClusterCommand("previous");
    }

    /**
     * Upon receipt, this shall cause the handler to be invoked for &quot;Next&quot;. User experience is
     * context-specific. This will often Go forward to the next media playback item.
     */
    public static ClusterCommand next() {
        return new ClusterCommand("next");
    }

    /**
     * Upon receipt, this shall start playback of the media backward in case the media is currently playing in the
     * forward direction or is not playing. If the playback is already happening in the backwards direction receipt of
     * this command shall increase the speed of the media playback backwards.
     * Different &quot;rewind&quot; speeds may be reflected on the media playback device based upon the number of
     * sequential calls to this function and the capability of the device. This is to avoid needing to define every
     * speed (multiple fast, slow motion, etc). If the PlaybackSpeed attribute is supported it shall be updated to
     * reflect the new speed of playback. If the playback speed cannot be changed for the media being played(for
     * example, in live streaming content not supporting seek), the status of NOT_ALLOWED shall be returned. If the
     * playback speed has reached the maximum supported speed for media playing backwards, the status of
     * SPEED_OUT_OF_RANGE shall be returned.
     */
    public static ClusterCommand rewind(Boolean audioAdvanceUnmuted) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (audioAdvanceUnmuted != null) {
            map.put("audioAdvanceUnmuted", audioAdvanceUnmuted);
        }
        return new ClusterCommand("rewind", map);
    }

    /**
     * Upon receipt, this shall start playback of the media in the forward direction in case the media is currently
     * playing in the backward direction or is not playing. If the playback is already happening in the forward
     * direction receipt of this command shall increase the speed of the media playback.
     * Different &quot;fast-forward&quot; speeds may be reflected on the media playback device based upon the number of
     * sequential calls to this function and the capability of the device. This is to avoid needing to define every
     * speed (multiple fast, slow motion, etc). If the PlaybackSpeed attribute is supported it shall be updated to
     * reflect the new speed of playback. If the playback speed cannot be changed for the media being played(for
     * example, in live streaming content not supporting seek), the status of NOT_ALLOWED shall be returned. If the
     * playback speed has reached the maximum supported speed for media playing forward, the status of
     * SPEED_OUT_OF_RANGE shall be returned.
     */
    public static ClusterCommand fastForward(Boolean audioAdvanceUnmuted) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (audioAdvanceUnmuted != null) {
            map.put("audioAdvanceUnmuted", audioAdvanceUnmuted);
        }
        return new ClusterCommand("fastForward", map);
    }

    /**
     * Upon receipt, this shall Skip forward in the media by the given number of milliseconds.
     */
    public static ClusterCommand skipForward(BigInteger deltaPositionMilliseconds) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (deltaPositionMilliseconds != null) {
            map.put("deltaPositionMilliseconds", deltaPositionMilliseconds);
        }
        return new ClusterCommand("skipForward", map);
    }

    /**
     * Upon receipt, this shall Skip backward in the media by the given number of milliseconds.
     */
    public static ClusterCommand skipBackward(BigInteger deltaPositionMilliseconds) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (deltaPositionMilliseconds != null) {
            map.put("deltaPositionMilliseconds", deltaPositionMilliseconds);
        }
        return new ClusterCommand("skipBackward", map);
    }

    /**
     * Upon receipt, this shall change the playback position in the media to the given position.
     */
    public static ClusterCommand seek(BigInteger position) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (position != null) {
            map.put("position", position);
        }
        return new ClusterCommand("seek", map);
    }

    /**
     * Upon receipt, the server shall set the active Audio Track to the one identified by the TrackID in the Track
     * catalog for the streaming media. If the TrackID does not exist in the Track catalog, OR does not correspond to
     * the streaming media OR no media is being streamed at the time of receipt of this command, the server will return
     * an error status of INVALID_ARGUMENT.
     */
    public static ClusterCommand activateAudioTrack(String trackId, Integer audioOutputIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (trackId != null) {
            map.put("trackId", trackId);
        }
        if (audioOutputIndex != null) {
            map.put("audioOutputIndex", audioOutputIndex);
        }
        return new ClusterCommand("activateAudioTrack", map);
    }

    /**
     * Upon receipt, the server shall set the active Text Track to the one identified by the TrackID in the Track
     * catalog for the streaming media. If the TrackID does not exist in the Track catalog, OR does not correspond to
     * the streaming media OR no media is being streamed at the time of receipt of this command, the server shall return
     * an error status of INVALID_ARGUMENT.
     */
    public static ClusterCommand activateTextTrack(String trackId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (trackId != null) {
            map.put("trackId", trackId);
        }
        return new ClusterCommand("activateTextTrack", map);
    }

    /**
     * If a Text Track is active (i.e. being displayed), upon receipt of this command, the server shall stop displaying
     * it.
     */
    public static ClusterCommand deactivateTextTrack() {
        return new ClusterCommand("deactivateTextTrack");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "currentState : " + currentState + "\n";
        str += "startTime : " + startTime + "\n";
        str += "duration : " + duration + "\n";
        str += "sampledPosition : " + sampledPosition + "\n";
        str += "playbackSpeed : " + playbackSpeed + "\n";
        str += "seekRangeEnd : " + seekRangeEnd + "\n";
        str += "seekRangeStart : " + seekRangeStart + "\n";
        str += "activeAudioTrack : " + activeAudioTrack + "\n";
        str += "availableAudioTracks : " + availableAudioTracks + "\n";
        str += "activeTextTrack : " + activeTextTrack + "\n";
        str += "availableTextTracks : " + availableTextTracks + "\n";
        return str;
    }
}
