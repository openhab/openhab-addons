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
package org.openhab.binding.dwdpollenflug.internal.dto;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DTO for data per region
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDRegionJSON {
    @SerializedName("region_id")
    public @Nullable Integer regionID;

    @SerializedName("region_name")
    public @Nullable String regionName;

    @SerializedName("partregion_id")
    public @Nullable Integer partRegionID;

    @SerializedName("partregion_name")
    public @Nullable String partRegionName;

    @SerializedName("Pollen")
    public @Nullable Map<String, DWDPollentypeJSON> pollen;
}
