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
package org.openhab.binding.evnotify.api.v2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the error data that is returned by evnotify v2 API.
 *
 * e.g.
 *
 * {"error":{"code":1900,"message":"Provided token is invalid or no longer valid."}}
 *
 * @author Michael Schmidt - Initial contribution
 */
@NonNullByDefault
public class ErrorDTO {

    @SerializedName("error")
    @Nullable
    public Error error;

    @Nullable
    public Integer getCode() {
        return error.code;
    }

    @Nullable
    public String getMessage() {
        return error.message;
    }

    private class Error {

        @Nullable
        @SerializedName("code")
        public Integer code;

        @Nullable
        @SerializedName("message")
        public String message;
    }
}
