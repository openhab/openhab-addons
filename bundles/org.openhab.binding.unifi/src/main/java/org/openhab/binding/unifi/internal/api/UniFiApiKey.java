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
package org.openhab.binding.unifi.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * API key model from the UniFi OS user management endpoints ({@code /proxy/users/api/v2/...}).
 * These keys are console-wide and can be used by any application (Protect, Access, Network).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiApiKey {
    public @Nullable String id;
    public @Nullable String name;

    @SerializedName("masked_api_key")
    public @Nullable String maskedApiKey;

    @SerializedName("full_api_key")
    public @Nullable String fullApiKey;

    @SerializedName("creator_user_id")
    public @Nullable String creatorUserId;
}
