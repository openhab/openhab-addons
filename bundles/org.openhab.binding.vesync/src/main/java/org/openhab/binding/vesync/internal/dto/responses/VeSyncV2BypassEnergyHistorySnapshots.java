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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV2BypassEnergyHistorySnapshots} class is used as a DTO to hold the Vesync's API's common response
 * data, with regard's to the energy use of an outlet device at multiple points in time.
 *
 * @author Marcel Goerentz - Initial contribution
 */
public class VeSyncV2BypassEnergyHistorySnapshots {

    @SerializedName("energyInfos")
    public List<VeSyncV2BypassEnergyHistoryInfoSnapshot> energyInfos = new ArrayList<VeSyncV2BypassEnergyHistoryInfoSnapshot>();

    @SerializedName("total")
    public int total = 0;
}
