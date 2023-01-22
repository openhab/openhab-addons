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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorInfo.class);

    public Optional<Double> indoortemp = Optional.empty();
    public Optional<Double> indoorhumidity = Optional.empty();
    public Optional<Double> outdoortemp = Optional.empty();
    public Optional<Double> compressorfrequency = Optional.empty();

    private SensorInfo() {
    }

    public static SensorInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        SensorInfo info = new SensorInfo();
        info.indoortemp = Optional.ofNullable(responseMap.get("htemp")).flatMap(value -> InfoParser.parseDouble(value));
        info.indoorhumidity = Optional.ofNullable(responseMap.get("hhum"))
                .flatMap(value -> InfoParser.parseDouble(value));
        info.outdoortemp = Optional.ofNullable(responseMap.get("otemp"))
                .flatMap(value -> InfoParser.parseDouble(value));
        info.compressorfrequency = Optional.ofNullable(responseMap.get("cmpfreq"))
                .flatMap(value -> InfoParser.parseDouble(value));

        return info;
    }
}
