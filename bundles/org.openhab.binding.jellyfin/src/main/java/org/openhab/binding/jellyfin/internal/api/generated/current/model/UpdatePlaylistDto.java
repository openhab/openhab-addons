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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Update existing playlist dto. Fields set to &#x60;null&#x60; will not be updated and keep their current values.
 */
@JsonPropertyOrder({ UpdatePlaylistDto.JSON_PROPERTY_NAME, UpdatePlaylistDto.JSON_PROPERTY_IDS,
        UpdatePlaylistDto.JSON_PROPERTY_USERS, UpdatePlaylistDto.JSON_PROPERTY_IS_PUBLIC })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdatePlaylistDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_IDS = "Ids";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> ids;

    public static final String JSON_PROPERTY_USERS = "Users";
    @org.eclipse.jdt.annotation.NonNull
    private List<PlaylistUserPermissions> users;

    public static final String JSON_PROPERTY_IS_PUBLIC = "IsPublic";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPublic;

    public UpdatePlaylistDto() {
    }

    public UpdatePlaylistDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the new playlist.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public UpdatePlaylistDto ids(@org.eclipse.jdt.annotation.NonNull List<UUID> ids) {
        this.ids = ids;
        return this;
    }

    public UpdatePlaylistDto addIdsItem(UUID idsItem) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        this.ids.add(idsItem);
        return this;
    }

    /**
     * Gets or sets item ids of the playlist.
     * 
     * @return ids
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getIds() {
        return ids;
    }

    @JsonProperty(value = JSON_PROPERTY_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIds(@org.eclipse.jdt.annotation.NonNull List<UUID> ids) {
        this.ids = ids;
    }

    public UpdatePlaylistDto users(@org.eclipse.jdt.annotation.NonNull List<PlaylistUserPermissions> users) {
        this.users = users;
        return this;
    }

    public UpdatePlaylistDto addUsersItem(PlaylistUserPermissions usersItem) {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        this.users.add(usersItem);
        return this;
    }

    /**
     * Gets or sets the playlist users.
     * 
     * @return users
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<PlaylistUserPermissions> getUsers() {
        return users;
    }

    @JsonProperty(value = JSON_PROPERTY_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsers(@org.eclipse.jdt.annotation.NonNull List<PlaylistUserPermissions> users) {
        this.users = users;
    }

    public UpdatePlaylistDto isPublic(@org.eclipse.jdt.annotation.NonNull Boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the playlist is public.
     * 
     * @return isPublic
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPublic() {
        return isPublic;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPublic(@org.eclipse.jdt.annotation.NonNull Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Return true if this UpdatePlaylistDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdatePlaylistDto updatePlaylistDto = (UpdatePlaylistDto) o;
        return Objects.equals(this.name, updatePlaylistDto.name) && Objects.equals(this.ids, updatePlaylistDto.ids)
                && Objects.equals(this.users, updatePlaylistDto.users)
                && Objects.equals(this.isPublic, updatePlaylistDto.isPublic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ids, users, isPublic);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdatePlaylistDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
        sb.append("    users: ").append(toIndentedString(users)).append("\n");
        sb.append("    isPublic: ").append(toIndentedString(isPublic)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Ids` to the URL query string
        if (getIds() != null) {
            for (int i = 0; i < getIds().size(); i++) {
                if (getIds().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getIds().get(i)))));
                }
            }
        }

        // add `Users` to the URL query string
        if (getUsers() != null) {
            for (int i = 0; i < getUsers().size(); i++) {
                if (getUsers().get(i) != null) {
                    joiner.add(getUsers().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sUsers%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `IsPublic` to the URL query string
        if (getIsPublic() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsPublic%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPublic()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdatePlaylistDto instance;

        public Builder() {
            this(new UpdatePlaylistDto());
        }

        protected Builder(UpdatePlaylistDto instance) {
            this.instance = instance;
        }

        public UpdatePlaylistDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public UpdatePlaylistDto.Builder ids(List<UUID> ids) {
            this.instance.ids = ids;
            return this;
        }

        public UpdatePlaylistDto.Builder users(List<PlaylistUserPermissions> users) {
            this.instance.users = users;
            return this;
        }

        public UpdatePlaylistDto.Builder isPublic(Boolean isPublic) {
            this.instance.isPublic = isPublic;
            return this;
        }

        /**
         * returns a built UpdatePlaylistDto instance.
         *
         * The builder is not reusable.
         */
        public UpdatePlaylistDto build() {
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
    public static UpdatePlaylistDto.Builder builder() {
        return new UpdatePlaylistDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdatePlaylistDto.Builder toBuilder() {
        return new UpdatePlaylistDto.Builder().name(getName()).ids(getIds()).users(getUsers()).isPublic(getIsPublic());
    }
}
