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
package org.openhab.binding.freeboxos.internal.api.lan;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.NetworkMode;

/**
 * The {@link LanConfig} is the Java class used to map the "LanConfig" structure used by the LAN configuration API
 *
 * https://dev.freebox.fr/sdk/os/lan/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanConfig {
    private @Nullable String ip;
    private @Nullable String name;
    private @Nullable String nameDns;
    private @Nullable String nameMdns;
    private @Nullable String nameNetbios;
    private NetworkMode mode = NetworkMode.UNKNOWN;

    public String getIp() {
        return Objects.requireNonNull(ip);
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public String getNameDns() {
        return Objects.requireNonNull(nameDns);
    }

    public String getNameMdns() {
        return Objects.requireNonNull(nameMdns);
    }

    public String getNameNetbios() {
        return Objects.requireNonNull(nameNetbios);
    }

    public NetworkMode getMode() {
        return mode;
    }

}
