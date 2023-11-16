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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.daikin.internal.api.InfoParser;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanMovement;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanSpeed;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Mods for Daikin Airbase Units
 *
 */
@NonNullByDefault
public class AirbaseControlInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseControlInfo.class);

    public String ret = "";
    public boolean power = false;
    public AirbaseMode mode = AirbaseMode.AUTO;
    /** Degrees in Celsius. */
    public Optional<Double> temp = Optional.empty();
    public AirbaseFanSpeed fanSpeed = AirbaseFanSpeed.LEVEL_1;
    public AirbaseFanMovement fanMovement = AirbaseFanMovement.STOPPED;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public Optional<Integer> targetHumidity = Optional.empty();

    private AirbaseControlInfo() {
    }

    public static AirbaseControlInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        AirbaseControlInfo info = new AirbaseControlInfo();
        info.ret = Optional.ofNullable(responseMap.get("ret")).orElse("");
        info.power = "1".equals(responseMap.get("pow"));
        info.mode = Optional.ofNullable(responseMap.get("mode")).flatMap(value -> InfoParser.parseInt(value))
                .map(value -> AirbaseMode.fromValue(value)).orElse(AirbaseMode.AUTO);
        info.temp = Optional.ofNullable(responseMap.get("stemp")).flatMap(value -> InfoParser.parseDouble(value));
        int fRate = Optional.ofNullable(responseMap.get("f_rate")).flatMap(value -> InfoParser.parseInt(value))
                .orElse(1);
        boolean fAuto = "1".equals(responseMap.getOrDefault("f_auto", "0"));
        boolean fAirside = "1".equals(responseMap.getOrDefault("f_airside", "0"));
        info.fanSpeed = AirbaseFanSpeed.fromValue(fRate, fAuto, fAirside);
        info.fanMovement = Optional.ofNullable(responseMap.get("f_dir")).flatMap(value -> InfoParser.parseInt(value))
                .map(value -> AirbaseFanMovement.fromValue(value)).orElse(AirbaseFanMovement.STOPPED);
        info.targetHumidity = Optional.ofNullable(responseMap.get("shum")).flatMap(value -> InfoParser.parseInt(value));
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new HashMap<>();
        params.put("pow", power ? "1" : "0");
        params.put("mode", Integer.toString(mode.getValue()));
        params.put("f_rate", Integer.toString(fanSpeed.getLevel()));
        params.put("f_auto", fanSpeed.getAuto() ? "1" : "0");
        params.put("f_airside", fanSpeed.getAirside() ? "1" : "0");
        params.put("f_dir", Integer.toString(fanMovement.getValue()));
        params.put("stemp", temp.orElse(20.0).toString());
        params.put("shum", targetHumidity.map(value -> value.toString()).orElse(""));

        return params;
    }
}
