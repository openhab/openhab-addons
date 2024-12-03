/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link VeSyncRequest} is a Java class used as a DTO to hold the Vesync's API's common request data.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequest {

    public transient HttpMethod httpMethod;

    @SerializedName("timeZone")
    public String timeZone = "America/New_York";

    @SerializedName("acceptLanguage")
    public String acceptLanguage = "en";

    @SerializedName("appVersion")
    public String appVersion = "2.5.1";

    @SerializedName("phoneBrand")
    public String phoneBrand = "SM N9005";

    @SerializedName("phoneOS")
    public String phoneOS = "Android";

    @SerializedName("traceId")
    public String traceId = "";

    @SerializedName("method")
    public String method;

    @SerializedName("deviceId")
    public String deviceId;

    public VeSyncRequest() {
        traceId = String.valueOf(System.currentTimeMillis());
        httpMethod = HttpMethod.POST;
    }
}
