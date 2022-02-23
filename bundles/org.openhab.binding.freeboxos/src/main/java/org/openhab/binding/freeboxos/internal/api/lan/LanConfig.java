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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LanConfig} is the Java class used to map the "LanConfig"
 * structure used by the LAN configuration API
 * https://dev.freebox.fr/sdk/os/lan/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanConfig implements ConnectivityData {
    public static class LanConfigResponse extends Response<LanConfig> {
    }

    public static enum NetworkMode {
        UNKNOWN,
        @SerializedName("router")
        ROUTER,
        @SerializedName("bridge")
        BRIDGE;
    }

    private @NonNullByDefault({}) String name;
    private @Nullable String ip;
    private @Nullable NetworkMode mode;

    public NetworkMode getMode() {
        NetworkMode localMode = mode;
        return localMode != null ? localMode : NetworkMode.UNKNOWN;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isReachable() {
        return true;
    }

    @Override
    public @Nullable ZonedDateTime getLastSeen() {
        return ZonedDateTime.now();
    }

    @Override
    public @Nullable String getIpv4() {
        return ip;
    }
}
