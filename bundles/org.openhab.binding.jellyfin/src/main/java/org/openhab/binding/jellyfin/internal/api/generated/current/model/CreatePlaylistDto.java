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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Create new playlist dto.
 */
@JsonPropertyOrder({ CreatePlaylistDto.JSON_PROPERTY_NAME, CreatePlaylistDto.JSON_PROPERTY_IDS,
        CreatePlaylistDto.JSON_PROPERTY_USER_ID, CreatePlaylistDto.JSON_PROPERTY_MEDIA_TYPE,
        CreatePlaylistDto.JSON_PROPERTY_USERS, CreatePlaylistDto.JSON_PROPERTY_IS_PUBLIC })

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
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
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
    @JsonProperty(JSON_PROPERTY_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getIds() {
        return ids;
    }

    @JsonProperty(JSON_PROPERTY_IDS)
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
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
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
    @JsonProperty(JSON_PROPERTY_MEDIA_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MediaType getMediaType() {
        return mediaType;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_TYPE)
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
    @JsonProperty(JSON_PROPERTY_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlaylistUserPermissions> getUsers() {
        return users;
    }

    @JsonProperty(JSON_PROPERTY_USERS)
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
    @JsonProperty(JSON_PROPERTY_IS_PUBLIC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsPublic() {
        return isPublic;
    }

    @JsonProperty(JSON_PROPERTY_IS_PUBLIC)
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
}
