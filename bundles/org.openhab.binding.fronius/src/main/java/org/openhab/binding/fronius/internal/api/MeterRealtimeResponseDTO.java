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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterRealtimeResponseDTO} is responsible for storing
 * the response from the powerflowrealtime api
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MeterRealtimeResponseDTO extends BaseFroniusResponse {
    @SerializedName("Body")
    private MeterRealtimeBodyDTO body;

    public MeterRealtimeBodyDTO getBody() {
        if (body == null) {
            body = new MeterRealtimeBodyDTO();
        }
        return body;
    }

    public void setBody(MeterRealtimeBodyDTO body) {
        this.body = body;
    }
}
