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
package org.openhab.binding.bmwcardata.internal.api.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * The value of a single technical descriptor. The same representation is used by the REST API and the stream.
 * Depending on the descriptor, {@code value} is a JSON number, boolean or string.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class TelematicValue {
    public @Nullable JsonElement value;
    public @Nullable String unit;
    public @Nullable String timestamp;

    /**
     * Response of {@code GET /customers/vehicles/{vin}/telematicData}.
     */
    public static class TelematicDataResponse {
        public @Nullable Map<String, TelematicValue> telematicData;
    }

    /**
     * Message published by the streaming broker on topic {@code <gcid>/<vin>}.
     */
    public static class StreamMessage {
        public @Nullable String vin;
        public @Nullable Map<String, TelematicValue> data;
    }
}
