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
package org.openhab.binding.fronius.internal.api.dto.ohmpilot;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OhmpilotRealtimeBodyDTO} is responsible for storing
 * the "Body" node of the {@link OhmpilotRealtimeResponseDTO}.
 *
 * @author Hannes Spenger - Initial contribution
 */
public class OhmpilotRealtimeBodyDTO {
    @SerializedName("Data")
    private OhmpilotRealtimeBodyDataDTO data;

    public OhmpilotRealtimeBodyDataDTO getData() {
        if (data == null) {
            data = new OhmpilotRealtimeBodyDataDTO();
        }
        return data;
    }

    public void setData(OhmpilotRealtimeBodyDataDTO data) {
        this.data = data;
    }
}
