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
 * Create new playlist dto.
 */
@JsonPropertyOrder({ CreatePlaylistDto.JSON_PROPERTY_NAME, CreatePlaylistDto.JSON_PROPERTY_IDS,
        CreatePlaylistDto.JSON_PROPERTY_USER_ID, CreatePlaylistDto.JSON_PROPERTY_MEDIA_TYPE,
        CreatePlaylistDto.JSON_PROPERTY_USERS, CreatePlaylistDto.JSON_PROPERTY_IS_PUBLIC })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CreatePlaylistDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_IDS = "Ids";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> ids = new ArrayList<>();

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_MEDIA_TYPE = "MediaType";
    @org.eclipse.jdt.annotation.NonNull
    private MediaType mediaType;

    public static final String JSON_PROPERTY_USERS = "Users";
    @org.eclipse.jdt.annotation.NonNull
    private List<PlaylistUserPermissions> users = new ArrayList<>();

    public static final String JSON_PROPERTY_IS_PUBLIC = "IsPublic";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPublic;

    public CreatePlaylistDto() {
    }

    public CreatePlaylistDto name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public CreatePlaylistDto ids(@org.eclipse.jdt.annotation.NonNull List<UUID> ids) {
        this.ids = ids;
        return this;
    }

    public CreatePlaylistDto addIdsItem(UUID idsItem) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        this.ids.add(idsItem);
        return this;
    }

    /**
     * Gets or sets item ids to add to the playlist.
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

    public CreatePlaylistDto userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user id.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public CreatePlaylistDto mediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Gets or sets the media type.
     * 
     * @return mediaType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaType getMediaType() {
        return mediaType;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public CreatePlaylistDto users(@org.eclipse.jdt.annotation.NonNull List<PlaylistUserPermissions> users) {
        this.users = users;
        return this;
    }

    public CreatePlaylistDto addUsersItem(PlaylistUserPermissions usersItem) {
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

    public CreatePlaylistDto isPublic(@org.eclipse.jdt.annotation.NonNull Boolean isPublic) {
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
     * Return true if this CreatePlaylistDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreatePlaylistDto createPlaylistDto = (CreatePlaylistDto) o;
        return Objects.equals(this.name, createPlaylistDto.name) && Objects.equals(this.ids, createPlaylistDto.ids)
                && Objects.equals(this.userId, createPlaylistDto.userId)
                && Objects.equals(this.mediaType, createPlaylistDto.mediaType)
                && Objects.equals(this.users, createPlaylistDto.users)
                && Objects.equals(this.isPublic, createPlaylistDto.isPublic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ids, userId, mediaType, users, isPublic);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreatePlaylistDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    mediaType: ").append(toIndentedString(mediaType)).append("\n");
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

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `MediaType` to the URL query string
        if (getMediaType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaType()))));
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

        private CreatePlaylistDto instance;

        public Builder() {
            this(new CreatePlaylistDto());
        }

        protected Builder(CreatePlaylistDto instance) {
            this.instance = instance;
        }

        public CreatePlaylistDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public CreatePlaylistDto.Builder ids(List<UUID> ids) {
            this.instance.ids = ids;
            return this;
        }

        public CreatePlaylistDto.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public CreatePlaylistDto.Builder mediaType(MediaType mediaType) {
            this.instance.mediaType = mediaType;
            return this;
        }

        public CreatePlaylistDto.Builder users(List<PlaylistUserPermissions> users) {
            this.instance.users = users;
            return this;
        }

        public CreatePlaylistDto.Builder isPublic(Boolean isPublic) {
            this.instance.isPublic = isPublic;
            return this;
        }

        /**
         * returns a built CreatePlaylistDto instance.
         *
         * The builder is not reusable.
         */
        public CreatePlaylistDto build() {
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
    public static CreatePlaylistDto.Builder builder() {
        return new CreatePlaylistDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CreatePlaylistDto.Builder toBuilder() {
        return new CreatePlaylistDto.Builder().name(getName()).ids(getIds()).userId(getUserId())
                .mediaType(getMediaType()).users(getUsers()).isPublic(getIsPublic());
    }
}
