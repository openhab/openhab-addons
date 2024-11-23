/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

/**
 * The {@link FreeboxLanHost} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxLanHost {
    private String id;
    private String primaryName;
    private String hostType;
    private boolean primaryNameManual;
    private FreeboxLanHostL2Ident l2ident;
    private String vendorName;
    private boolean persistent;
    private boolean reachable;
    private long lastTimeReachable;
    private boolean active;
    private long lastActivity;
    private List<FreeboxLanHostName> names;
    private List<FreeboxLanHostL3Connectivity> l3connectivities;

    public String getMAC() {
        return (l2ident != null && l2ident.isMacAddress()) ? l2ident.getId() : null;
    }

    public String getId() {
        return id;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String getHostType() {
        return hostType;
    }

    public boolean isPrimaryNameManual() {
        return primaryNameManual;
    }

    public FreeboxLanHostL2Ident getL2Ident() {
        return l2ident;
    }

    public String getVendorName() {
        return vendorName;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getLastTimeReachable() {
        return lastTimeReachable;
    }

    public boolean isActive() {
        return active;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public List<FreeboxLanHostName> getNames() {
        return names;
    }

    public List<FreeboxLanHostL3Connectivity> getL3Connectivities() {
        return l3connectivities;
    }
}
