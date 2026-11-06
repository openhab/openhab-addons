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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the system mode status
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - System mode permanence and expiry
 *
 */
public class SystemModeStatus {

    @SerializedName("mode")
    private String mode;

    @SerializedName("isPermanent")
    private Boolean isPermanent;

    @SerializedName("timeUntil")
    private String timeUntil;

    public String getMode() {
        return mode;
    }

    public @Nullable Boolean getIsPermanent() {
        return isPermanent;
    }

    public @Nullable String getTimeUntil() {
        return timeUntil;
    }
}
