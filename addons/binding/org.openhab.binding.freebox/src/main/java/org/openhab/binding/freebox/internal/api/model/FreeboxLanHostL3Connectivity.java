/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxLanHostL3Connectivity} is the Java class used to map the "LanHostL3Connectivity"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLanHostL3Connectivity {
    private String addr;
    private String af;
    private boolean active;
    private boolean reachable;
    private long lastActivity;
    private long lastTimeReachable;

    public String getAddr() {
        return addr;
    }

    public String getAf() {
        return af;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public long getLastTimeReachable() {
        return lastTimeReachable;
    }
}
