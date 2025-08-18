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
 * ContentLauncher
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ContentLauncherCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x050A;
    public static final String CLUSTER_NAME = "ContentLauncher";
    public static final String CLUSTER_PREFIX = "contentLauncher";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_ACCEPT_HEADER = "acceptHeader";
    public static final String ATTRIBUTE_SUPPORTED_STREAMING_PROTOCOLS = "supportedStreamingProtocols";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall provide a list of content types supported by the Video Player or Content App in the form of
     * entries in the HTTP &quot;Accept&quot; request header.
     */
    public List<String> acceptHeader; // 0 list R V
    /**
     * This attribute shall provide information about supported streaming protocols.
     */
    public SupportedProtocolsBitmap supportedStreamingProtocols; // 1 SupportedProtocolsBitmap R V

    // Structs
    /**
     * This object defines additional name&#x3D;value pairs that can be used for identifying content.
     */
    public static class AdditionalInfoStruct {
        /**
         * This field shall indicate the name of external id, ex. &quot;musicbrainz&quot;.
         */
        public String name; // string
        /**
         * This field shall indicate the value for external id, ex. &quot;ST0000000666661&quot;.
         */
        public String value; // string

        public AdditionalInfoStruct(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * This object defines inputs to a search for content for display or playback.
     */
    public static class ParameterStruct {
        /**
         * This field shall indicate the entity type.
         */
        public ParameterEnum type; // ParameterEnum
        /**
         * This field shall indicate the entity value, which is a search string, ex. “Manchester by the Sea”.
         */
        public String value; // string
        /**
         * This field shall indicate the list of additional external content identifiers.
         */
        public List<AdditionalInfoStruct> externalIdList; // list

        public ParameterStruct(ParameterEnum type, String value, List<AdditionalInfoStruct> externalIdList) {
            this.type = type;
            this.value = value;
            this.externalIdList = externalIdList;
        }
    }

    /**
     * This object defines inputs to a search for content for display or playback.
     */
    public static class ContentSearchStruct {
        /**
         * This field shall indicate the list of parameters comprising the search. If multiple parameters are provided,
         * the search parameters shall be joined with &#x27;AND&#x27; logic. e.g. action movies with Tom Cruise will be
         * represented as [{Actor: &#x27;Tom Cruise&#x27;}, {Type: &#x27;Movie&#x27;}, {Genre: &#x27;Action&#x27;}]
         */
        public List<ParameterStruct> parameterList; // list

        public ContentSearchStruct(List<ParameterStruct> parameterList) {
            this.parameterList = parameterList;
        }
    }

    /**
     * This object defines dimension which can be used for defining Size of background images.
     */
    public static class DimensionStruct {
        /**
         * This field shall indicate the width using the metric defined in Metric
         */
        public Double width; // double
        /**
         * This field shall indicate the height using the metric defined in Metric
         */
        public Double height; // double
        /**
         * This field shall indicate metric used for defining Height/Width.
         */
        public MetricTypeEnum metric; // MetricTypeEnum

        public DimensionStruct(Double width, Double height, MetricTypeEnum metric) {
            this.width = width;
            this.height = height;
            this.metric = metric;
        }
    }

    /**
     * This object defines style information which can be used by content providers to change the Media Player’s style
     * related properties.
     */
    public static class StyleInformationStruct {
        /**
         * This field shall indicate the URL of image used for Styling different Video Player sections like Logo,
         * Watermark etc. The syntax of this field shall follow the syntax as specified in RFC 1738 and shall use the
         * https scheme.
         */
        public String imageUrl; // string
        /**
         * This field shall indicate the color, in RGB or RGBA, used for styling different Video Player sections like
         * Logo, Watermark, etc. The value shall conform to the 6-digit or 8-digit format defined for CSS sRGB
         * hexadecimal color notation. Examples:
         * • #76DE19 for R&#x3D;0x76, G&#x3D;0xDE, B&#x3D;0x19, A absent
         * • #76DE1980 for R&#x3D;0x76, G&#x3D;0xDE, B&#x3D;0x19, A&#x3D;0x80
         */
        public String color; // string
        /**
         * This field shall indicate the size of the image used for Styling different Video Player sections like Logo,
         * Watermark etc.
         */
        public DimensionStruct size; // DimensionStruct

        public StyleInformationStruct(String imageUrl, String color, DimensionStruct size) {
            this.imageUrl = imageUrl;
            this.color = color;
            this.size = size;
        }
    }

    /**
     * This object defines Branding Information which can be provided by the client in order to customize the skin of
     * the Video Player during playback.
     */
    public static class BrandingInformationStruct {
        /**
         * This field shall indicate name of the provider for the given content.
         */
        public String providerName; // string
        /**
         * This field shall indicate background of the Video Player while content launch request is being processed by
         * it. This background information may also be used by the Video Player when it is in idle state.
         */
        public StyleInformationStruct background; // StyleInformationStruct
        /**
         * This field shall indicate the logo shown when the Video Player is launching. This is also used when the Video
         * Player is in the idle state and Splash field is not available.
         */
        public StyleInformationStruct logo; // StyleInformationStruct
        /**
         * This field shall indicate the style of progress bar for media playback.
         */
        public StyleInformationStruct progressBar; // StyleInformationStruct
        /**
         * This field shall indicate the screen shown when the Video Player is in an idle state. If this property is not
         * populated, the Video Player shall default to logo or the provider name.
         */
        public StyleInformationStruct splash; // StyleInformationStruct
        /**
         * This field shall indicate watermark shown when the media is playing.
         */
        public StyleInformationStruct watermark; // StyleInformationStruct

        public BrandingInformationStruct(String providerName, StyleInformationStruct background,
                StyleInformationStruct logo, StyleInformationStruct progressBar, StyleInformationStruct splash,
                StyleInformationStruct watermark) {
            this.providerName = providerName;
            this.background = background;
            this.logo = logo;
            this.progressBar = progressBar;
            this.splash = splash;
            this.watermark = watermark;
        }
    }

    /**
     * PlaybackPreferencesStruct defines the preferences sent by the client to the receiver in the ContentLauncher
     * LaunchURL or LaunchContent commands.
     */
    public static class PlaybackPreferencesStruct {
        /**
         * This field shall indicate the preferred position (in milliseconds) in the media to launch playback from. In
         * case the position falls in the middle of a frame, the server shall set the position to the beginning of that
         * frame and set the SampledPosition attribute on the MediaPlayback cluster accordingly. A value of null shall
         * indicate that playback position is not applicable for the current state of the media playback. (For example :
         * Live media with no known duration and where seek is not supported).
         */
        public BigInteger playbackPosition; // uint64
        /**
         * This field shall indicate the user’s preferred Text Track. A value of null shall indicate that the user did
         * not specify a preferred Text Track on the client. In such a case, the decision to display and select a Text
         * Track is up to the server.
         */
        public TrackPreferenceStruct textTrack; // TrackPreferenceStruct
        /**
         * This field shall indicate the list of the user’s preferred Audio Tracks. If the list contains multiple
         * values, each AudioTrack must also specify a unique audioOutputIndex to play the track on. A value of null
         * shall indicate that the user did not specify a preferred Audio Track on the client. In such a case, the
         * decision to play and select an Audio Track is up to the server.
         */
        public List<TrackPreferenceStruct> audioTracks; // list

        public PlaybackPreferencesStruct(BigInteger playbackPosition, TrackPreferenceStruct textTrack,
                List<TrackPreferenceStruct> audioTracks) {
            this.playbackPosition = playbackPosition;
            this.textTrack = textTrack;
            this.audioTracks = audioTracks;
        }
    }

    /**
     * This structure defines Text/Audio Track preferences.
     */
    public static class TrackPreferenceStruct {
        /**
         * This field shall contain one of the standard Tags for Identifying Languages RFC 5646, which identifies the
         * primary language used in the Track.
         */
        public String languageCode; // string
        /**
         * This field shall contain a list of enumerated CharacteristicEnum values that indicate a purpose, trait or
         * feature associated with the Track. A value of null shall indicate that there are no Characteristics
         * corresponding to the Track.
         */
        public List<MediaPlaybackCluster.CharacteristicEnum> characteristics; // list
        /**
         * This field if present shall indicate the index of the OutputInfoStruct from the OutputList attribute (from
         * the AudioOutput cluster) and indicates which audio output the Audio Track should be played on.
         * This field shall NOT be present if the track is not an audio track.
         * If the track is an audio track, this field MUST be present. A value of null shall indicate that the server
         * can choose the audio output(s) to play the Audio Track on.
         */
        public Integer audioOutputIndex; // uint8

        public TrackPreferenceStruct(String languageCode, List<MediaPlaybackCluster.CharacteristicEnum> characteristics,
                Integer audioOutputIndex) {
            this.languageCode = languageCode;
            this.characteristics = characteristics;
            this.audioOutputIndex = audioOutputIndex;
        }
    }

    // Enums
    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        URL_NOT_AVAILABLE(1, "Url Not Available"),
        AUTH_FAILED(2, "Auth Failed"),
        TEXT_TRACK_NOT_AVAILABLE(3, "Text Track Not Available"),
        AUDIO_TRACK_NOT_AVAILABLE(4, "Audio Track Not Available");

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

    public enum ParameterEnum implements MatterEnum {
        ACTOR(0, "Actor"),
        CHANNEL(1, "Channel"),
        CHARACTER(2, "Character"),
        DIRECTOR(3, "Director"),
        EVENT(4, "Event"),
        FRANCHISE(5, "Franchise"),
        GENRE(6, "Genre"),
        LEAGUE(7, "League"),
        POPULARITY(8, "Popularity"),
        PROVIDER(9, "Provider"),
        SPORT(10, "Sport"),
        SPORTS_TEAM(11, "Sports Team"),
        TYPE(12, "Type"),
        VIDEO(13, "Video"),
        SEASON(14, "Season"),
        EPISODE(15, "Episode"),
        ANY(16, "Any");

        public final Integer value;
        public final String label;

        private ParameterEnum(Integer value, String label) {
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

    public enum MetricTypeEnum implements MatterEnum {
        PIXELS(0, "Pixels"),
        PERCENTAGE(1, "Percentage");

        public final Integer value;
        public final String label;

        private MetricTypeEnum(Integer value, String label) {
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
    public static class SupportedProtocolsBitmap {
        public boolean dash;
        public boolean hls;

        public SupportedProtocolsBitmap(boolean dash, boolean hls) {
            this.dash = dash;
            this.hls = hls;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Device supports content search (non-app specific)
         */
        public boolean contentSearch;
        /**
         * 
         * Device supports basic URL-based file playback
         */
        public boolean urlPlayback;
        /**
         * 
         * Enables clients to implement more advanced media seeking behavior in their user interface, such as for
         * example a &quot;seek bar&quot;.
         */
        public boolean advancedSeek;
        /**
         * 
         * Device or app supports Text Tracks.
         */
        public boolean textTracks;
        /**
         * 
         * Device or app supports Audio Tracks.
         */
        public boolean audioTracks;

        public FeatureMap(boolean contentSearch, boolean urlPlayback, boolean advancedSeek, boolean textTracks,
                boolean audioTracks) {
            this.contentSearch = contentSearch;
            this.urlPlayback = urlPlayback;
            this.advancedSeek = advancedSeek;
            this.textTracks = textTracks;
            this.audioTracks = audioTracks;
        }
    }

    public ContentLauncherCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1290, "ContentLauncher");
    }

    protected ContentLauncherCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall launch the specified content with optional search criteria. This command returns a
     * Launch Response.
     */
    public static ClusterCommand launchContent(ContentSearchStruct search, Boolean autoPlay, String data,
            PlaybackPreferencesStruct playbackPreferences, Boolean useCurrentContext) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (search != null) {
            map.put("search", search);
        }
        if (autoPlay != null) {
            map.put("autoPlay", autoPlay);
        }
        if (data != null) {
            map.put("data", data);
        }
        if (playbackPreferences != null) {
            map.put("playbackPreferences", playbackPreferences);
        }
        if (useCurrentContext != null) {
            map.put("useCurrentContext", useCurrentContext);
        }
        return new ClusterCommand("launchContent", map);
    }

    /**
     * Upon receipt, this shall launch content from the specified URL.
     * The content types supported include those identified in the AcceptHeader and SupportedStreamingProtocols
     * attributes.
     * A check shall be made to ensure the URL is secure (uses HTTPS).
     * When playing a video stream in response to this command, an indication (ex. visual) of the identity of the origin
     * node of the video stream shall be provided. This could be in the form of a friendly name label which uniquely
     * identifies the node to the user. This friendly name label is typically assigned by the Matter Admin (ex. TV) at
     * the time of commissioning and, when it’s a device, is often editable by the user. It might be a combination of a
     * company name and friendly name, for example, ”Acme” or “Acme Streaming Service on Alice’s Phone”.
     * This command returns a Launch Response.
     */
    public static ClusterCommand launchUrl(String contentUrl, String displayString,
            BrandingInformationStruct brandingInformation, PlaybackPreferencesStruct playbackPreferences) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (contentUrl != null) {
            map.put("contentUrl", contentUrl);
        }
        if (displayString != null) {
            map.put("displayString", displayString);
        }
        if (brandingInformation != null) {
            map.put("brandingInformation", brandingInformation);
        }
        if (playbackPreferences != null) {
            map.put("playbackPreferences", playbackPreferences);
        }
        return new ClusterCommand("launchUrl", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "acceptHeader : " + acceptHeader + "\n";
        str += "supportedStreamingProtocols : " + supportedStreamingProtocols + "\n";
        return str;
    }
}
