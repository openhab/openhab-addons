/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link L3Connectivity} is the Java class used to map the "LanHostL3Connectivity"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class L3Connectivity {
    private static enum L3Af {
        UNKNOWN,
        @SerializedName("ipv4")
        IPV4,
        @SerializedName("ipv6")
        IPV6;
    }

    private @NonNullByDefault({}) String addr;
    private @Nullable L3Af af;
    private boolean active;

    private L3Af getAf() {
        L3Af localAf = af;
        return localAf != null ? localAf : L3Af.UNKNOWN;
    }

    public @Nullable String getIpv4() {
        return getAf() == L3Af.IPV4 ? addr : null;
    }

    public boolean isActive() {
        return active;
    }
}
