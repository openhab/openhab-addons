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
package org.openhab.binding.dwdpollenflug.internal.dto;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DWDRegion} class holds the internal data representation of each Region
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDRegion {
    @SerializedName("region_id")
    public int regionID = 0;

    @SerializedName("region_name")
    public String regionName = "";

    @SerializedName("partregion_id")
    public int partRegionID = 0;

    @SerializedName("partregion_name")
    public String partRegionName = "";

    @SerializedName("Pollen")
    private @Nullable Map<String, DWDPollentypeJSON> pollen;

    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        map.put(PROPERTY_REGION_NAME, regionName);
        map.put(PROPERTY_PARTREGION_NAME, partRegionName);
        return Collections.unmodifiableMap(map);
    }

    public int getRegionID() {
        if (partRegionID > 0) {
            return partRegionID;
        }
        return regionID;
    }

    public Map<String, State> getChannelsStateMap() {
        final Map<String, DWDPollentypeJSON> localPollen = pollen;
        if (localPollen != null) {
            Map<String, State> map = new HashMap<>();
            localPollen.forEach((k, jsonType) -> {
                final String pollenType = DWDPollenflugPollen.valueOf(k.toUpperCase()).getChannelName();
                map.put(pollenType + "#" + CHANNEL_TODAY, new StringType(jsonType.today));
                map.put(pollenType + "#" + CHANNEL_TOMORROW, new StringType(jsonType.tomorrow));
                map.put(pollenType + "#" + CHANNEL_DAYAFTER_TO, new StringType(jsonType.dayAfterTomorrow));
            });

            return Collections.unmodifiableMap(map);
        }

        return Collections.emptyMap();
    }
}
