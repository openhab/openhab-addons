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
 * The {@link OhmpilotRealtimeResponseDTO} is responsible for storing
 * the response from the GetOhmPilotRealtimeData api
 *
 * @author Hannes Spenger - Initial contribution
 */
public class OhmpilotRealtimeResponseDTO extends BaseFroniusResponse {
    @SerializedName("Body")
    private OhmpilotRealtimeBodyDTO body;

    public OhmpilotRealtimeBodyDTO getBody() {
        if (body == null) {
            body = new OhmpilotRealtimeBodyDTO();
        }
        return body;
    }

    public void setBody(OhmpilotRealtimeBodyDTO body) {
        this.body = body;
    }
}
