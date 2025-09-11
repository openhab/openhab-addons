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
 * EcosystemInformation
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EcosystemInformationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0750;
    public static final String CLUSTER_NAME = "EcosystemInformation";
    public static final String CLUSTER_PREFIX = "ecosystemInformation";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_DEVICE_DIRECTORY = "deviceDirectory";
    public static final String ATTRIBUTE_LOCATION_DIRECTORY = "locationDirectory";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This attribute shall contain the list of logical devices represented by a Bridged Node. Most of the time this
     * will contain a single entry, but may grow with more complex device compositions (e.g. another bridge.) An empty
     * list indicates that the information is not available.
     */
    public List<EcosystemDeviceStruct> deviceDirectory; // 0 list R F M
    /**
     * This attribute shall contain the list of rooms, areas and groups associated with the DeviceDirectory entries, and
     * shall NOT contain locations which are dynamically generated and removed by an ecosystem. (E.g. a location that is
     * generated and removed based on the user being home is not permitted. However, an initially generated location
     * name that does not quickly change is acceptable.) An empty list indicates that the information is not available.
     * LocationDirectory entries shall be removed if there is no DeviceDirectory that references it.
     */
    public List<EcosystemLocationStruct> locationDirectory; // 1 list R F M

    // Structs
    public static class EcosystemDeviceStruct {
        /**
         * This field shall indicate the device’s name, which is provided externally if the user consents. (For example,
         * provided by the user in an ecosystem specific interface.)
         */
        public String deviceName; // string
        /**
         * This field shall be present and set if the DeviceName field is present.
         * This field shall indicate the timestamp of when the DeviceName was last modified.
         */
        public BigInteger deviceNameLastEdit; // epoch-us
        /**
         * This field shall indicate the endpoint this EcosystemDeviceStruct is associated with on this Bridge.
         * This field shall be present and set to a valid endpoint if the device is accessible through the bridge.
         */
        public Integer bridgedEndpoint; // endpoint-no
        /**
         * This field shall indicate the endpoint this EcosystemDeviceStruct is associated with on the original device
         * represented by this bridge’s Bridged Node. If this bridge is receiving the device from another bridge, then
         * the OriginalEndpoint field value would be the same on both bridges. This field shall be present and set to a
         * valid endpoint on the original device if that device is a Matter device.
         */
        public Integer originalEndpoint; // endpoint-no
        /**
         * This field shall indicate all of the DeviceTypes within the DeviceTypeList in the Descriptor Cluster
         * associated with this EcosystemDeviceStruct entry.
         * This field shall contain a list of valid device type ids.
         */
        public List<DescriptorCluster.DeviceTypeStruct> deviceTypes; // list
        /**
         * This field shall specify the EcosystemLocationStruct entries in the LocationDirectory attribute associated
         * with this EcosystemDeviceStruct.
         */
        public List<String> uniqueLocationIDs; // list
        /**
         * This field shall indicate the timestamp of when the UniqueLocationIDs was last modified.
         * &gt; [!NOTE]
         * &gt; If multiple server instances update the UniqueLocationIDs field at the same time, it is possible one of
         * the updates will be missed. This is considered an acceptable limitation to reduce the complexity of the
         * design. Since this is meant to be provided from user input, it is unlikely these signals would be happening
         * at one time.
         */
        public BigInteger uniqueLocationIDsLastEdit; // epoch-us
        public Integer fabricIndex; // FabricIndex

        public EcosystemDeviceStruct(String deviceName, BigInteger deviceNameLastEdit, Integer bridgedEndpoint,
                Integer originalEndpoint, List<DescriptorCluster.DeviceTypeStruct> deviceTypes,
                List<String> uniqueLocationIDs, BigInteger uniqueLocationIDsLastEdit, Integer fabricIndex) {
            this.deviceName = deviceName;
            this.deviceNameLastEdit = deviceNameLastEdit;
            this.bridgedEndpoint = bridgedEndpoint;
            this.originalEndpoint = originalEndpoint;
            this.deviceTypes = deviceTypes;
            this.uniqueLocationIDs = uniqueLocationIDs;
            this.uniqueLocationIDsLastEdit = uniqueLocationIDsLastEdit;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class EcosystemLocationStruct {
        /**
         * This field shall indicate a unique identifier for a specific Ecosystem Information Cluster server instance
         * representing the location independent of its LocationDescriptor field.
         * UniqueLocationID can be used by the client to determine if the change is a relocation of the device or just a
         * renaming of the location.
         * No guarantees are given for consistency of the ID between server instances. The same location may be
         * represented by different IDs on different Ecosystem Information Cluster server instances, so only the history
         * from a single server instance should be considered when evaluating a change.
         * UniqueLocationID shall be changed when the LocationDescriptor changes from one existing location to another
         * location as a result of an external interaction. (For example, the user changes the location assignment.)
         * UniqueLocationID shall NOT be changed when the LocationDescriptor changes name, but still represents the same
         * location. (For example, the user renames a room.) UniqueLocationID shall be changed when LocationDescriptor
         * changes as a result of another Ecosystem Information Cluster server instance changing and the
         * UniqueLocationID on the remote server instance also changes.
         * UniqueLocationID shall NOT be changed when LocationDescriptor changes as a result of another Ecosystem
         * Information Cluster server instance changing and the UniqueLocationID on the remote server instance does not
         * change.
         */
        public String uniqueLocationId; // string
        /**
         * This field shall indicate the location (e.g. living room, driveway) and associated metadata that is provided
         * externally if the user consents. (For example, provided by the user in an ecosystem specific interface.)
         * &quot;Location&quot; in this context is typically used by the user’s grouping into rooms, areas or other
         * logical groupings of how devices are used. So a device might be part of multiple such &quot;Locations&quot;s.
         */
        public Locationdesc locationDescriptor; // locationdesc
        /**
         * This field shall indicate the timestamp of when the LocationDescriptor was last modified.
         */
        public BigInteger locationDescriptorLastEdit; // epoch-us
        public Integer fabricIndex; // FabricIndex

        public EcosystemLocationStruct(String uniqueLocationId, Locationdesc locationDescriptor,
                BigInteger locationDescriptorLastEdit, Integer fabricIndex) {
            this.uniqueLocationId = uniqueLocationId;
            this.locationDescriptor = locationDescriptor;
            this.locationDescriptorLastEdit = locationDescriptorLastEdit;
            this.fabricIndex = fabricIndex;
        }
    }

    public EcosystemInformationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1872, "EcosystemInformation");
    }

    protected EcosystemInformationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "deviceDirectory : " + deviceDirectory + "\n";
        str += "locationDirectory : " + locationDirectory + "\n";
        return str;
    }
}
