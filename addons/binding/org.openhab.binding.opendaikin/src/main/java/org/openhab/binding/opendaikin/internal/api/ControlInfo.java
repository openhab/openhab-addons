/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal.api;

import javax.ws.rs.client.WebTarget;

import org.openhab.binding.opendaikin.handler.OpenDaikinAcUnitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used by set and get control info.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class ControlInfo {
    public enum Mode {
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
                if (m.getValue() == value) {
                    return m;
                }
            }

            // Default to auto
            return AUTO;
        }
    }

    public enum FanMovement {
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

    private static Logger logger = LoggerFactory.getLogger(OpenDaikinAcUnitHandler.class);

    public boolean power;
    public Mode mode;
    /** Degrees in Celsius. */
    public double temp;
    public FanSpeed fanSpeed;
    public FanMovement fanMovement;
    /* Not supported by all units. Sets the target humidity for dehumidifying. */
    public int targetHumidity;

    private ControlInfo() {
    }

    public static ControlInfo parse(String response) {
        logger.debug("Parsing {}", response);
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
                        info.mode = Mode.fromValue(Integer.parseInt(value));
                        break;
                    case "stemp":
                        info.temp = Double.parseDouble(value);
                        break;
                    case "f_rate":
                        info.fanSpeed = FanSpeed.fromValue(value);
                        break;
                    case "f_dir":
                        info.fanMovement = FanMovement.fromValue(Integer.parseInt(value));
                        break;
                    case "shum":
                        info.targetHumidity = Integer.parseInt(value);
                }
            }
        }

        return info;
    }

    public WebTarget getParamString(WebTarget target) {
        return target.queryParam("pow", power ? 1 : 0).queryParam("mode", mode.getValue()).queryParam("stemp", temp)
                .queryParam("f_rate", fanSpeed.getValue()).queryParam("f_dir", fanMovement.getValue())
                .queryParam("shum", targetHumidity);
    }
}
