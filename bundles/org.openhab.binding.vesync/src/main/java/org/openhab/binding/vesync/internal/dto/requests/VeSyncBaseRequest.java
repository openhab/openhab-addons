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
package org.openhab.binding.vesync.internal.dto.requests;

import org.eclipse.jetty.http.HttpMethod;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncBaseRequest} is a Java class used as a DTO to hold the lowest common aspects of a VeSync Request
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncBaseRequest {

    public transient HttpMethod httpMethod;

    @SerializedName("acceptLanguage")
    public String acceptLanguage = "en";

    @SerializedName("traceId")
    public String traceId = "";

    @SerializedName("method")
    public String method = "";

    public VeSyncBaseRequest() {
        traceId = String.valueOf(System.currentTimeMillis());
        httpMethod = HttpMethod.POST;
    }
}
