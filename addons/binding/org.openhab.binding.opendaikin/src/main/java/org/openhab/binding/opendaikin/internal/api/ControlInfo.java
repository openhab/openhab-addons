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
package org.openhab.binding.opendaikin.internal.api;

import java.util.Optional;

import javax.ws.rs.client.WebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class ControlInfo {
    private static Logger logger = LoggerFactory.getLogger(ControlInfo.class);

    public enum Mode {
        UNKNOWN(-1),
        AUTO(0),
        DEHUMIDIFIER(2),
        COLD(3),
        HEAT(4),
        FAN(6);

        private int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Mode fromValue(int value) {
            for (Mode m : Mode.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }

            logger.debug("Unexpected Mode value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum FanSpeed {
        AUTO("A"),
        SILENCE("B"),
        LEVEL_1("3"),
        LEVEL_2("4"),
        LEVEL_3("5"),
        LEVEL_4("6"),
        LEVEL_5("7");

        private String value;

        FanSpeed(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FanSpeed fromValue(String value) {
            for (FanSpeed m : FanSpeed.values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }

            logger.debug("Unexpected FanSpeed value of \"{}\"", value);

            // Default to auto
            return AUTO;
        }
    }

    public enum FanMovement {
        UNKNOWN(-1),
        STOPPED(0),
        VERTICAL(1),
        HORIZONTAL(2),
        VERTICAL_AND_HORIZONTAL(3);

        private int value;

        FanMovement(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FanMovement fromValue(int value) {
            for (FanMovement m : FanMovement.values()) {
                if (m.getValue() == value) {
                    return m;
                }
            }

            logger.debug("Unexpected FanMovement value of \"{}\"", value);

            // Default to stopped
            return STOPPED;
        }
    }

    public boolean power = false;
    public Mode mode = Mode.AUTO;
    /** Degrees in Celsius. */
    public Optional<Double> temp = Optional.empty();
    public FanSpeed fanSpeed = FanSpeed.AUTO;
    public FanMovement fanMovement = FanMovement.STOPPED;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public Optional<Integer> targetHumidity = Optional.empty();

    private ControlInfo() {
    }

    public static ControlInfo parse(String response) {
        logger.debug("Parsing string: \"{}\"", response);
        ControlInfo info = new ControlInfo();

        for (String keyValuePair : response.split(",")) {
            if (keyValuePair.contains("=")) {
                String[] keyValue = keyValuePair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";

                switch (key) {
                    case "pow":
                        info.power = "1".equals(value);
                        break;
                    case "mode":
                        parseInt(value).ifPresent((modeValue) -> info.mode = Mode.fromValue(modeValue));
                        break;
                    case "stemp":
                        info.temp = parseDouble(value);
                        break;
                    case "f_rate":
                        info.fanSpeed = FanSpeed.fromValue(value);
                        break;
                    case "f_dir":
                        parseInt(value).ifPresent((fanValue) -> info.fanMovement = FanMovement.fromValue(fanValue));
                        break;
                    case "shum":
                        info.targetHumidity = parseInt(value);
                        break;
                }
            }
        }

        return info;
    }

    public WebTarget getParamString(WebTarget target) {
        WebTarget webTarget = target.queryParam("pow", power ? 1 : 0).queryParam("mode", mode.getValue())
                .queryParam("f_rate", fanSpeed.getValue()).queryParam("f_dir", fanMovement.getValue());

        if (temp.isPresent()) {
            webTarget = webTarget.queryParam("stemp", temp.get());
        } else {
            webTarget = webTarget.queryParam("stemp", "20.0");
        }

        if (targetHumidity.isPresent()) {
            webTarget = webTarget.queryParam("shum", targetHumidity.get());
        } else {
            // For some reason this can be empty, but nothing else can be
            webTarget = webTarget.queryParam("shum", "");
        }

        return webTarget;
    }

    private static Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (Exception e) {
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
