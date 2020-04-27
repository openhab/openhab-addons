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
package org.openhab.binding.daikin.internal.api.airbase;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.daikin.internal.api.InfoParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the basic_info call.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
public class AirbaseBasicInfo {
    private static final Logger logger = LoggerFactory.getLogger(AirbaseBasicInfo.class);

    public String mac;
    public String ret;
    public String ssid;

    private AirbaseBasicInfo() {
    }

    public static AirbaseBasicInfo parse(String response) {
        logger.debug("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        AirbaseBasicInfo info = new AirbaseBasicInfo();
        info.mac = responseMap.get("mac");
        info.ret = responseMap.get("ret");
        info.ssid = responseMap.get("ssid");
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new HashMap<>();
        params.put("ssid", ssid);
        return params;
    }
}
