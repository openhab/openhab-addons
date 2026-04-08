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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV2BypassEnergyHistoryInfoSnapshot} class is used as a DTO to hold the Vesync's API's common response
 * data, with regard's to the energy use of an outlet device at a given point in time.
 *
 * @author Marcel Goerentz - Initial contribution
 */
public class VeSyncV2BypassEnergyHistoryInfoSnapshot {

    @SerializedName("timestamp")
    public long timestamp = 0;

    @SerializedName("energy")
    public double energy = 0.00;
}
