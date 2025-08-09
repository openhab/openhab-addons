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
 * ModeSelect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ModeSelectCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0050;
    public static final String CLUSTER_NAME = "ModeSelect";
    public static final String CLUSTER_PREFIX = "modeSelect";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_DESCRIPTION = "description";
    public static final String ATTRIBUTE_STANDARD_NAMESPACE = "standardNamespace";
    public static final String ATTRIBUTE_SUPPORTED_MODES = "supportedModes";
    public static final String ATTRIBUTE_CURRENT_MODE = "currentMode";
    public static final String ATTRIBUTE_START_UP_MODE = "startUpMode";
    public static final String ATTRIBUTE_ON_MODE = "onMode";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute describes the purpose of the server, in readable text.
     * For example, a coffee machine may have a Mode Select cluster for the amount of milk to add, and another Mode
     * Select cluster for the amount of sugar to add. In this case, the first instance can have the description Milk and
     * the second instance can have the description Sugar. This allows the user to tell the purpose of each of the
     * instances.
     */
    public String description; // 0 string R V
    /**
     * This attribute, when not null, shall indicate a single standard namespace for any standard semantic tag value
     * supported in this or any other cluster instance with the same value of this attribute. A null value indicates no
     * standard namespace, and therefore, no standard semantic tags are provided in this cluster instance. Each standard
     * namespace and corresponding values and value meanings shall be defined in another document.
     */
    public Integer standardNamespace; // 1 namespace R V
    /**
     * This attribute is the list of supported modes that may be selected for the CurrentMode attribute. Each item in
     * this list represents a unique mode as indicated by the Mode field of the ModeOptionStruct. Each entry in this
     * list shall have a unique value for the Mode field.
     */
    public List<ModeOptionStruct> supportedModes; // 2 list R V
    /**
     * This attribute represents the current mode of the server.
     * The value of this field must match the Mode field of one of the entries in the SupportedModes attribute.
     */
    public Integer currentMode; // 3 uint8 R V
    /**
     * The StartUpMode attribute value indicates the desired startup mode for the server when it is supplied with power.
     * If this attribute is not null, the CurrentMode attribute shall be set to the StartUpMode value, when the server
     * is powered up, except in the case when the OnMode attribute overrides the StartUpMode attribute (see
     * OnModeWithPowerUp).
     * This behavior does not apply to reboots associated with OTA. After an OTA restart, the CurrentMode attribute
     * shall return to its value prior to the restart.
     * The value of this field shall match the Mode field of one of the entries in the SupportedModes attribute.
     * If this attribute is not implemented, or is set to the null value, it shall have no effect.
     */
    public Integer startUpMode; // 4 uint8 RW VO
    /**
     * Indicates the value of CurrentMode that depends on the state of the On/Off cluster on the same endpoint. If this
     * attribute is not present or is set to null, it shall NOT have an effect, otherwise the CurrentMode attribute
     * shall depend on the OnOff attribute of the On/Off cluster
     * The value of this field shall match the Mode field of one of the entries in the SupportedModes attribute.
     */
    public Integer onMode; // 5 uint8 RW VO

    // Structs
    /**
     * A Semantic Tag is meant to be interpreted by the client for the purpose the cluster serves.
     */
    public static class SemanticTagStruct {
        /**
         * This field shall indicate a manufacturer code (Vendor ID), and the Value field shall indicate a semantic tag
         * defined by the manufacturer. Each manufacturer code supports a single namespace of values. The same
         * manufacturer code and semantic tag value in separate cluster instances are part of the same namespace and
         * have the same meaning. For example: a manufacturer tag meaning &quot;pinch&quot;, has the same meaning in a
         * cluster whose purpose is to choose the amount of sugar, or amount of salt.
         */
        public Integer mfgCode; // vendor-id
        /**
         * This field shall indicate the semantic tag within a semantic tag namespace which is either manufacturer
         * specific or standard. For semantic tags in a standard namespace, see Standard Namespace.
         */
        public Integer value; // uint16

        public SemanticTagStruct(Integer mfgCode, Integer value) {
            this.mfgCode = mfgCode;
            this.value = value;
        }
    }

    /**
     * This is a struct representing a possible mode of the server.
     */
    public static class ModeOptionStruct {
        /**
         * This field is readable text that describes the mode option that can be used by a client to indicate to the
         * user what this option means. This field is meant to be readable and understandable by the user.
         */
        public String label; // string
        /**
         * The Mode field is used to identify the mode option. The value shall be unique for every item in the
         * SupportedModes attribute.
         */
        public Integer mode; // uint8
        /**
         * This field is a list of semantic tags that map to the mode option. This may be used by clients to determine
         * the meaning of the mode option as defined in a standard or manufacturer specific namespace. Semantic tags can
         * help clients look for options that meet certain criteria. A semantic tag shall be either a standard tag or
         * manufacturer specific tag as defined in each SemanticTagStruct list entry.
         * A mode option may have more than one semantic tag. A mode option may be mapped to a mixture of standard and
         * manufacturer specific semantic tags.
         * All standard semantic tags are from a single namespace indicated by the StandardNamespace attribute.
         * For example: A mode labeled &quot;100%&quot; can have both the HIGH (MS) and MAX (standard) semantic tag.
         * Clients seeking the option for either HIGH or MAX will find the same option in this case.
         */
        public List<SemanticTagStruct> semanticTags; // list

        public ModeOptionStruct(String label, Integer mode, List<SemanticTagStruct> semanticTags) {
            this.label = label;
            this.mode = mode;
            this.semanticTags = semanticTags;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This feature creates a dependency between an OnOff cluster instance and this cluster instance on the same
         * endpoint. See OnMode for more information.
         */
        public boolean onOff;

        public FeatureMap(boolean onOff) {
            this.onOff = onOff;
        }
    }

    public ModeSelectCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 80, "ModeSelect");
    }

    protected ModeSelectCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * On receipt of this command, if the NewMode field indicates a valid mode transition within the supported list, the
     * server shall set the CurrentMode attribute to the NewMode value, otherwise, the server shall respond with an
     * INVALID_COMMAND status response.
     */
    public static ClusterCommand changeToMode(Integer newMode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (newMode != null) {
            map.put("newMode", newMode);
        }
        return new ClusterCommand("changeToMode", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "description : " + description + "\n";
        str += "standardNamespace : " + standardNamespace + "\n";
        str += "supportedModes : " + supportedModes + "\n";
        str += "currentMode : " + currentMode + "\n";
        str += "startUpMode : " + startUpMode + "\n";
        str += "onMode : " + onMode + "\n";
        return str;
    }
}
