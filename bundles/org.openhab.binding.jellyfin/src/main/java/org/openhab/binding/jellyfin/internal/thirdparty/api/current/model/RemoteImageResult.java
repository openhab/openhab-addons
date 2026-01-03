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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class RemoteImageResult.
 */
@JsonPropertyOrder({ RemoteImageResult.JSON_PROPERTY_IMAGES, RemoteImageResult.JSON_PROPERTY_TOTAL_RECORD_COUNT,
        RemoteImageResult.JSON_PROPERTY_PROVIDERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoteImageResult {
    public static final String JSON_PROPERTY_IMAGES = "Images";
    @org.eclipse.jdt.annotation.Nullable
    private List<RemoteImageInfo> images;

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_PROVIDERS = "Providers";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> providers;

    public RemoteImageResult() {
    }

    public RemoteImageResult images(@org.eclipse.jdt.annotation.Nullable List<RemoteImageInfo> images) {
        this.images = images;
        return this;
    }

    public RemoteImageResult addImagesItem(RemoteImageInfo imagesItem) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imagesItem);
        return this;
    }

    /**
     * Gets or sets the images.
     * 
     * @return images
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<RemoteImageInfo> getImages() {
        return images;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImages(@org.eclipse.jdt.annotation.Nullable List<RemoteImageInfo> images) {
        this.images = images;
    }

    public RemoteImageResult totalRecordCount(@org.eclipse.jdt.annotation.Nullable Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total record count.
     * 
     * @return totalRecordCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TOTAL_RECORD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(value = JSON_PROPERTY_TOTAL_RECORD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalRecordCount(@org.eclipse.jdt.annotation.Nullable Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public RemoteImageResult providers(@org.eclipse.jdt.annotation.Nullable List<String> providers) {
        this.providers = providers;
        return this;
    }

    public RemoteImageResult addProvidersItem(String providersItem) {
        if (this.providers == null) {
            this.providers = new ArrayList<>();
        }
        this.providers.add(providersItem);
        return this;
    }

    /**
     * Gets or sets the providers.
     * 
     * @return providers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getProviders() {
        return providers;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviders(@org.eclipse.jdt.annotation.Nullable List<String> providers) {
        this.providers = providers;
    }

    /**
     * Return true if this RemoteImageResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteImageResult remoteImageResult = (RemoteImageResult) o;
        return Objects.equals(this.images, remoteImageResult.images)
                && Objects.equals(this.totalRecordCount, remoteImageResult.totalRecordCount)
                && Objects.equals(this.providers, remoteImageResult.providers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(images, totalRecordCount, providers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteImageResult {\n");
        sb.append("    images: ").append(toIndentedString(images)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
        sb.append("    providers: ").append(toIndentedString(providers)).append("\n");
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

        // add `Images` to the URL query string
        if (getImages() != null) {
            for (int i = 0; i < getImages().size(); i++) {
                if (getImages().get(i) != null) {
                    joiner.add(getImages().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sImages%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `TotalRecordCount` to the URL query string
        if (getTotalRecordCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTotalRecordCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTotalRecordCount()))));
        }

        // add `Providers` to the URL query string
        if (getProviders() != null) {
            for (int i = 0; i < getProviders().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sProviders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getProviders().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private RemoteImageResult instance;

        public Builder() {
            this(new RemoteImageResult());
        }

        protected Builder(RemoteImageResult instance) {
            this.instance = instance;
        }

        public RemoteImageResult.Builder images(List<RemoteImageInfo> images) {
            this.instance.images = images;
            return this;
        }

        public RemoteImageResult.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        public RemoteImageResult.Builder providers(List<String> providers) {
            this.instance.providers = providers;
            return this;
        }

        /**
         * returns a built RemoteImageResult instance.
         *
         * The builder is not reusable.
         */
        public RemoteImageResult build() {
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
    public static RemoteImageResult.Builder builder() {
        return new RemoteImageResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RemoteImageResult.Builder toBuilder() {
        return new RemoteImageResult.Builder().images(getImages()).totalRecordCount(getTotalRecordCount())
                .providers(getProviders());
    }
}
