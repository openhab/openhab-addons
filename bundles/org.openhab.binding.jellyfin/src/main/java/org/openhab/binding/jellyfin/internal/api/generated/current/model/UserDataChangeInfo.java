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
 * Class UserDataChangeInfo.
 */
@JsonPropertyOrder({ UserDataChangeInfo.JSON_PROPERTY_USER_ID, UserDataChangeInfo.JSON_PROPERTY_USER_DATA_LIST })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_USER_DATA_LIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UserItemDataDto> getUserDataList() {
        return userDataList;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_DATA_LIST, required = false)
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

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `UserDataList` to the URL query string
        if (getUserDataList() != null) {
            for (int i = 0; i < getUserDataList().size(); i++) {
                if (getUserDataList().get(i) != null) {
                    joiner.add(getUserDataList().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sUserDataList%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private UserDataChangeInfo instance;

        public Builder() {
            this(new UserDataChangeInfo());
        }

        protected Builder(UserDataChangeInfo instance) {
            this.instance = instance;
        }

        public UserDataChangeInfo.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public UserDataChangeInfo.Builder userDataList(List<UserItemDataDto> userDataList) {
            this.instance.userDataList = userDataList;
            return this;
        }

        /**
         * returns a built UserDataChangeInfo instance.
         *
         * The builder is not reusable.
         */
        public UserDataChangeInfo build() {
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
    public static UserDataChangeInfo.Builder builder() {
        return new UserDataChangeInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UserDataChangeInfo.Builder toBuilder() {
        return new UserDataChangeInfo.Builder().userId(getUserId()).userDataList(getUserDataList());
    }
}
