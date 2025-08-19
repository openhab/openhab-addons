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
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ActivatedCarbonFilterMonitoring
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ActivatedCarbonFilterMonitoringCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0072;
    public static final String CLUSTER_NAME = "ActivatedCarbonFilterMonitoring";
    public static final String CLUSTER_PREFIX = "activatedCarbonFilterMonitoring";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CONDITION = "condition";
    public static final String ATTRIBUTE_DEGRADATION_DIRECTION = "degradationDirection";
    public static final String ATTRIBUTE_CHANGE_INDICATION = "changeIndication";
    public static final String ATTRIBUTE_IN_PLACE_INDICATOR = "inPlaceIndicator";
    public static final String ATTRIBUTE_LAST_CHANGED_TIME = "lastChangedTime";
    public static final String ATTRIBUTE_REPLACEMENT_PRODUCT_LIST = "replacementProductList";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current condition of the resource in percent.
     */
    public Integer condition; // 0 percent R V
    /**
     * Indicates the direction of change for the condition of the resource over time, which helps to determine whether a
     * higher or lower condition value is considered optimal.
     */
    public DegradationDirectionEnum degradationDirection; // 1 DegradationDirectionEnum R V
    /**
     * This attribute shall be populated with a value from ChangeIndicationEnum that is indicative of the current
     * requirement to change the resource.
     */
    public ChangeIndicationEnum changeIndication; // 2 ChangeIndicationEnum R V
    /**
     * Indicates whether a resource is currently installed. A value of true shall indicate that a resource is installed.
     * A value of false shall indicate that a resource is not installed.
     */
    public Boolean inPlaceIndicator; // 3 bool R V
    /**
     * This attribute may indicates the time at which the resource has been changed, if supported by the server. The
     * attribute shall be null if it was never set or is unknown.
     */
    public Integer lastChangedTime; // 4 epoch-s RW VO
    /**
     * Indicates the list of supported products that may be used as replacements for the current resource. Each item in
     * this list represents a unique ReplacementProductStruct.
     */
    public List<ReplacementProductStruct> replacementProductList; // 5 list R V

    // Structs
    /**
     * Indicates the product identifier that can be used as a replacement for the resource.
     */
    public static class ReplacementProductStruct {
        public ProductIdentifierTypeEnum productIdentifierType; // ProductIdentifierTypeEnum
        public String productIdentifierValue; // string

        public ReplacementProductStruct(ProductIdentifierTypeEnum productIdentifierType,
                String productIdentifierValue) {
            this.productIdentifierType = productIdentifierType;
            this.productIdentifierValue = productIdentifierValue;
        }
    }

    // Enums
    /**
     * Indicates the direction in which the condition of the resource changes over time.
     */
    public enum DegradationDirectionEnum implements MatterEnum {
        UP(0, "Up"),
        DOWN(1, "Down");

        public final Integer value;
        public final String label;

        private DegradationDirectionEnum(Integer value, String label) {
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

    public enum ChangeIndicationEnum implements MatterEnum {
        OK(0, "Ok"),
        WARNING(1, "Warning"),
        CRITICAL(2, "Critical");

        public final Integer value;
        public final String label;

        private ChangeIndicationEnum(Integer value, String label) {
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
     * Indicate the type of identifier used to describe the product. Devices SHOULD use globally-recognized IDs over OEM
     * specific ones.
     */
    public enum ProductIdentifierTypeEnum implements MatterEnum {
        UPC(0, "Upc"),
        GTIN8(1, "Gtin 8"),
        EAN(2, "Ean"),
        GTIN14(3, "Gtin 14"),
        OEM(4, "Oem");

        public final Integer value;
        public final String label;

        private ProductIdentifierTypeEnum(Integer value, String label) {
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
         * Supports monitoring the condition of the resource in percentage
         */
        public boolean condition;
        /**
         * 
         * Supports warning indication
         */
        public boolean warning;
        /**
         * 
         * Supports specifying the list of replacement products
         */
        public boolean replacementProductList;

        public FeatureMap(boolean condition, boolean warning, boolean replacementProductList) {
            this.condition = condition;
            this.warning = warning;
            this.replacementProductList = replacementProductList;
        }
    }

    public ActivatedCarbonFilterMonitoringCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 114, "ActivatedCarbonFilterMonitoring");
    }

    protected ActivatedCarbonFilterMonitoringCluster(BigInteger nodeId, int endpointId, int clusterId,
            String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, the device shall reset the Condition and ChangeIndicator attributes, indicating full resource
     * availability and readiness for use, as initially configured. Invocation of this command may cause the
     * LastChangedTime to be updated automatically based on the clock of the server, if the server supports setting the
     * attribute.
     */
    public static ClusterCommand resetCondition() {
        return new ClusterCommand("resetCondition");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "condition : " + condition + "\n";
        str += "degradationDirection : " + degradationDirection + "\n";
        str += "changeIndication : " + changeIndication + "\n";
        str += "inPlaceIndicator : " + inPlaceIndicator + "\n";
        str += "lastChangedTime : " + lastChangedTime + "\n";
        str += "replacementProductList : " + replacementProductList + "\n";
        return str;
    }
}
