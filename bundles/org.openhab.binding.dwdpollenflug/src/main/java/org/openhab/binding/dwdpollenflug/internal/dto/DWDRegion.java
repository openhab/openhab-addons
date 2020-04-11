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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link DWDRegion} class holds the internal data representation of each Region
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDRegion {

    private int regionID;

    private final Map<String, String> properties;

    private final Map<String, StringType> channels = new HashMap<>();

    public DWDRegion(final DWDRegionJSON json) {
        if (json.regionID == null) {
            regionID = 0;
        } else {
            regionID = json.regionID;
        }

        Integer partRegionID = json.partRegionID;
        if (partRegionID > 0) {
            regionID = partRegionID;
        }

        properties = initProperties(json);

        parseChannels(json.pollen);
    }

    private Map<String, String> initProperties(DWDRegionJSON json) {
        Map<String, String> map = new HashMap<>();
        String regionName = json.regionName;
        if (regionName != null) {
            map.put(PROPERTY_REGION_NAME, regionName);
        }

        String partRegionName = json.partRegionName;
        if (partRegionName != null) {
            map.put(PROPERTY_PARTREGION_NAME, partRegionName);
        }

        return Collections.unmodifiableMap(map);
    }

    private void parseChannels(@Nullable final Map<String, DWDPollentypeJSON> pollen) {
        if (pollen == null) {
            return;
        }

        pollen.forEach((k, jsonType) -> {
            final String pollenType = DWDPollenflugPollen.valueOf(k.toUpperCase()).getChannelName();
            createChannel(pollenType, CHANNEL_TODAY, jsonType.today);
            createChannel(pollenType, CHANNEL_TOMORROW, jsonType.tomorrow);
            createChannel(pollenType, CHANNEL_DAYAFTER_TO, jsonType.dayafterTomorrow);
        });
    }

    private void createChannel(final String pollenType, final String subchannel, @Nullable String value) {
        final String channelName = pollenType + "#" + subchannel;
        if (value == null) {
            value = "-1";
        }
        channels.put(channelName, new StringType(value));
    }

    public int getRegionID() {
        return regionID;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, State> getChannels() {
        return Collections.unmodifiableMap(channels);
    }
}
