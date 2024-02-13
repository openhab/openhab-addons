/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.moonraker.internal;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link RPCResponse} encapsulates JSON-RPC 2.0 responses used by
 * {@link MoonrakerWebSocket}.
 *
 * @author Arjan Mels - Initial contribution
 */
public class RPCResponse {
    public class RPCError {
        Integer code;
        String message;
        @Nullable
        JsonObject data;
    };

    String jsonrpc;
    @Nullable
    String id;
    JsonElement result;
    RPCError error;
    @Nullable
    String method;
    @Nullable
    JsonArray params;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
