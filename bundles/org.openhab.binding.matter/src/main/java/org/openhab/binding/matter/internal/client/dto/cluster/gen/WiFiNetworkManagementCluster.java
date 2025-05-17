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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * WiFiNetworkManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WiFiNetworkManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0451;
    public static final String CLUSTER_NAME = "WiFiNetworkManagement";
    public static final String CLUSTER_PREFIX = "wiFiNetworkManagement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_SSID = "ssid";
    public static final String ATTRIBUTE_PASSPHRASE_SURROGATE = "passphraseSurrogate";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the SSID of the primary Wi-Fi network provided by this device.
     * A value of null shall indicate that no primary Wi-Fi network is available (e.g. because the Wi-Fi network has not
     * yet been configured by the user).
     * &gt; [!NOTE]
     * &gt; The SSID in Wi-Fi is a collection of 1-32 bytes, the text encoding of which is not specified.
     * Implementations must be careful to support transferring these byte strings without requiring a particular
     * encoding. The most common encoding is UTF- 8, however this is just a convention. Some configurations may use
     * Latin-1 or other character sets.
     */
    public OctetString ssid; // 0 octstr R V
    /**
     * This attribute shall contain an arbitrary numeric value; this value shall increase whenever the passphrase or PSK
     * associated with the primary Wi-Fi network provided by this device changes.
     * A value of null shall indicate that no primary Wi-Fi network is available.
     * Clients can subscribe to this attribute or compare its value to a locally cached copy to detect if a cached
     * passphrase value has become stale.
     * It is recommended that servers implement this attribute as either a timestamp or a counter. When implemented as a
     * counter it SHOULD be initialized with a random value.
     * &gt; [!NOTE]
     * &gt; The passphrase itself is not exposed as an attribute to avoid its unintentional retrieval or caching by
     * clients that use wildcard reads or otherwise routinely read all available attributes. It can be retrieved using
     * the NetworkPassphraseRequest command.
     */
    public BigInteger passphraseSurrogate; // 1 uint64 R M

    public WiFiNetworkManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1105, "WiFiNetworkManagement");
    }

    protected WiFiNetworkManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to request the current WPA-Personal passphrase or PSK associated with the Wi-Fi network
     * provided by this device.
     * If the command is not executed via a CASE session, the command shall be rejected with a status of
     * UNSUPPORTED_ACCESS.
     * If no primary Wi-Fi network is available (the SSID attribute is null), the command shall be rejected with a
     * status of INVALID_IN_STATE.
     * Otherwise a NetworkPassphraseResponse shall be generated.
     */
    public static ClusterCommand networkPassphraseRequest() {
        return new ClusterCommand("networkPassphraseRequest");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "ssid : " + ssid + "\n";
        str += "passphraseSurrogate : " + passphraseSurrogate + "\n";
        return str;
    }
}
