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
package org.openhab.binding.freeboxos.internal.api.lan.control;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.CDayRange;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.NetworkControlMode;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;

/**
 * The {@link NetworkControl} is the Java class used to map the structure used by the Network Control API
 *
 * https://dev.freebox.fr/sdk/os/lan/#net-object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class NetworkControl {
    private int profileId;
    private @Nullable ZonedDateTime nextChange;
    private NetworkControlMode overrideMode = NetworkControlMode.UNKNOWN;
    private NetworkControlMode currentMode = NetworkControlMode.UNKNOWN;
    private NetworkControlMode ruleMode = NetworkControlMode.UNKNOWN;
    private @Nullable ZonedDateTime overrideUntil;
    private boolean override;
    private List<String> macs = List.of();
    private List<LanHost> hosts = List.of();
    private int resolution;
    private List<CDayRange> cdayranges = List.of();

    public int getProfileId() {
        return profileId;
    }

    public @Nullable ZonedDateTime getNextChange() {
        return nextChange;
    }

    public NetworkControlMode getOverrideMode() {
        return overrideMode;
    }

    public NetworkControlMode getCurrentMode() {
        return currentMode;
    }

    public NetworkControlMode getRuleMode() {
        return ruleMode;
    }

    public @Nullable ZonedDateTime getOverrideUntil() {
        return overrideUntil;
    }

    public boolean isOverride() {
        return override;
    }

    public List<String> getMacs() {
        return macs;
    }

    public List<LanHost> getHosts() {
        return hosts;
    }

    public int getResolution() {
        return resolution;
    }

    public List<CDayRange> getCdayranges() {
        return cdayranges;
    }
}
