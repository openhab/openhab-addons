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

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to hold data on user permissions for playlists.
 */
@JsonPropertyOrder({ PlaylistUserPermissions.JSON_PROPERTY_USER_ID, PlaylistUserPermissions.JSON_PROPERTY_CAN_EDIT })

public class PlaylistUserPermissions {
    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_CAN_EDIT = "CanEdit";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canEdit;

    public PlaylistUserPermissions() {
    }

    public PlaylistUserPermissions userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
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

    public PlaylistUserPermissions canEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the user has edit permissions.
     * 
     * @return canEdit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAN_EDIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCanEdit() {
        return canEdit;
    }

    @JsonProperty(JSON_PROPERTY_CAN_EDIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * Return true if this PlaylistUserPermissions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaylistUserPermissions playlistUserPermissions = (PlaylistUserPermissions) o;
        return Objects.equals(this.userId, playlistUserPermissions.userId)
                && Objects.equals(this.canEdit, playlistUserPermissions.canEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, canEdit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaylistUserPermissions {\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    canEdit: ").append(toIndentedString(canEdit)).append("\n");
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
