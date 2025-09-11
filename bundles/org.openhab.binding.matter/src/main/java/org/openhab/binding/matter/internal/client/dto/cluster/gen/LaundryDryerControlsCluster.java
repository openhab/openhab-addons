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
 * LaundryDryerControls
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LaundryDryerControlsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x004A;
    public static final String CLUSTER_NAME = "LaundryDryerControls";
    public static final String CLUSTER_PREFIX = "laundryDryerControls";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_SUPPORTED_DRYNESS_LEVELS = "supportedDrynessLevels";
    public static final String ATTRIBUTE_SELECTED_DRYNESS_LEVEL = "selectedDrynessLevel";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the list of supported dryness levels available to the appliance in the currently selected mode. The
     * dryness level values are determined by the manufacturer. At least one dryness level value shall be provided in
     * the SupportedDrynessLevels list. The list of dryness levels may change depending on the currently-selected
     * Laundry Dryer mode.
     */
    public List<DrynessLevelEnum> supportedDrynessLevels; // 0 list R V
    /**
     * Indicates the currently-selected dryness level and it shall be the index into the SupportedDrynessLevels list of
     * the selected dryness level.
     * If an attempt is made to write this attribute with a value other than null or a value contained in
     * SupportedDrynessLevels, a CONSTRAINT_ERROR response shall be sent as the response. If an attempt is made to write
     * this attribute while the device is not in a state that supports modifying the dryness level, an INVALID_IN_STATE
     * error shall be sent as the response. A value of null shall indicate that there will be no dryness level setting
     * for the current mode.
     */
    public DrynessLevelEnum selectedDrynessLevel; // 1 DrynessLevelEnum RW VO

    // Enums
    /**
     * This enum provides a representation of the level of dryness that will be used while drying in a selected mode.
     * It is up to the device manufacturer to determine the mapping between the enum values and the corresponding
     * temperature level.
     */
    public enum DrynessLevelEnum implements MatterEnum {
        LOW(0, "Low"),
        NORMAL(1, "Normal"),
        EXTRA(2, "Extra"),
        MAX(3, "Max");

        public final Integer value;
        public final String label;

        private DrynessLevelEnum(Integer value, String label) {
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

    public LaundryDryerControlsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 74, "LaundryDryerControls");
    }

    protected LaundryDryerControlsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "supportedDrynessLevels : " + supportedDrynessLevels + "\n";
        str += "selectedDrynessLevel : " + selectedDrynessLevel + "\n";
        return str;
    }
}
