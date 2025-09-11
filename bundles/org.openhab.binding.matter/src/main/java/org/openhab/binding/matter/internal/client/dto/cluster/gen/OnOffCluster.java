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
 * OnOff
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OnOffCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0006;
    public static final String CLUSTER_NAME = "OnOff";
    public static final String CLUSTER_PREFIX = "onOff";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_ON_OFF = "onOff";
    public static final String ATTRIBUTE_GLOBAL_SCENE_CONTROL = "globalSceneControl";
    public static final String ATTRIBUTE_ON_TIME = "onTime";
    public static final String ATTRIBUTE_OFF_WAIT_TIME = "offWaitTime";
    public static final String ATTRIBUTE_START_UP_ON_OFF = "startUpOnOff";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute indicates whether the device type implemented on the endpoint is turned off or turned on, in these
     * cases the value of the OnOff attribute equals FALSE, or TRUE respectively.
     */
    public Boolean onOff; // 0 bool R V
    /**
     * In order to support the use case where the user gets back the last setting of a set of devices (e.g. level
     * settings for lights), a global scene is introduced which is stored when the devices are turned off and recalled
     * when the devices are turned on. The global scene is defined as the scene that is stored with group identifier 0
     * and scene identifier 0.
     * This attribute is defined in order to prevent a second Off command storing the all-devices-off situation as a
     * global scene, and to prevent a second On command destroying the current settings by going back to the global
     * scene.
     * This attribute shall be set to TRUE after the reception of a command which causes the OnOff attribute to be set
     * to TRUE, such as a standard On command, a MoveToLevel(WithOnOff) command, a RecallScene command or a
     * OnWithRecallGlobalScene command.
     * This attribute is set to FALSE after reception of a OffWithEffect command.
     */
    public Boolean globalSceneControl; // 16384 bool R V
    /**
     * This attribute specifies the length of time (in 1/10ths second) that the On state shall be maintained before
     * automatically transitioning to the Off state when using the OnWithTimedOff command. This attribute can be written
     * at any time, but writing a value only has effect when in the Timed On state. See OnWithTimedOff for more details.
     */
    public Integer onTime; // 16385 uint16 RW VO
    /**
     * This attribute specifies the length of time (in 1/10ths second) that the Off state shall be guarded to prevent
     * another OnWithTimedOff command turning the server back to its On state (e.g., when leaving a room, the lights are
     * turned off but an occupancy sensor detects the leaving person and attempts to turn the lights back on). This
     * attribute can be written at any time, but writing a value only has an effect when in the Timed On state followed
     * by a transition to the Delayed Off state, or in the Delayed Off state. See OnWithTimedOff for more details.
     */
    public Integer offWaitTime; // 16386 uint16 RW VO
    /**
     * This attribute shall define the desired startup behavior of a device when it is supplied with power and this
     * state shall be reflected in the OnOff attribute. If the value is null, the OnOff attribute is set to its previous
     * value. Otherwise, the behavior is defined in the table defining StartUpOnOffEnum.
     * This behavior does not apply to reboots associated with OTA. After an OTA restart, the OnOff attribute shall
     * return to its value prior to the restart.
     */
    public StartUpOnOffEnum startUpOnOff; // 16387 StartUpOnOffEnum RW VM

    // Enums
    public enum StartUpOnOffEnum implements MatterEnum {
        OFF(0, "Off"),
        ON(1, "On"),
        TOGGLE(2, "Toggle");

        public final Integer value;
        public final String label;

        private StartUpOnOffEnum(Integer value, String label) {
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
        DELAYED_ALL_OFF(0, "Delayed All Off"),
        DYING_LIGHT(1, "Dying Light");

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

    public enum DelayedAllOffEffectVariantEnum implements MatterEnum {
        DELAYED_OFF_FAST_FADE(0, "Delayed Off Fast Fade"),
        NO_FADE(1, "No Fade"),
        DELAYED_OFF_SLOW_FADE(2, "Delayed Off Slow Fade");

        public final Integer value;
        public final String label;

        private DelayedAllOffEffectVariantEnum(Integer value, String label) {
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

    public enum DyingLightEffectVariantEnum implements MatterEnum {
        DYING_LIGHT_FADE_OFF(0, "Dying Light Fade Off");

        public final Integer value;
        public final String label;

        private DyingLightEffectVariantEnum(Integer value, String label) {
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
    public static class OnOffControlBitmap {
        public boolean acceptOnlyWhenOn;

        public OnOffControlBitmap(boolean acceptOnlyWhenOn) {
            this.acceptOnlyWhenOn = acceptOnlyWhenOn;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This cluster is used for a lighting application.
         * On receipt of a Level Control cluster command that causes the OnOff attribute to be set to FALSE, the OnTime
         * attribute shall be set to 0.
         * On receipt of a Level Control cluster command that causes the OnOff attribute to be set to TRUE, if the value
         * of the OnTime attribute is equal to 0, the server shall set the OffWaitTime attribute to 0.
         */
        public boolean lighting;
        /**
         * 
         * When this feature is supported, the device exposing this server cluster exhibits &quot;dead front&quot;
         * behavior when the &quot;OnOff&quot; attribute is FALSE (Off). This &quot;dead front&quot; behavior includes:
         * • clusters other than this cluster that are also exposed may respond with failures to Invoke and Write
         * interactions. Such failure responses when in a &quot;dead front&quot; shall be with an INVALID_IN_STATE
         * status code.
         * • clusters other than this cluster may change the values of their attributes to best-effort values, due to
         * the actual values not being defined or available in this state. Device type specifications that require
         * support for the DF feature SHOULD define what these best-effort values are.
         * • Report Transactions shall continue to be generated. Such transactions may include best-effort values as
         * noted above.
         * • Event generation logic for clusters other than this cluster is unchanged (noting possible use of
         * best-effort attribute values as in the preceding bullets).
         * When this feature is supported and the OnOff attribute changes from TRUE to FALSE (e.g. when receiving an Off
         * Command, or due to a manual interaction on the device), it shall start executing this &quot;dead front&quot;
         * behavior.
         * When this feature is supported and the OnOff attribute changes from FALSE to TRUE (e.g. when receiving an On
         * Command, or due to a manual interaction on the device), it shall stop executing this &quot;dead front&quot;
         * behavior.
         * When this feature is supported, and any change of the &quot;dead front&quot; state leads to changes in
         * attributes of other clusters due to the &quot;dead front&quot; feature, these attribute changes shall NOT be
         * skipped or omitted from the usual processing associated with attribute changes. For example, if an attribute
         * changes from value 4 to null on &quot;dead front&quot; behavior due to an Off command being received, this
         * change shall be processed for reporting and subscriptions.
         */
        public boolean deadFrontBehavior;
        /**
         * 
         * When this feature is supported, the Off command shall be supported and the On and Toggle commands shall NOT
         * be supported.
         * This feature is useful for devices which can be turned off via the Off command received by an instance of
         * this cluster but cannot be turned on via commands received by an instance of this cluster due to regulatory
         * requirements.
         */
        public boolean offOnly;

        public FeatureMap(boolean lighting, boolean deadFrontBehavior, boolean offOnly) {
            this.lighting = lighting;
            this.deadFrontBehavior = deadFrontBehavior;
            this.offOnly = offOnly;
        }
    }

    public OnOffCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 6, "OnOff");
    }

    protected OnOffCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand off() {
        return new ClusterCommand("off");
    }

    public static ClusterCommand on() {
        return new ClusterCommand("on");
    }

    public static ClusterCommand toggle() {
        return new ClusterCommand("toggle");
    }

    /**
     * The OffWithEffect command allows devices to be turned off using enhanced ways of fading.
     */
    public static ClusterCommand offWithEffect(EffectIdentifierEnum effectIdentifier, Integer effectVariant) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (effectIdentifier != null) {
            map.put("effectIdentifier", effectIdentifier);
        }
        if (effectVariant != null) {
            map.put("effectVariant", effectVariant);
        }
        return new ClusterCommand("offWithEffect", map);
    }

    /**
     * This command allows the recall of the settings when the device was turned off.
     */
    public static ClusterCommand onWithRecallGlobalScene() {
        return new ClusterCommand("onWithRecallGlobalScene");
    }

    /**
     * This command allows devices to be turned on for a specific duration with a guarded off duration so that SHOULD
     * the device be subsequently turned off, further OnWithTimedOff commands, received during this time, are prevented
     * from turning the devices back on. Further OnWithTimedOff commands received while the server is turned on, will
     * update the period that the device is turned on.
     */
    public static ClusterCommand onWithTimedOff(OnOffControlBitmap onOffControl, Integer onTime, Integer offWaitTime) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (onOffControl != null) {
            map.put("onOffControl", onOffControl);
        }
        if (onTime != null) {
            map.put("onTime", onTime);
        }
        if (offWaitTime != null) {
            map.put("offWaitTime", offWaitTime);
        }
        return new ClusterCommand("onWithTimedOff", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "onOff : " + onOff + "\n";
        str += "globalSceneControl : " + globalSceneControl + "\n";
        str += "onTime : " + onTime + "\n";
        str += "offWaitTime : " + offWaitTime + "\n";
        str += "startUpOnOff : " + startUpOnOff + "\n";
        return str;
    }
}
