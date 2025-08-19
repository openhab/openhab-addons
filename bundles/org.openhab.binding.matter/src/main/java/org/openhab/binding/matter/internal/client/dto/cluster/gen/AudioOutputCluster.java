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
 * AudioOutput
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AudioOutputCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x050B;
    public static final String CLUSTER_NAME = "AudioOutput";
    public static final String CLUSTER_PREFIX = "audioOutput";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_OUTPUT_LIST = "outputList";
    public static final String ATTRIBUTE_CURRENT_OUTPUT = "currentOutput";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute provides the list of outputs supported by the device.
     */
    public List<OutputInfoStruct> outputList; // 0 list R V
    /**
     * This attribute contains the value of the index field of the currently selected OutputInfoStruct.
     */
    public Integer currentOutput; // 1 uint8 R V

    // Structs
    /**
     * This contains information about an output.
     */
    public static class OutputInfoStruct {
        /**
         * This field shall indicate the unique index into the list of outputs.
         */
        public Integer index; // uint8
        /**
         * This field shall indicate the type of output.
         */
        public OutputTypeEnum outputType; // OutputTypeEnum
        /**
         * The device defined and user editable output name, such as “Soundbar”, “Speakers”. This field may be blank,
         * but SHOULD be provided when known.
         */
        public String name; // string

        public OutputInfoStruct(Integer index, OutputTypeEnum outputType, String name) {
            this.index = index;
            this.outputType = outputType;
            this.name = name;
        }
    }

    // Enums
    /**
     * The type of output, expressed as an enum, with the following values:
     */
    public enum OutputTypeEnum implements MatterEnum {
        HDMI(0, "Hdmi"),
        BT(1, "Bt"),
        OPTICAL(2, "Optical"),
        HEADPHONE(3, "Headphone"),
        INTERNAL(4, "Internal"),
        OTHER(5, "Other");

        public final Integer value;
        public final String label;

        private OutputTypeEnum(Integer value, String label) {
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
         * Supports updates to output names
         */
        public boolean nameUpdates;

        public FeatureMap(boolean nameUpdates) {
            this.nameUpdates = nameUpdates;
        }
    }

    public AudioOutputCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1291, "AudioOutput");
    }

    protected AudioOutputCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall change the output on the device to the output at a specific index in the Output List.
     * Note that when the current output is set to an output of type HDMI, adjustments to volume via a Speaker endpoint
     * on the same node may cause HDMI volume up/down commands to be sent to the given HDMI output.
     */
    public static ClusterCommand selectOutput(Integer index) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (index != null) {
            map.put("index", index);
        }
        return new ClusterCommand("selectOutput", map);
    }

    /**
     * Upon receipt, this shall rename the output at a specific index in the Output List.
     * Updates to the output name shall appear in the device’s settings menus. Name updates may automatically be sent to
     * the actual device to which the output connects.
     */
    public static ClusterCommand renameOutput(Integer index, String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (index != null) {
            map.put("index", index);
        }
        if (name != null) {
            map.put("name", name);
        }
        return new ClusterCommand("renameOutput", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "outputList : " + outputList + "\n";
        str += "currentOutput : " + currentOutput + "\n";
        return str;
    }
}
