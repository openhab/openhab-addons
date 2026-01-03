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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ForgotPasswordResult
 */
@JsonPropertyOrder({ ForgotPasswordResult.JSON_PROPERTY_ACTION, ForgotPasswordResult.JSON_PROPERTY_PIN_FILE,
        ForgotPasswordResult.JSON_PROPERTY_PIN_EXPIRATION_DATE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ForgotPasswordResult {
    public static final String JSON_PROPERTY_ACTION = "Action";
    @org.eclipse.jdt.annotation.Nullable
    private ForgotPasswordAction action;

    public static final String JSON_PROPERTY_PIN_FILE = "PinFile";
    @org.eclipse.jdt.annotation.Nullable
    private String pinFile;

    public static final String JSON_PROPERTY_PIN_EXPIRATION_DATE = "PinExpirationDate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime pinExpirationDate;

    public ForgotPasswordResult() {
    }

    public ForgotPasswordResult action(@org.eclipse.jdt.annotation.Nullable ForgotPasswordAction action) {
        this.action = action;
        return this;
    }

    /**
     * Gets or sets the action.
     * 
     * @return action
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ForgotPasswordAction getAction() {
        return action;
    }

    @JsonProperty(value = JSON_PROPERTY_ACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAction(@org.eclipse.jdt.annotation.Nullable ForgotPasswordAction action) {
        this.action = action;
    }

    public ForgotPasswordResult pinFile(@org.eclipse.jdt.annotation.Nullable String pinFile) {
        this.pinFile = pinFile;
        return this;
    }

    /**
     * Gets or sets the pin file.
     * 
     * @return pinFile
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PIN_FILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPinFile() {
        return pinFile;
    }

    @JsonProperty(value = JSON_PROPERTY_PIN_FILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPinFile(@org.eclipse.jdt.annotation.Nullable String pinFile) {
        this.pinFile = pinFile;
    }

    public ForgotPasswordResult pinExpirationDate(
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime pinExpirationDate) {
        this.pinExpirationDate = pinExpirationDate;
        return this;
    }

    /**
     * Gets or sets the pin expiration date.
     * 
     * @return pinExpirationDate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PIN_EXPIRATION_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getPinExpirationDate() {
        return pinExpirationDate;
    }

    @JsonProperty(value = JSON_PROPERTY_PIN_EXPIRATION_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPinExpirationDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime pinExpirationDate) {
        this.pinExpirationDate = pinExpirationDate;
    }

    /**
     * Return true if this ForgotPasswordResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForgotPasswordResult forgotPasswordResult = (ForgotPasswordResult) o;
        return Objects.equals(this.action, forgotPasswordResult.action)
                && Objects.equals(this.pinFile, forgotPasswordResult.pinFile)
                && Objects.equals(this.pinExpirationDate, forgotPasswordResult.pinExpirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, pinFile, pinExpirationDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ForgotPasswordResult {\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    pinFile: ").append(toIndentedString(pinFile)).append("\n");
        sb.append("    pinExpirationDate: ").append(toIndentedString(pinExpirationDate)).append("\n");
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

        // add `Action` to the URL query string
        if (getAction() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAction%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAction()))));
        }

        // add `PinFile` to the URL query string
        if (getPinFile() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPinFile%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPinFile()))));
        }

        // add `PinExpirationDate` to the URL query string
        if (getPinExpirationDate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPinExpirationDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPinExpirationDate()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ForgotPasswordResult instance;

        public Builder() {
            this(new ForgotPasswordResult());
        }

        protected Builder(ForgotPasswordResult instance) {
            this.instance = instance;
        }

        public ForgotPasswordResult.Builder action(ForgotPasswordAction action) {
            this.instance.action = action;
            return this;
        }

        public ForgotPasswordResult.Builder pinFile(String pinFile) {
            this.instance.pinFile = pinFile;
            return this;
        }

        public ForgotPasswordResult.Builder pinExpirationDate(OffsetDateTime pinExpirationDate) {
            this.instance.pinExpirationDate = pinExpirationDate;
            return this;
        }

        /**
         * returns a built ForgotPasswordResult instance.
         *
         * The builder is not reusable.
         */
        public ForgotPasswordResult build() {
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
    public static ForgotPasswordResult.Builder builder() {
        return new ForgotPasswordResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ForgotPasswordResult.Builder toBuilder() {
        return new ForgotPasswordResult.Builder().action(getAction()).pinFile(getPinFile())
                .pinExpirationDate(getPinExpirationDate());
    }
}
