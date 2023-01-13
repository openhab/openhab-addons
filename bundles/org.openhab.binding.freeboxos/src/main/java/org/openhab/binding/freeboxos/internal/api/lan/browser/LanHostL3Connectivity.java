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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.L3Af;

/**
 * The {@link LanHostL3Connectivity} is the Java class used to map the "LanHostL3Connectivity" structure used by the Lan
 * Hosts Browser API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanHostL3Connectivity {
    private @Nullable String addr;
    private L3Af af = L3Af.UNKNOWN;
    private boolean active;
    private boolean reachable;
    private @Nullable ZonedDateTime lastActivity;
    private @Nullable ZonedDateTime lastTimeReachable;
    private @Nullable String model;

    public String getAddr() {
        return Objects.requireNonNull(addr);
    }

    public boolean isIPv4() {
        return L3Af.IPV4.equals(af);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isReachable() {
        return reachable;
    }

    public @Nullable ZonedDateTime getLastActivity() {
        return lastActivity;
    }

    public @Nullable ZonedDateTime getLastTimeReachable() {
        return lastTimeReachable;
    }

    public @Nullable String getModel() {
        return model;
    }
}
