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
public class ChatRequestBody {

    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    @JsonProperty("top_p")
    private Double topP;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private String user;
    private List<ChatTools> tools;
    @JsonProperty("tool_choice")
    private String toolChoice;

    public String getModel() {
        return model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public String getUser() {
        return user;
    }

    public List<ChatTools> getTools() {
        return tools;
    }

    public String getToolChoice() {
        return toolChoice;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setTools(List<ChatTools> tools) {
        this.tools = tools;
    }

    public void setToolChoice(String toolChoice) {
        this.toolChoice = toolChoice;
    }
}
