/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.api.entity.error;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Error response object from the LIVISI SmartHome api.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class ErrorResponseDTO {

    // General errors
    public static final int ERR_UNKNOWN = 1000;
    public static final int ERR_SERVICE_UNAVAILABLE = 1001;
    public static final int ERR_SERVICE_TIMEOUT = 1002;
    public static final int ERR_INTERNAL_API_ERROR = 1003;
    public static final int ERR_INVALID_SHC_OPERATION = 1004;
    public static final int ERR_MISSING_ARGUMENT_OR_WRONG_VALUE = 1005;
    public static final int ERR_SERVICE_TOO_BUSY = 1006;

    // Authentication and authorization errors
    public static final int ERR_UNKNOWN_AUTHENTICATION_ERROR = 2000;
    public static final int ERR_ACCESS_NOT_ALLOWED = 2001;
    public static final int ERR_INVALID_TOKEN_REQUEST = 2002;
    public static final int ERR_INVALID_CLIENT_CREDENTIALS = 2003;
    public static final int ERR_INVALID_TOKEN_SIGNATURE = 2004;
    public static final int ERR_SESSION_INITIALIZATION_FAILED = 2005;
    public static final int ERR_SESSION_EXISTS = 2006;
    public static final int ERR_TOKEN_EXPIRED = 2007;
    public static final int ERR_LOGIN_FROM_DIFFERENT_CLIENT = 2008;
    public static final int ERR_INVALID_USER_CREDENTIALS = 2009;
    public static final int ERR_REMOTE_ACCESS_NOT_ALLOWED = 2010;
    public static final int ERR_INSUFFICIENT_PERMISSIONS = 2011;
    public static final int ERR_SESSION_NOT_FOUND = 2012;
    public static final int ERR_ACCOUNT_TEMPORARY_LOCKED = 2013;

    // Entities
    public static final int ERR_ENTITY_DOES_NOT_EXIST = 3000;
    public static final int ERR_INVALID_REQUEST_CONTENT = 3001;
    public static final int ERR_NO_CHANGE_PERFORMED = 3002;
    public static final int ERR_ENTITY_ALREADY_EXISTS = 3003;
    public static final int ERR_INVALID_INTERACTION = 3004;

    // Products
    public static final int ERR_PREMIUM_SERVICE_CANNOT_BE_ENABLED_DIRECTLY = 3500;
    public static final int ERR_CANNOT_REMOVE_A_PRODUCT_THAT_WAS_PAID = 3501;

    // Actions
    public static final int ERR_INVALID_ACTION_TRIGGERED = 4000;
    public static final int ERR_INVALID_PARAMETER = 4001;
    public static final int ERR_TRIGGER_ACTION_NOT_ALLOWED = 4002;
    public static final int ERR_UNSUPPORTED_ACTION_TYPE = 4003;

    // Configuration
    public static final int ERR_ERROR_UPDATING_CONFIG = 5000;
    public static final int ERR_CONFIG_LOCKED_BY_OTHER_PROCESS = 5001;
    public static final int ERR_COMMUNICATION_WITH_SHC_FAILED = 5002;
    public static final int ERR_LATEST_TERMS_AND_CONDITIONS_NOT_ACCEPTED_BY_USER = 5003;
    public static final int ERR_ONE_SHC_ALREADY_REGISTERED = 5004;
    public static final int ERR_USER_HAS_NO_REGISTERED_SHC = 5005;
    public static final int ERR_CONTROLLER_OFFLINE = 5006;
    public static final int ERR_REGISTRATION_FAILURE = 5009;

    // Smart codes
    public static final int ERR_SMARTCODE_REQUEST_NOT_ALLOWED = 6000;
    public static final int ERR_SMARTCODE_CANNOT_BE_REDEEMED = 6001;
    public static final int ERR_RESTRICTED_ACCESS = 6002;

    @SerializedName("errorcode")
    private int code;
    @SerializedName("description")
    private String description;
    @SerializedName("messages")
    private List<String> messages;

    /**
     * @return the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the error code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the messages
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * @param messages the messages to set
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        String stringRepresentation = "ErrorResponse [code=" + code + ", description=" + description;
        if (messages != null) {
            stringRepresentation += ", messages=" + messages.toString();
        }
        stringRepresentation += "]";
        return stringRepresentation;
    }
}
