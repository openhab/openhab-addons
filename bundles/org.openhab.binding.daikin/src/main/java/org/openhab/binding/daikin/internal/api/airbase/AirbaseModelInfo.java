/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.daikin.internal.api.InfoParser;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by get model info.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class AirbaseModelInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseModelInfo.class);

    public String ret = "";
    public int zonespresent;
    public int commonzone;
    public int frate_steps; // fan rate steps
    public EnumSet<AirbaseFeature> features;

    private AirbaseModelInfo() {
        features = EnumSet.noneOf(AirbaseFeature.class);
    }

    public static AirbaseModelInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        AirbaseModelInfo info = new AirbaseModelInfo();
        info.ret = Optional.ofNullable(responseMap.get("ret")).orElse("");
        info.zonespresent = Optional.ofNullable(responseMap.get("en_zone")).flatMap(value -> InfoParser.parseInt(value))
                .orElse(0);
        info.commonzone = Optional.ofNullable(responseMap.get("en_common_zone"))
                .flatMap(value -> InfoParser.parseInt(value)).orElse(0);
        info.frate_steps = Optional.ofNullable(responseMap.get("frate_steps"))
                .flatMap(value -> InfoParser.parseInt(value)).orElse(1);
        for (AirbaseFeature f : AirbaseFeature.values()) {
            if ("1".equals(responseMap.get(f.getValue()))) {
                info.features.add(f);
            }
        }
        return info;
    }
}
