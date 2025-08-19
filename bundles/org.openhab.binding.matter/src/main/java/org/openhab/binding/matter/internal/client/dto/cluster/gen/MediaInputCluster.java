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
 * MediaInput
 *
 * @author Dan Cunningham - Initial contribution
 */
public class MediaInputCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0507;
    public static final String CLUSTER_NAME = "MediaInput";
    public static final String CLUSTER_PREFIX = "mediaInput";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_INPUT_LIST = "inputList";
    public static final String ATTRIBUTE_CURRENT_INPUT = "currentInput";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall provide a list of the media inputs supported by the device.
     */
    public List<InputInfoStruct> inputList; // 0 list R V
    /**
     * This attribute shall contain the value of the index field of the currently selected InputInfoStruct.
     */
    public Integer currentInput; // 1 uint8 R V

    // Structs
    /**
     * This contains information about an input.
     */
    public static class InputInfoStruct {
        /**
         * This field shall indicate the unique index into the list of Inputs.
         */
        public Integer index; // uint8
        /**
         * ### This field shall indicate the type of input
         */
        public InputTypeEnum inputType; // InputTypeEnum
        /**
         * This field shall indicate the input name, such as “HDMI 1”. This field may be blank, but SHOULD be provided
         * when known.
         */
        public String name; // string
        /**
         * This field shall indicate the user editable input description, such as “Living room Playstation”. This field
         * may be blank, but SHOULD be provided when known.
         */
        public String description; // string

        public InputInfoStruct(Integer index, InputTypeEnum inputType, String name, String description) {
            this.index = index;
            this.inputType = inputType;
            this.name = name;
            this.description = description;
        }
    }

    // Enums
    public enum InputTypeEnum implements MatterEnum {
        INTERNAL(0, "Internal"),
        AUX(1, "Aux"),
        COAX(2, "Coax"),
        COMPOSITE(3, "Composite"),
        HDMI(4, "Hdmi"),
        INPUT(5, "Input"),
        LINE(6, "Line"),
        OPTICAL(7, "Optical"),
        VIDEO(8, "Video"),
        SCART(9, "Scart"),
        USB(10, "Usb"),
        OTHER(11, "Other");

        public final Integer value;
        public final String label;

        private InputTypeEnum(Integer value, String label) {
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
         * Supports updates to the input names
         */
        public boolean nameUpdates;

        public FeatureMap(boolean nameUpdates) {
            this.nameUpdates = nameUpdates;
        }
    }

    public MediaInputCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1287, "MediaInput");
    }

    protected MediaInputCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this command shall change the media input on the device to the input at a specific index in the
     * Input List.
     */
    public static ClusterCommand selectInput(Integer index) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (index != null) {
            map.put("index", index);
        }
        return new ClusterCommand("selectInput", map);
    }

    /**
     * Upon receipt, this command shall display the active status of the input list on screen.
     */
    public static ClusterCommand showInputStatus() {
        return new ClusterCommand("showInputStatus");
    }

    /**
     * Upon receipt, this command shall hide the input list from the screen.
     */
    public static ClusterCommand hideInputStatus() {
        return new ClusterCommand("hideInputStatus");
    }

    /**
     * Upon receipt, this command shall rename the input at a specific index in the Input List. Updates to the input
     * name shall appear in the device’s settings menus.
     */
    public static ClusterCommand renameInput(Integer index, String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (index != null) {
            map.put("index", index);
        }
        if (name != null) {
            map.put("name", name);
        }
        return new ClusterCommand("renameInput", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "inputList : " + inputList + "\n";
        str += "currentInput : " + currentInput + "\n";
        return str;
    }
}
