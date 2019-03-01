/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.discovery;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 *
 */

public class SmartthingsDiscoveryData {

    private long openHabStartTime;
    private long hubTime;
    private String[] data;

    SmartthingsDiscoveryData() {
    }

    public long getOpenHabStartTime() {
        return openHabStartTime;
    }

    public long getHubTime() {
        return hubTime;
    }

    public String[] getData() {
        return data;
    }

}
