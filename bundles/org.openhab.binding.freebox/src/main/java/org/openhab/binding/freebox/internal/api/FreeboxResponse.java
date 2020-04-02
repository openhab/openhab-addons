/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FreeboxResponse} is the Java class used to map the "APIResponse"
 * structure used by the API
 * https://dev.freebox.fr/sdk/os/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxResponse<T> {
    public static enum ErrorCode {
        NONE,
        @SerializedName("auth_required")
        AUTHORIZATION_REQUIRED,
        @SerializedName("insufficient_rights")
        INSUFFICIENT_RIGHTS,
        @SerializedName("invalid_token")
        INVALID_TOKEN,
        @SerializedName("pending_token")
        PENDING_TOKEN,
        @SerializedName("denied_from_external_ip")
        DENIED_FROM_EXTERNAL_IP,
        @SerializedName("invalid_request")
        INVALID_REQUEST,
        @SerializedName("ratelimited")
        RATE_LIMITED,
        @SerializedName("new_apps_denied")
        NEW_APPS_DENIED,
        @SerializedName("apps_denied")
        APPS_DENIED,
        @SerializedName("internal_error")
        INTERNAL_ERROR;
        ;
    }

    private Boolean success;
    private ErrorCode errorCode = ErrorCode.NONE;
    private String uid;
    private String msg;
    private String missingRight;
    private T result;

    public void evaluate() throws FreeboxException {
        if (result == null && !(result instanceof EmptyResponse)) {
            throw new FreeboxException("Missing result data in API response");
        } else if (!isSuccess()) {
            throw new FreeboxException(this);
        }
    }

    public String getMissingRight() {
        return missingRight;
    }

    public Boolean isSuccess() {
        return success;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getUid() {
        return uid;
    }

    public String getMsg() {
        return msg;
    }

    public T getResult() {
        return result;
    }

}
