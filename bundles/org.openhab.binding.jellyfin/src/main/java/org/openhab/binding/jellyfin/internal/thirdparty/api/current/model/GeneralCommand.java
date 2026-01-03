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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * GeneralCommand
 */
@JsonPropertyOrder({ GeneralCommand.JSON_PROPERTY_NAME, GeneralCommand.JSON_PROPERTY_CONTROLLING_USER_ID,
        GeneralCommand.JSON_PROPERTY_ARGUMENTS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class GeneralCommand {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private GeneralCommandType name;

    public static final String JSON_PROPERTY_CONTROLLING_USER_ID = "ControllingUserId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID controllingUserId;

    public static final String JSON_PROPERTY_ARGUMENTS = "Arguments";
    @org.eclipse.jdt.annotation.Nullable
    private Map<String, String> arguments = new HashMap<>();

    public GeneralCommand() {
    }

    public GeneralCommand name(@org.eclipse.jdt.annotation.Nullable GeneralCommandType name) {
        this.name = name;
        return this;
    }

    /**
     * This exists simply to identify a set of known commands.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GeneralCommandType getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable GeneralCommandType name) {
        this.name = name;
    }

    public GeneralCommand controllingUserId(@org.eclipse.jdt.annotation.Nullable UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
        return this;
    }

    /**
     * Get controllingUserId
     * 
     * @return controllingUserId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getControllingUserId() {
        return controllingUserId;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setControllingUserId(@org.eclipse.jdt.annotation.Nullable UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public GeneralCommand arguments(@org.eclipse.jdt.annotation.Nullable Map<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public GeneralCommand putArgumentsItem(String key, String argumentsItem) {
        if (this.arguments == null) {
            this.arguments = new HashMap<>();
        }
        this.arguments.put(key, argumentsItem);
        return this;
    }

    /**
     * Get arguments
     * 
     * @return arguments
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ARGUMENTS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getArguments() {
        return arguments;
    }

    @JsonProperty(value = JSON_PROPERTY_ARGUMENTS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setArguments(@org.eclipse.jdt.annotation.Nullable Map<String, String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Return true if this GeneralCommand object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneralCommand generalCommand = (GeneralCommand) o;
        return Objects.equals(this.name, generalCommand.name)
                && Objects.equals(this.controllingUserId, generalCommand.controllingUserId)
                && Objects.equals(this.arguments, generalCommand.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, controllingUserId, arguments);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GeneralCommand {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    controllingUserId: ").append(toIndentedString(controllingUserId)).append("\n");
        sb.append("    arguments: ").append(toIndentedString(arguments)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `ControllingUserId` to the URL query string
        if (getControllingUserId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sControllingUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getControllingUserId()))));
        }

        // add `Arguments` to the URL query string
        if (getArguments() != null) {
            for (String _key : getArguments().keySet()) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sArguments%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key,
                                        containerSuffix),
                        getArguments().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getArguments().get(_key)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private GeneralCommand instance;

        public Builder() {
            this(new GeneralCommand());
        }

        protected Builder(GeneralCommand instance) {
            this.instance = instance;
        }

        public GeneralCommand.Builder name(GeneralCommandType name) {
            this.instance.name = name;
            return this;
        }

        public GeneralCommand.Builder controllingUserId(UUID controllingUserId) {
            this.instance.controllingUserId = controllingUserId;
            return this;
        }

        public GeneralCommand.Builder arguments(Map<String, String> arguments) {
            this.instance.arguments = arguments;
            return this;
        }

        /**
         * returns a built GeneralCommand instance.
         *
         * The builder is not reusable.
         */
        public GeneralCommand build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static GeneralCommand.Builder builder() {
        return new GeneralCommand.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public GeneralCommand.Builder toBuilder() {
        return new GeneralCommand.Builder().name(getName()).controllingUserId(getControllingUserId())
                .arguments(getArguments());
    }
}
