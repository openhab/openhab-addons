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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFeature;

/**
 * Class for holding the set of parameters used by get model info.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class AirbaseModelInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseModelInfo.class);

    public String ret;
    public Integer zonespresent;
    public Integer commonzone;
    public Integer frate_steps; // fan rate steps
    public EnumSet<AirbaseFeature> features;

    private AirbaseModelInfo() {
        features = EnumSet.noneOf(AirbaseFeature.class);
    }

    public static AirbaseModelInfo parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = Arrays.asList(response.split(",")).stream().filter(kv -> kv.contains("="))
                .map(kv -> {
                    String[] keyValue = kv.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    return new String[] { key, value };
                }).collect(Collectors.toMap(x -> x[0], x -> x[1]));

        AirbaseModelInfo info = new AirbaseModelInfo();
        info.ret = responseMap.get("ret");
        info.zonespresent = Integer.parseInt(responseMap.get("en_zone"));
        info.commonzone = Integer.parseInt(responseMap.get("en_common_zone"));  
        info.frate_steps = Integer.parseInt(responseMap.get("frate_steps"));      
        for (AirbaseFeature f: AirbaseFeature.values()) {
            if ("1".equals(responseMap.get(f.getValue()))) {
                info.features.add(f);
            }
        }
        return info;
    }

}
