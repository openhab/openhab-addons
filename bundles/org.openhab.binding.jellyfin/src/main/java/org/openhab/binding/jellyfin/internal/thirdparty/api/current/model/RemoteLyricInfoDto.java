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
 * The remote lyric info dto.
 */
@JsonPropertyOrder({ RemoteLyricInfoDto.JSON_PROPERTY_ID, RemoteLyricInfoDto.JSON_PROPERTY_PROVIDER_NAME,
        RemoteLyricInfoDto.JSON_PROPERTY_LYRICS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoteLyricInfoDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private String id;

    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.Nullable
    private String providerName;

    public static final String JSON_PROPERTY_LYRICS = "Lyrics";
    @org.eclipse.jdt.annotation.Nullable
    private LyricDto lyrics;

    public RemoteLyricInfoDto() {
    }

    public RemoteLyricInfoDto id(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id for the lyric.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
    }

    public RemoteLyricInfoDto providerName(@org.eclipse.jdt.annotation.Nullable String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Gets the provider name.
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.Nullable String providerName) {
        this.providerName = providerName;
    }

    public RemoteLyricInfoDto lyrics(@org.eclipse.jdt.annotation.Nullable LyricDto lyrics) {
        this.lyrics = lyrics;
        return this;
    }

    /**
     * Gets the lyrics.
     * 
     * @return lyrics
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LYRICS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LyricDto getLyrics() {
        return lyrics;
    }

    @JsonProperty(value = JSON_PROPERTY_LYRICS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLyrics(@org.eclipse.jdt.annotation.Nullable LyricDto lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Return true if this RemoteLyricInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteLyricInfoDto remoteLyricInfoDto = (RemoteLyricInfoDto) o;
        return Objects.equals(this.id, remoteLyricInfoDto.id)
                && Objects.equals(this.providerName, remoteLyricInfoDto.providerName)
                && Objects.equals(this.lyrics, remoteLyricInfoDto.lyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerName, lyrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteLyricInfoDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
        sb.append("    lyrics: ").append(toIndentedString(lyrics)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `ProviderName` to the URL query string
        if (getProviderName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderName()))));
        }

        // add `Lyrics` to the URL query string
        if (getLyrics() != null) {
            joiner.add(getLyrics().toUrlQueryString(prefix + "Lyrics" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RemoteLyricInfoDto instance;

        public Builder() {
            this(new RemoteLyricInfoDto());
        }

        protected Builder(RemoteLyricInfoDto instance) {
            this.instance = instance;
        }

        public RemoteLyricInfoDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public RemoteLyricInfoDto.Builder providerName(String providerName) {
            this.instance.providerName = providerName;
            return this;
        }

        public RemoteLyricInfoDto.Builder lyrics(LyricDto lyrics) {
            this.instance.lyrics = lyrics;
            return this;
        }

        /**
         * returns a built RemoteLyricInfoDto instance.
         *
         * The builder is not reusable.
         */
        public RemoteLyricInfoDto build() {
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
    public static RemoteLyricInfoDto.Builder builder() {
        return new RemoteLyricInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RemoteLyricInfoDto.Builder toBuilder() {
        return new RemoteLyricInfoDto.Builder().id(getId()).providerName(getProviderName()).lyrics(getLyrics());
    }
}
