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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for the mode
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Exact system mode expiry
 *
 */
public class Mode {

    Mode(String mode, @Nullable Instant until) {
        systemMode = mode;
        timeUntil = until == null ? null : until.truncatedTo(ChronoUnit.SECONDS).toString();
        permanent = until == null;
    }

    @SuppressWarnings("unused")
    @SerializedName("systemMode")
    private String systemMode;

    @SuppressWarnings("unused")
    @SerializedName("timeUntil")
    private String timeUntil;

    @SuppressWarnings("unused")
    @SerializedName("permanent")
    private boolean permanent;
}
