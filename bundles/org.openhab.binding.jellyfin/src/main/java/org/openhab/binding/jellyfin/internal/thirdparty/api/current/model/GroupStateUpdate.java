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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class GroupStateUpdate.
 */
@JsonPropertyOrder({ GroupStateUpdate.JSON_PROPERTY_STATE, GroupStateUpdate.JSON_PROPERTY_REASON })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class GroupStateUpdate {
    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.Nullable
    private GroupStateType state;

    public static final String JSON_PROPERTY_REASON = "Reason";
    @org.eclipse.jdt.annotation.Nullable
    private PlaybackRequestType reason;

    public GroupStateUpdate() {
    }

    public GroupStateUpdate state(@org.eclipse.jdt.annotation.Nullable GroupStateType state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the state of the group.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupStateType getState() {
        return state;
    }

    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.Nullable GroupStateType state) {
        this.state = state;
    }

    public GroupStateUpdate reason(@org.eclipse.jdt.annotation.Nullable PlaybackRequestType reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Gets the reason of the state change.
     * 
     * @return reason
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REASON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlaybackRequestType getReason() {
        return reason;
    }

    @JsonProperty(value = JSON_PROPERTY_REASON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReason(@org.eclipse.jdt.annotation.Nullable PlaybackRequestType reason) {
        this.reason = reason;
    }

    /**
     * Return true if this GroupStateUpdate object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupStateUpdate groupStateUpdate = (GroupStateUpdate) o;
        return Objects.equals(this.state, groupStateUpdate.state)
                && Objects.equals(this.reason, groupStateUpdate.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, reason);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupStateUpdate {\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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

        // add `State` to the URL query string
        if (getState() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sState%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getState()))));
        }

        // add `Reason` to the URL query string
        if (getReason() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sReason%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReason()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private GroupStateUpdate instance;

        public Builder() {
            this(new GroupStateUpdate());
        }

        protected Builder(GroupStateUpdate instance) {
            this.instance = instance;
        }

        public GroupStateUpdate.Builder state(GroupStateType state) {
            this.instance.state = state;
            return this;
        }

        public GroupStateUpdate.Builder reason(PlaybackRequestType reason) {
            this.instance.reason = reason;
            return this;
        }

        /**
         * returns a built GroupStateUpdate instance.
         *
         * The builder is not reusable.
         */
        public GroupStateUpdate build() {
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
    public static GroupStateUpdate.Builder builder() {
        return new GroupStateUpdate.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public GroupStateUpdate.Builder toBuilder() {
        return new GroupStateUpdate.Builder().state(getState()).reason(getReason());
    }
}
