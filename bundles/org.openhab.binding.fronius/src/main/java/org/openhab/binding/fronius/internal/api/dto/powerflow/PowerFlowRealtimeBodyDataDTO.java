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
package org.openhab.binding.fronius.internal.api.dto.powerflow;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeBodyDataDTO} is responsible for storing
 * the "Data" node of the {@link PowerFlowRealtimeBodyDTO}.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class PowerFlowRealtimeBodyDataDTO {

    @SerializedName("Site")
    private PowerFlowRealtimeSiteDTO site;

    @SerializedName("Inverters")
    private Map<String, PowerFlowRealtimeInverterDTO> inverters;

    public Map<String, PowerFlowRealtimeInverterDTO> getInverters() {
        if (inverters == null) {
            inverters = new HashMap<>();
        }
        return inverters;
    }

    public PowerFlowRealtimeSiteDTO getSite() {
        if (site == null) {
            site = new PowerFlowRealtimeSiteDTO();
        }
        return site;
    }
}
