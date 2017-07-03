/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class NetworkSetting.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class NetworkSetting {

    /** The netif. */
    private final String netif;

    /** The hw addr. */
    private final String hwAddr;

    /** The ip addr V 4. */
    private final String ipAddrV4;

    /** The ip addr V 6. */
    private final String ipAddrV6;

    /** The netmask. */
    private final String netmask;

    /** The gateway. */
    private final String gateway;

    /** The dns. */
    private final List<String> dns;

    /**
     * Instantiates a new network setting.
     *
     * @param netif the netif
     * @param hwAddr the hw addr
     * @param ipAddrV4 the ip addr V 4
     * @param ipAddrV6 the ip addr V 6
     * @param netmask the netmask
     * @param gateway the gateway
     * @param dns the dns
     */
    public NetworkSetting(String netif, String hwAddr, String ipAddrV4, String ipAddrV6, String netmask, String gateway,
            List<String> dns) {
        super();
        this.netif = netif;
        this.hwAddr = hwAddr;
        this.ipAddrV4 = ipAddrV4;
        this.ipAddrV6 = ipAddrV6;
        this.netmask = netmask;
        this.gateway = gateway;
        this.dns = dns;
    }

    /**
     * Gets the netif.
     *
     * @return the netif
     */
    public String getNetif() {
        return netif;
    }

    /**
     * Gets the hw addr.
     *
     * @return the hw addr
     */
    public String getHwAddr() {
        return hwAddr;
    }

    /**
     * Gets the ip addr V 4.
     *
     * @return the ip addr V 4
     */
    public String getIpAddrV4() {
        return ipAddrV4;
    }

    /**
     * Gets the ip addr V 6.
     *
     * @return the ip addr V 6
     */
    public String getIpAddrV6() {
        return ipAddrV6;
    }

    /**
     * Gets the netmask.
     *
     * @return the netmask
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * Gets the gateway.
     *
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Gets the dns.
     *
     * @return the dns
     */
    public List<String> getDns() {
        return dns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NetworkSetting [netif=" + netif + ", hwAddr=" + hwAddr + ", ipAddrV4=" + ipAddrV4 + ", ipAddrV6="
                + ipAddrV6 + ", netmask=" + netmask + ", gateway=" + gateway + ", dns=" + dns + "]";
    }
}
