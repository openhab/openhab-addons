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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * XbmcMetadataOptions
 */
@JsonPropertyOrder({ XbmcMetadataOptions.JSON_PROPERTY_USER_ID, XbmcMetadataOptions.JSON_PROPERTY_RELEASE_DATE_FORMAT,
        XbmcMetadataOptions.JSON_PROPERTY_SAVE_IMAGE_PATHS_IN_NFO,
        XbmcMetadataOptions.JSON_PROPERTY_ENABLE_PATH_SUBSTITUTION,
        XbmcMetadataOptions.JSON_PROPERTY_ENABLE_EXTRA_THUMBS_DUPLICATION })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class XbmcMetadataOptions {
    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private String userId;

    public static final String JSON_PROPERTY_RELEASE_DATE_FORMAT = "ReleaseDateFormat";
    @org.eclipse.jdt.annotation.NonNull
    private String releaseDateFormat;

    public static final String JSON_PROPERTY_SAVE_IMAGE_PATHS_IN_NFO = "SaveImagePathsInNfo";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean saveImagePathsInNfo;

    public static final String JSON_PROPERTY_ENABLE_PATH_SUBSTITUTION = "EnablePathSubstitution";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enablePathSubstitution;

    public static final String JSON_PROPERTY_ENABLE_EXTRA_THUMBS_DUPLICATION = "EnableExtraThumbsDuplication";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableExtraThumbsDuplication;

    public XbmcMetadataOptions() {
    }

    public XbmcMetadataOptions userId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Get userId
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
    }

    public XbmcMetadataOptions releaseDateFormat(@org.eclipse.jdt.annotation.NonNull String releaseDateFormat) {
        this.releaseDateFormat = releaseDateFormat;
        return this;
    }

    /**
     * Get releaseDateFormat
     * 
     * @return releaseDateFormat
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RELEASE_DATE_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getReleaseDateFormat() {
        return releaseDateFormat;
    }

    @JsonProperty(value = JSON_PROPERTY_RELEASE_DATE_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReleaseDateFormat(@org.eclipse.jdt.annotation.NonNull String releaseDateFormat) {
        this.releaseDateFormat = releaseDateFormat;
    }

    public XbmcMetadataOptions saveImagePathsInNfo(@org.eclipse.jdt.annotation.NonNull Boolean saveImagePathsInNfo) {
        this.saveImagePathsInNfo = saveImagePathsInNfo;
        return this;
    }

    /**
     * Get saveImagePathsInNfo
     * 
     * @return saveImagePathsInNfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SAVE_IMAGE_PATHS_IN_NFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSaveImagePathsInNfo() {
        return saveImagePathsInNfo;
    }

    @JsonProperty(value = JSON_PROPERTY_SAVE_IMAGE_PATHS_IN_NFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSaveImagePathsInNfo(@org.eclipse.jdt.annotation.NonNull Boolean saveImagePathsInNfo) {
        this.saveImagePathsInNfo = saveImagePathsInNfo;
    }

    public XbmcMetadataOptions enablePathSubstitution(
            @org.eclipse.jdt.annotation.NonNull Boolean enablePathSubstitution) {
        this.enablePathSubstitution = enablePathSubstitution;
        return this;
    }

    /**
     * Get enablePathSubstitution
     * 
     * @return enablePathSubstitution
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_PATH_SUBSTITUTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePathSubstitution() {
        return enablePathSubstitution;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_PATH_SUBSTITUTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePathSubstitution(@org.eclipse.jdt.annotation.NonNull Boolean enablePathSubstitution) {
        this.enablePathSubstitution = enablePathSubstitution;
    }

    public XbmcMetadataOptions enableExtraThumbsDuplication(
            @org.eclipse.jdt.annotation.NonNull Boolean enableExtraThumbsDuplication) {
        this.enableExtraThumbsDuplication = enableExtraThumbsDuplication;
        return this;
    }

    /**
     * Get enableExtraThumbsDuplication
     * 
     * @return enableExtraThumbsDuplication
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_EXTRA_THUMBS_DUPLICATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableExtraThumbsDuplication() {
        return enableExtraThumbsDuplication;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_EXTRA_THUMBS_DUPLICATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableExtraThumbsDuplication(
            @org.eclipse.jdt.annotation.NonNull Boolean enableExtraThumbsDuplication) {
        this.enableExtraThumbsDuplication = enableExtraThumbsDuplication;
    }

    /**
     * Return true if this XbmcMetadataOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        XbmcMetadataOptions xbmcMetadataOptions = (XbmcMetadataOptions) o;
        return Objects.equals(this.userId, xbmcMetadataOptions.userId)
                && Objects.equals(this.releaseDateFormat, xbmcMetadataOptions.releaseDateFormat)
                && Objects.equals(this.saveImagePathsInNfo, xbmcMetadataOptions.saveImagePathsInNfo)
                && Objects.equals(this.enablePathSubstitution, xbmcMetadataOptions.enablePathSubstitution)
                && Objects.equals(this.enableExtraThumbsDuplication, xbmcMetadataOptions.enableExtraThumbsDuplication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, releaseDateFormat, saveImagePathsInNfo, enablePathSubstitution,
                enableExtraThumbsDuplication);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class XbmcMetadataOptions {\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    releaseDateFormat: ").append(toIndentedString(releaseDateFormat)).append("\n");
        sb.append("    saveImagePathsInNfo: ").append(toIndentedString(saveImagePathsInNfo)).append("\n");
        sb.append("    enablePathSubstitution: ").append(toIndentedString(enablePathSubstitution)).append("\n");
        sb.append("    enableExtraThumbsDuplication: ").append(toIndentedString(enableExtraThumbsDuplication))
                .append("\n");
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

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `ReleaseDateFormat` to the URL query string
        if (getReleaseDateFormat() != null) {
            joiner.add(String.format(Locale.ROOT, "%sReleaseDateFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReleaseDateFormat()))));
        }

        // add `SaveImagePathsInNfo` to the URL query string
        if (getSaveImagePathsInNfo() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSaveImagePathsInNfo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSaveImagePathsInNfo()))));
        }

        // add `EnablePathSubstitution` to the URL query string
        if (getEnablePathSubstitution() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnablePathSubstitution%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePathSubstitution()))));
        }

        // add `EnableExtraThumbsDuplication` to the URL query string
        if (getEnableExtraThumbsDuplication() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableExtraThumbsDuplication%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableExtraThumbsDuplication()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private XbmcMetadataOptions instance;

        public Builder() {
            this(new XbmcMetadataOptions());
        }

        protected Builder(XbmcMetadataOptions instance) {
            this.instance = instance;
        }

        public XbmcMetadataOptions.Builder userId(String userId) {
            this.instance.userId = userId;
            return this;
        }

        public XbmcMetadataOptions.Builder releaseDateFormat(String releaseDateFormat) {
            this.instance.releaseDateFormat = releaseDateFormat;
            return this;
        }

        public XbmcMetadataOptions.Builder saveImagePathsInNfo(Boolean saveImagePathsInNfo) {
            this.instance.saveImagePathsInNfo = saveImagePathsInNfo;
            return this;
        }

        public XbmcMetadataOptions.Builder enablePathSubstitution(Boolean enablePathSubstitution) {
            this.instance.enablePathSubstitution = enablePathSubstitution;
            return this;
        }

        public XbmcMetadataOptions.Builder enableExtraThumbsDuplication(Boolean enableExtraThumbsDuplication) {
            this.instance.enableExtraThumbsDuplication = enableExtraThumbsDuplication;
            return this;
        }

        /**
         * returns a built XbmcMetadataOptions instance.
         *
         * The builder is not reusable.
         */
        public XbmcMetadataOptions build() {
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
    public static XbmcMetadataOptions.Builder builder() {
        return new XbmcMetadataOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public XbmcMetadataOptions.Builder toBuilder() {
        return new XbmcMetadataOptions.Builder().userId(getUserId()).releaseDateFormat(getReleaseDateFormat())
                .saveImagePathsInNfo(getSaveImagePathsInNfo()).enablePathSubstitution(getEnablePathSubstitution())
                .enableExtraThumbsDuplication(getEnableExtraThumbsDuplication());
    }
}
