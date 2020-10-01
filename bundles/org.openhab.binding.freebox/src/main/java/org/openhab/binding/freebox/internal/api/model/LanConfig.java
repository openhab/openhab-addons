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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanConfig} is the Java class used to map the "LanConfig"
 * structure used by the LAN configuration API
 * https://dev.freebox.fr/sdk/os/lan/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class LanConfig {
    public static enum NetworkMode {
        UNKNOWN,
        @SerializedName("router")
        ROUTER,
        @SerializedName("bridge")
        BRIDGE;
    }

    private String ip = "";
    private String name = "";
    private String nameDns = "";
    private String nameMdns = "";
    private String nameNetbios = "";
    private NetworkMode type = NetworkMode.UNKNOWN;

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public String getNameDns() {
        return nameDns;
    }

    public String getNameMdns() {
        return nameMdns;
    }

    public String getNameNetbios() {
        return nameNetbios;
    }

    public NetworkMode getType() {
        return type;
    }
}
