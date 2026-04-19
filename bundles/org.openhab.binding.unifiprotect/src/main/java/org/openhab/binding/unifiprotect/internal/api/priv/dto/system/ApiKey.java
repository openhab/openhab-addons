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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * API Key model from UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ApiKey {
    public String id;
    public String name;

    @SerializedName("masked_api_key")
    public String maskedApiKey;

    @SerializedName("full_api_key")
    public String fullApiKey;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("last_used_at")
    public String lastUsedAt;

    public Map<String, List<String>> permissions;

    @SerializedName("key_permissions")
    public List<String> keyPermissions;

    public List<String> scopes;

    @SerializedName("creator_user_id")
    public String creatorUserId;
}
