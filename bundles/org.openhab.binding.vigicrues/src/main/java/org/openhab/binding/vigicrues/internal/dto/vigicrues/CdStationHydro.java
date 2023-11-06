/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal.dto.vigicrues;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CdStationHydro} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class CdStationHydro {

    public class PereBoitEntVigiCru {
        @SerializedName("CdEntVigiCru")
        public String cdEntVigiCru;
    }

    public class CruesHistorique {
        @SerializedName("LbUsuel")
        private String name;
        @SerializedName("ValHauteur")
        private double height;
        @SerializedName("ValDebit")
        private double flow;

        public Map<String, String> getDescription() {
            Map<String, String> result = new HashMap<>();
            if (height != 0) {
                result.put(String.format("%s %s (%s)", FLOOD, HEIGHT, name),
                        String.format(Locale.US, "%.2f m", height));
            }
            if (flow != 0) {
                result.put(String.format("%s %s (%s)", FLOOD, FLOW, name), String.format(Locale.US, "%.2f m³/s", flow));
            }
            return result;
        }
    }

    public class VigilanceCrues {
        @SerializedName("PereBoitEntVigiCru")
        public PereBoitEntVigiCru pereBoitEntVigiCru;
        @SerializedName("CruesHistoriques")
        public List<CruesHistorique> cruesHistoriques;
        /*
         * Currently unused, maybe interesting in the future
         *
         * @SerializedName("StationPrevision")
         * public String stationPrevision;
         *
         * @SerializedName("Photo")
         * public String photo;
         *
         * @SerializedName("ZoomInitial")
         * public String zoomInitial;
         */
    }

    @SerializedName("VigilanceCrues")
    public VigilanceCrues vigilanceCrues;
    /*
     * Currently unused, maybe interesting in the future
     *
     * @SerializedName("VersionFlux")
     * public String versionFlux;
     *
     * @SerializedName("CdStationHydro")
     * public String cdStationHydro;
     *
     * @SerializedName("LbStationHydro")
     * public String lbStationHydro;
     *
     * @SerializedName("LbCoursEau")
     * public String lbCoursEau;
     *
     * @SerializedName("CdStationHydroAncienRef")
     * public String cdStationHydroAncienRef;
     *
     * @SerializedName("CdCommune")
     * public String cdCommune;
     *
     * @SerializedName("error_msg")
     * public String errorMsg;
     */
}
