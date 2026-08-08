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
package org.openhab.binding.gemini.internal.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.voice.text.interpreter.llm.LLMToolCall;

import com.google.gson.JsonSyntaxException;

/**
 * A DTO to store information about a Gemini tool call.
 * Extends {@link LLMToolCall} with additional fields required by the Gemini API.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiLLMToolCall extends LLMToolCall {
    public final @Nullable String id;
    public final @Nullable String thoughtSignature;

    public GeminiLLMToolCall(String tool, Map<String, Object> params, @Nullable String id,
            @Nullable String thoughtSignature) {
        super(tool, params);
        this.id = id;
        this.thoughtSignature = thoughtSignature;
    }

    public static GeminiLLMToolCall fromJson(String json) throws JsonSyntaxException {
        GeminiLLMToolCall call = GSON.fromJson(json, GeminiLLMToolCall.class);
        if (call == null) {
            throw new JsonSyntaxException("Deserialized GeminiLLMToolCall is null.");
        }
        // Gson bypasses the constructor, so fields declared non-null can still be null after
        // deserialization; Objects.isNull avoids the "redundant null check" compiler warning
        if (Objects.isNull(call.tool) || Objects.isNull(call.params)) {
            throw new JsonSyntaxException("Deserialized GeminiLLMToolCall has null tool or params.");
        }
        return call;
    }

    /**
     * Serializes a batch of parallel tool calls into a single JSON array string.
     */
    public static String toJsonList(List<GeminiLLMToolCall> calls) {
        return GSON.toJson(calls);
    }

    /**
     * Deserializes either a single tool call (JSON object, the legacy format) or a batch of
     * parallel tool calls (JSON array) into a list.
     */
    public static List<GeminiLLMToolCall> listFromJson(String json) throws JsonSyntaxException {
        if (!json.trim().startsWith("[")) {
            return List.of(fromJson(json));
        }
        GeminiLLMToolCall[] calls = GSON.fromJson(json, GeminiLLMToolCall[].class);
        if (calls == null) {
            throw new JsonSyntaxException("Deserialized GeminiLLMToolCall list is null.");
        }
        for (GeminiLLMToolCall call : calls) {
            if (call == null || Objects.isNull(call.tool) || Objects.isNull(call.params)) {
                throw new JsonSyntaxException("Deserialized GeminiLLMToolCall has null tool or params.");
            }
        }
        return List.of(calls);
    }

    /**
     * Serializes the results of a batch of parallel tool calls into a JSON array string.
     */
    public static String resultsToJson(List<String> results) {
        return GSON.toJson(results);
    }

    /**
     * Deserializes the results of a batch of parallel tool calls from a JSON array string.
     */
    public static List<String> resultsFromJson(String json) throws JsonSyntaxException {
        String[] results = GSON.fromJson(json, String[].class);
        if (results == null) {
            throw new JsonSyntaxException("Deserialized tool call results list is null.");
        }
        return List.of(results);
    }
}
