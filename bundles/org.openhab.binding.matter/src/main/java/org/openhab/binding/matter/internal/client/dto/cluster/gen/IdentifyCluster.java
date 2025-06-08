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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * Identify
 *
 * @author Dan Cunningham - Initial contribution
 */
public class IdentifyCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0003;
    public static final String CLUSTER_NAME = "Identify";
    public static final String CLUSTER_PREFIX = "identify";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_IDENTIFY_TIME = "identifyTime";
    public static final String ATTRIBUTE_IDENTIFY_TYPE = "identifyType";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the remaining length of time, in seconds, that the endpoint will continue to identify itself.
     * If this attribute is set to a value other than 0 then the device shall enter its identification state, in order
     * to indicate to an observer which of several nodes and/or endpoints it is. It is recommended that this state
     * consists of flashing a light with a period of 0.5 seconds. The IdentifyTime attribute shall be decremented every
     * second while in this state.
     * If this attribute reaches or is set to the value 0 then the device shall terminate its identification state.
     */
    public Integer identifyTime; // 0 uint16 RW VO
    /**
     * Indicates how the identification state is presented to the user.
     * This attribute shall contain one of the values defined in IdentifyTypeEnum. The value None shall NOT be used if
     * the device is capable of presenting its identification state using one of the other methods defined in
     * IdentifyTypeEnum.
     */
    public IdentifyTypeEnum identifyType; // 1 IdentifyTypeEnum R V

    // Enums
    public enum IdentifyTypeEnum implements MatterEnum {
        NONE(0, "None"),
        LIGHT_OUTPUT(1, "Light Output"),
        VISIBLE_INDICATOR(2, "Visible Indicator"),
        AUDIBLE_BEEP(3, "Audible Beep"),
        DISPLAY(4, "Display"),
        ACTUATOR(5, "Actuator");

        public final Integer value;
        public final String label;

        private IdentifyTypeEnum(Integer value, String label) {
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

    public enum EffectIdentifierEnum implements MatterEnum {
        BLINK(0, "Blink"),
        BREATHE(1, "Breathe"),
        OKAY(2, "Okay"),
        CHANNEL_CHANGE(11, "Channel Change"),
        FINISH_EFFECT(254, "Finish Effect"),
        STOP_EFFECT(255, "Stop Effect");

        public final Integer value;
        public final String label;

        private EffectIdentifierEnum(Integer value, String label) {
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

    public enum EffectVariantEnum implements MatterEnum {
        DEFAULT(0, "Default");

        public final Integer value;
        public final String label;

        private EffectVariantEnum(Integer value, String label) {
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

    public IdentifyCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 3, "Identify");
    }

    protected IdentifyCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command starts or stops the receiving device identifying itself.
     */
    public static ClusterCommand identify(Integer identifyTime) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (identifyTime != null) {
            map.put("identifyTime", identifyTime);
        }
        return new ClusterCommand("identify", map);
    }

    /**
     * This command allows the support of feedback to the user, such as a certain light effect. It is used to allow an
     * implementation to provide visual feedback to the user under certain circumstances such as a color light turning
     * green when it has successfully connected to a network. The use of this command and the effects themselves are
     * entirely up to the implementer to use whenever a visual feedback is useful but it is not the same as and does not
     * replace the identify mechanism used during commissioning.
     */
    public static ClusterCommand triggerEffect(EffectIdentifierEnum effectIdentifier, EffectVariantEnum effectVariant) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (effectIdentifier != null) {
            map.put("effectIdentifier", effectIdentifier);
        }
        if (effectVariant != null) {
            map.put("effectVariant", effectVariant);
        }
        return new ClusterCommand("triggerEffect", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "identifyTime : " + identifyTime + "\n";
        str += "identifyType : " + identifyType + "\n";
        return str;
    }
}
