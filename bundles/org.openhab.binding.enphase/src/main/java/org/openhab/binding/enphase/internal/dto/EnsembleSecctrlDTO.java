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
 * Data class for the aggregated IQ Battery data from the {@code /ivp/ensemble/secctrl} api call. Only the battery
 * storage related fields are mapped.
 *
 * @author Cedric Boon - Initial contribution
 */
public class EnsembleSecctrlDTO {
    @SerializedName("ENC_agg_soc")
    public Integer encAggSoc;
    @SerializedName("ENC_agg_avail_energy")
    public Integer encAggAvailEnergy;
}
