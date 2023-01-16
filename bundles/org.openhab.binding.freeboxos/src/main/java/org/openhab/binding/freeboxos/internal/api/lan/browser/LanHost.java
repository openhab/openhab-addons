/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.lan.browser;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.HostNameSource;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.HostType;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.LanAccessPoint;

/**
 * The {@link LanHost} is the Java class used to map the "LanHost" structure used by the Lan Hosts Browser API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanHost {
    private @Nullable String id;
    private @Nullable String primaryName;
    private HostType hostType = HostType.UNKNOWN;
    private boolean primaryNameManual;
    private @Nullable LanHostL2Ident l2ident;
    private @Nullable String vendorName;
    private boolean persistent;
    private boolean reachable;
    private @Nullable ZonedDateTime lastTimeReachable;
    private boolean active;
    private @Nullable ZonedDateTime lastActivity;
    private ZonedDateTime firstActivity = ApiConstants.EPOCH_ZERO;
    private List<LanHostName> names = List.of();
    private List<LanHostL3Connectivity> l3connectivities = List.of();

    private @Nullable LanAccessPoint accessPoint;

    public String getId() {
        return Objects.requireNonNull(id);
    }

    public Optional<String> getPrimaryName() {
        return Optional.ofNullable(primaryName);
    }

    public HostType getHostType() {
        return hostType;
    }

    public boolean isPrimaryNameManual() {
        return primaryNameManual;
    }

    public LanHostL2Ident getL2ident() {
        return Objects.requireNonNull(l2ident);
    }

    public Optional<String> getVendorName() {
        return Optional.ofNullable(vendorName);
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isReachable() {
        return reachable;
    }

    public @Nullable ZonedDateTime getLastTimeReachable() {
        return lastTimeReachable;
    }

    public boolean isActive() {
        return active;
    }

    public @Nullable ZonedDateTime getLastActivity() {
        return lastActivity;
    }

    public ZonedDateTime getFirstActivity() {
        return firstActivity;
    }

    public List<LanHostName> getNames() {
        return names;
    }

    public List<LanHostL3Connectivity> getL3connectivities() {
        return l3connectivities;
    }

    public @Nullable String getMac() {
        LanHostL2Ident localIdent = getL2ident();
        return localIdent.isMac() ? localIdent.getId().toLowerCase() : null;
    }

    public Optional<String> getUPnPName() {
        return getNames().stream().filter(name -> name.getSource() == HostNameSource.UPNP).findFirst()
                .map(name -> name.getName());
    }

    public @Nullable String getIpv4() {
        return l3connectivities.stream().filter(c -> c.isActive() && c.isIPv4()).map(c -> c.getAddr()).findFirst()
                .orElse(null);
    }

    public @Nullable ZonedDateTime getLastSeen() {
        ZonedDateTime localLastActivity = lastActivity;
        if (lastTimeReachable == null && localLastActivity == null) {
            return null;
        }
        if (lastTimeReachable == null) {
            return lastActivity;
        }
        if (localLastActivity == null) {
            return lastTimeReachable;
        } else {
            return localLastActivity.isAfter(lastTimeReachable) ? lastActivity : lastTimeReachable;
        }
    }

    public @Nullable LanAccessPoint getAccessPoint() {
        return accessPoint;
    }

}
