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
 * RefrigeratorAlarm
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RefrigeratorAlarmCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0057;
    public static final String CLUSTER_NAME = "RefrigeratorAlarm";
    public static final String CLUSTER_PREFIX = "refrigeratorAlarm";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MASK = "mask";
    public static final String ATTRIBUTE_LATCH = "latch";
    public static final String ATTRIBUTE_STATE = "state";
    public static final String ATTRIBUTE_SUPPORTED = "supported";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates a bitmap where each bit set in the Mask attribute corresponds to an alarm that shall be enabled.
     */
    public AlarmBitmap mask; // 0 AlarmBitmap R V
    /**
     * Indicates a bitmap where each bit set in the Latch attribute shall indicate that the corresponding alarm will be
     * latched when set, and will not reset to inactive when the underlying condition which caused the alarm is no
     * longer present, and so requires an explicit reset using the Reset command.
     */
    public AlarmBitmap latch; // 1 AlarmBitmap R V
    /**
     * Indicates a bitmap where each bit shall represent the state of an alarm. The value of true means the alarm is
     * active, otherwise the alarm is inactive.
     */
    public AlarmBitmap state; // 2 AlarmBitmap R V
    /**
     * Indicates a bitmap where each bit shall represent whether or not an alarm is supported. The value of true means
     * the alarm is supported, otherwise the alarm is not supported.
     * If an alarm is not supported, the corresponding bit in Mask, Latch, and State shall be false.
     */
    public AlarmBitmap supported; // 3 AlarmBitmap R V

    // Structs
    /**
     * This event shall be generated when one or more alarms change state, and shall have these fields:
     */
    public static class Notify {
        /**
         * This field shall indicate those alarms that have become active.
         */
        public AlarmBitmap active; // AlarmBitmap
        /**
         * This field shall indicate those alarms that have become inactive.
         */
        public AlarmBitmap inactive; // AlarmBitmap
        /**
         * This field shall be a copy of the new State attribute value that resulted in the event being generated. That
         * is, this field shall have all the bits in Active set and shall NOT have any of the bits in Inactive set.
         */
        public AlarmBitmap state; // AlarmBitmap
        /**
         * This field shall be a copy of the Mask attribute when this event was generated.
         */
        public AlarmBitmap mask; // AlarmBitmap

        public Notify(AlarmBitmap active, AlarmBitmap inactive, AlarmBitmap state, AlarmBitmap mask) {
            this.active = active;
            this.inactive = inactive;
            this.state = state;
            this.mask = mask;
        }
    }

    // Bitmaps
    public static class AlarmBitmap {
        public boolean doorOpen;

        public AlarmBitmap(boolean doorOpen) {
            this.doorOpen = doorOpen;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This feature indicates that alarms can be reset via the Reset command.
         */
        public boolean reset;

        public FeatureMap(boolean reset) {
            this.reset = reset;
        }
    }

    public RefrigeratorAlarmCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 87, "RefrigeratorAlarm");
    }

    protected RefrigeratorAlarmCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command resets active and latched alarms (if possible). Any generated Notify event shall contain fields that
     * represent the state of the server after the command has been processed.
     */
    public static ClusterCommand reset(AlarmBitmap alarms) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (alarms != null) {
            map.put("alarms", alarms);
        }
        return new ClusterCommand("reset", map);
    }

    /**
     * This command allows a client to request that an alarm be enabled or suppressed at the server.
     */
    public static ClusterCommand modifyEnabledAlarms(AlarmBitmap mask) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (mask != null) {
            map.put("mask", mask);
        }
        return new ClusterCommand("modifyEnabledAlarms", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "mask : " + mask + "\n";
        str += "latch : " + latch + "\n";
        str += "state : " + state + "\n";
        str += "supported : " + supported + "\n";
        return str;
    }
}
