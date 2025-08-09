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

import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ForgotPasswordResult
 */
@JsonPropertyOrder({ ForgotPasswordResult.JSON_PROPERTY_ACTION, ForgotPasswordResult.JSON_PROPERTY_PIN_FILE,
        ForgotPasswordResult.JSON_PROPERTY_PIN_EXPIRATION_DATE })

public class ForgotPasswordResult {
    public static final String JSON_PROPERTY_ACTION = "Action";
    @org.eclipse.jdt.annotation.NonNull
    private ForgotPasswordAction action;

    public static final String JSON_PROPERTY_PIN_FILE = "PinFile";
    @org.eclipse.jdt.annotation.NonNull
    private String pinFile;

    public static final String JSON_PROPERTY_PIN_EXPIRATION_DATE = "PinExpirationDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime pinExpirationDate;

    public ForgotPasswordResult() {
    }

    public ForgotPasswordResult action(@org.eclipse.jdt.annotation.NonNull ForgotPasswordAction action) {
        this.action = action;
        return this;
    }

    /**
     * Gets or sets the action.
     * 
     * @return action
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ForgotPasswordAction getAction() {
        return action;
    }

    @JsonProperty(JSON_PROPERTY_ACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAction(@org.eclipse.jdt.annotation.NonNull ForgotPasswordAction action) {
        this.action = action;
    }

    public ForgotPasswordResult pinFile(@org.eclipse.jdt.annotation.NonNull String pinFile) {
        this.pinFile = pinFile;
        return this;
    }

    /**
     * Gets or sets the pin file.
     * 
     * @return pinFile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PIN_FILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPinFile() {
        return pinFile;
    }

    @JsonProperty(JSON_PROPERTY_PIN_FILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPinFile(@org.eclipse.jdt.annotation.NonNull String pinFile) {
        this.pinFile = pinFile;
    }

    public ForgotPasswordResult pinExpirationDate(
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime pinExpirationDate) {
        this.pinExpirationDate = pinExpirationDate;
        return this;
    }

    /**
     * Gets or sets the pin expiration date.
     * 
     * @return pinExpirationDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PIN_EXPIRATION_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getPinExpirationDate() {
        return pinExpirationDate;
    }

    @JsonProperty(JSON_PROPERTY_PIN_EXPIRATION_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPinExpirationDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime pinExpirationDate) {
        this.pinExpirationDate = pinExpirationDate;
    }

    /**
     * Return true if this ForgotPasswordResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForgotPasswordResult forgotPasswordResult = (ForgotPasswordResult) o;
        return Objects.equals(this.action, forgotPasswordResult.action)
                && Objects.equals(this.pinFile, forgotPasswordResult.pinFile)
                && Objects.equals(this.pinExpirationDate, forgotPasswordResult.pinExpirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, pinFile, pinExpirationDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ForgotPasswordResult {\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    pinFile: ").append(toIndentedString(pinFile)).append("\n");
        sb.append("    pinExpirationDate: ").append(toIndentedString(pinExpirationDate)).append("\n");
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
