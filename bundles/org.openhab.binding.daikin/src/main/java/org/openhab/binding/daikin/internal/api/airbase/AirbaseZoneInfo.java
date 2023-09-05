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
package org.openhab.binding.daikin.internal.api.airbase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.daikin.internal.api.InfoParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the basic_info call.
 *
 * @author Paul Smedley - Initial contribution
 * @author Jimmy Tanagra - Refactor zone array to 0-based
 *
 */
@NonNullByDefault
public class AirbaseZoneInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseZoneInfo.class);

    public String zonenames = "";
    public boolean[] zone = new boolean[8];

    private AirbaseZoneInfo() {
    }

    public static AirbaseZoneInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        AirbaseZoneInfo info = new AirbaseZoneInfo();
        info.zonenames = Optional.ofNullable(responseMap.get("zone_name")).orElse("");
        String zoneinfo = Optional.ofNullable(responseMap.get("zone_onoff")).orElse("");

        String[] zones = zoneinfo.split(";");

        int count = Math.min(info.zone.length, zones.length);
        for (int i = 0; i < count; i++) {
            info.zone[i] = "1".equals(zones[i]);
        }
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new LinkedHashMap<>();
        String onoffstring = IntStream.range(0, zone.length).mapToObj(idx -> zone[idx] ? "1" : "0")
                .collect(Collectors.joining(";"));
        params.put("zone_name", zonenames);
        params.put("zone_onoff", onoffstring);

        return params;
    }
}
