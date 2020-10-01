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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanHostName} is the Java class used to map the "LanHostName"
 * structure used by the Lan Hosts Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class LanHostName {
    public static enum NameSource {
        UNKNOWN,
        @SerializedName("dhcp")
        DHCP,
        @SerializedName("netbios")
        NETBIOS,
        @SerializedName("mdns")
        MDNS,
        @SerializedName("upnp")
        UPNP;
    }

    private String name = "";
    private @Nullable NameSource source;

    public String getName() {
        return name;
    }

    public NameSource getSource() {
        return source != null ? source : NameSource.UNKNOWN;
    }
}
