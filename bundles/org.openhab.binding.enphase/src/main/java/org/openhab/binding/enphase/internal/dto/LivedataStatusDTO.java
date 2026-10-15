/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for the Envoy live status data from the {@code /ivp/livedata/status} api call. Only the battery storage
 * related fields are mapped.
 *
 * @author Cedric Boon - Initial contribution
 */
public class LivedataStatusDTO {

    public static class MetersDTO {
        @SerializedName("enc_agg_soc")
        public Integer encAggSoc;
        @SerializedName("enc_agg_energy")
        public Integer encAggEnergy;
        public PowerDTO storage;
    }

    public static class PowerDTO {
        /**
         * Aggregated active power in milliwatts. For storage a positive value means the batteries are discharging.
         */
        @SerializedName("agg_p_mw")
        public Long aggPMw;
    }

    public MetersDTO meters;
}
