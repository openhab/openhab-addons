/**
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager;

/**
 * Defines an API result that returns a single object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Response<ResultType> {
    public static enum ErrorCode {
        AUTH_REQUIRED,
        BAD_LOGIN,
        TOO_SHORT,
        IN_DICTIONNARY,
        BAD_XKCD,
        NOT_ENOUGH_DIFFERENT_CHARS,
        INVALID_TOKEN,
        PENDING_TOKEN,
        INSUFFICIENT_RIGHTS,
        DENIED_FROM_EXTERNAL_IP,
        INVALID_REQUEST,
        RATELIMITED,
        NEW_APPS_DENIED,
        APPS_AUTHORIZATION_DENIED,
        APPS_AUTHORIZATION_TIMEOUT,
        PASSWORD_RESET_DENIED,
        APPS_DENIED,
        INTERNAL_ERROR,
        SERVICE_DOWN,
        DISK_FULL,
        OP_FAILED,
        DISK_BUSY,
        ARRAY_START_FAILED,
        ARRAY_STOP_FAILED,
        ARRAY_NOT_FOUND,
        INVAL,
        NODEV,
        NOENT,
        NETDOWN,
        BUSY,
        INVALID_PORT,
        INSECURE_PASSWORD,
        INVALID_PROVIDER,
        INVALID_NEXT_HOP,
        INVALID_API_VERSION,
        INVAL_WPS_MACFILTER,
        INVAL_WPS_NEEDS_CCMP,
        INVALID_ID,
        PATH_NOT_FOUND,
        ACCESS_DENIED,
        DESTINATION_CONFLICT,
        CANCELLED,
        TASK_NOT_FOUND,
        HTTP,
        INVALID_URL,
        INVALID_OPERATION,
        INVALID_FILE,
        CTX_FILE_ERROR,
        HIBERNATING,
        TOO_MANY_TASKS,
        EXISTS,
        EXIST,
        CONNECTION_REFUSED,
        NO_FREEBOX,
        ALREADY_AUTHORIZED,
        ECRC,
        ERR_001,
        ERR_002,
        ERR_003,
        ERR_004,
        ERR_005,
        ERR_009,
        ERR_010,
        ERR_030,
        ERR_031,
        NONE,
        UNKNOWN;
    }

    private ErrorCode errorCode = ErrorCode.NONE;
    private LoginManager.Permission missingRight = LoginManager.Permission.NONE;
    private String msg = "";
    private List<ResultType> result = List.of();
    private boolean success;

    public List<ResultType> getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public LoginManager.Permission getMissingRight() {
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

import com.google.gson.annotations.SerializedName;

/**
 * Defines an API result that returns a single object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Response<T> {
    public static enum ErrorCode {
        NONE,
        @SerializedName("auth_required")
        AUTHORIZATION_REQUIRED,
        @SerializedName("internal_error")
        INTERNAL_ERROR,
        @SerializedName("invalid_token")
        INVALID_TOKEN;
    }

    private boolean success;
    private ErrorCode errorCode = ErrorCode.NONE;
    private String msg = "";

    private @Nullable Permission missingRight;
    private @Nullable T result;

    public @Nullable T getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable Permission getMissingRight() {
>>>>>>> 46dadb1 SAT warnings handling
        return missingRight;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMsg() {
        return msg;
    }
}
