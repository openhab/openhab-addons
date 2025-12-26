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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * PinRedeemResult
 */
@JsonPropertyOrder({ PinRedeemResult.JSON_PROPERTY_SUCCESS, PinRedeemResult.JSON_PROPERTY_USERS_RESET })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PinRedeemResult {
    public static final String JSON_PROPERTY_SUCCESS = "Success";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean success;

    public static final String JSON_PROPERTY_USERS_RESET = "UsersReset";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> usersReset = new ArrayList<>();

    public PinRedeemResult() {
    }

    public PinRedeemResult success(@org.eclipse.jdt.annotation.NonNull Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Users.PinRedeemResult is success.
     * 
     * @return success
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUCCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSuccess() {
        return success;
    }

    @JsonProperty(value = JSON_PROPERTY_SUCCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuccess(@org.eclipse.jdt.annotation.NonNull Boolean success) {
        this.success = success;
    }

    public PinRedeemResult usersReset(@org.eclipse.jdt.annotation.NonNull List<String> usersReset) {
        this.usersReset = usersReset;
        return this;
    }

    public PinRedeemResult addUsersResetItem(String usersResetItem) {
        if (this.usersReset == null) {
            this.usersReset = new ArrayList<>();
        }
        this.usersReset.add(usersResetItem);
        return this;
    }

    /**
     * Gets or sets the users reset.
     * 
     * @return usersReset
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USERS_RESET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getUsersReset() {
        return usersReset;
    }

    @JsonProperty(value = JSON_PROPERTY_USERS_RESET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsersReset(@org.eclipse.jdt.annotation.NonNull List<String> usersReset) {
        this.usersReset = usersReset;
    }

    /**
     * Return true if this PinRedeemResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PinRedeemResult pinRedeemResult = (PinRedeemResult) o;
        return Objects.equals(this.success, pinRedeemResult.success)
                && Objects.equals(this.usersReset, pinRedeemResult.usersReset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, usersReset);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PinRedeemResult {\n");
        sb.append("    success: ").append(toIndentedString(success)).append("\n");
        sb.append("    usersReset: ").append(toIndentedString(usersReset)).append("\n");
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

        // add `Success` to the URL query string
        if (getSuccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSuccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSuccess()))));
        }

        // add `UsersReset` to the URL query string
        if (getUsersReset() != null) {
            for (int i = 0; i < getUsersReset().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sUsersReset%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getUsersReset().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private PinRedeemResult instance;

        public Builder() {
            this(new PinRedeemResult());
        }

        protected Builder(PinRedeemResult instance) {
            this.instance = instance;
        }

        public PinRedeemResult.Builder success(Boolean success) {
            this.instance.success = success;
            return this;
        }

        public PinRedeemResult.Builder usersReset(List<String> usersReset) {
            this.instance.usersReset = usersReset;
            return this;
        }

        /**
         * returns a built PinRedeemResult instance.
         *
         * The builder is not reusable.
         */
        public PinRedeemResult build() {
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
    public static PinRedeemResult.Builder builder() {
        return new PinRedeemResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PinRedeemResult.Builder toBuilder() {
        return new PinRedeemResult.Builder().success(getSuccess()).usersReset(getUsersReset());
    }
}
