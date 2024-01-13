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
package org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class PortalIotCommandRequest {

    @SerializedName("auth")
    final PortalAuthRequestParameter auth;

    @SerializedName("cmdName")
    final String commandName;

    @SerializedName("payload")
    final Object payload;

    @SerializedName("payloadType")
    final String payloadType;

    @SerializedName("td")
    final String td = "q";

    @SerializedName("toId")
    final String targetDeviceId;

    @SerializedName("toRes")
    final String targetResource;

    @SerializedName("toType")
    final String targetClass;

    public PortalIotCommandRequest(PortalAuthRequestParameter auth, String commandName, Object payload,
            String targetDeviceId, String targetResource, String targetClass, boolean json) {
        this.auth = auth;
        this.commandName = commandName;
        this.payload = payload;
        this.targetDeviceId = targetDeviceId;
        this.targetResource = targetResource;
        this.targetClass = targetClass;
        this.payloadType = json ? "j" : "x";
    }

    public static class JsonPayloadHeader {
        @SerializedName("pri")
        public final int pri = 1;
        @SerializedName("ts")
        public final long timestamp;
        @SerializedName("tzm")
        public final int tzm = 480;
        @SerializedName("ver")
        public final String version = "0.0.50";

        public JsonPayloadHeader() {
            timestamp = System.currentTimeMillis();
        }
    }
}
