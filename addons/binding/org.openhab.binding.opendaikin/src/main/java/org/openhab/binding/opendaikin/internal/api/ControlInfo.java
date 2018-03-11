/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal.api;

import java.util.Optional;

import javax.ws.rs.client.WebTarget;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class ControlInfo {
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

            // Default to stopped
            return STOPPED;
        }
    }

    public boolean power;
    public Mode mode;
    /** Degrees in Celsius. */
    public Optional<Double> temp;
    public FanSpeed fanSpeed;
    public FanMovement fanMovement;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public Optional<Integer> targetHumidity;

    private ControlInfo() {
    }

    public static ControlInfo parse(String response) {
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
            webTarget.queryParam("stemp", temp);
        } else {
            webTarget.queryParam("stemp", "20.0");
        }

        if (targetHumidity.isPresent()) {
            webTarget.queryParam("shum", targetHumidity);
        } else {
            // For some reason this can be empty, but nothing else can be
            webTarget.queryParam("shum", "");
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
