/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.internal.config;

/**
 * The {@link SenseBoxConfiguration} is the base class for configuration
 * information held by devices and modules
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxConfiguration {

    private long refreshInterval;

    private String senseBoxId;

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getSenseBoxId() {
        return senseBoxId;
    }

    public void setSenseBoxId(String senseBoxId) {
        this.senseBoxId = senseBoxId;
    }
}
