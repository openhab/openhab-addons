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
package org.openhab.binding.freeboxos.internal.api.player;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.PowerState;

/**
 * The {@link PlayerStatus} is the Java class used to map the "ConnectionStatus" structure used by the connection API
 *
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerStatus {
    private PowerState powerState = PowerState.UNKNOWN;
    private @Nullable PlayerStatusInformation player;
    private @Nullable PlayerStatusForegroundApp foregroundApp;

    public PowerState getPowerState() {
        return powerState;
    }

    public PlayerStatusInformation getPlayer() {
        return Objects.requireNonNull(player);
    }

    public PlayerStatusForegroundApp getForegroundApp() {
        return Objects.requireNonNull(foregroundApp);
    }
}
