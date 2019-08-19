/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the basic_info call.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
public class AirbaseZoneInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseZoneInfo.class);

    public String zonenames;
    public boolean zone[] = new boolean[9];

    private AirbaseZoneInfo() {
    }

    public static AirbaseZoneInfo parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = Arrays.asList(response.split(",")).stream().filter(kv -> kv.contains("="))
                .map(kv -> {
                    String[] keyValue = kv.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    return new String[] { key, value };
                }).collect(Collectors.toMap(x -> x[0], x -> x[1]));

        AirbaseZoneInfo info = new AirbaseZoneInfo();
        info.zonenames = responseMap.get("zone_name");
        String zoneinfo = responseMap.get("zone_onoff");

        String[] Zones = zoneinfo.split("%3b");

        for (int i = 1; i < 9; i++)
            info.zone[i] = "1".equals(Zones[i-1]);
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new LinkedHashMap<>();
        String onoffstring = IntStream.range(0, zone.length).mapToObj(idx -> zone[idx] ? "1" : "0").collect(Collectors.joining("%3b"));
        params.put("zone_name", zonenames);
        params.put("zone_onoff", onoffstring);

        return params;
    }

}
