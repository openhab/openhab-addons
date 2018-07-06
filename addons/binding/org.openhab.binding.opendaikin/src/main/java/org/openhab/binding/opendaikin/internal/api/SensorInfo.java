/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal.api;

import java.util.Optional;

import org.openhab.binding.opendaikin.handler.OpenDaikinAcUnitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the get_sensor_info call.
 *
 * @author Tim Waterhouse - Initial contribution
 *
 */
public class SensorInfo {
    private static Logger logger = LoggerFactory.getLogger(OpenDaikinAcUnitHandler.class);

    public Optional<Double> indoortemp;
    public Optional<Double> indoorhumidity;
    public Optional<Double> outdoortemp;

    private SensorInfo() {
    }

    public static SensorInfo parse(String response) {
        logger.debug("Parsing {}", response);
        SensorInfo info = new SensorInfo();

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
                        } else {
                            info.indoortemp = Optional.empty();
                        }
                        break;
                    case "hhum":
                        // "-" indicates no value
                        if (!"-".equals(value)) {
                            info.indoorhumidity = Optional.of(Double.parseDouble(value));
                        } else {
                            info.indoorhumidity = Optional.empty();
                        }
                        break;
                    case "otemp":
                        // "-" indicates no value
                        if (!"-".equals(value)) {
                            info.outdoortemp = Optional.of(Double.parseDouble(value));
                        } else {
                            info.outdoortemp = Optional.empty();
                        }
                        break;
                }
            }
        }

        return info;
    }
}
