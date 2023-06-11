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
package org.openhab.binding.daikin.internal.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.daikin.internal.api.Enums.AdvancedMode;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;
import org.openhab.binding.daikin.internal.api.Enums.FanSpeed;
import org.openhab.binding.daikin.internal.api.Enums.Mode;
import org.openhab.binding.daikin.internal.api.Enums.SpecialMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley <paul@smedley.id.au> - mods for Daikin Airbase
 *
 */
@NonNullByDefault
public class ControlInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlInfo.class);

    public String ret = "";
    public boolean power = false;
    public Mode mode = Mode.AUTO;
    /** Degrees in Celsius. */
    public Optional<Double> temp = Optional.empty();
    public FanSpeed fanSpeed = FanSpeed.AUTO;
    public FanMovement fanMovement = FanMovement.STOPPED;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public Optional<Integer> targetHumidity = Optional.empty();
    public AdvancedMode advancedMode = AdvancedMode.UNKNOWN;

    private ControlInfo() {
    }

    public static ControlInfo parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        ControlInfo info = new ControlInfo();
        info.ret = Optional.ofNullable(responseMap.get("ret")).orElse("");
        info.power = "1".equals(responseMap.get("pow"));
        info.mode = Optional.ofNullable(responseMap.get("mode")).flatMap(value -> InfoParser.parseInt(value))
                .map(value -> Mode.fromValue(value)).orElse(Mode.AUTO);
        info.temp = Optional.ofNullable(responseMap.get("stemp")).flatMap(value -> InfoParser.parseDouble(value));
        info.fanSpeed = Optional.ofNullable(responseMap.get("f_rate")).map(value -> FanSpeed.fromValue(value))
                .orElse(FanSpeed.AUTO);
        info.fanMovement = Optional.ofNullable(responseMap.get("f_dir")).flatMap(value -> InfoParser.parseInt(value))
                .map(value -> FanMovement.fromValue(value)).orElse(FanMovement.STOPPED);
        info.targetHumidity = Optional.ofNullable(responseMap.get("shum")).flatMap(value -> InfoParser.parseInt(value));

        info.advancedMode = Optional.ofNullable(responseMap.get("adv")).map(value -> AdvancedMode.fromValue(value))
                .orElse(AdvancedMode.UNKNOWN);
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

    public SpecialMode getSpecialMode() {
        return SpecialMode.fromAdvancedMode(advancedMode);
    }
}
