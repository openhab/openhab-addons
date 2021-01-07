/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.lan;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LanHost} is the Java class used to map the "LanHost"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class LanHost implements ConnectivityData {
    private @Nullable String primaryName;
    private @Nullable L2Ident l2ident;
    private @Nullable String vendorName;
    private @Nullable List<L3Connectivity> l3connectivities;
    private @Nullable LanAccessPoint accessPoint;
    private boolean reachable;
    private long lastTimeReachable;
    private long lastActivity;

    public @Nullable String getMac() {
        return l2ident != null ? l2ident.getIfMac() : null;
    }

    @Override
    public @Nullable String getIpv4() {
        List<L3Connectivity> connectivities = l3connectivities;
        if (connectivities != null) {
            Optional<@Nullable String> match = connectivities.stream().filter(c -> c.isActive() && c.getIpv4() != null)
                    .map(c -> c.getIpv4()).findFirst();
            return match.isPresent() ? match.get() : null;
        }
        return null;
    }

    public String getPrimaryNameOrElse(String defaulted) {
        return primaryName != null ? primaryName : defaulted;
    }

    public @Nullable String getVendorName() {
        return vendorName;
    }

    @Override
    public boolean isReachable() {
        return reachable;
    }

    @Override
    public long getLastSeen() {
        return Math.max(lastActivity, lastTimeReachable);
    }

    public @Nullable LanAccessPoint getAccessPoint() {
        return accessPoint;
    }
}
