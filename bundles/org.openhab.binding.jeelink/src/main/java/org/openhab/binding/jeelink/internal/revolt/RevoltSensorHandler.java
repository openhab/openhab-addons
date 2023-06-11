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
package org.openhab.binding.jeelink.internal.revolt;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a Revolt Energy Meter sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class RevoltSensorHandler extends JeeLinkSensorHandler<RevoltReading> {
    private final Logger logger = LoggerFactory.getLogger(RevoltSensorHandler.class);

    public RevoltSensorHandler(Thing thing, String sensorType) {
        super(thing, sensorType);
    }

    @Override
    public Class<RevoltReading> getReadingClass() {
        return RevoltReading.class;
    }

    @Override
    public ReadingPublisher<RevoltReading> createPublisher() {
        ReadingPublisher<RevoltReading> publisher = new ReadingPublisher<RevoltReading>() {
            @Override
            public void publish(RevoltReading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    BigDecimal power = new BigDecimal(reading.getPower()).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal powerFactor = new BigDecimal(reading.getPowerFactor()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal consumption = new BigDecimal(reading.getConsumption()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal current = new BigDecimal(reading.getCurrent()).setScale(2, RoundingMode.HALF_UP);

                    logger.debug(
                            "updating states for thing {}: power={}, powerFactor={}, consumption={}, current={}, voltage={}, frequency={} ",
                            getThing().getUID().getId(), power, powerFactor, consumption, current, reading.getVoltage(),
                            reading.getFrequency());

                    updateState(CURRENT_POWER_CHANNEL, new QuantityType<>(power, Units.WATT));
                    updateState(POWER_FACTOR_CHANNEL, new DecimalType(powerFactor));
                    updateState(CONSUMPTION_CHANNEL, new QuantityType<>(consumption, Units.WATT_HOUR));
                    updateState(ELECTRIC_CURRENT_CHANNEL, new QuantityType<>(current, Units.AMPERE));
                    updateState(ELECTRIC_POTENTIAL_CHANNEL, new QuantityType<>(reading.getVoltage(), Units.VOLT));
                    updateState(FREQUENCY_CHANNEL, new QuantityType<>(reading.getFrequency(), Units.HERTZ));
                }
            }

            @Override
            public void dispose() {
            }
        };

        return publisher;
    }
}
