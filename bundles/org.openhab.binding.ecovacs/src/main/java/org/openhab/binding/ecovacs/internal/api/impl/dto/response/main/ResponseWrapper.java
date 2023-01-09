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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.main;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class ResponseWrapper<T> {
    @SerializedName("code")
    private final String code;

    @SerializedName("time")
    private final String time;

    @SerializedName("msg")
    private final String message;

    @SerializedName("data")
    private final T data;

    @SerializedName("success")
    private final boolean success;

    public ResponseWrapper(String code, String time, String message, T data, boolean success) {
        this.code = code;
        this.time = time;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }
}
