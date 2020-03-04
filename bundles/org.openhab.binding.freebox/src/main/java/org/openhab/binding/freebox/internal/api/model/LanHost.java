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
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.freebox.internal.api.model.LanHostL3Connectivity.L3Af;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanHost} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
public class LanHost {
    private String id;
    private String primaryName;
    private String hostType;
    private boolean primaryNameManual;
    private LanHostL2Ident l2ident;
    private String vendorName;
    private boolean persistent;
    private boolean reachable;
    private long lastTimeReachable;
    private boolean active;
    private long lastActivity;
    private List<LanHostName> names;
    private List<LanHostL3Connectivity> l3connectivities;
    @SerializedName("interface")
    private String intf;

    public String getMAC() {
        return (l2ident != null && l2ident.isMacAddress()) ? l2ident.getId() : null;
    }

    public boolean sameMac(@NonNull String mac) {
        return mac.equalsIgnoreCase(getMAC());
    }

    private Stream<LanHostL3Connectivity> getActiveConnections() {
        return l3connectivities.stream().filter(c -> c.isActive());
    }

    public String getIpv4() {
        Optional<LanHostL3Connectivity> match = getActiveConnections().filter(c -> c.getAf() == L3Af.IPV4)
                .findFirst();
        return match.isPresent() ? match.get().getAddr() : null;
    }

    public String getId() {
        return id;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public boolean hasPrimaryName() {
        return primaryName != null && !primaryName.isEmpty();
    }

    public String getHostType() {
        return hostType;
    }

    public boolean isPrimaryNameManual() {
        return primaryNameManual;
    }

    public LanHostL2Ident getL2Ident() {
        return l2ident;
    }

    public String getVendorName() {
        return vendorName;
    }

    public boolean hasVendorName() {
        return vendorName != null && !vendorName.isEmpty();
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isReachable() {
        return reachable;
    }

    public boolean isActive() {
        return active;
    }

    public long getLastSeen() {
        return Math.max(lastActivity, lastTimeReachable);
    }

    public List<LanHostName> getNames() {
        return names;
    }

    public List<LanHostL3Connectivity> getL3Connectivities() {
        return l3connectivities;
    }

    public String getInterface() {
        return this.intf;
    }
}
