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

public class LibraryUpdateInfo {
    public static final String JSON_PROPERTY_FOLDERS_ADDED_TO = "FoldersAddedTo";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> foldersAddedTo = new ArrayList<>();

    public static final String JSON_PROPERTY_FOLDERS_REMOVED_FROM = "FoldersRemovedFrom";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> foldersRemovedFrom = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_ADDED = "ItemsAdded";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> itemsAdded = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_REMOVED = "ItemsRemoved";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> itemsRemoved = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEMS_UPDATED = "ItemsUpdated";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> itemsUpdated = new ArrayList<>();

    public static final String JSON_PROPERTY_COLLECTION_FOLDERS = "CollectionFolders";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> collectionFolders = new ArrayList<>();

    public static final String JSON_PROPERTY_IS_EMPTY = "IsEmpty";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isEmpty;

    public LibraryUpdateInfo() {
    }

    @JsonCreator
    public LibraryUpdateInfo(@JsonProperty(JSON_PROPERTY_IS_EMPTY) Boolean isEmpty) {
        this();
        this.isEmpty = isEmpty;
    }

    public LibraryUpdateInfo foldersAddedTo(@org.eclipse.jdt.annotation.NonNull List<String> foldersAddedTo) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FOLDERS_ADDED_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getFoldersAddedTo() {
        return foldersAddedTo;
    }

    @JsonProperty(JSON_PROPERTY_FOLDERS_ADDED_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFoldersAddedTo(@org.eclipse.jdt.annotation.NonNull List<String> foldersAddedTo) {
        this.foldersAddedTo = foldersAddedTo;
    }

    public LibraryUpdateInfo foldersRemovedFrom(@org.eclipse.jdt.annotation.NonNull List<String> foldersRemovedFrom) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FOLDERS_REMOVED_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getFoldersRemovedFrom() {
        return foldersRemovedFrom;
    }

    @JsonProperty(JSON_PROPERTY_FOLDERS_REMOVED_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFoldersRemovedFrom(@org.eclipse.jdt.annotation.NonNull List<String> foldersRemovedFrom) {
        this.foldersRemovedFrom = foldersRemovedFrom;
    }

    public LibraryUpdateInfo itemsAdded(@org.eclipse.jdt.annotation.NonNull List<String> itemsAdded) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEMS_ADDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getItemsAdded() {
        return itemsAdded;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS_ADDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsAdded(@org.eclipse.jdt.annotation.NonNull List<String> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

    public LibraryUpdateInfo itemsRemoved(@org.eclipse.jdt.annotation.NonNull List<String> itemsRemoved) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEMS_REMOVED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getItemsRemoved() {
        return itemsRemoved;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS_REMOVED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsRemoved(@org.eclipse.jdt.annotation.NonNull List<String> itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }

    public LibraryUpdateInfo itemsUpdated(@org.eclipse.jdt.annotation.NonNull List<String> itemsUpdated) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEMS_UPDATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getItemsUpdated() {
        return itemsUpdated;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS_UPDATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemsUpdated(@org.eclipse.jdt.annotation.NonNull List<String> itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }

    public LibraryUpdateInfo collectionFolders(@org.eclipse.jdt.annotation.NonNull List<String> collectionFolders) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COLLECTION_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getCollectionFolders() {
        return collectionFolders;
    }

    @JsonProperty(JSON_PROPERTY_COLLECTION_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCollectionFolders(@org.eclipse.jdt.annotation.NonNull List<String> collectionFolders) {
        this.collectionFolders = collectionFolders;
    }

    /**
     * Get isEmpty
     * 
     * @return isEmpty
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_EMPTY)
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
}
