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
 * This is a dto used for parsing the JSON response from ChatGPT.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Artur Fedjukevits - Added fields and edited the class
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    private List<Choice> choices;
    private String id;
    private String object;
    private int created;
    private String model;
    private Usage usage;

    public List<Choice> getChoices() {
        return choices;
    }

    public String getId() {
        return id;
    }

    public int getCreated() {
        return created;
    }

    public String getObject() {
        return object;
    }

    public String getModel() {
        return model;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        @JsonProperty("message")
        private ChatMessage chatMessage;

        @JsonProperty("finish_reason")
        private String finishReason;
        private int index;

        public ChatMessage getChatMessage() {
            return chatMessage;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public int getIndex() {
            return index;
        }

        public void setChatMessage(ChatMessage chatMessage) {
            this.chatMessage = chatMessage;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("total_tokens")
        private int totalTokens;

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
