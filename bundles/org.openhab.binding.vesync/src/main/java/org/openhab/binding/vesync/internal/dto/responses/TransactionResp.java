/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TransactionResp} class is used as a DTO to hold the Vesync's API's common response data.
 * This typically defines data around the request that was sent, and the status regarding its processing.
 *
 * @author David Goodyear - Initial contribution
 */
public class TransactionResp {

    @SerializedName("traceId")
    public String traceId;

    @SerializedName("code")
    public String code;

    @SerializedName("msg")
    public String msg;

    public String getMsg() {
        return msg;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getCode() {
        return code;
    }

    public boolean isMsgSuccess() {
        return (msg != null) ? "request success".equals(msg) : false;
    }

    public boolean isMsgDeviceOffline() {
        return (msg != null) ? "device offline".equals(msg) : false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [traceId=\"" + traceId + "\", msg=\"" + msg + "\", code=\"" + code
                + "\"]";
    }
}
