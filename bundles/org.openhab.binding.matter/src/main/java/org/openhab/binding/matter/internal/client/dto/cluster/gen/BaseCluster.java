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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.Gson;

/**
 * undefined
 *
 * @author Dan Cunningham - Initial contribution
 */

public class BaseCluster {

    protected static final Gson GSON = new Gson();
    public BigInteger nodeId;
    public int endpointId;
    public int id;
    public String name;

    public interface MatterEnum {
        Integer getValue();

        String getLabel();

        public static <E extends MatterEnum> E fromValue(Class<E> enumClass, int value) {
            E[] constants = enumClass.getEnumConstants();
            if (constants != null) {
                for (E enumConstant : constants) {
                    if (enumConstant != null) {
                        if (enumConstant.getValue().equals(value)) {
                            return enumConstant;
                        }
                    }
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    public BaseCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        this.nodeId = nodeId;
        this.endpointId = endpointId;
        this.id = clusterId;
        this.name = clusterName;
    }

    public static class OctetString {
        public byte[] value;

        public OctetString(byte[] value) {
            this.value = value;
        }

        public OctetString(String hexString) {
            int length = hexString.length();
            value = new byte[length / 2];
            for (int i = 0; i < length; i += 2) {
                value[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i + 1), 16));
            }
        }

        public @NonNull String toHexString() {
            StringBuilder hexString = new StringBuilder();
            for (byte b : value) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }

        public @NonNull String toString() {
            return toHexString();
        }
    }

    // Structs
    public static class AtomicAttributeStatusStruct {
        public Integer attributeId; // attrib-id
        public Integer statusCode; // status

        public AtomicAttributeStatusStruct(Integer attributeId, Integer statusCode) {
            this.attributeId = attributeId;
            this.statusCode = statusCode;
        }
    }

    public static class MeasurementAccuracyRangeStruct {
        public BigInteger rangeMin; // int64
        public BigInteger rangeMax; // int64
        public Integer percentMax; // percent100ths
        public Integer percentMin; // percent100ths
        public Integer percentTypical; // percent100ths
        public BigInteger fixedMax; // uint64
        public BigInteger fixedMin; // uint64
        public BigInteger fixedTypical; // uint64

        public MeasurementAccuracyRangeStruct(BigInteger rangeMin, BigInteger rangeMax, Integer percentMax,
                Integer percentMin, Integer percentTypical, BigInteger fixedMax, BigInteger fixedMin,
                BigInteger fixedTypical) {
            this.rangeMin = rangeMin;
            this.rangeMax = rangeMax;
            this.percentMax = percentMax;
            this.percentMin = percentMin;
            this.percentTypical = percentTypical;
            this.fixedMax = fixedMax;
            this.fixedMin = fixedMin;
            this.fixedTypical = fixedTypical;
        }
    }

    public static class MeasurementAccuracyStruct {
        public MeasurementTypeEnum measurementType; // MeasurementTypeEnum
        public Boolean measured; // bool
        public BigInteger minMeasuredValue; // int64
        public BigInteger maxMeasuredValue; // int64
        public List<MeasurementAccuracyRangeStruct> accuracyRanges; // list

        public MeasurementAccuracyStruct(MeasurementTypeEnum measurementType, Boolean measured,
                BigInteger minMeasuredValue, BigInteger maxMeasuredValue,
                List<MeasurementAccuracyRangeStruct> accuracyRanges) {
            this.measurementType = measurementType;
            this.measured = measured;
            this.minMeasuredValue = minMeasuredValue;
            this.maxMeasuredValue = maxMeasuredValue;
            this.accuracyRanges = accuracyRanges;
        }
    }

    public static class Date {
        public Integer year; // uint8
        public Integer month; // uint8
        public Integer day; // uint8
        public Integer dayOfWeek; // uint8

        public Date(Integer year, Integer month, Integer day, Integer dayOfWeek) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.dayOfWeek = dayOfWeek;
        }
    }

    public static class Locationdesc {
        public String locationName; // string
        public Integer floorNumber; // int16
        public Integer areaType; // tag

        public Locationdesc(String locationName, Integer floorNumber, Integer areaType) {
            this.locationName = locationName;
            this.floorNumber = floorNumber;
            this.areaType = areaType;
        }
    }

    public static class Semtag {
        public Integer mfgCode; // vendor-id
        public Integer namespaceId; // namespace
        public Integer tag; // tag
        public String label; // string

        public Semtag(Integer mfgCode, Integer namespaceId, Integer tag, String label) {
            this.mfgCode = mfgCode;
            this.namespaceId = namespaceId;
            this.tag = tag;
            this.label = label;
        }
    }

    public static class Tod {
        public Integer hours; // uint8
        public Integer minutes; // uint8
        public Integer seconds; // uint8
        public Integer hundredths; // uint8

        public Tod(Integer hours, Integer minutes, Integer seconds, Integer hundredths) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
            this.hundredths = hundredths;
        }
    }

    // Enums
    public enum AtomicRequestTypeEnum implements MatterEnum {
        BEGIN_WRITE(0, "BeginWrite"),
        COMMIT_WRITE(1, "CommitWrite"),
        ROLLBACK_WRITE(2, "RollbackWrite");

        public final Integer value;
        public final String label;

        private AtomicRequestTypeEnum(Integer value, String label) {
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

    public enum MeasurementTypeEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        VOLTAGE(1, "Voltage"),
        ACTIVE_CURRENT(2, "ActiveCurrent"),
        REACTIVE_CURRENT(3, "ReactiveCurrent"),
        APPARENT_CURRENT(4, "ApparentCurrent"),
        ACTIVE_POWER(5, "ActivePower"),
        REACTIVE_POWER(6, "ReactivePower"),
        APPARENT_POWER(7, "ApparentPower"),
        RMS_VOLTAGE(8, "RmsVoltage"),
        RMS_CURRENT(9, "RmsCurrent"),
        RMS_POWER(10, "RmsPower"),
        FREQUENCY(11, "Frequency"),
        POWER_FACTOR(12, "PowerFactor"),
        NEUTRAL_CURRENT(13, "NeutralCurrent"),
        ELECTRICAL_ENERGY(14, "ElectricalEnergy");

        public final Integer value;
        public final String label;

        private MeasurementTypeEnum(Integer value, String label) {
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

    public enum SoftwareVersionCertificationStatusEnum implements MatterEnum {
        DEV_TEST(0, "DevTest"),
        PROVISIONAL(1, "Provisional"),
        CERTIFIED(2, "Certified"),
        REVOKED(3, "Revoked");

        public final Integer value;
        public final String label;

        private SoftwareVersionCertificationStatusEnum(Integer value, String label) {
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

    public enum Namespace implements MatterEnum {
        CLOSURE(1, "Closure"),
        COMPASS_DIRECTION(2, "CompassDirection"),
        COMPASS_LOCATION(3, "CompassLocation"),
        DIRECTION(4, "Direction"),
        LEVEL(5, "Level"),
        LOCATION(6, "Location"),
        NUMBER(7, "Number"),
        POSITION(8, "Position"),
        ELECTRICAL_MEASUREMENT(10, "ElectricalMeasurement"),
        LAUNDRY(14, "Laundry"),
        POWER_SOURCE(15, "PowerSource"),
        AREA_NAMESPACE(16, "AreaNamespace"),
        LANDMARK_NAMESPACE(17, "LandmarkNamespace"),
        RELATIVE_POSITION(18, "RelativePosition"),
        REFRIGERATOR(65, "Refrigerator"),
        ROOM_AIR_CONDITIONER(66, "RoomAirConditioner"),
        SWITCHES(67, "Switches");

        public final Integer value;
        public final String label;

        private Namespace(Integer value, String label) {
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

    public enum Priority implements MatterEnum {
        DEBUG(0, "Debug"),
        INFO(1, "Info"),
        CRITICAL(2, "Critical");

        public final Integer value;
        public final String label;

        private Priority(Integer value, String label) {
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

    public enum Status implements MatterEnum {
        SUCCESS(0, "Success"),
        FAILURE(1, "Failure"),
        INVALID_SUBSCRIPTION(125, "InvalidSubscription"),
        UNSUPPORTED_ACCESS(126, "UnsupportedAccess"),
        UNSUPPORTED_ENDPOINT(127, "UnsupportedEndpoint"),
        INVALID_ACTION(128, "InvalidAction"),
        UNSUPPORTED_COMMAND(129, "UnsupportedCommand"),
        INVALID_COMMAND(133, "InvalidCommand"),
        UNSUPPORTED_ATTRIBUTE(134, "UnsupportedAttribute"),
        CONSTRAINT_ERROR(135, "ConstraintError"),
        UNSUPPORTED_WRITE(136, "UnsupportedWrite"),
        RESOURCE_EXHAUSTED(137, "ResourceExhausted"),
        NOT_FOUND(139, "NotFound"),
        UNREPORTABLE_ATTRIBUTE(140, "UnreportableAttribute"),
        INVALID_DATA_TYPE(141, "InvalidDataType"),
        UNSUPPORTED_READ(143, "UnsupportedRead"),
        DATA_VERSION_MISMATCH(146, "DataVersionMismatch"),
        TIMEOUT(148, "Timeout"),
        UNSUPPORTED_NODE(155, "UnsupportedNode"),
        BUSY(156, "Busy"),
        ACCESS_RESTRICTED(157, "AccessRestricted"),
        UNSUPPORTED_CLUSTER(195, "UnsupportedCluster"),
        NO_UPSTREAM_SUBSCRIPTION(197, "NoUpstreamSubscription"),
        NEEDS_TIMED_INTERACTION(198, "NeedsTimedInteraction"),
        UNSUPPORTED_EVENT(199, "UnsupportedEvent"),
        PATHS_EXHAUSTED(200, "PathsExhausted"),
        TIMED_REQUEST_MISMATCH(201, "TimedRequestMismatch"),
        FAILSAFE_REQUIRED(202, "FailsafeRequired"),
        INVALID_IN_STATE(203, "InvalidInState"),
        NO_COMMAND_RESPONSE(204, "NoCommandResponse"),
        TERMS_AND_CONDITIONS_CHANGED(205, "TermsAndConditionsChanged"),
        MAINTENANCE_REQUIRED(206, "MaintenanceRequired");

        public final Integer value;
        public final String label;

        private Status(Integer value, String label) {
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
    public static class WildcardPathFlagsBitmap {
        public boolean wildcardSkipRootNode;
        public boolean wildcardSkipGlobalAttributes;
        public boolean wildcardSkipAttributeList;
        public boolean reserved;
        public boolean wildcardSkipCommandLists;
        public boolean wildcardSkipCustomElements;
        public boolean wildcardSkipFixedAttributes;
        public boolean wildcardSkipChangesOmittedAttributes;
        public boolean wildcardSkipDiagnosticsClusters;

        public WildcardPathFlagsBitmap(boolean wildcardSkipRootNode, boolean wildcardSkipGlobalAttributes,
                boolean wildcardSkipAttributeList, boolean reserved, boolean wildcardSkipCommandLists,
                boolean wildcardSkipCustomElements, boolean wildcardSkipFixedAttributes,
                boolean wildcardSkipChangesOmittedAttributes, boolean wildcardSkipDiagnosticsClusters) {
            this.wildcardSkipRootNode = wildcardSkipRootNode;
            this.wildcardSkipGlobalAttributes = wildcardSkipGlobalAttributes;
            this.wildcardSkipAttributeList = wildcardSkipAttributeList;
            this.reserved = reserved;
            this.wildcardSkipCommandLists = wildcardSkipCommandLists;
            this.wildcardSkipCustomElements = wildcardSkipCustomElements;
            this.wildcardSkipFixedAttributes = wildcardSkipFixedAttributes;
            this.wildcardSkipChangesOmittedAttributes = wildcardSkipChangesOmittedAttributes;
            this.wildcardSkipDiagnosticsClusters = wildcardSkipDiagnosticsClusters;
        }
    }

    public static class FeatureMap {
        public List<Boolean> map;

        public FeatureMap(List<Boolean> map) {
            this.map = map;
        }
    }
}
