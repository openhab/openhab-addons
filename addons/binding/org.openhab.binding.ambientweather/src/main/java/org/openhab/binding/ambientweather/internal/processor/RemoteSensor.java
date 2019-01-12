/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
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
    private final Logger logger = LoggerFactory.getLogger(RemoteSensor.class);

    // Maximum number of remote sensors that can be supported by a station
    private static final int MAX_SENSORS = 10;

    private int numberOfSensors;

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
        String sensorNumber = String.valueOf(i);
        StringReader stringReader = new StringReader(jsonData);
        JsonReader reader = new JsonReader(stringReader);
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (("temp" + sensorNumber + "f").equals(name)) {
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_TEMPERATURE,
                            new QuantityType<>(reader.nextDouble(), ImperialUnits.FAHRENHEIT));
                } else if (("humidity" + sensorNumber).equals(name)) {
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_HUMIDITY,
                            new QuantityType<>(reader.nextDouble(), SmartHomeUnits.PERCENT));
                } else if (("soiltemp" + sensorNumber).equals(name)) {
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_SOIL_TEMPERATURE,
                            new QuantityType<>(reader.nextDouble(), ImperialUnits.FAHRENHEIT));
                } else if (("soilhum" + sensorNumber).equals(name)) {
                    double soilMoisture = reader.nextDouble();
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_SOIL_MOISTURE,
                            new QuantityType<>(soilMoisture, SmartHomeUnits.PERCENT));
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_SOIL_MOISTURE_LEVEL,
                            new StringType(convertSoilMoistureToString(soilMoisture)));
                } else if (("batt" + sensorNumber).equals(name)) {
                    handler.updateChannel(CHGRP_REMOTE_SENSOR + sensorNumber + "#" + CH_BATTERY_INDICATOR,
                            new StringType(reader.nextString()));
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

    /*
     * Convert the soil moisture to a string representation
     */
    private String convertSoilMoistureToString(double soilMoisture) {
        String result = "UNKNOWN";
        if (soilMoisture >= 0 && soilMoisture < 33.0) {
            result = "VERY DRY";
        } else if (soilMoisture >= 33.0 && soilMoisture < 60.0) {
            result = "DRY";
        } else if (soilMoisture >= 60.0 && soilMoisture < 80.0) {
            result = "MOIST";
        } else if (soilMoisture >= 80.0 && soilMoisture < 93.0) {
            result = "WET";
        } else if (soilMoisture >= 93.0 && soilMoisture <= 100.0) {
            result = "VERY WET";
        }
        return result;
    }
}
