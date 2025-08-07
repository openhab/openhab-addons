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
 * PinRedeemResult
 */
@JsonPropertyOrder({ PinRedeemResult.JSON_PROPERTY_SUCCESS, PinRedeemResult.JSON_PROPERTY_USERS_RESET })

public class PinRedeemResult {
    public static final String JSON_PROPERTY_SUCCESS = "Success";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean success;

    public static final String JSON_PROPERTY_USERS_RESET = "UsersReset";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> usersReset = new ArrayList<>();

    public PinRedeemResult() {
    }

    public PinRedeemResult success(@org.eclipse.jdt.annotation.NonNull Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Users.PinRedeemResult is success.
     * 
     * @return success
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUCCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSuccess() {
        return success;
    }

    @JsonProperty(JSON_PROPERTY_SUCCESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuccess(@org.eclipse.jdt.annotation.NonNull Boolean success) {
        this.success = success;
    }

    public PinRedeemResult usersReset(@org.eclipse.jdt.annotation.NonNull List<String> usersReset) {
        this.usersReset = usersReset;
        return this;
    }

    public PinRedeemResult addUsersResetItem(String usersResetItem) {
        if (this.usersReset == null) {
            this.usersReset = new ArrayList<>();
        }
        this.usersReset.add(usersResetItem);
        return this;
    }

    /**
     * Gets or sets the users reset.
     * 
     * @return usersReset
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USERS_RESET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUsersReset() {
        return usersReset;
    }

    @JsonProperty(JSON_PROPERTY_USERS_RESET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsersReset(@org.eclipse.jdt.annotation.NonNull List<String> usersReset) {
        this.usersReset = usersReset;
    }

    /**
     * Return true if this PinRedeemResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PinRedeemResult pinRedeemResult = (PinRedeemResult) o;
        return Objects.equals(this.success, pinRedeemResult.success)
                && Objects.equals(this.usersReset, pinRedeemResult.usersReset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, usersReset);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PinRedeemResult {\n");
        sb.append("    success: ").append(toIndentedString(success)).append("\n");
        sb.append("    usersReset: ").append(toIndentedString(usersReset)).append("\n");
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
