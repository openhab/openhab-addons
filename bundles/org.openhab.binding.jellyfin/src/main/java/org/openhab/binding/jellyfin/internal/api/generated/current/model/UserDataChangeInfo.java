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
 * Class UserDataChangeInfo.
 */
@JsonPropertyOrder({ UserDataChangeInfo.JSON_PROPERTY_USER_ID, UserDataChangeInfo.JSON_PROPERTY_USER_DATA_LIST })

public class UserDataChangeInfo {
    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_USER_DATA_LIST = "UserDataList";
    @org.eclipse.jdt.annotation.NonNull
    private List<UserItemDataDto> userDataList = new ArrayList<>();

    public UserDataChangeInfo() {
    }

    public UserDataChangeInfo userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
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

    public UserDataChangeInfo userDataList(@org.eclipse.jdt.annotation.NonNull List<UserItemDataDto> userDataList) {
        this.userDataList = userDataList;
        return this;
    }

    public UserDataChangeInfo addUserDataListItem(UserItemDataDto userDataListItem) {
        if (this.userDataList == null) {
            this.userDataList = new ArrayList<>();
        }
        this.userDataList.add(userDataListItem);
        return this;
    }

    /**
     * Gets or sets the user data list.
     * 
     * @return userDataList
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_DATA_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UserItemDataDto> getUserDataList() {
        return userDataList;
    }

    @JsonProperty(JSON_PROPERTY_USER_DATA_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserDataList(@org.eclipse.jdt.annotation.NonNull List<UserItemDataDto> userDataList) {
        this.userDataList = userDataList;
    }

    /**
     * Return true if this UserDataChangeInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDataChangeInfo userDataChangeInfo = (UserDataChangeInfo) o;
        return Objects.equals(this.userId, userDataChangeInfo.userId)
                && Objects.equals(this.userDataList, userDataChangeInfo.userDataList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userDataList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDataChangeInfo {\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    userDataList: ").append(toIndentedString(userDataList)).append("\n");
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
