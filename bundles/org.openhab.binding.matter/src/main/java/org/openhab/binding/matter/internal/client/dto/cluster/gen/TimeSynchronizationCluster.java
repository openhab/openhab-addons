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
 * TimeSynchronization
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TimeSynchronizationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0038;
    public static final String CLUSTER_NAME = "TimeSynchronization";
    public static final String CLUSTER_PREFIX = "timeSynchronization";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_UTC_TIME = "utcTime";
    public static final String ATTRIBUTE_GRANULARITY = "granularity";
    public static final String ATTRIBUTE_TIME_SOURCE = "timeSource";
    public static final String ATTRIBUTE_TRUSTED_TIME_SOURCE = "trustedTimeSource";
    public static final String ATTRIBUTE_DEFAULT_NTP = "defaultNtp";
    public static final String ATTRIBUTE_TIME_ZONE = "timeZone";
    public static final String ATTRIBUTE_DST_OFFSET = "dstOffset";
    public static final String ATTRIBUTE_LOCAL_TIME = "localTime";
    public static final String ATTRIBUTE_TIME_ZONE_DATABASE = "timeZoneDatabase";
    public static final String ATTRIBUTE_NTP_SERVER_AVAILABLE = "ntpServerAvailable";
    public static final String ATTRIBUTE_TIME_ZONE_LIST_MAX_SIZE = "timeZoneListMaxSize";
    public static final String ATTRIBUTE_DST_OFFSET_LIST_MAX_SIZE = "dstOffsetListMaxSize";
    public static final String ATTRIBUTE_SUPPORTS_DNS_RESOLVE = "supportsDnsResolve";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * If the node has achieved time synchronization, this shall indicate the current time as a UTC epoch-us (Epoch Time
     * in Microseconds).
     * If the node has not achieved time synchronization, this shall be null. This attribute may be set when a
     * SetUTCTime is received.
     */
    public BigInteger utcTime; // 0 epoch-us R V
    /**
     * The granularity of the error that the node is willing to guarantee on the time synchronization. It is of type
     * GranularityEnum.
     * This value shall be set to NoTimeGranularity if UTCTime is null and shall NOT be set to NoTimeGranularity if
     * UTCTime is non-null.
     */
    public GranularityEnum granularity; // 1 GranularityEnum R V
    /**
     * The node’s time source. This attribute indicates what method the node is using to sync, whether the source uses
     * NTS or not and whether the source is internal or external to the Matter network. This attribute may be used by a
     * client to determine its level of trust in the UTCTime. It is of type TimeSourceEnum.
     * If a node is unsure if the selected NTP server is within the Matter network, it SHOULD select one of the
     * NonMatter* values.
     * This value shall be set to None if UTCTime is null and shall NOT be set to None if UTCTime is non-null.
     */
    public TimeSourceEnum timeSource; // 2 TimeSourceEnum R V
    /**
     * A Node ID, endpoint, and associated fabric index of a Node that may be used as trusted time source. See Section
     * 11.17.13, “Time source prioritization”. This attribute reflects the last value set by an administrator using the
     * SetTrustedTimeSource command. If the value is null, no trusted time source has yet been set.
     */
    public TrustedTimeSourceStruct trustedTimeSource; // 3 TrustedTimeSourceStruct R V
    /**
     * The default NTP server that this Node may use if other time sources are unavailable. This attribute is settable
     * by an Administrator using the SetDefaultNTP command. It SHOULD be set by the Commissioner during commissioning.
     * If no default NTP server is available, the Commissioner may set this value to null. The default IANA assigned NTP
     * port of 123 shall be used to access the NTP server.
     * If set, the format of this attribute shall be a domain name or a static IPv6 address with no port, in text
     * format, as specified in RFC 5952. The address format shall follow the recommendations in Section 4 and shall NOT
     * contain a port number.
     */
    public String defaultNtp; // 4 string R V
    /**
     * A list of time zone offsets from UTC and when they shall take effect. This attribute uses a list of time offset
     * configurations to allow Nodes to handle scheduled regulatory time zone changes. This attribute shall NOT be used
     * to indicate daylight savings time changes (see DSTOffset attribute for daylight savings time).
     * The first entry shall have a ValidAt entry of 0. If there is a second entry, it shall have a non-zero ValidAt
     * time.
     * If a node supports a TimeZoneDatabase, and it has data for the given time zone Name and the given Offset matches,
     * the node may update its own DSTOffset attribute to add new DST change times as required, based on the Name fields
     * of the TimeZoneStruct. Administrators may add additional entries to the DSTOffset of other Nodes with the same
     * time zone, if required.
     * If a node does not support a TimeZoneDatabase, the Name field of the TimeZoneStruct is only applicable for
     * client-side localization. In particular:
     * • If the node does not support a TimeZoneDatabase, the Name field shall NOT be used to calculate the local time.
     * • If the node does not support a TimeZoneDatabase, the Name field shall NOT be used to calculate DST start or end
     * dates.
     * When time passes, the node SHOULD remove any entries which are no longer active and change the ValidAt time for
     * the currently used TimeZoneStruct list item to zero.
     * This attribute shall have at least one entry. If the node does not have a default time zone and no time zone has
     * been set, it may set this value to a list containing a single TimeZoneStruct with an offset of 0 (UTC) and a
     * ValidAt time of 0.
     */
    public List<TimeZoneStruct> timeZone; // 5 list R V
    /**
     * A list of offsets to apply for daylight savings time, and their validity period. List entries shall be sorted by
     * ValidStarting time.
     * A list entry shall NOT have a ValidStarting time that is smaller than the ValidUntil time of the previous entry.
     * There shall be at most one list entry with a null ValidUntil time and, if such an entry is present, it shall
     * appear last in the list.
     * Over time, the node SHOULD remove any entries which are no longer active from the list.
     * Over time, if the node supports a TimeZoneDatabase and it has information available for the given time zone name,
     * it may update its own list to add additional entries.
     * If a time zone does not use DST, this shall be indicated by a single entry with a 0 offset and a null ValidUntil
     * field.
     */
    public List<DSTOffsetStruct> dstOffset; // 6 list R V
    /**
     * The computed current local time of the node as a epoch-us (Epoch Time in Microseconds). The value of LocalTime
     * shall be the sum of the UTCTime, the offset of the currently valid TimeZoneStruct from the TimeZone attribute
     * (converted to microseconds), and the offset of the currently valid DSTOffsetStruct from the DSTOffset attribute
     * (converted to microseconds), if such an entry exists.
     * If the node has not achieved time synchronization, this shall be null. If the node has an empty DSTOffset, this
     * shall be null.
     */
    public BigInteger localTime; // 7 epoch-us R V
    /**
     * Indicates whether the node has access to a time zone database. Nodes with a time zone database may update their
     * own DSTOffset attribute to add new entries and may push DSTOffset updates to other Nodes in the same time zone as
     * required.
     */
    public TimeZoneDatabaseEnum timeZoneDatabase; // 8 TimeZoneDatabaseEnum R V
    /**
     * If the node is running an RFC 5905 NTPv4 compliant server on port 123, this value shall be True. If the node is
     * not currently running an NTP server, this value shall be False.
     */
    public Boolean ntpServerAvailable; // 9 bool R V
    /**
     * Number of supported list entries in the TimeZone attribute. This attribute may take the value of 1 or 2, where
     * the optional second list entry may be used to handle scheduled regulatory time zone changes.
     */
    public Integer timeZoneListMaxSize; // 10 uint8 R V
    /**
     * Number of supported list entries in DSTOffset attribute. This value must be at least 1.
     */
    public Integer dstOffsetListMaxSize; // 11 uint8 R V
    /**
     * This attribute is true if the node supports resolving a domain name. DefaultNTP Address values for these nodes
     * may include domain names. If this is False, the Address for a DefaultNTP shall be an IPv6 address.
     */
    public Boolean supportsDnsResolve; // 12 bool R V

    // Structs
    /**
     * This event shall be generated when the node stops applying the current DSTOffset and there are no entries in the
     * list with a larger ValidStarting time, indicating the need to possibly get new DST data. This event shall also be
     * generated if the DSTOffset list is cleared either by a SetTimeZone command, or by a SetDSTOffset command with an
     * empty list.
     * The node shall generate this event if the node has not generated a DSTTableEmpty event in the last hour, and the
     * DSTOffset list is empty when the node attempts to update its time. DSTTableEmpty events corresponding to a time
     * update SHOULD NOT be generated more often than once per hour.
     * There is no data for this event.
     */
    public static class DstTableEmpty {
        public DstTableEmpty() {
        }
    }

    /**
     * This event shall be generated when the node starts or stops applying a DST offset.
     */
    public static class DstStatus {
        /**
         * Indicates whether the current DST offset is being applied (i.e, daylight savings time is applied, as opposed
         * to standard time).
         */
        public Boolean dstOffsetActive; // bool

        public DstStatus(Boolean dstOffsetActive) {
            this.dstOffsetActive = dstOffsetActive;
        }
    }

    /**
     * This event shall be generated when the node changes its time zone offset or name. It shall NOT be sent for DST
     * changes that are not accompanied by a time zone change.
     */
    public static class TimeZoneStatus {
        /**
         * Current time zone offset from UTC in seconds.
         */
        public Integer offset; // int32
        /**
         * Current time zone name. This name SHOULD use the country/city format specified by the IANA Time Zone
         * Database.
         */
        public String name; // string

        public TimeZoneStatus(Integer offset, String name) {
            this.offset = offset;
            this.name = name;
        }
    }

    /**
     * This event shall be generated if the node has not generated a TimeFailure event in the last hour, and the node is
     * unable to get a time from any source. This event SHOULD NOT be generated more often than once per hour.
     */
    public static class TimeFailure {
        public TimeFailure() {
        }
    }

    /**
     * This event shall be generated if the TrustedTimeSource is set to null upon fabric removal or by a
     * SetTrustedTimeSource command.
     * This event shall also be generated if the node has not generated a MissingTrustedTimeSource event in the last
     * hour, and the node fails to update its time from the TrustedTimeSource because the TrustedTimeSource is null or
     * the specified peer cannot be reached. MissingTrustedTimeSource events corresponding to a time update SHOULD NOT
     * be generated more often than once per hour.
     */
    public static class MissingTrustedTimeSource {
        public MissingTrustedTimeSource() {
        }
    }

    public static class TrustedTimeSourceStruct {
        /**
         * The Fabric Index associated with the Fabric of the client which last set the value of the trusted time source
         * node.
         */
        public Integer fabricIndex; // fabric-idx
        /**
         * Node ID of the trusted time source node on the Fabric associated with the entry.
         */
        public BigInteger nodeId; // node-id
        /**
         * Endpoint on the trusted time source node that contains the Time Synchronization cluster server.
         */
        public Integer endpoint; // endpoint-no

        public TrustedTimeSourceStruct(Integer fabricIndex, BigInteger nodeId, Integer endpoint) {
            this.fabricIndex = fabricIndex;
            this.nodeId = nodeId;
            this.endpoint = endpoint;
        }
    }

    public static class FabricScopedTrustedTimeSourceStruct {
        /**
         * Node ID of the trusted time source node on the Fabric of the issuer.
         */
        public BigInteger nodeId; // node-id
        /**
         * Endpoint on the trusted time source node that contains the Time Synchronization cluster server. This is
         * provided to avoid having to do discovery of the location of that endpoint by walking over all endpoints and
         * checking their Descriptor Cluster.
         */
        public Integer endpoint; // endpoint-no

        public FabricScopedTrustedTimeSourceStruct(BigInteger nodeId, Integer endpoint) {
            this.nodeId = nodeId;
            this.endpoint = endpoint;
        }
    }

    public static class TimeZoneStruct {
        /**
         * The time zone offset from UTC in seconds.
         */
        public Integer offset; // int32
        /**
         * The UTC time when the offset shall be applied.
         */
        public BigInteger validAt; // epoch-us
        /**
         * The time zone name SHOULD provide a human-readable time zone name and it SHOULD use the country/city format
         * specified by the IANA Time Zone Database. The Name field may be used for display. If the node supports a
         * TimeZoneDatabase it may use the Name field to set its own DST offsets if it has database information for the
         * supplied time zone Name and the given Offset matches.
         */
        public String name; // string

        public TimeZoneStruct(Integer offset, BigInteger validAt, String name) {
            this.offset = offset;
            this.validAt = validAt;
            this.name = name;
        }
    }

    public static class DSTOffsetStruct {
        /**
         * The DST offset in seconds. Normally this is in the range of 0 to 3600 seconds (1 hour), but this field will
         * accept any values in the int32 range to accommodate potential future legislation that does not fit with these
         * assumptions.
         */
        public Integer offset; // int32
        /**
         * The UTC time when the offset shall be applied.
         */
        public BigInteger validStarting; // epoch-us
        /**
         * The UTC time when the offset shall stop being applied. Providing a null value here indicates a permanent DST
         * change. If this value is non-null the value shall be larger than the ValidStarting time.
         */
        public BigInteger validUntil; // epoch-us

        public DSTOffsetStruct(Integer offset, BigInteger validStarting, BigInteger validUntil) {
            this.offset = offset;
            this.validStarting = validStarting;
            this.validUntil = validUntil;
        }
    }

    // Enums
    public enum GranularityEnum implements MatterEnum {
        NO_TIME_GRANULARITY(0, "No Time Granularity"),
        MINUTES_GRANULARITY(1, "Minutes Granularity"),
        SECONDS_GRANULARITY(2, "Seconds Granularity"),
        MILLISECONDS_GRANULARITY(3, "Milliseconds Granularity"),
        MICROSECONDS_GRANULARITY(4, "Microseconds Granularity");

        public final Integer value;
        public final String label;

        private GranularityEnum(Integer value, String label) {
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

    public enum TimeSourceEnum implements MatterEnum {
        NONE(0, "None"),
        UNKNOWN(1, "Unknown"),
        ADMIN(2, "Admin"),
        NODE_TIME_CLUSTER(3, "Node Time Cluster"),
        NON_MATTER_SNTP(4, "Non Matter Sntp"),
        NON_MATTER_NTP(5, "Non Matter Ntp"),
        MATTER_SNTP(6, "Matter Sntp"),
        MATTER_NTP(7, "Matter Ntp"),
        MIXED_NTP(8, "Mixed Ntp"),
        NON_MATTER_SNTPNTS(9, "Non Matter Sntpnts"),
        NON_MATTER_NTPNTS(10, "Non Matter Ntpnts"),
        MATTER_SNTPNTS(11, "Matter Sntpnts"),
        MATTER_NTPNTS(12, "Matter Ntpnts"),
        MIXED_NTPNTS(13, "Mixed Ntpnts"),
        CLOUD_SOURCE(14, "Cloud Source"),
        PTP(15, "Ptp"),
        GNSS(16, "Gnss");

        public final Integer value;
        public final String label;

        private TimeSourceEnum(Integer value, String label) {
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

    /**
     * It indicates what the device knows about the contents of the IANA Time Zone Database. Partial support on a device
     * may be used to omit historical data, less commonly used time zones, and/or time zones not related to the region a
     * product is sold in.
     */
    public enum TimeZoneDatabaseEnum implements MatterEnum {
        FULL(0, "Full"),
        PARTIAL(1, "Partial"),
        NONE(2, "None");

        public final Integer value;
        public final String label;

        private TimeZoneDatabaseEnum(Integer value, String label) {
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

    public enum StatusCodeEnum implements MatterEnum {
        TIME_NOT_ACCEPTED(2, "Time Not Accepted");

        public final Integer value;
        public final String label;

        private StatusCodeEnum(Integer value, String label) {
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
         * Allows a server to translate a UTC time to a local time using the time zone and daylight savings time (DST)
         * offsets. If a server supports the TimeZone feature, it shall support the SetTimeZone and SetDSTOffset
         * commands, and TimeZone and DSTOffset attributes, and shall expose the local time through the LocalTime
         * attribute.
         */
        public boolean timeZone;
        /**
         * 
         * Allows a node to use NTP/SNTP for time synchronization.
         */
        public boolean ntpClient;
        /**
         * 
         * Allows a Node to host an NTP server for the network so that other Nodes can achieve a high accuracy time
         * synchronization within the network. See Section 11.17.15, “Acting as an NTP Server”.
         */
        public boolean ntpServer;
        /**
         * 
         * This node also supports a time synchronization client and can connect to and read time from other nodes.
         */
        public boolean timeSyncClient;

        public FeatureMap(boolean timeZone, boolean ntpClient, boolean ntpServer, boolean timeSyncClient) {
            this.timeZone = timeZone;
            this.ntpClient = ntpClient;
            this.ntpServer = ntpServer;
            this.timeSyncClient = timeSyncClient;
        }
    }

    public TimeSynchronizationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 56, "TimeSynchronization");
    }

    protected TimeSynchronizationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command may be issued by Administrator to set the time. If the Commissioner does not have a valid time
     * source, it may send a Granularity of NoTimeGranularity.
     * Upon receipt of this command, the node may update its UTCTime attribute to match the time specified in the
     * command, if the stated Granularity and TimeSource are acceptable. The node shall update its UTCTime attribute if
     * its current Granularity is NoTimeGranularity.
     * If the time is updated, the node shall also update its Granularity attribute based on the granularity specified
     * in the command and the expected clock drift of the node. This SHOULD normally be one level lower than the stated
     * command Granularity. It shall also update its TimeSource attribute to Admin. It shall also update its Last Known
     * Good UTC Time as defined in Section 3.5.6.1, “Last Known Good UTC Time”.
     * If the node updates its UTCTime attribute, it shall accept the command with a status code of SUCCESS. If it opts
     * to not update its time, it shall fail the command with a cluster specific Status Code of TimeNotAccepted.
     */
    public static ClusterCommand setUtcTime(BigInteger utcTime, GranularityEnum granularity,
            TimeSourceEnum timeSource) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (utcTime != null) {
            map.put("utcTime", utcTime);
        }
        if (granularity != null) {
            map.put("granularity", granularity);
        }
        if (timeSource != null) {
            map.put("timeSource", timeSource);
        }
        return new ClusterCommand("setUtcTime", map);
    }

    /**
     * This command shall set the TrustedTimeSource attribute. Upon receipt of this command:
     * • If the TrustedTimeSource field in the command is null, the node shall set the TrustedTimeSource attribute to
     * null and shall generate a MissingTrustedTimeSource event.
     * • Otherwise, the node shall set the TrustedTimeSource attribute to a struct which has NodeID and Endpoint fields
     * matching those in the TrustedTimeSource field and has its FabricIndex field set to the command’s accessing fabric
     * index.
     */
    public static ClusterCommand setTrustedTimeSource(FabricScopedTrustedTimeSourceStruct trustedTimeSource,
            Integer fabricIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (trustedTimeSource != null) {
            map.put("trustedTimeSource", trustedTimeSource);
        }
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        return new ClusterCommand("setTrustedTimeSource", map);
    }

    /**
     * This command is used to set the time zone of the node.
     * If the given list is larger than the TimeZoneListMaxSize, the node shall respond with RESOURCE_EXHAUSTED and the
     * TimeZone attribute shall NOT be updated.
     * If the given list does not conform to the list requirements in TimeZone attribute the node shall respond with a
     * CONSTRAINT_ERROR and the TimeZone attribute shall NOT be updated.
     * If there are no errors in the list, the TimeZone field shall be copied to the TimeZone attribute. A
     * TimeZoneStatus event shall be generated with the new time zone information.
     * If the node supports a time zone database and it has information available for the time zone that will be
     * applied, it may set its DSTOffset attribute, otherwise the DSTOffset attribute shall be set to an empty list. A
     * DSTTableEmpty event shall be generated if the DSTOffset attribute is empty. A DSTStatus event shall be generated
     * if the node was previously applying a DST offset.
     */
    public static ClusterCommand setTimeZone(List<TimeZoneStruct> timeZone) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (timeZone != null) {
            map.put("timeZone", timeZone);
        }
        return new ClusterCommand("setTimeZone", map);
    }

    /**
     * This command is used to set the DST offsets for a node.
     * • If the length of DSTOffset is larger than DSTOffsetListMaxSize, the node shall respond with RESOURCE_EXHAUSTED.
     * • Else if the list entries do not conform to the list requirements for DSTOffset attribute, the node shall
     * respond with CONSTRAINT_ERROR.
     * If there are no errors in the list, the DSTOffset field shall be copied to the DSTOffset attribute.
     * If the DSTOffset attribute change causes a corresponding change to the DST state, a DSTStatus event shall be
     * generated. If the list is empty, the node shall generate a DSTTableEmpty event.
     */
    public static ClusterCommand setDstOffset(List<DSTOffsetStruct> dstOffset) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (dstOffset != null) {
            map.put("dstOffset", dstOffset);
        }
        return new ClusterCommand("setDstOffset", map);
    }

    /**
     * This command is used to set the DefaultNTP attribute. If the DefaultNTP Address field does not conform to the
     * requirements in the DefaultNTP attribute description, the command shall fail with a status code of
     * INVALID_COMMAND. If the node does not support DNS resolution (as specified in SupportsDNSResolve) and the
     * provided Address is a domain name, the command shall fail with a status code of INVALID_COMMAND. Otherwise, the
     * node shall set the DefaultNTP attribute to match the DefaultNTP provided in this command.
     */
    public static ClusterCommand setDefaultNtp(String defaultNtp) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (defaultNtp != null) {
            map.put("defaultNtp", defaultNtp);
        }
        return new ClusterCommand("setDefaultNtp", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "utcTime : " + utcTime + "\n";
        str += "granularity : " + granularity + "\n";
        str += "timeSource : " + timeSource + "\n";
        str += "trustedTimeSource : " + trustedTimeSource + "\n";
        str += "defaultNtp : " + defaultNtp + "\n";
        str += "timeZone : " + timeZone + "\n";
        str += "dstOffset : " + dstOffset + "\n";
        str += "localTime : " + localTime + "\n";
        str += "timeZoneDatabase : " + timeZoneDatabase + "\n";
        str += "ntpServerAvailable : " + ntpServerAvailable + "\n";
        str += "timeZoneListMaxSize : " + timeZoneListMaxSize + "\n";
        str += "dstOffsetListMaxSize : " + dstOffsetListMaxSize + "\n";
        str += "supportsDnsResolve : " + supportsDnsResolve + "\n";
        return str;
    }
}
