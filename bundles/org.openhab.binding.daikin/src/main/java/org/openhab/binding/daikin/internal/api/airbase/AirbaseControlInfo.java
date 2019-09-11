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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanMovement;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseFanSpeed;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseEnums.AirbaseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley <paul@smedley.id.au> - Mods for Daikin Airbase Units
 *
 */
public class AirbaseControlInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirbaseControlInfo.class);

    public String ret;
    public boolean power = false;
    public AirbaseMode mode = AirbaseMode.AUTO;
    /** Degrees in Celsius. */
    public Optional<Double> temp = Optional.empty();
    public AirbaseFanSpeed fanSpeed = AirbaseFanSpeed.AUTO;
    public AirbaseFanMovement fanMovement = AirbaseFanMovement.STOPPED;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public Optional<Integer> targetHumidity = Optional.empty();

    private AirbaseControlInfo() {
    }

    public static AirbaseControlInfo parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = Arrays.asList(response.split(",")).stream().filter(kv -> kv.contains("="))
                .map(kv -> {
                    String[] keyValue = kv.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    return new String[] { key, value };
                }).collect(Collectors.toMap(x -> x[0], x -> x[1]));

        AirbaseControlInfo info = new AirbaseControlInfo();
        info.ret = responseMap.get("ret");
        info.power = "1".equals(responseMap.get("pow"));
        info.mode = Optional.ofNullable(responseMap.get("mode")).flatMap(value -> parseInt(value))
                .map(value -> AirbaseMode.fromValue(value)).orElse(AirbaseMode.AUTO);
        info.temp = Optional.ofNullable(responseMap.get("stemp")).flatMap(value -> parseDouble(value));
        info.fanSpeed = Optional.ofNullable(responseMap.get("f_rate")).map(value -> AirbaseFanSpeed.fromValue(value))
                .orElse(AirbaseFanSpeed.AUTO);
        info.fanMovement = Optional.ofNullable(responseMap.get("f_dir")).flatMap(value -> parseInt(value))
                .map(value -> AirbaseFanMovement.fromValue(value)).orElse(AirbaseFanMovement.STOPPED);
        info.targetHumidity = Optional.ofNullable(responseMap.get("shum")).flatMap(value -> parseInt(value));
        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new HashMap<>();
        params.put("pow", power ? "1" : "0");
        params.put("mode", Integer.toString(mode.getValue()));
        params.put("f_rate", fanSpeed.getValue());
        params.put("f_dir", Integer.toString(fanMovement.getValue()));
        params.put("stemp", temp.orElse(20.0).toString());
        params.put("shum", targetHumidity.map(value -> value.toString()).orElse(""));

        return params;
    }

    private static Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
