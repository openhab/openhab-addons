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
 * Groups
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GroupsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0004;
    public static final String CLUSTER_NAME = "Groups";
    public static final String CLUSTER_PREFIX = "groups";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_NAME_SUPPORT = "nameSupport";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute provides legacy, read-only access to whether the Group Names feature is supported. The most
     * significant bit, bit 7 (GroupNames), shall be equal to bit 0 of the FeatureMap attribute (GN Feature). All other
     * bits shall be 0.
     */
    public NameSupportBitmap nameSupport; // 0 NameSupportBitmap R V

    // Bitmaps
    public static class NameSupportBitmap {
        public boolean groupNames;

        public NameSupportBitmap(boolean groupNames) {
            this.groupNames = groupNames;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * The Group Names feature indicates the ability to store a name for a group when a group is added.
         */
        public boolean groupNames;

        public FeatureMap(boolean groupNames) {
            this.groupNames = groupNames;
        }
    }

    public GroupsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 4, "Groups");
    }

    protected GroupsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * The AddGroup command allows a client to add group membership in a particular group for the server endpoint.
     */
    public static ClusterCommand addGroup(Integer groupId, String groupName) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (groupName != null) {
            map.put("groupName", groupName);
        }
        return new ClusterCommand("addGroup", map);
    }

    /**
     * The ViewGroup command allows a client to request that the server responds with a ViewGroupResponse command
     * containing the name string for a particular group.
     */
    public static ClusterCommand viewGroup(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("viewGroup", map);
    }

    /**
     * The GetGroupMembership command allows a client to inquire about the group membership of the server endpoint, in a
     * number of ways.
     */
    public static ClusterCommand getGroupMembership(List<Integer> groupList) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupList != null) {
            map.put("groupList", groupList);
        }
        return new ClusterCommand("getGroupMembership", map);
    }

    /**
     * The RemoveGroup command allows a client to request that the server removes the membership for the server
     * endpoint, if any, in a particular group.
     */
    public static ClusterCommand removeGroup(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("removeGroup", map);
    }

    /**
     * The RemoveAllGroups command allows a client to direct the server to remove all group associations for the server
     * endpoint.
     */
    public static ClusterCommand removeAllGroups() {
        return new ClusterCommand("removeAllGroups");
    }

    /**
     * The AddGroupIfIdentifying command allows a client to add group membership in a particular group for the server
     * endpoint, on condition that the endpoint is identifying itself. Identifying functionality is controlled using the
     * Identify cluster, (see Identify Cluster).
     * For correct operation of the AddGroupIfIdentifying command, any endpoint that supports the Groups server cluster
     * shall also support the Identify server cluster.
     * This command might be used to assist configuring group membership in the absence of a commissioning tool.
     */
    public static ClusterCommand addGroupIfIdentifying(Integer groupId, String groupName) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (groupName != null) {
            map.put("groupName", groupName);
        }
        return new ClusterCommand("addGroupIfIdentifying", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "nameSupport : " + nameSupport + "\n";
        return str;
    }
}
