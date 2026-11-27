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
    /**
     * {@code true} when this call was made in the same model turn as the one stored in the preceding
     * TOOL_CALL conversation message (parallel function calling), {@code null} otherwise. Gson omits
     * the field when null, so single calls keep the original message format.
     */
    public final @Nullable Boolean parallel;

    public GeminiLLMToolCall(String tool, Map<String, Object> params, @Nullable String id,
            @Nullable String thoughtSignature) {
        this(tool, params, id, thoughtSignature, null);
    }

    public GeminiLLMToolCall(String tool, Map<String, Object> params, @Nullable String id,
            @Nullable String thoughtSignature, @Nullable Boolean parallel) {
        super(tool, params);
        this.id = id;
        this.thoughtSignature = thoughtSignature;
        this.parallel = parallel;
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
}
