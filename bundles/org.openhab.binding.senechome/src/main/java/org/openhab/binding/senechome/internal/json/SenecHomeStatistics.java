/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model provides the current statistics by the inverter.
 *
 * @author Korbinian Probst - Initial Contribution
 */
public class SenecHomeStatistics implements Serializable {

    private static final long serialVersionUID = -7479338432170375451L;

    /**
     * total Wh charged to the battery
     */
    public @SerializedName("LIVE_BAT_CHARGE") String liveBatCharge;

    /**
     * total Wh discharged from the battery
     */
    public @SerializedName("LIVE_BAT_DISCHARGE") String liveBatDischarge;

    /**
     * total Wh imported from grid
     */
    public @SerializedName("LIVE_GRID_IMPORT") String liveGridImport;

    /**
     * total Wh supplied to the grid
     */
    public @SerializedName("LIVE_GRID_EXPORT") String liveGridExport;

    @Override
    public String toString() {
        return "SenecHomeStatistics [liveBatCharge=" + liveBatCharge + ", liveBatDischarge= " + liveBatDischarge
                + ", liveGridImport= " + liveGridImport + ", liveGridExport= " + liveGridExport + "]";
    }
}
