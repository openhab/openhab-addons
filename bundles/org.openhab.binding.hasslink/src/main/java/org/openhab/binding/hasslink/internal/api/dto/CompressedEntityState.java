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
package org.openhab.binding.hasslink.internal.api.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Represents entity state data within Home Assistant compressed WebSocket updates
 * ({@code subscribe_entities}). Maps compressed property keys to entity state fields.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CompressedEntityState {

    /** The entity state string value (e.g. "on", "off", "21.5"). */
    @SerializedName("s")
    public @Nullable String state;

    /** Map of modified or initial state attribute key-value pairs. */
    @SerializedName("a")
    public @Nullable Map<String, JsonElement> attributes;

    /** Unix epoch timestamp (seconds) when the entity state last changed. */
    @SerializedName("lc")
    public @Nullable Double lastChanged;

    /** Unix epoch timestamp (seconds) when the entity was last updated in Home Assistant. */
    @SerializedName("lu")
    public @Nullable Double lastUpdated;

    /** Home Assistant context metadata identifier or object associated with the state update. */
    @SerializedName("c")
    public @Nullable Object context;
}
