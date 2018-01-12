/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.binding.jeelink.internal.RollingAveragePublisher;
import org.openhab.binding.jeelink.internal.RollingReadingAverage;
import org.openhab.binding.jeelink.internal.config.LaCrosseTemperatureSensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LaCrosse Temperature Sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseTemperatureSensorHandler extends JeeLinkSensorHandler<LaCrosseTemperatureReading> {
    private final Logger logger = LoggerFactory.getLogger(LaCrosseTemperatureSensorHandler.class);

    public LaCrosseTemperatureSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public String getSketchName() {
        return "LaCrosseITPlusReader";
    }

    @Override
    public ReadingPublisher<LaCrosseTemperatureReading> createPublisher() {
        ReadingPublisher<LaCrosseTemperatureReading> publisher = new ReadingPublisher<LaCrosseTemperatureReading>() {
            @Override
            public void publish(LaCrosseTemperatureReading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    BigDecimal temp = new BigDecimal(reading.getTemperature()).setScale(1, RoundingMode.HALF_UP);

                    logger.debug(
                            "updating states for thing {} ({}): temp={} ({}), humidity={}, batteryNew={}, batteryLow={}",
                            getThing().getLabel(), getThing().getUID().getId(), temp, reading.getTemperature(),
                            reading.getHumidity(), reading.isBatteryNew(), reading.isBatteryLow());
                    updateState(TEMPERATURE_CHANNEL, new DecimalType(temp));
                    updateState(HUMIDITY_CHANNEL, new DecimalType(reading.getHumidity()));
                    updateState(BATTERY_NEW_CHANNEL, reading.isBatteryNew() ? OnOffType.ON : OnOffType.OFF);
                    updateState(BATTERY_LOW_CHANNEL, reading.isBatteryLow() ? OnOffType.ON : OnOffType.OFF);
                }
            }

            @Override
            public void dispose() {
            }
        };

        LaCrosseTemperatureSensorConfig cfg = getConfigAs(LaCrosseTemperatureSensorConfig.class);
        if (cfg.bufferSize > 1) {
            publisher = new RollingAveragePublisher<LaCrosseTemperatureReading>(cfg.bufferSize, cfg.updateInterval,
                    publisher, scheduler) {
                @Override
                public RollingReadingAverage<LaCrosseTemperatureReading> createRollingReadingAverage(int bufferSize) {
                    return new LaCrosseRollingReadingAverage(bufferSize);
                }
            };
        }

        if (cfg.maxDiff > 0) {
            publisher = new DifferenceCheckingPublisher(cfg.maxDiff, publisher);
        }

        publisher = new BoundsCheckingPublisher(cfg.minTemp, cfg.maxTemp, publisher);

        return publisher;
    }
}
