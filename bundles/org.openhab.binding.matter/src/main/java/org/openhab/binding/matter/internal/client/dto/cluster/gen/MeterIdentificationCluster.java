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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;

/**
 * MeterIdentification
 *
 * @author Dan Cunningham - Initial contribution
 */
public class MeterIdentificationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0B06;
    public static final String CLUSTER_NAME = "MeterIdentification";
    public static final String CLUSTER_PREFIX = "meterIdentification";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_METER_TYPE = "meterType";
    public static final String ATTRIBUTE_POINT_OF_DELIVERY = "pointOfDelivery";
    public static final String ATTRIBUTE_METER_SERIAL_NUMBER = "meterSerialNumber";
    public static final String ATTRIBUTE_PROTOCOL_VERSION = "protocolVersion";
    public static final String ATTRIBUTE_POWER_THRESHOLD = "powerThreshold";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the Meter type features, decided by manufacturer. If the type is unavailable, this attribute shall be
     * null.
     */
    public MeterTypeEnum meterType; // 0 MeterTypeEnum R V
    /**
     * Indicates the unique identification of the connection point for the premises for billing purposes. If the point
     * of delivery is unavailable, this attribute shall be null.
     */
    public String pointOfDelivery; // 1 string R V
    /**
     * Indicates the serial number of the meter. If the serial number is unavailable, this attribute shall be null.
     */
    public String meterSerialNumber; // 2 string R V
    /**
     * Indicates the underlying protocol version to express local market features. If the protocol version is
     * unavailable, this attribute shall be null.
     */
    public String protocolVersion; // 3 string R V
    public PowerThresholdStruct powerThreshold; // 4 PowerThresholdStruct R V

    // Enums
    public enum MeterTypeEnum implements MatterEnum {
        UTILITY(0, "Utility"),
        PRIVATE(1, "Private"),
        GENERIC(2, "Generic");

        private final Integer value;
        private final String label;

        private MeterTypeEnum(Integer value, String label) {
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
         * Supports information about power threshold
         */
        public boolean powerThreshold;

        public FeatureMap(boolean powerThreshold) {
            this.powerThreshold = powerThreshold;
        }
    }

    public MeterIdentificationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 2822, "MeterIdentification");
    }

    protected MeterIdentificationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "meterType : " + meterType + "\n";
        str += "pointOfDelivery : " + pointOfDelivery + "\n";
        str += "meterSerialNumber : " + meterSerialNumber + "\n";
        str += "protocolVersion : " + protocolVersion + "\n";
        str += "powerThreshold : " + powerThreshold + "\n";
        return str;
    }
}
