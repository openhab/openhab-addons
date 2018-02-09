/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.ec3k;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.binding.jeelink.internal.RollingAveragePublisher;
import org.openhab.binding.jeelink.internal.RollingReadingAverage;
import org.openhab.binding.jeelink.internal.config.JeeLinkSensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a EC3000 sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kSensorHandler extends JeeLinkSensorHandler<Ec3kReading> {
    private final Logger logger = LoggerFactory.getLogger(Ec3kSensorHandler.class);

    public Ec3kSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public String getSketchName() {
        return "ec3kSerial";
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

                    updateState(CURRENT_WATT_CHANNEL, new DecimalType(currentWatt));
                    updateState(MAX_WATT_CHANNEL, new DecimalType(maxWatt));
                    updateState(CONSUMPTION_CHANNEL, new DecimalType(reading.getConsumptionTotal()));
                    updateState(APPLIANCE_TIME_CHANNEL, new DecimalType(reading.getApplianceTime()));
                    updateState(SENSOR_TIME_CHANNEL, new DecimalType(reading.getSensorTime()));
                    updateState(RESETS_CHANNEL, new DecimalType(reading.getResets()));
                }
            }

            @Override
            public void dispose() {
            }
        };

        JeeLinkSensorConfig cfg = getConfigAs(JeeLinkSensorConfig.class);
        if (cfg.bufferSize > 1) {
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
