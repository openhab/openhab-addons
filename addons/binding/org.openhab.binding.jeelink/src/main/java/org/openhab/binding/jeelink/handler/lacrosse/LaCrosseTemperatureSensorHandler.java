/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler.lacrosse;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.jeelink.config.LaCrosseTemperatureSensorConfig;
import org.openhab.binding.jeelink.handler.JeeLinkSensorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LaCrosse Temperature Sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseTemperatureSensorHandler extends JeeLinkSensorHandler<LaCrosseTemperatureReading> {
    private final Logger logger = LoggerFactory.getLogger(LaCrosseTemperatureSensorHandler.class);

    private LaCrosseTemperatureReading lastReading = null;

    private float minTemp = Float.NaN;
    private float maxTemp = Float.NaN;

    public LaCrosseTemperatureSensorHandler(Thing thing) {
        super(thing, LaCrosseTemperatureReading.class);
    }

    @Override
    public synchronized void initialize() {
        super.initialize();

        minTemp = getConfig().as(LaCrosseTemperatureSensorConfig.class).minTemp;
        maxTemp = getConfig().as(LaCrosseTemperatureSensorConfig.class).maxTemp;

        logger.debug("LaCrosse sensor for thing {} ({}) created with limits {}..{}", getThing().getLabel(),
                getThing().getUID(), minTemp, maxTemp);
    }

    @Override
    public void updateReadingStates(LaCrosseTemperatureReading reading) {
        if (reading != null) {
            BigDecimal temp = new BigDecimal(reading.getTemperature()).setScale(1, RoundingMode.HALF_UP);

            if (lastReading != null) {
                // sanity check
                int humDiff = Math.abs(lastReading.getHumidity() - reading.getHumidity());
                if (humDiff > 3) {
                    logger.warn("thing {} ({}) has dubious humidity reading: humidity={}, lastHumidity={}",
                            getThing().getLabel(), getThing().getUID().getId(), reading.getHumidity(),
                            lastReading.getHumidity());
                }

                float tempDiff = Math.abs(lastReading.getTemperature() - reading.getTemperature());
                if (tempDiff > 0.5f) {
                    logger.warn("thing {} ({}) has dubious temperature reading: temp={}, lastTemp={}",
                            getThing().getLabel(), getThing().getUID().getId(), reading.getTemperature(),
                            lastReading.getTemperature());
                }
            }
            lastReading = reading;

            logger.debug("updating states for thing {} ({}): temp={} ({}), humidity={}, batteryNew={}, batteryLow={}",
                    getThing().getLabel(), getThing().getUID().getId(), temp, reading.getTemperature(),
                    reading.getHumidity(), reading.isBatteryNew(), reading.isBatteryLow());
            updateState(getThing().getChannel(TEMPERATURE_CHANNEL).getUID(), new DecimalType(temp));
            updateState(getThing().getChannel(HUMIDITY_CHANNEL).getUID(), new DecimalType(reading.getHumidity()));
            updateState(getThing().getChannel(BATTERY_NEW_CHANNEL).getUID(),
                    reading.isBatteryNew() ? OnOffType.ON : OnOffType.OFF);
            updateState(getThing().getChannel(BATTERY_LOW_CHANNEL).getUID(),
                    reading.isBatteryLow() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public boolean isReadingWithinBounds(LaCrosseTemperatureReading reading) {
        if (minTemp != Float.NaN && reading.getTemperature() < minTemp) {
            logger.warn("thing {} ({}) has invalid temperature reading: temp={}", getThing().getLabel(),
                    getThing().getUID().getId(), reading.getTemperature());
            return false;
        } else if (maxTemp != Float.NaN && reading.getTemperature() > maxTemp) {
            logger.warn("thing {} ({}) has invalid temperature reading: temp={}", getThing().getLabel(),
                    getThing().getUID().getId(), reading.getTemperature());
            return false;
        }

        return true;
    }
}
