/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * A CloudrainAPIError captures error messages returned from the Cloudrain API
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAPIError {

    public static final String ERROR_TYPE_UNKNOWN = "Unknown error type";
    public static final String ERROR_TYPE_VALUE = "ValueError";
    public static final String ERROR_MSG_UNAVAILABLE = "No error details available";

    @SerializedName(value = "errorMessage", alternate = { "message", "error" })
    private String message = ERROR_MSG_UNAVAILABLE;

    @SerializedName("errorType")
    private String type = ERROR_TYPE_UNKNOWN;

    @SerializedName("stackTrace")
    private String[] stacktrace = { "" };

    private int status;

    private String fullResponse = "";

    /**
     * Creates an instance without further information
     */
    public CloudrainAPIError() {
    }

    /**
     * Creates an instance with the API's HTTP response status
     *
     * @param status the API's HTTP response status
     */
    public CloudrainAPIError(int status) {
        this.status = status;
    }

    /**
     * Creates an instance with the API's HTTP response status and an error message
     *
     * @param status the API's HTTP response status
     * @param message an error message
     */
    public CloudrainAPIError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the API's response error message
     *
     * @return the API's response error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the API's response error message
     *
     * @param message the API's response error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the API's full content response
     *
     * @return the API's full content response if available
     */
    public @Nullable String getFullResponse() {
        return fullResponse;
    }

    /**
     * Sets the API's full content response
     *
     * @param fullResponse the API's full content response
     */
    public void setFullResponse(String fullResponse) {
        this.fullResponse = fullResponse;
    }

    /**
     *
     * Returns the API response if available as partial String with the specified number of characters. This is intended
     * to be used for shorter logs in case of large responses.
     *
     * @param length the number of characters of the response
     * @return the substring of the API response. Null if no response is available.
     */
    public @Nullable String getPartialResponse(int length) {
        String partialResponse = fullResponse;
        if (partialResponse.length() > length) {
            partialResponse = partialResponse.substring(0, length);
        }
        return partialResponse;
    }

    /**
     * Returns the API's HTTP response status
     *
     * @return the API's HTTP response status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the API's HTTP response status
     *
     * @param status the API's HTTP response status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Returns the API's stack trace. Array with empty String if not set.
     *
     * @return the API's HTTP response status
     */
    public String[] getStacktrace() {
        return stacktrace;
    }

    /**
     * Sets the API's stack trace
     *
     * @param stacktrace the API's stack trace
     */
    public void setStacktrace(String[] stacktrace) {
        this.stacktrace = stacktrace;
    }

    /**
     * Returns the APIs error type if available or defaults to <code>ERROR_TYPE_UNKNOWN</code>
     *
     * @return the APIs error type if available.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the APIs error type
     *
     * @param type APIs error type
     */
    public void setType(String type) {
        this.type = type;
    }
}
