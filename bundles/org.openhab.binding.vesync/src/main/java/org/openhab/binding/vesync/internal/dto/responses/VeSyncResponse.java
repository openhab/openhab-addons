/**
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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncResponse} is a Java class used as a DTO to hold the Vesync's API's common response data.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncResponse {

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
        return "VesyncResponse [traceId=\"" + traceId + "\", msg=\"" + msg + "\", code=\"" + code + "\"]";
    }
}
