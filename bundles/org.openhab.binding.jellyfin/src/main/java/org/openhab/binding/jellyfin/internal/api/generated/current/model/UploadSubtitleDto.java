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
 * Upload subtitles dto.
 */
@JsonPropertyOrder({ UploadSubtitleDto.JSON_PROPERTY_LANGUAGE, UploadSubtitleDto.JSON_PROPERTY_FORMAT,
        UploadSubtitleDto.JSON_PROPERTY_IS_FORCED, UploadSubtitleDto.JSON_PROPERTY_IS_HEARING_IMPAIRED,
        UploadSubtitleDto.JSON_PROPERTY_DATA })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UploadSubtitleDto {
    public static final String JSON_PROPERTY_LANGUAGE = "Language";
    @org.eclipse.jdt.annotation.Nullable
    private String language;

    public static final String JSON_PROPERTY_FORMAT = "Format";
    @org.eclipse.jdt.annotation.Nullable
    private String format;

    public static final String JSON_PROPERTY_IS_FORCED = "IsForced";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isForced;

    public static final String JSON_PROPERTY_IS_HEARING_IMPAIRED = "IsHearingImpaired";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isHearingImpaired;

    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.Nullable
    private String data;

    public UploadSubtitleDto() {
    }

    public UploadSubtitleDto language(@org.eclipse.jdt.annotation.Nullable String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets or sets the subtitle language.
     * 
     * @return language
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getLanguage() {
        return language;
    }

    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLanguage(@org.eclipse.jdt.annotation.Nullable String language) {
        this.language = language;
    }

    public UploadSubtitleDto format(@org.eclipse.jdt.annotation.Nullable String format) {
        this.format = format;
        return this;
    }

    /**
     * Gets or sets the subtitle format.
     * 
     * @return format
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FORMAT, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getFormat() {
        return format;
    }

    @JsonProperty(value = JSON_PROPERTY_FORMAT, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFormat(@org.eclipse.jdt.annotation.Nullable String format) {
        this.format = format;
    }

    public UploadSubtitleDto isForced(@org.eclipse.jdt.annotation.Nullable Boolean isForced) {
        this.isForced = isForced;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the subtitle is forced.
     * 
     * @return isForced
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_FORCED, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Boolean getIsForced() {
        return isForced;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FORCED, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIsForced(@org.eclipse.jdt.annotation.Nullable Boolean isForced) {
        this.isForced = isForced;
    }

    public UploadSubtitleDto isHearingImpaired(@org.eclipse.jdt.annotation.Nullable Boolean isHearingImpaired) {
        this.isHearingImpaired = isHearingImpaired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the subtitle is for hearing impaired.
     * 
     * @return isHearingImpaired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_HEARING_IMPAIRED, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Boolean getIsHearingImpaired() {
        return isHearingImpaired;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_HEARING_IMPAIRED, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIsHearingImpaired(@org.eclipse.jdt.annotation.Nullable Boolean isHearingImpaired) {
        this.isHearingImpaired = isHearingImpaired;
    }

    public UploadSubtitleDto data(@org.eclipse.jdt.annotation.Nullable String data) {
        this.data = data;
        return this;
    }

    /**
     * Gets or sets the subtitle data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DATA, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getData() {
        return data;
    }

    @JsonProperty(value = JSON_PROPERTY_DATA, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setData(@org.eclipse.jdt.annotation.Nullable String data) {
        this.data = data;
    }

    /**
     * Return true if this UploadSubtitleDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UploadSubtitleDto uploadSubtitleDto = (UploadSubtitleDto) o;
        return Objects.equals(this.language, uploadSubtitleDto.language)
                && Objects.equals(this.format, uploadSubtitleDto.format)
                && Objects.equals(this.isForced, uploadSubtitleDto.isForced)
                && Objects.equals(this.isHearingImpaired, uploadSubtitleDto.isHearingImpaired)
                && Objects.equals(this.data, uploadSubtitleDto.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, format, isForced, isHearingImpaired, data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UploadSubtitleDto {\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
        sb.append("    isForced: ").append(toIndentedString(isForced)).append("\n");
        sb.append("    isHearingImpaired: ").append(toIndentedString(isHearingImpaired)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

        // add `Language` to the URL query string
        if (getLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLanguage()))));
        }

        // add `Format` to the URL query string
        if (getFormat() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFormat()))));
        }

        // add `IsForced` to the URL query string
        if (getIsForced() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsForced%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsForced()))));
        }

        // add `IsHearingImpaired` to the URL query string
        if (getIsHearingImpaired() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsHearingImpaired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHearingImpaired()))));
        }

        // add `Data` to the URL query string
        if (getData() != null) {
            joiner.add(String.format(Locale.ROOT, "%sData%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getData()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UploadSubtitleDto instance;

        public Builder() {
            this(new UploadSubtitleDto());
        }

        protected Builder(UploadSubtitleDto instance) {
            this.instance = instance;
        }

        public UploadSubtitleDto.Builder language(String language) {
            this.instance.language = language;
            return this;
        }

        public UploadSubtitleDto.Builder format(String format) {
            this.instance.format = format;
            return this;
        }

        public UploadSubtitleDto.Builder isForced(Boolean isForced) {
            this.instance.isForced = isForced;
            return this;
        }

        public UploadSubtitleDto.Builder isHearingImpaired(Boolean isHearingImpaired) {
            this.instance.isHearingImpaired = isHearingImpaired;
            return this;
        }

        public UploadSubtitleDto.Builder data(String data) {
            this.instance.data = data;
            return this;
        }

        /**
         * returns a built UploadSubtitleDto instance.
         *
         * The builder is not reusable.
         */
        public UploadSubtitleDto build() {
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
    public static UploadSubtitleDto.Builder builder() {
        return new UploadSubtitleDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UploadSubtitleDto.Builder toBuilder() {
        return new UploadSubtitleDto.Builder().language(getLanguage()).format(getFormat()).isForced(getIsForced())
                .isHearingImpaired(getIsHearingImpaired()).data(getData());
    }
}
