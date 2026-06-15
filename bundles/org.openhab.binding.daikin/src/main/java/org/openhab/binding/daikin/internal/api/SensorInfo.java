/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the get_sensor_info call.
 *
 * @author Tim Waterhouse - Initial contribution
 *
 */
@NonNullByDefault
public class SensorInfo {

    public @Nullable Double indoortemp;
    public @Nullable Double indoorhumidity;
    public @Nullable Double outdoortemp;
    public @Nullable Double compressorfrequency;

    private SensorInfo() {
    }

    public static SensorInfo parse(String response) {
        Logger logger = LoggerFactory.getLogger(SensorInfo.class);
        logger.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        SensorInfo info = new SensorInfo();
        info.indoortemp = Optional.ofNullable(responseMap.get("htemp")).flatMap(InfoParser::parseDouble).orElse(null);
        info.indoorhumidity = Optional.ofNullable(responseMap.get("hhum")).flatMap(InfoParser::parseDouble)
                .orElse(null);
        info.outdoortemp = Optional.ofNullable(responseMap.get("otemp")).flatMap(InfoParser::parseDouble).orElse(null);
        info.compressorfrequency = Optional.ofNullable(responseMap.get("cmpfreq")).flatMap(InfoParser::parseDouble)
                .orElse(null);
        return info;
    }
}
