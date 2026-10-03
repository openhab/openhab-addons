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
package org.openhab.binding.gme.internal.api;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePriceEntry;
import org.openhab.binding.gme.internal.model.GmePriceEntryMapper;
import org.openhab.binding.gme.internal.model.GmePriceEntryRaw;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parses GME zonal price data.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePriceDataParser {

    private GmePriceDataParser() {
    }

    public static List<GmePriceEntry> parse(Reader reader) {
        return parse(reader, GmeGranularity.PT60);
    }

    public static List<GmePriceEntry> parse(Reader reader, GmeGranularity granularity) {
        JsonElement root = JsonParser.parseReader(reader);

        if (!root.isJsonArray()) {
            throw new IllegalArgumentException("Expected GME price data to be a JSON array");
        }

        List<GmePriceEntry> entries = new ArrayList<>();

        for (JsonElement element : root.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject object = element.getAsJsonObject();

            GmePriceEntryRaw raw = new GmePriceEntryRaw(getRequiredString(object, "FlowDate"),
                    getRequiredString(object, "Hour"), getRequiredString(object, "Market"),
                    getRequiredString(object, "Zone"), getRequiredString(object, "Price"),
                    getRequiredString(object, "Period"), getOptionalString(object, "Notes"));

            GmePriceEntryMapper.map(raw, granularity).ifPresent(entries::add);
        }

        return entries;
    }

    private static String getRequiredString(JsonObject object, String name) {
        JsonElement element = object.get(name);

        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing required GME field: " + name);
        }

        return element.getAsString();
    }

    private static @Nullable String getOptionalString(JsonObject object, String name) {
        JsonElement element = object.get(name);

        if (element == null || element.isJsonNull()) {
            return null;
        }

        return element.getAsString();
    }
}
