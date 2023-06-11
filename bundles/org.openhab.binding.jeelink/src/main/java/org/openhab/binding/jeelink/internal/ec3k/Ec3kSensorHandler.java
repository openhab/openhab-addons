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
package org.openhab.binding.jeelink.internal.ec3k;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.binding.jeelink.internal.RollingAveragePublisher;
import org.openhab.binding.jeelink.internal.RollingReadingAverage;
import org.openhab.binding.jeelink.internal.config.BufferedSensorConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an EC3000 sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kSensorHandler extends JeeLinkSensorHandler<Ec3kReading> {
    private final Logger logger = LoggerFactory.getLogger(Ec3kSensorHandler.class);

    public Ec3kSensorHandler(Thing thing, String sensorType) {
        super(thing, sensorType);
    }

    @Override
    public Class<Ec3kReading> getReadingClass() {
        return Ec3kReading.class;
    }

    @Override
    public ReadingPublisher<Ec3kReading> createPublisher() {
        ReadingPublisher<Ec3kReading> publisher = new ReadingPublisher<Ec3kReading>() {
            @Override
            public void publish(Ec3kReading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    BigDecimal currentWatt = new BigDecimal(reading.getCurrentWatt()).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal maxWatt = new BigDecimal(reading.getMaxWatt()).setScale(1, RoundingMode.HALF_UP);

                    logger.debug(
                            "updating states for thing {}: currWatt={} ({}), maxWatt={}, consumption={}, secondsOn={}, secondsTotal={}",
                            getThing().getUID().getId(), currentWatt, reading.getCurrentWatt(), maxWatt,
                            reading.getConsumptionTotal(), reading.getApplianceTime(), reading.getSensorTime());

                    updateState(CURRENT_POWER_CHANNEL, new QuantityType<>(currentWatt, Units.WATT));
                    updateState(MAX_POWER_CHANNEL, new QuantityType<>(maxWatt, Units.WATT));
                    updateState(CONSUMPTION_CHANNEL,
                            new QuantityType<>(reading.getConsumptionTotal(), Units.WATT_HOUR));
                    updateState(APPLIANCE_TIME_CHANNEL, new QuantityType<>(reading.getApplianceTime(), Units.HOUR));
                    updateState(SENSOR_TIME_CHANNEL, new QuantityType<>(reading.getSensorTime(), Units.HOUR));
                    updateState(RESETS_CHANNEL, new DecimalType(reading.getResets()));
                }
            }

            @Override
            public void dispose() {
            }
        };

        BufferedSensorConfig cfg = getConfigAs(BufferedSensorConfig.class);
        if (cfg.bufferSize > 1 && cfg.updateInterval > 0) {
            publisher = new RollingAveragePublisher<Ec3kReading>(cfg.bufferSize, cfg.updateInterval, publisher,
                    scheduler) {
                @Override
                public RollingReadingAverage<Ec3kReading> createRollingReadingAverage(int bufferSize) {
                    return new Ec3kRollingReadingAverage(bufferSize);
                }
            };
        }

        return publisher;
    }
}
