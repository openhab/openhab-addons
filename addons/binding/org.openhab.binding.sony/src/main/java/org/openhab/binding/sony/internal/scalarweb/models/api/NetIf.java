/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class NetIf.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class NetIf {

    /** The netif. */
    private final String netif;

    /**
     * Instantiates a new net if.
     *
     * @param netif the netif
     */
    public NetIf(String netif) {
        super();
        this.netif = netif;
    }

    /**
     * Gets the netif.
     *
     * @return the netif
     */
    public String getNetif() {
        return netif;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NetIf [netif=" + netif + "]";
    }
}
