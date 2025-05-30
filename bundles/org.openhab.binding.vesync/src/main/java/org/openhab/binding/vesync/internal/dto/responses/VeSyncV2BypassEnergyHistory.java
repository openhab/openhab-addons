/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * The {@link VeSyncV2BypassEnergyHistory} is a Java class used as a DTO to hold the Vesync's API's common response
 * data, in regard to an outlet device.
 *
 * @author Marcel Goerentz - Initial contribution
 */
public class VeSyncV2BypassEnergyHistory extends VeSyncResponse {

    @SerializedName("result")
    public EnergyHistory result;

    public class EnergyHistory extends VeSyncResponse {

        @SerializedName("result")
        public Result result = new Result();

        public class Result {

            @SerializedName("energyInfos")
            public List<EnergyInfo> energyInfos = new ArrayList<EnergyInfo>();

            public class EnergyInfo {

                @SerializedName("timestamp")
                public long timestamp = 0;

                @SerializedName("energy")
                public double energy = 0.00;
            }

            @SerializedName("total")
            public int total = 0;
        }
    }
}
