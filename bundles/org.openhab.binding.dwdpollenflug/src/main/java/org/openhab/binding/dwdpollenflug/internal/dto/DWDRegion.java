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
        regionID = json.regionID;

        if (json.partRegionID > 0) {
            regionID = json.partRegionID;
        }

        properties = initProperties(json);

        json.getPollen().forEach((k, jsonType) -> {
            final String pollenType = DWDPollenflugPollen.valueOf(k.toUpperCase()).getChannelName();
            channels.put(pollenType + "#" + CHANNEL_TODAY, new StringType(jsonType.today));
            channels.put(pollenType + "#" + CHANNEL_TOMORROW, new StringType(jsonType.tomorrow));
            channels.put(pollenType + "#" + CHANNEL_DAYAFTER_TO, new StringType(jsonType.dayAfterTomorrow));
        });
    }

    private Map<String, String> initProperties(DWDRegionJSON json) {
        Map<String, String> map = new HashMap<>();
        map.put(PROPERTY_REGION_NAME, json.regionName);
        map.put(PROPERTY_PARTREGION_NAME, json.partRegionName);
        return Collections.unmodifiableMap(map);
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
