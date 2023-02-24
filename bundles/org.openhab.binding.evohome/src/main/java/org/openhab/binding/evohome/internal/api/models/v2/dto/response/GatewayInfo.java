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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the gateway info
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class GatewayInfo {

    @SerializedName("gatewayId")
    private String gatewayId;

    @SerializedName("mac")
    private String macAddress;

    @SerializedName("crc")
    private String crc;

    @SerializedName("isWiFi")
    private boolean isWifi;

    public String getGatewayId() {
        return gatewayId;
    }
}
