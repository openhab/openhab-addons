/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing an NEEO system information (serialize/deserialize json use only). This model only represents a
 * small portion of the system information
 *
 * @author Tim Roberts
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
        return hostname.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof NeeoSystemInfo)) {
            return false;
        }

        return StringUtils.equals(hostname, ((NeeoSystemInfo) obj).hostname);
    }

}
