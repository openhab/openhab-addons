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
package org.openhab.binding.jeelink.internal.lacrosse;

import static org.openhab.binding.jeelink.internal.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.binding.jeelink.internal.RollingAveragePublisher;
import org.openhab.binding.jeelink.internal.RollingReadingAverage;
import org.openhab.binding.jeelink.internal.config.LaCrosseTemperatureSensorConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LaCrosse Temperature Sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseTemperatureSensorHandler extends JeeLinkSensorHandler<LaCrosseTemperatureReading> {
    private final Logger logger = LoggerFactory.getLogger(LaCrosseTemperatureSensorHandler.class);

    public LaCrosseTemperatureSensorHandler(Thing thing, String sensorType) {
        super(thing, sensorType);
    }

    @Override
    public Class<LaCrosseTemperatureReading> getReadingClass() {
        return LaCrosseTemperatureReading.class;
    }

    @Override
    public ReadingPublisher<LaCrosseTemperatureReading> createPublisher() {
        return new ReadingPublisher<LaCrosseTemperatureReading>() {
            private final Map<Integer, ReadingPublisher<LaCrosseTemperatureReading>> channelPublishers = new HashMap<>();

            @Override
            public void publish(LaCrosseTemperatureReading reading) {
                if (reading != null) {
                    int channelNo = reading.getChannel();

                    ReadingPublisher<LaCrosseTemperatureReading> publisher;
                    synchronized (channelPublishers) {
                        publisher = channelPublishers.get(channelNo);
                        if (publisher == null) {
                            publisher = createPublisherForChannel(channelNo);
                            channelPublishers.put(channelNo, publisher);

                            createMissingChannels(reading.getChannel());
                        }
                    }

                    publisher.publish(reading);
                }
            }

            private void createMissingChannels(int channelNo) {
                List<Channel> missingChannels = new ArrayList<>();

                String idSuffix = channelNo > 1 ? String.valueOf(channelNo) : "";
                String labelSuffix = channelNo > 1 ? " " + channelNo : "";
                for (String channelName : new String[] { TEMPERATURE_CHANNEL, HUMIDITY_CHANNEL }) {
                    if (getThing().getChannel(channelName + idSuffix) == null) {
                        missingChannels.add(ChannelBuilder
                                .create(new ChannelUID(getThing().getUID(), channelName + idSuffix), "Number")
                                .withType(new ChannelTypeUID(getThing().getThingTypeUID().getBindingId(), channelName))
                                .withLabel(Objects.requireNonNull(StringUtils.capitalize(channelName + labelSuffix)))
                                .build());
                    }
                }
                missingChannels.addAll(getThing().getChannels());

                if (!missingChannels.isEmpty()) {
                    ThingBuilder thingBuilder = editThing();
                    thingBuilder.withChannels(missingChannels);
                    updateThing(thingBuilder.build());
                }
            }

            @Override
            public void dispose() {
                synchronized (channelPublishers) {
                    for (ReadingPublisher<LaCrosseTemperatureReading> p : channelPublishers.values()) {
                        p.dispose();
                    }
                    channelPublishers.clear();
                }
            }
        };
    }

    public ReadingPublisher<LaCrosseTemperatureReading> createPublisherForChannel(int channelNo) {
        ReadingPublisher<LaCrosseTemperatureReading> publisher = new ReadingPublisher<LaCrosseTemperatureReading>() {
            @Override
            public void publish(LaCrosseTemperatureReading reading) {
                if (reading != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    BigDecimal temp = new BigDecimal(reading.getTemperature()).setScale(1, RoundingMode.HALF_UP);

                    if (channelNo == 1) {
                        logger.debug(
                                "updating states for thing {} ({}): temp={} ({}), humidity={}, batteryNew={}, batteryLow={}",
                                getThing().getLabel(), getThing().getUID().getId(), temp, reading.getTemperature(),
                                reading.getHumidity(), reading.isBatteryNew(), reading.isBatteryLow());
                        updateState(TEMPERATURE_CHANNEL, new QuantityType<>(temp, SIUnits.CELSIUS));
                        updateState(HUMIDITY_CHANNEL, new QuantityType<>(reading.getHumidity(), Units.PERCENT));
                        updateState(BATTERY_NEW_CHANNEL, reading.isBatteryNew() ? OnOffType.ON : OnOffType.OFF);
                        updateState(BATTERY_LOW_CHANNEL, reading.isBatteryLow() ? OnOffType.ON : OnOffType.OFF);
                    } else {
                        logger.debug("updating states for channel {} of thing {} ({}): temp={} ({}), humidity={}",
                                reading.getChannel(), getThing().getLabel(), getThing().getUID().getId(), temp,
                                reading.getTemperature(), reading.getHumidity());
                        updateState(TEMPERATURE_CHANNEL + reading.getChannel(),
                                new QuantityType<>(temp, SIUnits.CELSIUS));
                        updateState(HUMIDITY_CHANNEL + reading.getChannel(),
                                new QuantityType<>(reading.getHumidity(), Units.PERCENT));
                    }
                }
            }

            @Override
            public void dispose() {
            }
        };

        LaCrosseTemperatureSensorConfig cfg = getConfigAs(LaCrosseTemperatureSensorConfig.class);
        if (cfg.bufferSize > 1 && cfg.updateInterval > 0) {
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
