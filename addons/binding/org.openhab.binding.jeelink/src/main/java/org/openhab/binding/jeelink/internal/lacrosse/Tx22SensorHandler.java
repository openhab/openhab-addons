/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a TX22 Temperature/Humidity Sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class Tx22SensorHandler extends JeeLinkSensorHandler<Tx22Reading> {
    private final Logger logger = LoggerFactory.getLogger(Tx22SensorHandler.class);

    public Tx22SensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<Tx22Reading> getReadingClass() {
        return Tx22Reading.class;
    }

    @Override
    public ReadingPublisher<Tx22Reading> createPublisher() {
        ReadingPublisher<Tx22Reading> publisher = new ReadingPublisher<Tx22Reading>() {
            @Override
            public void publish(Tx22Reading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    logger.debug("updating states for thing {} ({}): {}", getThing().getLabel(),
                            getThing().getUID().getId(), reading);

                    updateState(BATTERY_NEW_CHANNEL, reading.isBatteryNew() ? OnOffType.ON : OnOffType.OFF);
                    updateState(BATTERY_LOW_CHANNEL, reading.isBatteryLow() ? OnOffType.ON : OnOffType.OFF);

                    if (reading.hasTemperature()) {
                        BigDecimal temp = new BigDecimal(reading.getTemperature()).setScale(1, RoundingMode.HALF_UP);
                        updateState(TEMPERATURE_CHANNEL, new QuantityType<>(temp, SIUnits.CELSIUS));
                    }
                    if (reading.hasHumidity()) {
                        updateState(HUMIDITY_CHANNEL,
                                new QuantityType<>(reading.getHumidity(), SmartHomeUnits.PERCENT));
                    }
                    if (reading.hasRain()) {
                        updateState(RAIN_CHANNEL, new QuantityType<>(reading.getRain(), MILLI(SIUnits.METRE)));
                    }
                    if (reading.hasPressure()) {
                        updateState(PRESSURE_CHANNEL, new QuantityType<>(reading.getPressure(), HECTO(SIUnits.PASCAL)));
                    }
                    if (reading.hasWindDirection()) {
                        updateState(WIND_ANGLE_CHANNEL,
                                new QuantityType<>(reading.getWindDirection(), SmartHomeUnits.DEGREE_ANGLE));
                    }
                    if (reading.hasWindSpeed()) {
                        updateState(WIND_STENGTH_CHANNEL,
                                new QuantityType<>(reading.getWindSpeed(), SmartHomeUnits.METRE_PER_SECOND));
                    }
                    if (reading.hasWindGust()) {
                        updateState(GUST_STRENGTH_CHANNEL,
                                new QuantityType<>(reading.getWindGust(), SmartHomeUnits.METRE_PER_SECOND));
                    }
                }
            }

            @Override
            public void dispose() {
            }
        };

        return publisher;
    }
}
