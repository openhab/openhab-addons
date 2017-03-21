/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler.ec3k;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.jeelink.handler.JeeLinkSensorHandler;
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
        super(thing, Ec3kReading.class);
    }

    @Override
    public void updateReadingStates(Ec3kReading reading) {
        if (reading != null) {
            BigDecimal currentWatt = new BigDecimal(reading.getCurrentWatt()).setScale(1, RoundingMode.HALF_UP);
            BigDecimal maxWatt = new BigDecimal(reading.getMaxWatt()).setScale(1, RoundingMode.HALF_UP);

            logger.debug(
                    "updating states for thing {}: currWatt={} ({}), maxWatt={}, consumption={}, secondsOn={}, secondsTotal={}",
                    getThing().getUID().getId(), currentWatt, reading.getCurrentWatt(), maxWatt,
                    reading.getConsumptionTotal(), reading.getApplianceTime(), reading.getSensorTime());

            updateState(getThing().getChannel(CURRENT_WATT_CHANNEL).getUID(), new DecimalType(currentWatt));
            updateState(getThing().getChannel(MAX_WATT_CHANNEL).getUID(), new DecimalType(maxWatt));
            updateState(getThing().getChannel(CONSUMPTION_CHANNEL).getUID(),
                    new DecimalType(reading.getConsumptionTotal()));
            updateState(getThing().getChannel(APPLIANCE_TIME_CHANNEL).getUID(),
                    new DecimalType(reading.getApplianceTime()));
            updateState(getThing().getChannel(SENSOR_TIME_CHANNEL).getUID(), new DecimalType(reading.getSensorTime()));
            updateState(getThing().getChannel(RESETS_CHANNEL).getUID(), new DecimalType(reading.getResets()));
        }
    }
}
