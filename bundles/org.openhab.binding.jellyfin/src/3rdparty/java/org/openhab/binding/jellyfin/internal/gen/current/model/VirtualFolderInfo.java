/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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


package org.openhab.binding.jellyfin.internal.gen.current.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openhab.binding.jellyfin.internal.gen.current.model.CollectionTypeOptions;
import org.openhab.binding.jellyfin.internal.gen.current.model.LibraryOptions;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.openhab.binding.jellyfin.internal.gen.ApiClient;
/**
 * Used to hold information about a user&#39;s list of configured virtual folders.
 */
@JsonPropertyOrder({
  VirtualFolderInfo.JSON_PROPERTY_NAME,
  VirtualFolderInfo.JSON_PROPERTY_LOCATIONS,
  VirtualFolderInfo.JSON_PROPERTY_COLLECTION_TYPE,
  VirtualFolderInfo.JSON_PROPERTY_LIBRARY_OPTIONS,
  VirtualFolderInfo.JSON_PROPERTY_ITEM_ID,
  VirtualFolderInfo.JSON_PROPERTY_PRIMARY_IMAGE_ITEM_ID,
  VirtualFolderInfo.JSON_PROPERTY_REFRESH_PROGRESS,
  VirtualFolderInfo.JSON_PROPERTY_REFRESH_STATUS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class VirtualFolderInfo {
  public static final String JSON_PROPERTY_NAME = "Name";
  @org.eclipse.jdt.annotation.Nullable

  private String name;

  public static final String JSON_PROPERTY_LOCATIONS = "Locations";
  @org.eclipse.jdt.annotation.Nullable

  private List<String> locations;

  public static final String JSON_PROPERTY_COLLECTION_TYPE = "CollectionType";
  @org.eclipse.jdt.annotation.Nullable

  private CollectionTypeOptions collectionType;

  public static final String JSON_PROPERTY_LIBRARY_OPTIONS = "LibraryOptions";
  @org.eclipse.jdt.annotation.Nullable

  private LibraryOptions libraryOptions;

  public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
  @org.eclipse.jdt.annotation.Nullable

  private String itemId;

  public static final String JSON_PROPERTY_PRIMARY_IMAGE_ITEM_ID = "PrimaryImageItemId";
  @org.eclipse.jdt.annotation.Nullable

  private String primaryImageItemId;

  public static final String JSON_PROPERTY_REFRESH_PROGRESS = "RefreshProgress";
  @org.eclipse.jdt.annotation.Nullable

  private Double refreshProgress;

  public static final String JSON_PROPERTY_REFRESH_STATUS = "RefreshStatus";
  @org.eclipse.jdt.annotation.Nullable

  private String refreshStatus;

  public VirtualFolderInfo() { 
  }

  public VirtualFolderInfo name(@org.eclipse.jdt.annotation.Nullable
 String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets or sets the name.
   * @return name
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_NAME)
  public String getName() {
    return name;
  }


  @JsonProperty(value = JSON_PROPERTY_NAME)
  public void setName(@org.eclipse.jdt.annotation.Nullable
 String name) {
    this.name = name;
  }


  public VirtualFolderInfo locations(@org.eclipse.jdt.annotation.Nullable
 List<String> locations) {
    this.locations = locations;
    return this;
  }

  public VirtualFolderInfo addLocationsItem(String locationsItem) {
    if (this.locations == null) {
      this.locations = new ArrayList<>();
    }
    this.locations.add(locationsItem);
    return this;
  }

  /**
   * Gets or sets the locations.
   * @return locations
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_LOCATIONS)
  public List<String> getLocations() {
    return locations;
  }


  @JsonProperty(value = JSON_PROPERTY_LOCATIONS)
  public void setLocations(@org.eclipse.jdt.annotation.Nullable
 List<String> locations) {
    this.locations = locations;
  }


  public VirtualFolderInfo collectionType(@org.eclipse.jdt.annotation.Nullable
 CollectionTypeOptions collectionType) {
    this.collectionType = collectionType;
    return this;
  }

  /**
   * Gets or sets the type of the collection.
   * @return collectionType
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_COLLECTION_TYPE)
  public CollectionTypeOptions getCollectionType() {
    return collectionType;
  }


  @JsonProperty(value = JSON_PROPERTY_COLLECTION_TYPE)
  public void setCollectionType(@org.eclipse.jdt.annotation.Nullable
 CollectionTypeOptions collectionType) {
    this.collectionType = collectionType;
  }


  public VirtualFolderInfo libraryOptions(@org.eclipse.jdt.annotation.Nullable
 LibraryOptions libraryOptions) {
    this.libraryOptions = libraryOptions;
    return this;
  }

  /**
   * Get libraryOptions
   * @return libraryOptions
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS)
  public LibraryOptions getLibraryOptions() {
    return libraryOptions;
  }


  @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS)
  public void setLibraryOptions(@org.eclipse.jdt.annotation.Nullable
 LibraryOptions libraryOptions) {
    this.libraryOptions = libraryOptions;
  }


  public VirtualFolderInfo itemId(@org.eclipse.jdt.annotation.Nullable
 String itemId) {
    this.itemId = itemId;
    return this;
  }

  /**
   * Gets or sets the item identifier.
   * @return itemId
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_ITEM_ID)
  public String getItemId() {
    return itemId;
  }


  @JsonProperty(value = JSON_PROPERTY_ITEM_ID)
  public void setItemId(@org.eclipse.jdt.annotation.Nullable
 String itemId) {
    this.itemId = itemId;
  }


  public VirtualFolderInfo primaryImageItemId(@org.eclipse.jdt.annotation.Nullable
 String primaryImageItemId) {
    this.primaryImageItemId = primaryImageItemId;
    return this;
  }

  /**
   * Gets or sets the primary image item identifier.
   * @return primaryImageItemId
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ITEM_ID)
  public String getPrimaryImageItemId() {
    return primaryImageItemId;
  }


  @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ITEM_ID)
  public void setPrimaryImageItemId(@org.eclipse.jdt.annotation.Nullable
 String primaryImageItemId) {
    this.primaryImageItemId = primaryImageItemId;
  }


  public VirtualFolderInfo refreshProgress(@org.eclipse.jdt.annotation.Nullable
 Double refreshProgress) {
    this.refreshProgress = refreshProgress;
    return this;
  }

  /**
   * Get refreshProgress
   * @return refreshProgress
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_REFRESH_PROGRESS)
  public Double getRefreshProgress() {
    return refreshProgress;
  }


  @JsonProperty(value = JSON_PROPERTY_REFRESH_PROGRESS)
  public void setRefreshProgress(@org.eclipse.jdt.annotation.Nullable
 Double refreshProgress) {
    this.refreshProgress = refreshProgress;
  }


  public VirtualFolderInfo refreshStatus(@org.eclipse.jdt.annotation.Nullable
 String refreshStatus) {
    this.refreshStatus = refreshStatus;
    return this;
  }

  /**
   * Get refreshStatus
   * @return refreshStatus
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_REFRESH_STATUS)
  public String getRefreshStatus() {
    return refreshStatus;
  }


  @JsonProperty(value = JSON_PROPERTY_REFRESH_STATUS)
  public void setRefreshStatus(@org.eclipse.jdt.annotation.Nullable
 String refreshStatus) {
    this.refreshStatus = refreshStatus;
  }


  /**
   * Return true if this VirtualFolderInfo object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VirtualFolderInfo virtualFolderInfo = (VirtualFolderInfo) o;
    return Objects.equals(this.name, virtualFolderInfo.name) &&
        Objects.equals(this.locations, virtualFolderInfo.locations) &&
        Objects.equals(this.collectionType, virtualFolderInfo.collectionType) &&
        Objects.equals(this.libraryOptions, virtualFolderInfo.libraryOptions) &&
        Objects.equals(this.itemId, virtualFolderInfo.itemId) &&
        Objects.equals(this.primaryImageItemId, virtualFolderInfo.primaryImageItemId) &&
        Objects.equals(this.refreshProgress, virtualFolderInfo.refreshProgress) &&
        Objects.equals(this.refreshStatus, virtualFolderInfo.refreshStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, locations, collectionType, libraryOptions, itemId, primaryImageItemId, refreshProgress, refreshStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VirtualFolderInfo {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    locations: ").append(toIndentedString(locations)).append("\n");
    sb.append("    collectionType: ").append(toIndentedString(collectionType)).append("\n");
    sb.append("    libraryOptions: ").append(toIndentedString(libraryOptions)).append("\n");
    sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
    sb.append("    primaryImageItemId: ").append(toIndentedString(primaryImageItemId)).append("\n");
    sb.append("    refreshProgress: ").append(toIndentedString(refreshProgress)).append("\n");
    sb.append("    refreshStatus: ").append(toIndentedString(refreshStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
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

    // add `Name` to the URL query string
    if (getName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getName()))));
    }

    // add `Locations` to the URL query string
    if (getLocations() != null) {
      for (int i = 0; i < getLocations().size(); i++) {
        joiner.add(String.format(java.util.Locale.ROOT, "%sLocations%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getLocations().get(i)))));
      }
    }

    // add `CollectionType` to the URL query string
    if (getCollectionType() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sCollectionType%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCollectionType()))));
    }

    // add `LibraryOptions` to the URL query string
    if (getLibraryOptions() != null) {
      joiner.add(getLibraryOptions().toUrlQueryString(prefix + "LibraryOptions" + suffix));
    }

    // add `ItemId` to the URL query string
    if (getItemId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sItemId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
    }

    // add `PrimaryImageItemId` to the URL query string
    if (getPrimaryImageItemId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sPrimaryImageItemId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageItemId()))));
    }

    // add `RefreshProgress` to the URL query string
    if (getRefreshProgress() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sRefreshProgress%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRefreshProgress()))));
    }

    // add `RefreshStatus` to the URL query string
    if (getRefreshStatus() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sRefreshStatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRefreshStatus()))));
    }

    return joiner.toString();
  }

    public static class Builder {

    private VirtualFolderInfo instance;

    public Builder() {
      this(new VirtualFolderInfo());
    }

    protected Builder(VirtualFolderInfo instance) {
      this.instance = instance;
    }

    public VirtualFolderInfo.Builder name(String name) {
      this.instance.name = name;
      return this;
    }
    public VirtualFolderInfo.Builder locations(List<String> locations) {
      this.instance.locations = locations;
      return this;
    }
    public VirtualFolderInfo.Builder collectionType(CollectionTypeOptions collectionType) {
      this.instance.collectionType = collectionType;
      return this;
    }
    public VirtualFolderInfo.Builder libraryOptions(LibraryOptions libraryOptions) {
      this.instance.libraryOptions = libraryOptions;
      return this;
    }
    public VirtualFolderInfo.Builder itemId(String itemId) {
      this.instance.itemId = itemId;
      return this;
    }
    public VirtualFolderInfo.Builder primaryImageItemId(String primaryImageItemId) {
      this.instance.primaryImageItemId = primaryImageItemId;
      return this;
    }
    public VirtualFolderInfo.Builder refreshProgress(Double refreshProgress) {
      this.instance.refreshProgress = refreshProgress;
      return this;
    }
    public VirtualFolderInfo.Builder refreshStatus(String refreshStatus) {
      this.instance.refreshStatus = refreshStatus;
      return this;
    }


    /**
    * returns a built VirtualFolderInfo instance.
    *
    * The builder is not reusable.
    */
    public VirtualFolderInfo build() {
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
  public static VirtualFolderInfo.Builder builder() {
    return new VirtualFolderInfo.Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public VirtualFolderInfo.Builder toBuilder() {
    return new VirtualFolderInfo.Builder()
      .name(getName())
      .locations(getLocations())
      .collectionType(getCollectionType())
      .libraryOptions(getLibraryOptions())
      .itemId(getItemId())
      .primaryImageItemId(getPrimaryImageItemId())
      .refreshProgress(getRefreshProgress())
      .refreshStatus(getRefreshStatus());
  }

}

