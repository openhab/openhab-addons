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
package org.openhab.binding.veluxactive.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Envelope shared by VELUX API responses.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ApiResponse {
    public @Nullable String status;
    @SerializedName("time_server")
    public @Nullable Long timeServer;
    public @Nullable JsonElement body;
    public @Nullable JsonElement error;
}
