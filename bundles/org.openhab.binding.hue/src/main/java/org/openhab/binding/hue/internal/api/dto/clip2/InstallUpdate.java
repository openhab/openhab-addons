/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Unofficial DTO for install software update command.
 * 
 * Note: this is not documented in the official Hue API, so the implementation is inferred from the
 * <a href="https://github.com/openhue/openhue-api/tree/main/src/device_software_update">OpenHue API</a>
 * which is not (yet) fully tested or confirmed to work.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class InstallUpdate {
    private @Nullable @SerializedName("install_state") String installState;

    /**
     * Set the install software update command parameter.
     */
    public InstallUpdate setInstallUpdate() {
        installState = "install";
        return this;
    }
}
