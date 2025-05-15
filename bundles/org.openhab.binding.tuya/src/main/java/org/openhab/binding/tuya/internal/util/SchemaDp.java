/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;

import com.google.gson.Gson;

/**
 * The {@link SchemaDp} is a wrapper for the information of a single datapoint
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SchemaDp {
    private static final Map<String, String> REMOTE_LOCAL_TYPE_MAP = Map.of( //
            "Boolean", "bool", //
            "Enum", "enum", //
            "Integer", "value", //
            "String", "string", //
            "Json", "string");

    public int id = 0;
    public String code = "";
    public String type = "";
    public String label = "";
    public @Nullable Double min;
    public @Nullable Double max;
    public @Nullable List<String> range;

    public static SchemaDp fromRemoteSchema(Gson gson, DeviceSchema.Description function) {
        SchemaDp schemaDp = new SchemaDp();
        schemaDp.code = function.code.replace("_v2", "");
        schemaDp.id = function.dp_id;
        schemaDp.type = REMOTE_LOCAL_TYPE_MAP.getOrDefault(function.type, "raw"); // fallback to raw

        if ("enum".equalsIgnoreCase(schemaDp.type) && function.values.contains("range")) {
            schemaDp.range = Objects.requireNonNull(
                    gson.fromJson(function.values.replaceAll("\\\\", ""), DeviceSchema.EnumRange.class)).range;
        } else if ("value".equalsIgnoreCase(schemaDp.type) && function.values.contains("min")
                && function.values.contains("max")) {
            DeviceSchema.NumericRange numericRange = Objects.requireNonNull(
                    gson.fromJson(function.values.replaceAll("\\\\", ""), DeviceSchema.NumericRange.class));
            schemaDp.min = numericRange.min;
            schemaDp.max = numericRange.max;
        }

        return schemaDp;
    }
}
