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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ListEntVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ListEntVigiCru {
    @SerializedName("CdEntVigiCru")
    public String cdEntVigiCru;
    @SerializedName("TypEntVigiCru")
    public String typEntVigiCru;
    @SerializedName("LbEntVigiCru")
    public String lbEntVigiCru;
    @SerializedName("CdDistrictEntVigiCru_1")
    public String cdDistrictEntVigiCru1;
    @SerializedName("CdDistrictEntVigiCru_2")
    public String cdDistrictEntVigiCru2;
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
    @SerializedName("aNMoinsUn")
    public List<ANMoinsUn> aNMoinsUn;
}
