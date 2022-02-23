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
package org.openhab.binding.freeboxos.internal.api.player;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PlayerStatus} is the Java class used to map the "ConnectionStatus"
 * structure used by the connection API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerStatus {
    public static class PlayerStatusResponse extends Response<PlayerStatus> {
    }

    public static enum PowerState {
        UNKNOWN,
        @SerializedName("standby")
        STANDBY,
        @SerializedName("running")
        RUNNING;
    }

    private @Nullable PowerState powerState;

    public PowerState getPowerState() {
        PowerState power = powerState;
        return power != null ? power : PowerState.UNKNOWN;
    }
}
