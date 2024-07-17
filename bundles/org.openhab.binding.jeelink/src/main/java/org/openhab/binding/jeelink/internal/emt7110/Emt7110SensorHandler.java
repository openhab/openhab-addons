/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.emt7110;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an EMT7110 sensor thing.
 *
 * @author Timo Schober - Initial contribution
 */
public class Emt7110SensorHandler extends JeeLinkSensorHandler<Emt7110Reading> {
    private final Logger logger = LoggerFactory.getLogger(Emt7110SensorHandler.class);

    public Emt7110SensorHandler(Thing thing, String sensorType) {
        super(thing, sensorType);
    }

    @Override
    public Class<Emt7110Reading> getReadingClass() {
        return Emt7110Reading.class;
    }

    @Override
    public void initialize() {
        super.initialize();

        logger.debug("initilized handler for thing {} ({})}", getThing().getLabel(), getThing().getUID().getId());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUid, Command command) {
        logger.debug("received command for thing {} ({}): {}", getThing().getLabel(), getThing().getUID().getId(),
                command);
    }

    @Override
    public ReadingPublisher<Emt7110Reading> createPublisher() {
        return new ReadingPublisher<>() {
            @Override
            public void publish(Emt7110Reading reading) {
                if (reading != null) {
                    updateState(CURRENT_POWER_CHANNEL, new QuantityType<>(reading.getPower(), Units.WATT));
                    updateState(CONSUMPTION_CHANNEL, new QuantityType<>(reading.getaPower(), Units.KILOWATT_HOUR));
                    updateState(ELECTRIC_POTENTIAL_CHANNEL, new QuantityType<>(reading.getVoltage(), Units.VOLT));
                    updateState(ELECTRIC_CURRENT_CHANNEL,
                            new QuantityType<>(reading.getCurrent() / 1000, Units.AMPERE));
                }
            }

            @Override
            public void dispose() {
            }
        };
    }
}
