/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * This is a dto used for parsing the JSON response from ChatGPT.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ChatResponse {

    private List<Choice> choices;
    private String id;
    private String object;
    private int created;
    private String model;

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

    public static class Choice {
        private Message message;

        @SerializedName("finish_reason")
        private String finishReason;
        private int index;

        public Message getMessage() {
            return message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public int getIndex() {
            return index;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
