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
package org.openhab.binding.fronius.internal.api.dto.meter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterRealtimeBodyDTO} is responsible for storing
 * the "Body" node of the {@link MeterRealtimeResponseDTO}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MeterRealtimeBodyDTO {
    @SerializedName("Data")
    private MeterRealtimeBodyDataDTO data;

    public MeterRealtimeBodyDataDTO getData() {
        if (data == null) {
            data = new MeterRealtimeBodyDataDTO();
        }
        return data;
    }

    public void setData(MeterRealtimeBodyDataDTO data) {
        this.data = data;
    }
}
