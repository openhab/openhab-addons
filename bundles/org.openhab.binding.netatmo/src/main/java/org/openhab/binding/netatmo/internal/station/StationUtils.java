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
package org.openhab.binding.netatmo.internal.station;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;

/**
 * This class holds various methods common to stations
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class StationUtils {

    public static void getMeasurements(NetatmoBridgeHandler handler, String device, @Nullable String module,
            String scale, List<String> types, List<String> channels, Map<String, Float> channelMeasurements) {
        if (types.size() != channels.size()) {
            throw new UnsupportedOperationException("types and channels lists are different sizes.");
        }

        List<Float> measurements = handler.getStationMeasureResponses(device, module, scale, types);
        if (measurements.size() != types.size()) {
            throw new UnsupportedOperationException("types and measurements lists are different sizes.");
        }

        int i = 0;
        for (Float measurement : measurements) {
            channelMeasurements.put(channels.get(i++), measurement);
        }
    }

    public static void addMeasurement(Set<String> channelIds, List<String> channels, List<String> types, String channel,
            String type) {
        if (channelIds.contains(channel)) {
            channels.add(channel);
            types.add(type);
        }
    }
}
