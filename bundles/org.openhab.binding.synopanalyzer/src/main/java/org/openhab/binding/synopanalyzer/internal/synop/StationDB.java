/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyzer.internal.synop;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StationDB} creates is a DTO for stations.json database.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
public class StationDB {
    public class Station {
        public String country;
        public String pack;
        @SerializedName("id_omm")
        public int idOmm;
        @SerializedName("numer_sta")
        public long numerSta;
        @SerializedName("usual_name")
        public String usualName;
        public double latitude;
        public double longitude;
        public double elevation;
        @SerializedName("station_type")
        public int stationType;

        public String getLocation() {
            return Double.toString(latitude) + "," + Double.toString(longitude);
        }
    }

    public List<Station> stations;
}
