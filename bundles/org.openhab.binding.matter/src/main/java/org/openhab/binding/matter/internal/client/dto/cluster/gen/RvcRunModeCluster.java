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
 * RvcRunMode
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RvcRunModeCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0054;
    public static final String CLUSTER_NAME = "RvcRunMode";
    public static final String CLUSTER_PREFIX = "rvcRunMode";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_SUPPORTED_MODES = "supportedModes";
    public static final String ATTRIBUTE_CURRENT_MODE = "currentMode";
    public static final String ATTRIBUTE_START_UP_MODE = "startUpMode";
    public static final String ATTRIBUTE_ON_MODE = "onMode";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall contain the list of supported modes that may be selected for the CurrentMode attribute. Each
     * item in this list represents a unique mode as indicated by the Mode field of the ModeOptionStruct.
     * Each entry in this list shall have a unique value for the Mode field. Each entry in this list shall have a unique
     * value for the Label field.
     */
    public List<ModeOptionStruct> supportedModes; // 0 list R V
    /**
     * Indicates the current mode of the server.
     * The value of this field shall match the Mode field of one of the entries in the SupportedModes attribute.
     * The value of this attribute may change at any time via an out-of-band interaction outside of the server, such as
     * interactions with a user interface, via internal mode changes due to autonomously progressing through a sequence
     * of operations, on system time-outs or idle delays, or via interactions coming from a fabric other than the one
     * which last executed a ChangeToMode.
     */
    public Integer currentMode; // 1 uint8 R V
    /**
     * Indicates the desired startup mode for the server when it is supplied with power.
     * If this attribute is not null, the CurrentMode attribute shall be set to the StartUpMode value, when the server
     * is powered up, except in the case when the OnMode attribute overrides the StartUpMode attribute (see
     * OnModeWithPowerUp).
     * This behavior does not apply to reboots associated with OTA. After an OTA restart, the CurrentMode attribute
     * shall return to its value prior to the restart.
     * The value of this field shall match the Mode field of one of the entries in the SupportedModes attribute.
     * If this attribute is not implemented, or is set to the null value, it shall have no effect.
     */
    public Integer startUpMode; // 2 uint8 RW VO
    /**
     * Indicates whether the value of CurrentMode depends on the state of the On/Off cluster on the same endpoint. If
     * this attribute is not present or is set to null, there is no dependency, otherwise the CurrentMode attribute
     * shall depend on the OnOff attribute in the On/Off cluster
     * The value of this field shall match the Mode field of one of the entries in the SupportedModes attribute.
     */
    public Integer onMode; // 3 uint8 RW VO

    // Structs
    /**
     * A Mode Tag is meant to be interpreted by the client for the purpose the cluster serves.
     */
    public static class ModeTagStruct {
        /**
         * If the MfgCode field exists, the Value field shall be in the manufacturer-specific value range (see Section
         * 1.10.8, “Mode Namespace”).
         * This field shall indicate the manufacturer’s VendorID and it shall determine the meaning of the Value field.
         * The same manufacturer code and mode tag value in separate cluster instances are part of the same namespace
         * and have the same meaning. For example: a manufacturer tag meaning &quot;pinch&quot; can be used both in a
         * cluster whose purpose is to choose the amount of sugar, or in a cluster whose purpose is to choose the amount
         * of salt.
         */
        public Integer mfgCode; // vendor-id
        /**
         * This field shall indicate the mode tag within a mode tag namespace which is either manufacturer specific or
         * standard.
         */
        public ModeTag value; // ModeTag

        public ModeTagStruct(Integer mfgCode, ModeTag value) {
            this.mfgCode = mfgCode;
            this.value = value;
        }
    }

    /**
     * This is a struct representing a possible mode of the server.
     */
    public static class ModeOptionStruct {
        /**
         * This field shall indicate readable text that describes the mode option, so that a client can provide it to
         * the user to indicate what this option means. This field is meant to be readable and understandable by the
         * user.
         */
        public String label; // string
        /**
         * This field is used to identify the mode option.
         */
        public Integer mode; // uint8
        /**
         * This field shall contain a list of tags that are associated with the mode option. This may be used by clients
         * to determine the full or the partial semantics of a certain mode, depending on which tags they understand,
         * using standard definitions and/or manufacturer specific namespace definitions.
         * The standard mode tags are defined in this cluster specification. For the derived cluster instances, if the
         * specification of the derived cluster defines a namespace, the set of standard mode tags also includes the
         * mode tag values from that namespace.
         * Mode tags can help clients look for options that meet certain criteria, render the user interface, use the
         * mode in an automation, or to craft help text their voice-driven interfaces. A mode tag shall be either a
         * standard tag or a manufacturer specific tag, as defined in each ModeTagStruct list entry.
         * A mode option may have more than one mode tag. A mode option may be associated with a mixture of standard and
         * manufacturer specific mode tags. A mode option shall be associated with at least one standard mode tag.
         * A few examples are provided below.
         * • A mode named &quot;100%&quot; can have both the High (manufacturer specific) and Max (standard) mode tag.
         * Clients seeking the mode for either High or Max will find the same mode in this case.
         * • A mode that includes a LowEnergy tag can be displayed by the client using a widget icon that shows a green
         * leaf.
         * • A mode that includes a LowNoise tag may be used by the client when the user wishes for a lower level of
         * audible sound, less likely to disturb the household’s activities.
         * • A mode that includes a LowEnergy tag (standard, defined in this cluster specification) and also a Delicate
         * tag (standard, defined in the namespace of a Laundry Mode derived cluster).
         * • A mode that includes both a generic Quick tag (defined here), and Vacuum and Mop tags, (defined in the RVC
         * Clean cluster that is a derivation of this cluster).
         */
        public List<ModeTagStruct> modeTags; // list

        public ModeOptionStruct(String label, Integer mode, List<ModeTagStruct> modeTags) {
            this.label = label;
            this.mode = mode;
            this.modeTags = modeTags;
        }
    }

    // Enums
    public enum ModeChangeStatus implements MatterEnum {
        STUCK(65, "Stuck"),
        DUST_BIN_MISSING(66, "Dust Bin Missing"),
        DUST_BIN_FULL(67, "Dust Bin Full"),
        WATER_TANK_EMPTY(68, "Water Tank Empty"),
        WATER_TANK_MISSING(69, "Water Tank Missing"),
        WATER_TANK_LID_OPEN(70, "Water Tank Lid Open"),
        MOP_CLEANING_PAD_MISSING(71, "Mop Cleaning Pad Missing"),
        BATTERY_LOW(72, "Battery Low");

        public final Integer value;
        public final String label;

        private ModeChangeStatus(Integer value, String label) {
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

    public enum ModeTag implements MatterEnum {
        AUTO(0, "Auto"),
        QUICK(1, "Quick"),
        QUIET(2, "Quiet"),
        LOW_NOISE(3, "Low Noise"),
        LOW_ENERGY(4, "Low Energy"),
        VACATION(5, "Vacation"),
        MIN(6, "Min"),
        MAX(7, "Max"),
        NIGHT(8, "Night"),
        DAY(9, "Day"),
        IDLE(16384, "Idle"),
        CLEANING(16385, "Cleaning"),
        MAPPING(16386, "Mapping");

        public final Integer value;
        public final String label;

        private ModeTag(Integer value, String label) {
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
         * This feature creates a dependency between an OnOff cluster instance and this cluster instance on the same
         * endpoint. See OnMode for more information.
         */
        public boolean onOff;

        public FeatureMap(boolean onOff) {
            this.onOff = onOff;
        }
    }

    public RvcRunModeCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 84, "RvcRunMode");
    }

    protected RvcRunModeCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to change device modes.
     * On receipt of this command the device shall respond with a ChangeToModeResponse command.
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
        str += "supportedModes : " + supportedModes + "\n";
        str += "currentMode : " + currentMode + "\n";
        str += "startUpMode : " + startUpMode + "\n";
        str += "onMode : " + onMode + "\n";
        return str;
    }
}
