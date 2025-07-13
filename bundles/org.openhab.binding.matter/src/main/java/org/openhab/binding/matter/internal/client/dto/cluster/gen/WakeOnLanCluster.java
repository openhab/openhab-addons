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

/**
 * WakeOnLan
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WakeOnLanCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0503;
    public static final String CLUSTER_NAME = "WakeOnLan";
    public static final String CLUSTER_PREFIX = "wakeOnLan";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_MAC_ADDRESS = "macAddress";
    public static final String ATTRIBUTE_LINK_LOCAL_ADDRESS = "linkLocalAddress";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the current MAC address of the device. Only 48-bit MAC Addresses shall be used for this attribute as
     * required by the Wake on LAN protocol.
     * Format of this attribute shall be an upper-case hex-encoded string representing the hex address, like
     * 12345678ABCD.
     */
    public String macAddress; // 0 string R V
    /**
     * Indicates the current link-local address of the device. Only 128-bit IPv6 link-local addresses shall be used for
     * this attribute.
     * &gt; [!NOTE]
     * &gt; Some companies may consider MAC Address to be protected data subject to PII handling considerations and will
     * therefore choose not to include it or read it. The MAC Address can often be determined using ARP in IPv4 or NDP
     * in IPv6.
     */
    public OctetString linkLocalAddress; // 1 ipv6adr R V

    public WakeOnLanCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1283, "WakeOnLan");
    }

    protected WakeOnLanCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "macAddress : " + macAddress + "\n";
        str += "linkLocalAddress : " + linkLocalAddress + "\n";
        return str;
    }
}
