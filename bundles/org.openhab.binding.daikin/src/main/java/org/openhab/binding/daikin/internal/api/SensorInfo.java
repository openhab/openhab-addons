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
package org.openhab.binding.daikin.internal.api;

import java.util.Optional;

/**
 * Holds information from the get_sensor_info call.
 *
 * @author Tim Waterhouse - Initial contribution
 *
 */
public class SensorInfo {
    public Optional<Double> indoortemp;
    public Optional<Double> indoorhumidity;
    public Optional<Double> outdoortemp;

    private SensorInfo() {
    }

    public static SensorInfo parse(String response) {
        SensorInfo info = new SensorInfo();
        info.indoortemp = Optional.empty();
        info.indoorhumidity = Optional.empty();
        info.outdoortemp = Optional.empty();

        for (String keyValuePair : response.split(",")) {
            if (keyValuePair.contains("=")) {
                String[] keyValue = keyValuePair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";

                switch (key) {
                    case "htemp":
                        // "-" indicates no value
                        if (!"-".equals(value)) {
                            info.indoortemp = Optional.of(Double.parseDouble(value));
                        }
                        break;
                    case "hhum":
                        // "-" indicates no value
                        if (!"-".equals(value)) {
                            info.indoorhumidity = Optional.of(Double.parseDouble(value));
                        }
                        break;
                    case "otemp":
                        // "-" indicates no value
                        if (!"-".equals(value)) {
                            info.outdoortemp = Optional.of(Double.parseDouble(value));
                        }
                        break;
                }
            }
        }

        return info;
    }
}
