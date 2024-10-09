/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal.handler;

import static org.openhab.binding.sensorpush.internal.SensorPushBindingConstants.*;
import static org.openhab.core.library.unit.ImperialUnits.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.PASCAL;
import static org.openhab.core.library.unit.Units.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorpush.internal.config.SensorConfiguration;
import org.openhab.binding.sensorpush.internal.protocol.Sample;
import org.openhab.binding.sensorpush.internal.protocol.Sensor;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensorHandler} is responsible for handling SensorPush sensor data.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class SensorHandler extends BaseThingHandler {

    private static final String MODE_STATION = "station";
    private static final String MODE_METEOROLOGICAL = "meteorological";

    private final Logger logger = LoggerFactory.getLogger(SensorHandler.class);

    private SensorConfiguration config = new SensorConfiguration();
    private @Nullable String address;
    private boolean adjustPressure = false;

    /**
     * Adjust the supplied atmospheric pressure to mean sea level based on the provided sensor altitude and
     * using a standard scale.
     *
     * @param stationPressure The pressure reading from the sensor in inches or mercury
     * @param altitude The altitude of the sensor from MSL in feet
     * @return The adjusted atmospheric pressure in inches of mercury (in Hg)
     */
    public static Double pressureAdjust(Double stationPressure, int altitude) {
        Double hm = 0.3048 * altitude; // convert feet to meters
        return stationPressure / Math.pow(((288 - 0.0065 * hm) / 288), 5.2561);
    }

    public SensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensorConfiguration.class);
        if (config.id.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not configured");
            return;
        }

        logger.debug("Initializing handler for sensor {}", config.id);

        if (MODE_METEOROLOGICAL.equalsIgnoreCase(config.pressureMode)) {
            adjustPressure = true;
        } else if (!MODE_STATION.equalsIgnoreCase(config.pressureMode)) {
            logger.warn("Parameter pressureMode set to invalid value for sensor ID {}. Assuming station mode.",
                    config.id);
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands accepted
    }

    public void handleUpdate(String id, @Nullable Sample sample, @Nullable Sensor sensor) {
        if (config.id.equals(id)) {
            logger.trace("Received update for sensor id {}", id);

            if (getThing().getStatusInfo().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }

            if (sample != null) {
                Float temperature = sample.temperature;
                if (temperature != null) {
                    updateState(CHANNEL_TEMPERATURE, QuantityType.valueOf(temperature, FAHRENHEIT));
                }

                Float humidity = sample.humidity;
                if (humidity != null) {
                    updateState(CHANNEL_HUMIDITY, QuantityType.valueOf(humidity, PERCENT));
                }

                String observed = sample.observed;
                if (observed != null) {
                    updateState(CHANNEL_TIME, new DateTimeType(observed));
                }

                if (sample.barometricPressure != null) {
                    Double barometricPressure = sample.barometricPressure.doubleValue();
                    if (adjustPressure) {
                        if (sample.altitude != null) {
                            barometricPressure = pressureAdjust(barometricPressure, sample.altitude);
                        } else if (config.altitude != null) {
                            barometricPressure = pressureAdjust(barometricPressure, config.altitude);
                        } else {
                            logger.warn(
                                    "Pressure mode set to Meteorological for sensor {}, but no sensor altitude is configured. Using station pressure.",
                                    config.id);
                        }
                    }
                    updateState(CHANNEL_PRESSURE, QuantityType.valueOf(barometricPressure, INCH_OF_MERCURY));
                }

                Float dewpoint = sample.dewpoint;
                if (dewpoint != null) {
                    updateState(CHANNEL_DEWPOINT, QuantityType.valueOf(dewpoint, FAHRENHEIT));
                }

                Float vpd = sample.vpd;
                if (vpd != null) {
                    updateState(CHANNEL_VPD, QuantityType.valueOf(vpd, KILO(PASCAL)));
                }
            }

            if (sensor != null) {
                Float batteryVoltage = sensor.batteryVoltage;
                Integer rssi = sensor.rssi;
                if (batteryVoltage != null) {
                    updateState(CHANNEL_VOLTAGE, QuantityType.valueOf(batteryVoltage, VOLT));
                }
                if (rssi != null) {
                    updateState(CHANNEL_RSSI_DBM, QuantityType.valueOf(rssi, DECIBEL_MILLIWATTS));
                    updateState(CHANNEL_RSSI, new DecimalType(rssiBars(rssi).longValue()));
                }

                setProperties(sensor);
            }
        }
    }

    /**
     * Convert RSSI value in dBm to the 0-4 number required for a channel of type system.signal-strength. An actual
     * range of 1-4 is used so that the result matches the signal strength bars displayed in the SensorPush app.
     */
    public Long rssiBars(int rssi) {
        if (rssi > -50) {
            return Long.valueOf(4);
        } else if (rssi > -70) {
            return Long.valueOf(3);
        } else if (rssi > -85) {
            return Long.valueOf(2);
        } else {
            return Long.valueOf(1);
        }
    }

    public void setProperties(Sensor sensor) {
        // Set the bluetooth address property if it isn't set already
        String address = sensor.address;
        if (this.address == null && address != null) {
            Map<String, String> properties = editProperties();
            properties.put(PROPERTY_ADDRESS, address);
            updateProperties(properties);
            this.address = address;
        }
    }
}
