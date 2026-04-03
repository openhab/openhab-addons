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
 * DTO for install software update command.
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
