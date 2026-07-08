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
package org.openhab.binding.chatgpt.internal.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.voice.text.interpreter.llm.LLMToolCall;

import com.google.gson.JsonSyntaxException;

/**
 * A DTO to store information about a ChatGPT tool call.
 * Extends {@link LLMToolCall} with additional fields required by the OpenAI API.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ChatGPTLLMToolCall extends LLMToolCall {
    public final @Nullable String id;

    public ChatGPTLLMToolCall(String tool, Map<String, Object> params, @Nullable String id) {
        super(tool, params);
        this.id = id;
    }

    public static ChatGPTLLMToolCall fromJson(String json) throws JsonSyntaxException {
        ChatGPTLLMToolCall call = GSON.fromJson(json, ChatGPTLLMToolCall.class);
        if (call == null) {
            throw new JsonSyntaxException("Deserialized ChatGPTLLMToolCall is null.");
        }
        return call;
    }
}
