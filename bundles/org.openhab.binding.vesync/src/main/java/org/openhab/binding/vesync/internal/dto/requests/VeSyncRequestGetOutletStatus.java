/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestGetOutletStatus} is a Java class used as a DTO to hold the Vesync's API's common
 * request data for V2 ByPass payloads.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class VeSyncRequestGetOutletStatus extends VeSyncRequest {

    @SerializedName("cid")
    public String cid = "";

    @SerializedName("configModule")
    public String configModule = "";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("subDeviceNo")
    public int subDeviceNo = 0;

    @SerializedName("token")
    public String token = "";

    @SerializedName("userCountryCode")
    public String userCountryCode = "";

    @SerializedName("deviceId")
    public String deviceId = "";

    @SerializedName("configModel")
    public String configModel = "";

    @SerializedName("payload")
    public Payload payload = new Payload();

    public class Payload {

        @SerializedName("data")
        public Data data = new Data();

        // Empty class
        public class Data {
        }

        @SerializedName("method")
        public String method = "";

        @SerializedName("subDeviceNo")
        public int subDeviceNo = 0;

        @SerializedName("source")
        public String source = "APP";
    }
}
