/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing an NEEO system information (serialize/deserialize json use only). This model only represents a
 * small portion of the system information
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoSystemInfo {

    /** The host name of the brain. */
    private final String hostname;

    /**
     * Creates the system information from the hostname
     *
     * @param hostname the non-empty hostname
     */
    public NeeoSystemInfo(String hostname) {
        NeeoUtil.requireNotEmpty(hostname, "hostname cannot be null");
        this.hostname = hostname;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostname() {
        return hostname;
    }

    @Override
    public int hashCode() {
        return (hostname == null) ? 0 : hostname.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NeeoSystemInfo other = (NeeoSystemInfo) obj;
        if (hostname == null) {
            if (other.hostname != null) {
                return false;
            }
        } else if (!hostname.equals(other.hostname)) {
            return false;
        }
        return true;
    }

}
