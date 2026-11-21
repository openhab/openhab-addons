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
 * Represents a state change delta for a single entity in Home Assistant compressed
 * WebSocket event updates ({@code subscribe_entities}).
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CompressedEntityDiff {

    /** Partial or full state updates to add or merge into the cached entity state. */
    @SerializedName("+")
    public @Nullable CompressedEntityState add;

    /** Map of attribute keys removed from the entity state during this update. */
    @SerializedName("-")
    public @Nullable Map<String, JsonElement> remove;
}
