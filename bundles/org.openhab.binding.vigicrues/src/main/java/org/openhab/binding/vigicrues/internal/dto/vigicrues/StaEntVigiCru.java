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
package org.openhab.binding.vigicrues.internal.dto.vigicrues;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StaEntVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class StaEntVigiCru {
    @SerializedName("CdEntVigiCru")
    public String cdEntVigiCru;

    @SerializedName("TypEntVigiCru")
    public String typEntVigiCru;

    @SerializedName("LbEntVigiCru")
    public String lbEntVigiCru;

    @SerializedName("CdDistrictEntVigiCru_1")
    public String cdDistrictEntVigiCru1;

    @SerializedName("DtHrCreatEntVigiCru")
    public String dtHrCreatEntVigiCru;

    @SerializedName("DtHrMajEntVigiCru")
    public String dtHrMajEntVigiCru;

    @SerializedName("StEntVigiCru")
    public String stEntVigiCru;

    @SerializedName("CdTCC")
    public String cdTCC;

    @SerializedName("CdInt")
    public String cdInt;

    @SerializedName("CdStationHydro")
    public String cdStationHydro;

    @SerializedName("CdStationHydroAncienRef")
    public String cdStationHydroAncienRef;

    @SerializedName("LbStationHydro")
    public String lbStationHydro;

    @SerializedName("TypStationHydro")
    public String typStationHydro;

    @SerializedName("CdCommune")
    public String cdCommune;

    @SerializedName("CdZoneHydro")
    public String cdZoneHydro;

    @SerializedName("NomEntiteHydrographique")
    public String nomEntiteHydrographique;

    @SerializedName("CoordXStationHydro")
    public String coordXStationHydro;

    @SerializedName("CoordYStationHydro")
    public String coordYStationHydro;

    @SerializedName("ProjCoordStationHydro")
    public String projCoordStationHydro;

    @SerializedName("VigilanceCrues")
    public VigilanceCrues vigilanceCrues;
}
