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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeBodyData} is responsible for storing
 * the "Data" node of the {@link PowerFlowRealtimeBody}.
 *
 * @author Thomas Rokohl - Initial contribution
 */
@NonNullByDefault
public class PowerFlowRealtimeBodyData {
    @SerializedName("Site")
    private @Nullable PowerFlowRealtimeSite site;

    @SerializedName("Inverters")
    private @Nullable Map<String, PowerFlowRealtimeInverter> inverters;

    public Map<String, PowerFlowRealtimeInverter> getInverters() {
        Map<String, PowerFlowRealtimeInverter> localInverters = inverters;
        if (localInverters == null) {
            inverters = localInverters = new HashMap<>();
        }
        return localInverters;
    }

    public @Nullable PowerFlowRealtimeSite getSite() {
        return site;
    }
}
