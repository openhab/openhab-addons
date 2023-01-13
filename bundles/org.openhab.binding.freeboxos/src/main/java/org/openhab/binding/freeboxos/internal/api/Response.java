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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;

/**
 * Defines an API result that returns a single object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Response<ResultType> {
    private ErrorCode errorCode = ErrorCode.NONE;
    private Permission missingRight = Permission.NONE;
    private String msg = "";
    private @Nullable ResultType result;
    private boolean success;

    public @Nullable ResultType getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Permission getMissingRight() {
        return missingRight;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMsg() {
        return msg;
    }
}
