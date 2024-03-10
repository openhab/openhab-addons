/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.heliosventilation.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeliosVentilationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationBindingConstants {

    public static final String BINDING_ID = "heliosventilation";

    public static final String DATAPOINT_FILE = "datapoints.properties";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HELIOS_VENTILATION = new ThingTypeUID(BINDING_ID, "ventilation");

    public static final Map<Byte, HeliosVentilationDataPoint> DATAPOINTS;

    private static final Logger LOGGER;
    static {
        /* logger is used by readChannelProperties() so we need to initialize logger first. */
        LOGGER = LoggerFactory.getLogger(HeliosVentilationBindingConstants.class);
        DATAPOINTS = readChannelProperties();
    }
    // List of all Channel ids
    // Channel ids are only in datapoints.properties and thing-types.xml

    /**
     * parse datapoints from properties
     *
     */
    private static Map<Byte, HeliosVentilationDataPoint> readChannelProperties() {
        HashMap<Byte, HeliosVentilationDataPoint> result = new HashMap<>();

        URL resource = Thread.currentThread().getContextClassLoader().getResource(DATAPOINT_FILE);
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());

            Enumeration<Object> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String channel = (String) keys.nextElement();
                HeliosVentilationDataPoint dp;
                try {
                    dp = new HeliosVentilationDataPoint(channel, properties.getProperty(channel));
                    if (result.containsKey(dp.address())) {
                        result.get(dp.address()).append(dp);
                    } else {
                        result.put(dp.address(), dp);
                    }
                } catch (HeliosPropertiesFormatException e) {
                    LOGGER.warn("could not read resource file {}, binding will probably fail: {}", DATAPOINT_FILE,
                            e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("could not read resource file {}, binding will probably fail: {}", DATAPOINT_FILE,
                    e.getMessage());
        }

        return result;
    }
}
