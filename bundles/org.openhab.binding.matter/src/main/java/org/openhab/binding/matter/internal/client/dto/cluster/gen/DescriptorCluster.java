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
 * Descriptor
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DescriptorCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x001D;
    public static final String CLUSTER_NAME = "Descriptor";
    public static final String CLUSTER_PREFIX = "descriptor";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_DEVICE_TYPE_LIST = "deviceTypeList";
    public static final String ATTRIBUTE_SERVER_LIST = "serverList";
    public static final String ATTRIBUTE_CLIENT_LIST = "clientList";
    public static final String ATTRIBUTE_PARTS_LIST = "partsList";
    public static final String ATTRIBUTE_TAG_LIST = "tagList";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This is a list of device types and corresponding revisions declaring endpoint conformance (see DeviceTypeStruct).
     * At least one device type entry shall be present.
     * An endpoint shall conform to all device types listed in the DeviceTypeList. A cluster instance that is in common
     * for more than one device type in the DeviceTypeList shall be supported as a shared cluster instance on the
     * endpoint.
     */
    public List<DeviceTypeStruct> deviceTypeList; // 0 list R V
    /**
     * This attribute shall list each cluster ID for the server clusters present on the endpoint instance.
     */
    public List<Integer> serverList; // 1 list R V
    /**
     * This attribute shall list each cluster ID for the client clusters present on the endpoint instance.
     */
    public List<Integer> clientList; // 2 list R V
    /**
     * This attribute indicates composition of the device type instance. Device type instance composition shall include
     * the endpoints in this list.
     * See Endpoint Composition for more information about which endpoints to include in this list.
     */
    public List<Integer> partsList; // 3 list R V
    /**
     * This attribute shall be used to disambiguate sibling endpoints in certain situations, as defined in the
     * Disambiguation section in the System Model specification. An example of such a situation might be a device with
     * two buttons, with this attribute being used to indicate which of the two endpoints corresponds to the button on
     * the left side.
     * It may also be used to provide information about an endpoint (e.g. the relative location of a Temperature sensor
     * in a Temperature Controlled Cabinet).
     * • A client SHOULD use these tags to convey disambiguation information and other relevant information to the user
     * (e.g. showing it in a user interface), as appropriate.
     * • A client SHOULD use these tags in its logic to make decisions, as appropriate.
     * For example, a client may identify which endpoint maps to a certain function, orientation or labeling.
     * A client may use the Label field of each SemanticTagStruct, if present in each structure, to indicate
     * characteristics of an endpoint, or to augment what is provided in the TagID field of the same structure.
     */
    public List<Semtag> tagList; // 4 list R V

    // Structs
    /**
     * The device type and revision define endpoint conformance to a release of a device type definition. See the Data
     * Model specification for more information.
     */
    public static class DeviceTypeStruct {
        /**
         * This shall indicate the device type definition. The endpoint shall conform to the device type definition and
         * cluster specifications required by the device type.
         */
        public Integer deviceType; // devtype-id
        /**
         * This is the implemented revision of the device type definition. The endpoint shall conform to this revision
         * of the device type.
         */
        public Integer revision; // uint16

        public DeviceTypeStruct(Integer deviceType, Integer revision) {
            this.deviceType = deviceType;
            this.revision = revision;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * See the Disambiguation section in the System Model spec for conformance requirements for this feature and the
         * corresponding attribute.
         */
        public boolean tagList;

        public FeatureMap(boolean tagList) {
            this.tagList = tagList;
        }
    }

    public DescriptorCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 29, "Descriptor");
    }

    protected DescriptorCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "deviceTypeList : " + deviceTypeList + "\n";
        str += "serverList : " + serverList + "\n";
        str += "clientList : " + clientList + "\n";
        str += "partsList : " + partsList + "\n";
        str += "tagList : " + tagList + "\n";
        return str;
    }
}
