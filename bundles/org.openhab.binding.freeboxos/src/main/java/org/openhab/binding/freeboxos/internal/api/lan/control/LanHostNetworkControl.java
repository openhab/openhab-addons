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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.NetworkControlMode;

/**
 * The {@link LanHostNetworkControl} is the Java class used to map the "LanHostNetworkControl" structure used by the Lan
 * Hosts Browser API
 *
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanHostNetworkControl {
    private @Nullable String profileId;
    private @Nullable String name;
    private NetworkControlMode currentMode = NetworkControlMode.UNKNOWN;

    public String getProfileId() {
        return Objects.requireNonNull(profileId);
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public NetworkControlMode getCurrentMode() {
        return currentMode;
    }
}
