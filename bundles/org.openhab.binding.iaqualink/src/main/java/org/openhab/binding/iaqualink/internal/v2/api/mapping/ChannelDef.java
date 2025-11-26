/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.v2.api.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import net.minidev.json.JSONArray;

/**
 * Definition of a channel, paired with a JSON path to get or set associated values.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class ChannelDef {
    String id;
    String label;
    String itemType;
    String typeId;
    JsonPath valuePath;
    @Nullable
    JsonPath appliesPath;

    public ChannelDef(String id, String label, String itemType, String typeId, String valuePath,
            @Nullable String appliesPath) {
        this.id = id;
        this.label = label;
        this.itemType = itemType;
        this.typeId = typeId;
        this.valuePath = JsonPath.compile(valuePath);

        if (!this.valuePath.isDefinite()) {
            throw new IllegalArgumentException("valuePath must be definite: " + valuePath);
        }

        if (appliesPath != null) {
            this.appliesPath = JsonPath.compile(appliesPath);
        }
    }

    public ChannelDef(String id, String label, String itemType, String typeId, String valuePath) {
        this(id, label, itemType, typeId, valuePath, null);
    }

    public String id() {
        return id;
    }

    public String itemType() {
        return itemType;
    }

    public String label() {
        return label;
    }

    public String typeId() {
        return typeId;
    }

    public @Nullable Object value(DeviceState deviceState) {
        return tryValue(deviceState, valuePath);
    }

    public boolean appliesToState(DeviceState deviceState) {
        JsonPath pathToUse = Objects.requireNonNullElse(appliesPath, valuePath);
        return !isEmptyResult(tryValue(deviceState, pathToUse));
    }

    public DeviceState updateJson(Object value) {
        DocumentContext ctx = JsonPath.parse("{}");
        String[] segments = valuePath.getPath().split("(?=\\[)");

        for (int i = 1; i < segments.length; i++) {
            String subpath = String.join("", Arrays.copyOfRange(segments, 0, i));
            Object toPut = i == segments.length - 1 ? value : new HashMap<>();
            String key = segments[i].substring(2, segments[i].length() - 2);
            ctx = ctx.put(subpath, key, toPut);
        }

        return new DeviceState(ctx);
    }

    private static boolean isEmptyResult(@Nullable Object result) {
        return result == null || result instanceof JSONArray && ((JSONArray) result).isEmpty();
    }

    private static @Nullable Object tryValue(DeviceState deviceState, JsonPath path) {
        try {
            return deviceState.documentContext.read(path);
        } catch (PathNotFoundException pnfe) {
            return null;
        }
    }
}
