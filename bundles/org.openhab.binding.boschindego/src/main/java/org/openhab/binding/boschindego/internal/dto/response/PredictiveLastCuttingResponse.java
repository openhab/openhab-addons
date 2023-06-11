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
package org.openhab.binding.boschindego.internal.dto.response;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response for last cutting time.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class PredictiveLastCuttingResponse {
    @SerializedName("last_mowed")
    public String lastCutting;

    public @Nullable Instant getLastCutting() {
        try {
            return ZonedDateTime.parse(lastCutting).toInstant();
        } catch (final DateTimeParseException e) {
            // Ignored
        }
        return null;
    }
}
