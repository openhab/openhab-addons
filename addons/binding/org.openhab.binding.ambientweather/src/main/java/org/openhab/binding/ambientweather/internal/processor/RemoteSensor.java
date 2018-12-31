/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.processor;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * The {@link RemoteSensor} is responsible for updating
 * the channels associated with the WS-1400-IP series weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class RemoteSensor {
    private final Logger logger = LoggerFactory.getLogger(RemoteSensor.class);

    private double temperature;

    private double humidity;

    @Nullable
    private String battery;

    @Nullable
    private String jsonData;

    public void setData(final String jsonData) {
        this.jsonData = jsonData;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    @Nullable
    public String getBattery() {
        return battery;
    }

    public boolean remoteSensor1Exists() {
        return remoteSensorExists("temp1f", "humidity1", "batt1");
    }

    public boolean remoteSensor2Exists() {
        return remoteSensorExists("temp2f", "humidity2", "batt2");
    }

    public boolean remoteSensor3Exists() {
        return remoteSensorExists("temp3f", "humidity3", "batt3");
    }

    public boolean remoteSensor4Exists() {
        return remoteSensorExists("temp4f", "humidity4", "batt4");
    }

    public boolean remoteSensor5Exists() {
        return remoteSensorExists("temp5f", "humidity5", "batt5");
    }

    public boolean remoteSensor6Exists() {
        return remoteSensorExists("temp6f", "humidity6", "batt6");
    }

    public boolean remoteSensor7Exists() {
        return remoteSensorExists("temp7f", "humidity7", "batt7");
    }

    public boolean remoteSensor8Exists() {
        return remoteSensorExists("temp8f", "humidity8", "batt8");
    }

    public boolean remoteSensor9Exists() {
        return remoteSensorExists("temp9f", "humidity9", "batt9");
    }

    public boolean remoteSensor10Exists() {
        return remoteSensorExists("temp10f", "humidity10", "batt10");
    }

    private boolean remoteSensorExists(String temperatureName, String humidityName, String batteryName) {
        if (jsonData == null) {
            throw new IllegalArgumentException("Json data not set");
        }
        StringReader stringReader = new StringReader(jsonData);
        JsonReader reader = new JsonReader(stringReader);
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals(temperatureName)) {
                    temperature = reader.nextDouble();
                } else if (name.equals(humidityName)) {
                    humidity = reader.nextDouble();
                } else if (name.equals(batteryName)) {
                    battery = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return true;
        } catch (IOException e) {
            logger.debug("IOException from JsonReader: {}", e.getMessage(), e);
            return false;
        } finally {
            try {
                reader.close();
                stringReader.close();
            } catch (IOException e) {
                // Eat the exception
            }
        }
    }
}
