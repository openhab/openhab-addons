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

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the request to get the network interface information and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NetIf {

    /** The network interface to get */
    private final String netif;

    /**
     * Instantiates a new network interface request
     *
     * @param netif the non-null, non-empty interface name
     */
    public NetIf(final String netif) {
        Validate.notEmpty(netif, "netif cannot be empty");
        this.netif = netif;
    }

    /**
     * Gets the network interface name
     *
     * @return the non-null, non-empty network interface name
     */
    public String getNetif() {
        return netif;
    }

    @Override
    public String toString() {
        return "NetIf [netif=" + netif + "]";
    }
}
