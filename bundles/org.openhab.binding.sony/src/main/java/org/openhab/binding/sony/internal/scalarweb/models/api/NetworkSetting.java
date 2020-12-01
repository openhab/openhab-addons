/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the network interface settings and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NetworkSetting {

    /** The network interface name */
    private @Nullable String netif;

    /** The mac address. */
    private @Nullable String hwAddr;

    /** The ip v4 address */
    private @Nullable String ipAddrV4;

    /** The ip v6 address */
    private @Nullable String ipAddrV6;

    /** The network mask */
    private @Nullable String netmask;

    /** The gateway */
    private @Nullable String gateway;

    /** The list of DNS names */
    private @Nullable List<@Nullable String> dns;

    /**
     * Constructor used for deserialization only
     */
    public NetworkSetting() {
    }

    /**
     * Gets the network interface name
     *
     * @return the network interface name
     */
    public @Nullable String getNetif() {
        return netif;
    }

    /**
     * Gets the mac address
     *
     * @return the mac address
     */
    public @Nullable String getHwAddr() {
        return hwAddr;
    }

    /**
     * Gets the IP Address (v4)
     *
     * @return the IP Address (v4)
     */
    public @Nullable String getIpAddrV4() {
        return ipAddrV4;
    }

    /**
     * Gets the IP Address (v6)
     *
     * @return the IP Address (V6)
     */
    public @Nullable String getIpAddrV6() {
        return ipAddrV6;
    }

    /**
     * Gets the network mask
     *
     * @return the network mask
     */
    public @Nullable String getNetmask() {
        return netmask;
    }

    /**
     * Gets the gateway
     *
     * @return the gateway
     */
    public @Nullable String getGateway() {
        return gateway;
    }

    /**
     * Gets list of DNS names
     *
     * @return the non-null, possibly empty unmodifiable list of DNS names
     */
    public List<@Nullable String> getDns() {
        return dns == null ? Collections.emptyList() : Collections.unmodifiableList(dns);
    }

    @Override
    public @Nullable String toString() {
        return "NetworkSetting [netif=" + netif + ", hwAddr=" + hwAddr + ", ipAddrV4=" + ipAddrV4 + ", ipAddrV6="
                + ipAddrV6 + ", netmask=" + netmask + ", gateway=" + gateway + ", dns=" + dns + "]";
    }
}
