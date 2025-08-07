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
import java.util.Objects;

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
    @org.eclipse.jdt.annotation.NonNull
    private List<RemoteImageInfo> images;

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_PROVIDERS = "Providers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> providers;

    public RemoteImageResult() {
    }

    public RemoteImageResult images(@org.eclipse.jdt.annotation.NonNull List<RemoteImageInfo> images) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RemoteImageInfo> getImages() {
        return images;
    }

    @JsonProperty(JSON_PROPERTY_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImages(@org.eclipse.jdt.annotation.NonNull List<RemoteImageInfo> images) {
        this.images = images;
    }

    public RemoteImageResult totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total record count.
     * 
     * @return totalRecordCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public RemoteImageResult providers(@org.eclipse.jdt.annotation.NonNull List<String> providers) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getProviders() {
        return providers;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviders(@org.eclipse.jdt.annotation.NonNull List<String> providers) {
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
}
