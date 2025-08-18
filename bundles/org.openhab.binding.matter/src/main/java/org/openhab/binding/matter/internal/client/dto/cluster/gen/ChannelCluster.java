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
 * Channel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ChannelCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0504;
    public static final String CLUSTER_NAME = "Channel";
    public static final String CLUSTER_PREFIX = "channel";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CHANNEL_LIST = "channelList";
    public static final String ATTRIBUTE_LINEUP = "lineup";
    public static final String ATTRIBUTE_CURRENT_CHANNEL = "currentChannel";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall provide the list of supported channels.
     */
    public List<ChannelInfoStruct> channelList; // 0 list R V
    /**
     * This attribute shall identify the channel lineup using external data sources.
     */
    public LineupInfoStruct lineup; // 1 LineupInfoStruct R V
    /**
     * This attribute shall contain the current channel. When supported but a channel is not currently tuned to (if a
     * content application is in foreground), the value of the field shall be null.
     */
    public ChannelInfoStruct currentChannel; // 2 ChannelInfoStruct R V

    // Structs
    /**
     * This indicates a channel in a channel lineup.
     * While the major and minor numbers in the ChannelInfoStruct support use of ATSC channel format, a lineup may use
     * other formats which can map into these numeric values.
     */
    public static class ChannelInfoStruct {
        /**
         * This field shall indicate the channel major number value (for example, using ATSC format). When the channel
         * number is expressed as a string, such as &quot;13.1&quot; or &quot;256&quot;, the major number would be 13 or
         * 256, respectively. This field is required but shall be set to 0 for channels such as over-the-top channels
         * that are not represented by a major or minor number.
         */
        public Integer majorNumber; // uint16
        /**
         * This field shall indicate the channel minor number value (for example, using ATSC format). When the channel
         * number is expressed as a string, such as &quot;13.1&quot; or &quot;256&quot;, the minor number would be 1 or
         * 0, respectively. This field is required but shall be set to 0 for channels such as over-the-top channels that
         * are not represented by a major or minor number.
         */
        public Integer minorNumber; // uint16
        /**
         * This field shall indicate the marketing name for the channel, such as “The CW&quot; or &quot;Comedy
         * Central&quot;. This field is optional, but SHOULD be provided when known.
         */
        public String name; // string
        /**
         * This field shall indicate the call sign of the channel, such as &quot;PBS&quot;. This field is optional, but
         * SHOULD be provided when known.
         */
        public String callSign; // string
        /**
         * This field shall indicate the local affiliate call sign, such as &quot;KCTS&quot;. This field is optional,
         * but SHOULD be provided when known.
         */
        public String affiliateCallSign; // string
        /**
         * This shall indicate the unique identifier for a specific channel. This field is optional, but SHOULD be
         * provided when MajorNumber and MinorNumber are not available.
         */
        public String identifier; // string
        /**
         * This shall indicate the type or grouping of a specific channel. This field is optional, but SHOULD be
         * provided when known.
         */
        public ChannelTypeEnum type; // ChannelTypeEnum

        public ChannelInfoStruct(Integer majorNumber, Integer minorNumber, String name, String callSign,
                String affiliateCallSign, String identifier, ChannelTypeEnum type) {
            this.majorNumber = majorNumber;
            this.minorNumber = minorNumber;
            this.name = name;
            this.callSign = callSign;
            this.affiliateCallSign = affiliateCallSign;
            this.identifier = identifier;
            this.type = type;
        }
    }

    /**
     * The Lineup Info allows references to external lineup sources like Gracenote. The combination of OperatorName,
     * LineupName, and PostalCode MUST uniquely identify a lineup.
     */
    public static class LineupInfoStruct {
        /**
         * This field shall indicate the name of the operator, for example “Comcast”.
         */
        public String operatorName; // string
        /**
         * This field shall indicate the name of the provider lineup, for example &quot;Comcast King County&quot;. This
         * field is optional, but SHOULD be provided when known.
         */
        public String lineupName; // string
        /**
         * This field shall indicate the postal code (zip code) for the location of the device, such as
         * &quot;98052&quot;. This field is optional, but SHOULD be provided when known.
         */
        public String postalCode; // string
        /**
         * This field shall indicate the type of lineup. This field is optional, but SHOULD be provided when known.
         */
        public LineupInfoTypeEnum lineupInfoType; // LineupInfoTypeEnum

        public LineupInfoStruct(String operatorName, String lineupName, String postalCode,
                LineupInfoTypeEnum lineupInfoType) {
            this.operatorName = operatorName;
            this.lineupName = lineupName;
            this.postalCode = postalCode;
            this.lineupInfoType = lineupInfoType;
        }
    }

    /**
     * This indicates a program within an electronic program guide (EPG).
     */
    public static class ProgramStruct {
        /**
         * This field shall indicate a unique identifier for a program within an electronic program guide list. The
         * identifier shall be unique across multiple channels.
         */
        public String identifier; // string
        /**
         * This field shall indicate the channel associated to the program.
         */
        public ChannelInfoStruct channel; // ChannelInfoStruct
        /**
         * This field shall indicate an epoch time in seconds indicating the start time of a program, as a UTC time.
         * This field can represent a past or future value.
         */
        public Integer startTime; // epoch-s
        /**
         * This field shall indicate an epoch time in seconds indicating the end time of a program, as a UTC time. This
         * field can represent a past or future value but shall be greater than the StartTime.
         */
        public Integer endTime; // epoch-s
        /**
         * This field shall indicate the title or name for the specific program. For example, “MCIS: Los Angeles”.
         */
        public String title; // string
        /**
         * This field shall indicate the subtitle for the specific program. For example, “Maybe Today&quot; which is an
         * episode name for “MCIS: Los Angeles”. This field is optional but shall be provided if applicable and known.
         */
        public String subtitle; // string
        /**
         * This field shall indicate the brief description for the specific program. For example, a description of an
         * episode. This field is optional but shall be provided if known.
         */
        public String description; // string
        /**
         * This field shall indicate the audio language for the specific program. The value is a string containing one
         * of the standard Tags for Identifying Languages RFC 5646. This field is optional but shall be provided if
         * known.
         */
        public List<String> audioLanguages; // list
        /**
         * This field shall be used for indicating the level of parental guidance recommended for of a particular
         * program. This can be any rating system used in the country or region where the program is broadcast. For
         * example, in the United States “TV-PG” may contain material that parents can find not suitable for younger
         * children but can be accepted in general for older children. This field is optional but shall be provided if
         * known.
         */
        public List<String> ratings; // list
        /**
         * This field shall represent a URL of a thumbnail that clients can use to render an image for the program. The
         * syntax of this field shall follow the syntax as specified in RFC 1738 and shall use the https scheme.
         */
        public String thumbnailUrl; // string
        /**
         * This field shall represent a URL of a poster that clients can use to render an image for the program on the
         * detail view. The syntax of this field shall follow the syntax as specified in RFC 1738 and shall use the
         * https scheme.
         */
        public String posterArtUrl; // string
        /**
         * This field shall represent the DVB-I URL associated to the program. The syntax of this field shall follow the
         * syntax as specified in RFC 1738 and shall use the https scheme.
         */
        public String dvbiUrl; // string
        /**
         * This field shall be a string, in ISO 8601 format, representing the date on which the program was released.
         * This field is optional but when provided, the year shall be provided as part of the string.
         */
        public String releaseDate; // string
        /**
         * This field shall represent a string providing additional information on the parental guidance. This field is
         * optional.
         */
        public String parentalGuidanceText; // string
        /**
         * This field shall represent the recording status of the program. This field is required if the RecordProgram
         * feature is set.
         */
        public RecordingFlagBitmap recordingFlag; // RecordingFlagBitmap
        /**
         * This field shall represent the information of a series such as season and episode number. This field is
         * optional but SHOULD be provided if the program represents a series and this information is available.
         */
        public SeriesInfoStruct seriesInfo; // SeriesInfoStruct
        /**
         * This field shall represent the category of a particular program. This field is optional but shall be provided
         * if known.
         */
        public List<ProgramCategoryStruct> categoryList; // list
        /**
         * This field shall represent a list of the cast or the crew on the program. A single cast member may have more
         * than one role. This field is optional but shall be provided if known.
         */
        public List<ProgramCastStruct> castList; // list
        /**
         * This field shall indicate the list of additional external content identifiers.
         */
        public List<ContentLauncherCluster.AdditionalInfoStruct> externalIdList; // list

        public ProgramStruct(String identifier, ChannelInfoStruct channel, Integer startTime, Integer endTime,
                String title, String subtitle, String description, List<String> audioLanguages, List<String> ratings,
                String thumbnailUrl, String posterArtUrl, String dvbiUrl, String releaseDate,
                String parentalGuidanceText, RecordingFlagBitmap recordingFlag, SeriesInfoStruct seriesInfo,
                List<ProgramCategoryStruct> categoryList, List<ProgramCastStruct> castList,
                List<ContentLauncherCluster.AdditionalInfoStruct> externalIdList) {
            this.identifier = identifier;
            this.channel = channel;
            this.startTime = startTime;
            this.endTime = endTime;
            this.title = title;
            this.subtitle = subtitle;
            this.description = description;
            this.audioLanguages = audioLanguages;
            this.ratings = ratings;
            this.thumbnailUrl = thumbnailUrl;
            this.posterArtUrl = posterArtUrl;
            this.dvbiUrl = dvbiUrl;
            this.releaseDate = releaseDate;
            this.parentalGuidanceText = parentalGuidanceText;
            this.recordingFlag = recordingFlag;
            this.seriesInfo = seriesInfo;
            this.categoryList = categoryList;
            this.castList = castList;
            this.externalIdList = externalIdList;
        }
    }

    /**
     * This object defines the category associated to a program.
     */
    public static class ProgramCategoryStruct {
        /**
         * This field shall represent the category or genre of the program. Ex. News.
         */
        public String category; // string
        /**
         * This field shall represent the sub-category or sub-genre of the program. Ex. Local.
         */
        public String subCategory; // string

        public ProgramCategoryStruct(String category, String subCategory) {
            this.category = category;
            this.subCategory = subCategory;
        }
    }

    /**
     * This object provides the episode information related to a program.
     */
    public static class SeriesInfoStruct {
        /**
         * This field shall represent the season of the series associated to the program.
         */
        public String season; // string
        /**
         * This field shall represent the episode of the program.
         */
        public String episode; // string

        public SeriesInfoStruct(String season, String episode) {
            this.season = season;
            this.episode = episode;
        }
    }

    /**
     * This object provides the cast information related to a program.
     */
    public static class ProgramCastStruct {
        /**
         * This field shall represent the name of the cast member.
         */
        public String name; // string
        /**
         * This field shall represent the role of the cast member. Ex. Actor, Director.
         */
        public String role; // string

        public ProgramCastStruct(String name, String role) {
            this.name = name;
            this.role = role;
        }
    }

    /**
     * This object defines the pagination structure.
     */
    public static class PageTokenStruct {
        /**
         * This field shall indicate the maximum number of entries that should be retrieved from the program guide in a
         * single response. It allows clients to specify the size of the paginated result set based on their needs.
         */
        public Integer limit; // uint16
        /**
         * This field shall indicate the cursor that pinpoints the start of the upcoming data page. In a Cursor-based
         * pagination system, the field acts as a reference point, ensuring the set of results corresponds directly to
         * the data following the specified cursor. In a Offset-based pagination system, the field, along with limit,
         * indicate the offset from which entries in the program guide will be retrieved.
         */
        public String after; // string
        /**
         * This field shall indicate the cursor that pinpoints the end of the upcoming data page. In a Cursor-based
         * pagination system, the field acts as a reference point, ensuring the set of results corresponds directly to
         * the data preceding the specified cursor. In a Offset-based pagination system, the field, along with limit,
         * indicate the offset from which entries in the program guide will be retrieved.
         */
        public String before; // string

        public PageTokenStruct(Integer limit, String after, String before) {
            this.limit = limit;
            this.after = after;
            this.before = before;
        }
    }

    /**
     * This object defines the paging structure that includes the previous and next pagination tokens.
     */
    public static class ChannelPagingStruct {
        /**
         * This field shall indicate the token to retrieve the preceding page. Absence of this field denotes the
         * response as the initial page.
         */
        public PageTokenStruct previousToken; // PageTokenStruct
        /**
         * This field shall indicate the token to retrieve the next page. Absence of this field denotes the response as
         * the last page.
         */
        public PageTokenStruct nextToken; // PageTokenStruct

        public ChannelPagingStruct(PageTokenStruct previousToken, PageTokenStruct nextToken) {
            this.previousToken = previousToken;
            this.nextToken = nextToken;
        }
    }

    // Enums
    public enum LineupInfoTypeEnum implements MatterEnum {
        MSO(0, "Mso");

        public final Integer value;
        public final String label;

        private LineupInfoTypeEnum(Integer value, String label) {
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
        MULTIPLE_MATCHES(1, "Multiple Matches"),
        NO_MATCHES(2, "No Matches");

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

    public enum ChannelTypeEnum implements MatterEnum {
        SATELLITE(0, "Satellite"),
        CABLE(1, "Cable"),
        TERRESTRIAL(2, "Terrestrial"),
        OTT(3, "Ott");

        public final Integer value;
        public final String label;

        private ChannelTypeEnum(Integer value, String label) {
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
    public static class RecordingFlagBitmap {
        public boolean scheduled;
        public boolean recordSeries;
        public boolean recorded;

        public RecordingFlagBitmap(boolean scheduled, boolean recordSeries, boolean recorded) {
            this.scheduled = scheduled;
            this.recordSeries = recordSeries;
            this.recorded = recorded;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Provides list of available channels.
         */
        public boolean channelList;
        /**
         * 
         * Provides lineup info, which is a reference to an external source of lineup information.
         */
        public boolean lineupInfo;
        /**
         * 
         * Provides electronic program guide information.
         */
        public boolean electronicGuide;
        /**
         * 
         * Provides ability to record program.
         */
        public boolean recordProgram;

        public FeatureMap(boolean channelList, boolean lineupInfo, boolean electronicGuide, boolean recordProgram) {
            this.channelList = channelList;
            this.lineupInfo = lineupInfo;
            this.electronicGuide = electronicGuide;
            this.recordProgram = recordProgram;
        }
    }

    public ChannelCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1284, "Channel");
    }

    protected ChannelCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Change the channel to the channel case-insensitive exact matching the value passed as an argument.
     * The match priority order shall be: Identifier, AffiliateCallSign, CallSign, Name, Number. In the match string,
     * the Channel number should be presented in the &quot;Major.Minor&quot; format, such as &quot;13.1&quot;.
     * Upon receipt, this shall generate a ChangeChannelResponse command.
     * Upon success, the CurrentChannel attribute, if supported, shall be updated to reflect the change.
     */
    public static ClusterCommand changeChannel(String match) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (match != null) {
            map.put("match", match);
        }
        return new ClusterCommand("changeChannel", map);
    }

    /**
     * Change the channel to the channel with the given Number in the ChannelList attribute.
     */
    public static ClusterCommand changeChannelByNumber(Integer majorNumber, Integer minorNumber) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (majorNumber != null) {
            map.put("majorNumber", majorNumber);
        }
        if (minorNumber != null) {
            map.put("minorNumber", minorNumber);
        }
        return new ClusterCommand("changeChannelByNumber", map);
    }

    /**
     * This command provides channel up and channel down functionality, but allows channel index jumps of size Count.
     * When the value of the increase or decrease is larger than the number of channels remaining in the given
     * direction, then the behavior shall be to return to the beginning (or end) of the channel list and continue. For
     * example, if the current channel is at index 0 and count value of -1 is given, then the current channel should
     * change to the last channel.
     */
    public static ClusterCommand skipChannel(Integer count) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (count != null) {
            map.put("count", count);
        }
        return new ClusterCommand("skipChannel", map);
    }

    /**
     * This command retrieves the program guide. It accepts several filter parameters to return specific schedule and
     * program information from a content app. The command shall receive in response a ProgramGuideResponse. Standard
     * error codes shall be used when arguments provided are not valid. For example, if StartTime is greater than
     * EndTime, the status code INVALID_ACTION shall be returned.
     */
    public static ClusterCommand getProgramGuide(Integer startTime, Integer endTime,
            List<ChannelInfoStruct> channelList, PageTokenStruct pageToken, RecordingFlagBitmap recordingFlag,
            List<ContentLauncherCluster.AdditionalInfoStruct> externalIdList, OctetString data) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (startTime != null) {
            map.put("startTime", startTime);
        }
        if (endTime != null) {
            map.put("endTime", endTime);
        }
        if (channelList != null) {
            map.put("channelList", channelList);
        }
        if (pageToken != null) {
            map.put("pageToken", pageToken);
        }
        if (recordingFlag != null) {
            map.put("recordingFlag", recordingFlag);
        }
        if (externalIdList != null) {
            map.put("externalIdList", externalIdList);
        }
        if (data != null) {
            map.put("data", data);
        }
        return new ClusterCommand("getProgramGuide", map);
    }

    /**
     * Record a specific program or series when it goes live. This functionality enables DVR recording features.
     */
    public static ClusterCommand recordProgram(String programIdentifier, Boolean shouldRecordSeries,
            List<ContentLauncherCluster.AdditionalInfoStruct> externalIdList, OctetString data) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (programIdentifier != null) {
            map.put("programIdentifier", programIdentifier);
        }
        if (shouldRecordSeries != null) {
            map.put("shouldRecordSeries", shouldRecordSeries);
        }
        if (externalIdList != null) {
            map.put("externalIdList", externalIdList);
        }
        if (data != null) {
            map.put("data", data);
        }
        return new ClusterCommand("recordProgram", map);
    }

    /**
     * Cancel recording for a specific program or series.
     */
    public static ClusterCommand cancelRecordProgram(String programIdentifier, Boolean shouldRecordSeries,
            List<ContentLauncherCluster.AdditionalInfoStruct> externalIdList, OctetString data) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (programIdentifier != null) {
            map.put("programIdentifier", programIdentifier);
        }
        if (shouldRecordSeries != null) {
            map.put("shouldRecordSeries", shouldRecordSeries);
        }
        if (externalIdList != null) {
            map.put("externalIdList", externalIdList);
        }
        if (data != null) {
            map.put("data", data);
        }
        return new ClusterCommand("cancelRecordProgram", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "channelList : " + channelList + "\n";
        str += "lineup : " + lineup + "\n";
        str += "currentChannel : " + currentChannel + "\n";
        return str;
    }
}
