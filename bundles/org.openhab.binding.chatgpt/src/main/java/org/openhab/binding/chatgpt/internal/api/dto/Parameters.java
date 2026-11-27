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
package org.openhab.binding.chatgpt.internal.api.dto;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameters {

    private String type;
    private Map<String, Property> properties;
    private List<String> required;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public static class Property {

        private String type;
        private String description;
        @JsonProperty("enum")
        private @Nullable List<String> enumValues;
        private @Nullable Property items;

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public @Nullable List<String> getEnumValues() {
            return enumValues;
        }

        public @Nullable Property getItems() {
            return items;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setEnumValues(@Nullable List<String> enumValues) {
            this.enumValues = enumValues;
        }

        public void setItems(@Nullable Property items) {
            this.items = items;
        }
    }
}
