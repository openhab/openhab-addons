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
package org.openhab.binding.daikin.internal.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the basic_info call.
 *
 * @author Jimmy Tanagra - Initial contribution
 *
 */
@NonNullByDefault
public class BasicInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInfo.class);

    public String mac = "";
    public String ret = "";
    public String ssid = "";

    private BasicInfo() {
    }

    public static BasicInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        BasicInfo info = new BasicInfo();
        info.mac = Optional.ofNullable(responseMap.get("mac")).orElse("");
        info.ret = Optional.ofNullable(responseMap.get("ret")).orElse("");
        info.ssid = Optional.ofNullable(responseMap.get("ssid")).orElse("");
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new HashMap<>();
        params.put("ssid", ssid);
        return params;
    }
}
