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
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * The {@link RemoteSensor} is responsible for updating the remote sensor
 * channels for the remote sensors attached to a weather station.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class RemoteSensor {
    // Maximum number of remote sensors that can be supported by a station
    private static final int MAX_SENSORS = 10;

    private final Logger logger = LoggerFactory.getLogger(RemoteSensor.class);

    private final TreeMap<Double, String> soilMoistureMap = new TreeMap<>();

    private int numberOfSensors;

    public RemoteSensor() {
        initializeSoilMoistureMap();
    }

    public void setNumberOfSensors(int numberOfSensors) {
        if (numberOfSensors < 1 || numberOfSensors > MAX_SENSORS) {
            throw new IllegalArgumentException("Invalid number of sensors");
        }
        this.numberOfSensors = numberOfSensors;
    }

    public void updateChannels(AmbientWeatherStationHandler handler, final @Nullable String jsonData) {
        if (jsonData == null) {
            throw new IllegalArgumentException("Json data is null");
        }
        if (numberOfSensors < 1 || numberOfSensors > MAX_SENSORS) {
            throw new IllegalStateException("Number of sensors has not been set");
        }
        for (int sensorNumber = 1; sensorNumber <= numberOfSensors; sensorNumber++) {
            updateSensorChannels(handler, sensorNumber, jsonData);
        }
    }

    /*
     * Iterate through the JSON object and update the channels for which
     * there are remote sensor values
     */
    private void updateSensorChannels(AmbientWeatherStationHandler handler, int i, final @Nullable String jsonData) {
        if (jsonData == null) {
            return;
        }
        String sensorNumber = String.valueOf(i);
        StringReader stringReader = new StringReader(jsonData);
        JsonReader reader = new JsonReader(stringReader);
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (("temp" + sensorNumber + "f").equals(name)) {
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_TEMPERATURE, reader.nextDouble(),
                            ImperialUnits.FAHRENHEIT);
                } else if (("dewPoint" + sensorNumber).equals(name)) {
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_DEW_POINT, reader.nextDouble(),
                            ImperialUnits.FAHRENHEIT);
                } else if (("feelsLike" + sensorNumber).equals(name)) {
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_FEELING_TEMPERATURE,
                            reader.nextDouble(), ImperialUnits.FAHRENHEIT);
                } else if (("humidity" + sensorNumber).equals(name)) {
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_HUMIDITY, reader.nextDouble(),
                            Units.PERCENT);
                } else if (("soiltemp" + sensorNumber).equals(name)) {
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_SOIL_TEMPERATURE, reader.nextDouble(),
                            ImperialUnits.FAHRENHEIT);
                } else if (("soilhum" + sensorNumber).equals(name)) {
                    Double soilMoisture = reader.nextDouble();
                    handler.updateQuantity(CHGRP_REMOTE_SENSOR + sensorNumber, CH_SOIL_MOISTURE, soilMoisture,
                            Units.PERCENT);
                    handler.updateString(CHGRP_REMOTE_SENSOR + sensorNumber, CH_SOIL_MOISTURE_LEVEL,
                            convertSoilMoistureToString(soilMoisture));
                } else if (("batt" + sensorNumber).equals(name)) {
                    handler.updateString(CHGRP_REMOTE_SENSOR + sensorNumber, CH_BATTERY_INDICATOR, reader.nextString());
                } else {
                    reader.skipValue();
                }
            }
        } catch (IOException e) {
            logger.debug("IOException from JsonReader: {}", e.getMessage(), e);
        } finally {
            try {
                reader.close();
                stringReader.close();
            } catch (IOException e) {
                // Eat the exception
            }
        }
    }

    private void initializeSoilMoistureMap() {
        soilMoistureMap.put(33.0, "VERY DRY");
        soilMoistureMap.put(60.0, "DRY");
        soilMoistureMap.put(80.0, "MOIST");
        soilMoistureMap.put(93.0, "WET");
        soilMoistureMap.put(100.0, "VERY WET");
    }

    /*
     * Convert the soil moisture to a string representation
     */
    private String convertSoilMoistureToString(double soilMoisture) {
        Double key = soilMoistureMap.ceilingKey(soilMoisture);
        return key == null ? "UNKNOWN" : soilMoistureMap.getOrDefault(key, "UNKNOWN");
    }
}
