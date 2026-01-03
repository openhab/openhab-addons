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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class LibraryUpdateInfo.
 */
@JsonPropertyOrder({ LibraryUpdateInfo.JSON_PROPERTY_FOLDERS_ADDED_TO,
        LibraryUpdateInfo.JSON_PROPERTY_FOLDERS_REMOVED_FROM, LibraryUpdateInfo.JSON_PROPERTY_ITEMS_ADDED,
        LibraryUpdateInfo.JSON_PROPERTY_ITEMS_REMOVED, LibraryUpdateInfo.JSON_PROPERTY_ITEMS_UPDATED,
        LibraryUpdateInfo.JSON_PROPERTY_COLLECTION_FOLDERS, LibraryUpdateInfo.JSON_PROPERTY_IS_EMPTY })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryUpdateInfo {
    public static final String JSON_PROPERTY_FOLDERS_ADDED_TO = "FoldersAddedTo";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> foldersAddedTo = new ArrayList<>();

    public static final String JSON_PROPERTY_FOLDERS_REMOVED_FROM = "FoldersRemovedFrom";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> foldersRemovedFrom = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_ADDED = "ItemsAdded";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> itemsAdded = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_REMOVED = "ItemsRemoved";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> itemsRemoved = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_UPDATED = "ItemsUpdated";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> itemsUpdated = new ArrayList<>();

    public static final String JSON_PROPERTY_COLLECTION_FOLDERS = "CollectionFolders";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> collectionFolders = new ArrayList<>();

    public static final String JSON_PROPERTY_IS_EMPTY = "IsEmpty";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isEmpty;

    public LibraryUpdateInfo() {
    }

    @JsonCreator
    public LibraryUpdateInfo(@JsonProperty(JSON_PROPERTY_IS_EMPTY) Boolean isEmpty) {
        this();
        this.isEmpty = isEmpty;
    }

    public LibraryUpdateInfo foldersAddedTo(@org.eclipse.jdt.annotation.Nullable List<String> foldersAddedTo) {
        this.foldersAddedTo = foldersAddedTo;
        return this;
    }

    public LibraryUpdateInfo addFoldersAddedToItem(String foldersAddedToItem) {
        if (this.foldersAddedTo == null) {
            this.foldersAddedTo = new ArrayList<>();
        }
        this.foldersAddedTo.add(foldersAddedToItem);
        return this;
    }

    /**
     * Gets or sets the folders added to.
     * 
     * @return foldersAddedTo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FOLDERS_ADDED_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getFoldersAddedTo() {
        return foldersAddedTo;
    }

    @JsonProperty(value = JSON_PROPERTY_FOLDERS_ADDED_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFoldersAddedTo(@org.eclipse.jdt.annotation.Nullable List<String> foldersAddedTo) {
        this.foldersAddedTo = foldersAddedTo;
    }

    public LibraryUpdateInfo foldersRemovedFrom(@org.eclipse.jdt.annotation.Nullable List<String> foldersRemovedFrom) {
        this.foldersRemovedFrom = foldersRemovedFrom;
        return this;
    }

    public LibraryUpdateInfo addFoldersRemovedFromItem(String foldersRemovedFromItem) {
        if (this.foldersRemovedFrom == null) {
            this.foldersRemovedFrom = new ArrayList<>();
        }
        this.foldersRemovedFrom.add(foldersRemovedFromItem);
        return this;
    }

    /**
     * Gets or sets the folders removed from.
     * 
     * @return foldersRemovedFrom
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FOLDERS_REMOVED_FROM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getFoldersRemovedFrom() {
        return foldersRemovedFrom;
    }

    @JsonProperty(value = JSON_PROPERTY_FOLDERS_REMOVED_FROM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFoldersRemovedFrom(@org.eclipse.jdt.annotation.Nullable List<String> foldersRemovedFrom) {
        this.foldersRemovedFrom = foldersRemovedFrom;
    }

    public LibraryUpdateInfo itemsAdded(@org.eclipse.jdt.annotation.Nullable List<String> itemsAdded) {
        this.itemsAdded = itemsAdded;
        return this;
    }

    public LibraryUpdateInfo addItemsAddedItem(String itemsAddedItem) {
        if (this.itemsAdded == null) {
            this.itemsAdded = new ArrayList<>();
        }
        this.itemsAdded.add(itemsAddedItem);
        return this;
    }

    /**
     * Gets or sets the items added.
     * 
     * @return itemsAdded
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEMS_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getItemsAdded() {
        return itemsAdded;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsAdded(@org.eclipse.jdt.annotation.Nullable List<String> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

    public LibraryUpdateInfo itemsRemoved(@org.eclipse.jdt.annotation.Nullable List<String> itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
        return this;
    }

    public LibraryUpdateInfo addItemsRemovedItem(String itemsRemovedItem) {
        if (this.itemsRemoved == null) {
            this.itemsRemoved = new ArrayList<>();
        }
        this.itemsRemoved.add(itemsRemovedItem);
        return this;
    }

    /**
     * Gets or sets the items removed.
     * 
     * @return itemsRemoved
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEMS_REMOVED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getItemsRemoved() {
        return itemsRemoved;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS_REMOVED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsRemoved(@org.eclipse.jdt.annotation.Nullable List<String> itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }

    public LibraryUpdateInfo itemsUpdated(@org.eclipse.jdt.annotation.Nullable List<String> itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
        return this;
    }

    public LibraryUpdateInfo addItemsUpdatedItem(String itemsUpdatedItem) {
        if (this.itemsUpdated == null) {
            this.itemsUpdated = new ArrayList<>();
        }
        this.itemsUpdated.add(itemsUpdatedItem);
        return this;
    }

    /**
     * Gets or sets the items updated.
     * 
     * @return itemsUpdated
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEMS_UPDATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getItemsUpdated() {
        return itemsUpdated;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS_UPDATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsUpdated(@org.eclipse.jdt.annotation.Nullable List<String> itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }

    public LibraryUpdateInfo collectionFolders(@org.eclipse.jdt.annotation.Nullable List<String> collectionFolders) {
        this.collectionFolders = collectionFolders;
        return this;
    }

    public LibraryUpdateInfo addCollectionFoldersItem(String collectionFoldersItem) {
        if (this.collectionFolders == null) {
            this.collectionFolders = new ArrayList<>();
        }
        this.collectionFolders.add(collectionFoldersItem);
        return this;
    }

    /**
     * Get collectionFolders
     * 
     * @return collectionFolders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COLLECTION_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getCollectionFolders() {
        return collectionFolders;
    }

    @JsonProperty(value = JSON_PROPERTY_COLLECTION_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCollectionFolders(@org.eclipse.jdt.annotation.Nullable List<String> collectionFolders) {
        this.collectionFolders = collectionFolders;
    }

    /**
     * Get isEmpty
     * 
     * @return isEmpty
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_EMPTY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsEmpty() {
        return isEmpty;
    }

    /**
     * Return true if this LibraryUpdateInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryUpdateInfo libraryUpdateInfo = (LibraryUpdateInfo) o;
        return Objects.equals(this.foldersAddedTo, libraryUpdateInfo.foldersAddedTo)
                && Objects.equals(this.foldersRemovedFrom, libraryUpdateInfo.foldersRemovedFrom)
                && Objects.equals(this.itemsAdded, libraryUpdateInfo.itemsAdded)
                && Objects.equals(this.itemsRemoved, libraryUpdateInfo.itemsRemoved)
                && Objects.equals(this.itemsUpdated, libraryUpdateInfo.itemsUpdated)
                && Objects.equals(this.collectionFolders, libraryUpdateInfo.collectionFolders)
                && Objects.equals(this.isEmpty, libraryUpdateInfo.isEmpty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foldersAddedTo, foldersRemovedFrom, itemsAdded, itemsRemoved, itemsUpdated,
                collectionFolders, isEmpty);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryUpdateInfo {\n");
        sb.append("    foldersAddedTo: ").append(toIndentedString(foldersAddedTo)).append("\n");
        sb.append("    foldersRemovedFrom: ").append(toIndentedString(foldersRemovedFrom)).append("\n");
        sb.append("    itemsAdded: ").append(toIndentedString(itemsAdded)).append("\n");
        sb.append("    itemsRemoved: ").append(toIndentedString(itemsRemoved)).append("\n");
        sb.append("    itemsUpdated: ").append(toIndentedString(itemsUpdated)).append("\n");
        sb.append("    collectionFolders: ").append(toIndentedString(collectionFolders)).append("\n");
        sb.append("    isEmpty: ").append(toIndentedString(isEmpty)).append("\n");
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

        // add `FoldersAddedTo` to the URL query string
        if (getFoldersAddedTo() != null) {
            for (int i = 0; i < getFoldersAddedTo().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sFoldersAddedTo%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getFoldersAddedTo().get(i)))));
            }
        }

        // add `FoldersRemovedFrom` to the URL query string
        if (getFoldersRemovedFrom() != null) {
            for (int i = 0; i < getFoldersRemovedFrom().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sFoldersRemovedFrom%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getFoldersRemovedFrom().get(i)))));
            }
        }

        // add `ItemsAdded` to the URL query string
        if (getItemsAdded() != null) {
            for (int i = 0; i < getItemsAdded().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sItemsAdded%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getItemsAdded().get(i)))));
            }
        }

        // add `ItemsRemoved` to the URL query string
        if (getItemsRemoved() != null) {
            for (int i = 0; i < getItemsRemoved().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sItemsRemoved%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getItemsRemoved().get(i)))));
            }
        }

        // add `ItemsUpdated` to the URL query string
        if (getItemsUpdated() != null) {
            for (int i = 0; i < getItemsUpdated().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sItemsUpdated%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getItemsUpdated().get(i)))));
            }
        }

        // add `CollectionFolders` to the URL query string
        if (getCollectionFolders() != null) {
            for (int i = 0; i < getCollectionFolders().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sCollectionFolders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getCollectionFolders().get(i)))));
            }
        }

        // add `IsEmpty` to the URL query string
        if (getIsEmpty() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsEmpty%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsEmpty()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LibraryUpdateInfo instance;

        public Builder() {
            this(new LibraryUpdateInfo());
        }

        protected Builder(LibraryUpdateInfo instance) {
            this.instance = instance;
        }

        public LibraryUpdateInfo.Builder foldersAddedTo(List<String> foldersAddedTo) {
            this.instance.foldersAddedTo = foldersAddedTo;
            return this;
        }

        public LibraryUpdateInfo.Builder foldersRemovedFrom(List<String> foldersRemovedFrom) {
            this.instance.foldersRemovedFrom = foldersRemovedFrom;
            return this;
        }

        public LibraryUpdateInfo.Builder itemsAdded(List<String> itemsAdded) {
            this.instance.itemsAdded = itemsAdded;
            return this;
        }

        public LibraryUpdateInfo.Builder itemsRemoved(List<String> itemsRemoved) {
            this.instance.itemsRemoved = itemsRemoved;
            return this;
        }

        public LibraryUpdateInfo.Builder itemsUpdated(List<String> itemsUpdated) {
            this.instance.itemsUpdated = itemsUpdated;
            return this;
        }

        public LibraryUpdateInfo.Builder collectionFolders(List<String> collectionFolders) {
            this.instance.collectionFolders = collectionFolders;
            return this;
        }

        public LibraryUpdateInfo.Builder isEmpty(Boolean isEmpty) {
            this.instance.isEmpty = isEmpty;
            return this;
        }

        /**
         * returns a built LibraryUpdateInfo instance.
         *
         * The builder is not reusable.
         */
        public LibraryUpdateInfo build() {
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
    public static LibraryUpdateInfo.Builder builder() {
        return new LibraryUpdateInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryUpdateInfo.Builder toBuilder() {
        return new LibraryUpdateInfo.Builder().foldersAddedTo(getFoldersAddedTo())
                .foldersRemovedFrom(getFoldersRemovedFrom()).itemsAdded(getItemsAdded()).itemsRemoved(getItemsRemoved())
                .itemsUpdated(getItemsUpdated()).collectionFolders(getCollectionFolders()).isEmpty(getIsEmpty());
    }
}
