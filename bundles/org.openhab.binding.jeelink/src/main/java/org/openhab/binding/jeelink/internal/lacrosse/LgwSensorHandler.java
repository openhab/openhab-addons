/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.lacrosse;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LGW Sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
@NonNullByDefault
public class LgwSensorHandler extends JeeLinkSensorHandler<LgwReading> {
    private final Logger logger = LoggerFactory.getLogger(LgwSensorHandler.class);
    private boolean hasHumidityChannel;
    private boolean hasPressureChannel;

    public LgwSensorHandler(Thing thing, String sensorType) {
        super(thing, sensorType);

        hasHumidityChannel = getThing().getChannel(HUMIDITY_CHANNEL) != null;
        hasPressureChannel = getThing().getChannel(PRESSURE_CHANNEL) != null;
    }

    @Override
    public Class<LgwReading> getReadingClass() {
        return LgwReading.class;
    }

    @Override
    public ReadingPublisher<LgwReading> createPublisher() {
        ReadingPublisher<LgwReading> publisher = new ReadingPublisher<LgwReading>() {
            @Override
            public void publish(LgwReading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    logger.debug("updating states for thing {} ({}): {}", getThing().getLabel(),
                            getThing().getUID().getId(), reading);

                    if (reading.hasTemperature()) {
                        BigDecimal temp = new BigDecimal(reading.getTemperature()).setScale(1, RoundingMode.HALF_UP);
                        updateState(TEMPERATURE_CHANNEL, new QuantityType<>(temp, SIUnits.CELSIUS));
                    }

                    if (reading.hasHumidity()) {
                        if (!hasHumidityChannel) {
                            ThingBuilder thingBuilder = editThing();
                            thingBuilder.withChannel(ChannelBuilder
                                    .create(new ChannelUID(getThing().getUID(), HUMIDITY_CHANNEL), "Number:Humidity")
                                    .withType(new ChannelTypeUID(getThing().getThingTypeUID().getBindingId(),
                                            HUMIDITY_CHANNEL))
                                    .withLabel(StringUtils.capitalize(HUMIDITY_CHANNEL)).build());
                            updateThing(thingBuilder.build());

                            hasHumidityChannel = true;
                        }

                        updateState(HUMIDITY_CHANNEL,
                                new QuantityType<>(reading.getHumidity(), SmartHomeUnits.PERCENT));
                    }

                    if (reading.hasPressure()) {
                        if (!hasPressureChannel) {
                            ThingBuilder thingBuilder = editThing();
                            thingBuilder.withChannel(ChannelBuilder
                                    .create(new ChannelUID(getThing().getUID(), PRESSURE_CHANNEL), "Number:Pressure")
                                    .withType(new ChannelTypeUID(getThing().getThingTypeUID().getBindingId(),
                                            PRESSURE_CHANNEL))
                                    .withLabel(StringUtils.capitalize(PRESSURE_CHANNEL)).build());
                            updateThing(thingBuilder.build());

                            hasPressureChannel = true;
                        }

                        updateState(PRESSURE_CHANNEL, new QuantityType<>(reading.getPressure(), HECTO(SIUnits.PASCAL)));
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
