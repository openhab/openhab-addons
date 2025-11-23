/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.chatgpt.internal.dto.functions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
public class CreateIntent {
    private String name;
    private Entities entities;
    private List<String> matchedItems;
    private String answer;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("entities")
    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

    @JsonProperty("matched_items")
    public List<String> getMatchedItems() {
        return matchedItems != null ? matchedItems : List.of();
    }

    public void setMatchedItems(List<String> matchedItems) {
        this.matchedItems = matchedItems;
    }

    @JsonProperty("answer")
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static class Entities {
        private String object;
        private String location;
        private String period;

        @JsonProperty("object")
        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        @JsonProperty("location")
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @JsonProperty("period")
        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }
    }
}
