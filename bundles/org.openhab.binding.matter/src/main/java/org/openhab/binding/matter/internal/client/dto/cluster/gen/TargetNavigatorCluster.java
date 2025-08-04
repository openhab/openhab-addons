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
 * TargetNavigator
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TargetNavigatorCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0505;
    public static final String CLUSTER_NAME = "TargetNavigator";
    public static final String CLUSTER_PREFIX = "targetNavigator";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_TARGET_LIST = "targetList";
    public static final String ATTRIBUTE_CURRENT_TARGET = "currentTarget";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates a list of targets that can be navigated to within the experience presented to the user by the Endpoint
     * (Video Player or Content App). The list shall NOT contain any entries with the same Identifier in the
     * TargetInfoStruct object.
     */
    public List<TargetInfoStruct> targetList; // 0 list R V
    /**
     * Indicates the Identifier for the target which is currently in foreground on the corresponding Endpoint (Video
     * Player or Content App), or 0xFF to indicate that no target is in the foreground.
     * When not 0xFF, the CurrentTarget shall be an Identifier value contained within one of the TargetInfoStruct
     * objects in the TargetList attribute.
     */
    public Integer currentTarget; // 1 uint8 R V

    // Structs
    /**
     * This event shall be generated when there is a change in either the active target or the list of available targets
     * or both.
     */
    public static class TargetUpdated {
        public List<TargetInfoStruct> targetList; // list
        public Integer currentTarget; // uint8
        public OctetString data; // octstr

        public TargetUpdated(List<TargetInfoStruct> targetList, Integer currentTarget, OctetString data) {
            this.targetList = targetList;
            this.currentTarget = currentTarget;
            this.data = data;
        }
    }

    /**
     * This indicates an object describing the navigable target.
     */
    public static class TargetInfoStruct {
        /**
         * This field shall contain an unique id within the TargetList.
         */
        public Integer identifier; // uint8
        /**
         * This field shall contain a name string for the TargetInfoStruct.
         */
        public String name; // string

        public TargetInfoStruct(Integer identifier, String name) {
            this.identifier = identifier;
            this.name = name;
        }
    }

    // Enums
    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        TARGET_NOT_FOUND(1, "Target Not Found"),
        NOT_ALLOWED(2, "Not Allowed");

        public final Integer value;
        public final String label;

        private StatusEnum(Integer value, String label) {
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

    public TargetNavigatorCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1285, "TargetNavigator");
    }

    protected TargetNavigatorCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall navigation the UX to the target identified.
     */
    public static ClusterCommand navigateTarget(Integer target, String data) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (target != null) {
            map.put("target", target);
        }
        if (data != null) {
            map.put("data", data);
        }
        return new ClusterCommand("navigateTarget", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "targetList : " + targetList + "\n";
        str += "currentTarget : " + currentTarget + "\n";
        return str;
    }
}
