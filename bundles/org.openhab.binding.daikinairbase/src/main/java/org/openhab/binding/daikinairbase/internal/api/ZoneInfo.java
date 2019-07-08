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
package org.openhab.binding.daikinairbase.internal.api;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.StringTokenizer; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the basic_info call.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
public class ZoneInfo {
    private static Logger LOGGER = LoggerFactory.getLogger(ZoneInfo.class);

    public String zonenames;
    public boolean zone1 = false;
    public boolean zone2 = false;
    public boolean zone3 = false;
    public boolean zone4 = false;
    public boolean zone5 = false;
    public boolean zone6 = false;
    public boolean zone7 = false;
    public boolean zone8 = false;

    private ZoneInfo() {
    }

    public static ZoneInfo parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = Arrays.asList(response.split(",")).stream().filter(kv -> kv.contains("="))
                .map(kv -> {
                    String[] keyValue = kv.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    return new String[] { key, value };
                }).collect(Collectors.toMap(x -> x[0], x -> x[1]));

        ZoneInfo info = new ZoneInfo();
        info.zonenames = responseMap.get("zone_name");
        String zoneinfo = responseMap.get("zone_onoff");
        StringTokenizer zones = new StringTokenizer(zoneinfo, "%3b");
        info.zone1="1".equals(zones.nextToken());
        info.zone2="1".equals(zones.nextToken());
        info.zone3="1".equals(zones.nextToken());
        info.zone4="1".equals(zones.nextToken());
        info.zone5="1".equals(zones.nextToken());
        info.zone6="1".equals(zones.nextToken());
        info.zone7="1".equals(zones.nextToken());
        info.zone8="1".equals(zones.nextToken());

        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new LinkedHashMap<>();
        StringBuilder onoffstring = new StringBuilder()
            .append(zone1 ? "1" : "0")
            .append("%3b")
            .append(zone2 ? "1" : "0")
            .append("%3b")
            .append(zone3 ? "1" : "0")
            .append("%3b")
            .append(zone4 ? "1" : "0")
            .append("%3b")
            .append(zone5 ? "1" : "0")
            .append("%3b")
            .append(zone6 ? "1" : "0")
            .append("%3b")
            .append(zone7 ? "1" : "0")
            .append("%3b")
            .append(zone8 ? "1" : "0");

        params.put("zone_name", zonenames);
        params.put("zone_onoff", onoffstring.toString());

        return params;
    }

}
