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

/**
 * LaundryWasherControls
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LaundryWasherControlsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0053;
    public static final String CLUSTER_NAME = "LaundryWasherControls";
    public static final String CLUSTER_PREFIX = "laundryWasherControls";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_SPIN_SPEEDS = "spinSpeeds";
    public static final String ATTRIBUTE_SPIN_SPEED_CURRENT = "spinSpeedCurrent";
    public static final String ATTRIBUTE_NUMBER_OF_RINSES = "numberOfRinses";
    public static final String ATTRIBUTE_SUPPORTED_RINSES = "supportedRinses";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the list of spin speeds available to the appliance in the currently selected mode. The spin speed
     * values are determined by the manufacturer. At least one spin speed value shall be provided in the SpinSpeeds
     * list. The list of spin speeds may change depending on the currently selected Laundry Washer mode. For example,
     * Quick mode might have a completely different list of SpinSpeeds than Delicates mode.
     */
    public List<String> spinSpeeds; // 0 list R V
    /**
     * Indicates the currently selected spin speed. It is the index into the SpinSpeeds list of the selected spin speed,
     * as such, this attribute can be an integer between 0 and the number of entries in SpinSpeeds - 1. If a value is
     * received that is outside of the defined constraints, a CONSTRAINT_ERROR shall be sent as the response. If a value
     * is attempted to be written that doesn’t match a valid index (e.g. an index of 5 when the list has 4 values), a
     * CONSTRAINT_ERROR shall be sent as the response. If null is written to this attribute, there will be no spin speed
     * for the
     * selected cycle. If the value is null, there will be no spin speed on the current mode.
     */
    public Integer spinSpeedCurrent; // 1 uint8 RW VO
    /**
     * Indicates how many times a rinse cycle shall be performed on a device for the current mode of operation. A value
     * of None shall indicate that no rinse cycle will be performed. This value may be set by the client to adjust the
     * number of rinses that are performed for the current mode of operation. If the device is not in a compatible state
     * to accept the provided value, an INVALID_IN_STATE error shall be sent as the response.
     */
    public NumberOfRinsesEnum numberOfRinses; // 2 NumberOfRinsesEnum RW VO
    /**
     * Indicates the amount of rinses allowed for a specific mode. Each entry shall indicate a NumberOfRinsesEnum value
     * that is possible in the selected mode on the device. The value of this attribute may change at runtime based on
     * the currently selected mode. Each entry shall be distinct.
     */
    public List<NumberOfRinsesEnum> supportedRinses; // 3 list R V

    // Enums
    /**
     * The NumberOfRinsesEnum provides a representation of the number of rinses that will be performed for a selected
     * mode. NumberOfRinsesEnum is derived from enum8. It is up to the device manufacturer to determine the mapping
     * between the enum values and the corresponding numbers of rinses.
     */
    public enum NumberOfRinsesEnum implements MatterEnum {
        NONE(0, "None"),
        NORMAL(1, "Normal"),
        EXTRA(2, "Extra"),
        MAX(3, "Max");

        public final Integer value;
        public final String label;

        private NumberOfRinsesEnum(Integer value, String label) {
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
         * This feature indicates multiple spin speeds are supported in at least one supported mode. Note that some
         * modes may not support multiple spin speeds even if this feature is supported.
         */
        public boolean spin;
        /**
         * 
         * This feature indicates multiple rinse cycles are supported in at least one supported mode. Note that some
         * modes may not support selection of the number of rinse cycles even if this feature is supported.
         */
        public boolean rinse;

        public FeatureMap(boolean spin, boolean rinse) {
            this.spin = spin;
            this.rinse = rinse;
        }
    }

    public LaundryWasherControlsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 83, "LaundryWasherControls");
    }

    protected LaundryWasherControlsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "spinSpeeds : " + spinSpeeds + "\n";
        str += "spinSpeedCurrent : " + spinSpeedCurrent + "\n";
        str += "numberOfRinses : " + numberOfRinses + "\n";
        str += "supportedRinses : " + supportedRinses + "\n";
        return str;
    }
}
