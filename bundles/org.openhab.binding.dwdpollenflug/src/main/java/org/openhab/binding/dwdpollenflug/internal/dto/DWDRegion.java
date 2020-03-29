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

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Class to hold Response of Region
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
public class DWDRegion {
    @SerializedName("region_id")
    private @Nullable Integer regionID;

    @SerializedName("region_name")
    private @Nullable String regionName;

    @SerializedName("partregion_id")
    private @Nullable Integer partregionID;

    @SerializedName("partregion_name")
    private @Nullable String partregionName;

    @SerializedName("Pollen")
    private @Nullable Map<String, DWDPollen> pollen;

    @Expose(serialize = false, deserialize = false)
    private Map<String, String> properties;

    public int getId() {
        if (isPartRegion()) {
            return partregionID;
        } else {
            return regionID;
        }
    }

    public void init() {
        initProperties();
    }

    private void initProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(PROPERTY_REGION_ID, Integer.toString(regionID));
        map.put(PROPERTY_PARTREGION_NAME, regionName);

        if (isPartRegion()) {
            map.put(PROPERTY_PARTREGION_ID, Integer.toString(partregionID));
            map.put(PROPERTY_PARTREGION_NAME, partregionName);
        }
    }

    public boolean isPartRegion() {
        return partregionID > 0;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, DWDPollen> getPollen() {
        return pollen;
    }
}
