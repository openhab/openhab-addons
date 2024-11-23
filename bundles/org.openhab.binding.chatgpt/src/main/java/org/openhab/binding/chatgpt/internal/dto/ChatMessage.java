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
package org.openhab.binding.chatgpt.internal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system"),
        TOOL("tool");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    private String role;
    private String content;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    private String name;

    @JsonProperty("function_call")
    ChatFunctionCall functionCall;

    @JsonProperty("tool_calls")
    List<ChatToolCalls> toolCalls;

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public ChatFunctionCall getFunctionCall() {
        return functionCall;
    }

    public List<ChatToolCalls> getToolCalls() {
        return toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFunctionCall(ChatFunctionCall functionCall) {
        this.functionCall = functionCall;
    }

    public void setToolCalls(List<ChatToolCalls> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
