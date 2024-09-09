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
package org.openhab.binding.senechome.internal.dto;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model provides the current statistics by the inverter.
 *
 * Section "STATISTIC" in Senec JSON.
 *
 * @author Korbinian Probst - Initial Contribution
 */
public class SenecHomeStatistics implements Serializable {

    private static final long serialVersionUID = -1102310892637495823L;

    /**
     * total Wh charged to the battery (kWh)
     */
    public @SerializedName("LIVE_BAT_CHARGE") String liveBatCharge;

    /**
     * total Wh discharged from the battery (kWh)
     */
    public @SerializedName("LIVE_BAT_DISCHARGE") String liveBatDischarge;

    /**
     * total Wh imported from grid (kWh)
     */
    public @SerializedName("LIVE_GRID_IMPORT") String liveGridImport;

    /**
     * total Wh supplied to the grid (kWh)
     */
    public @SerializedName("LIVE_GRID_EXPORT") String liveGridExport;

    /**
     * Total house consumption (kWh)
     */
    public @SerializedName("LIVE_HOUSE_CONS") String liveHouseConsumption;

    /**
     * Total Wh produced (kWh)
     */
    public @SerializedName("LIVE_PV_GEN") String livePowerGenerator;

    /**
     * Total Wh provided to Wallbox (Wh)
     */
    public @SerializedName("LIVE_WB_ENERGY") String[] liveWallboxEnergy;

    @Override
    public String toString() {
        return "SenecHomeStatistics [liveBatCharge=" + liveBatCharge + ", liveBatDischarge=" + liveBatDischarge
                + ", liveGridImport=" + liveGridImport + ", liveGridExport=" + liveGridExport
                + ", liveHouseConsumption=" + liveHouseConsumption + ", livePowerGen=" + livePowerGenerator
                + ", liveWallboxEnergy=" + Arrays.toString(liveWallboxEnergy) + "]";
    }
}
