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
        if (call.tool == null || call.params == null) {
            throw new JsonSyntaxException("Deserialized GeminiLLMToolCall has null tool or params.");
        }
        return call;
    }
}
