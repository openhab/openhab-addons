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
package org.openhab.binding.gce.internal.handler;

import static org.openhab.binding.gce.internal.GCEBindingConstants.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.gce.internal.config.AnalogInputConfiguration;
import org.openhab.binding.gce.internal.config.CounterConfiguration;
import org.openhab.binding.gce.internal.config.DigitalInputConfiguration;
import org.openhab.binding.gce.internal.config.Ipx800Configuration;
import org.openhab.binding.gce.internal.config.RelayOutputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ipx800v3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Ipx800v3Handler extends BaseThingHandler implements Ipx800EventListener {
    private static final String PROPERTY_SEPARATOR = "-";

    private final Logger logger = LoggerFactory.getLogger(Ipx800v3Handler.class);

    private @NonNullByDefault({}) Ipx800Configuration configuration;
    private @NonNullByDefault({}) Ipx800DeviceConnector connector;
    private Optional<Ipx800MessageParser> parser = Optional.empty();

    private final Map<String, @Nullable PortData> portDatas = new HashMap<>();

    private class LongPressEvaluator implements Runnable {
        private final ZonedDateTime referenceTime;
        private final String port;
        private final String eventChannelId;

        public LongPressEvaluator(Channel channel, String port, PortData portData) {
            this.referenceTime = portData.getTimestamp();
            this.port = port;
            this.eventChannelId = channel.getUID().getId() + PROPERTY_SEPARATOR + CHANNEL_TYPE_PUSH_BUTTON_TRIGGER;
        }

        @Override
        public void run() {
            PortData currentData = portDatas.get(port);
            if (currentData != null && currentData.getValue() == 1 && currentData.getTimestamp() == referenceTime) {
                triggerChannel(eventChannelId, EVENT_LONG_PRESS);
            }
        }
    }

    public Ipx800v3Handler(Thing thing) {
        super(thing);
        logger.debug("Create a IPX800 Handler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(Ipx800Configuration.class);

        logger.debug("Initializing IPX800 handler for uid '{}'", getThing().getUID());

        connector = new Ipx800DeviceConnector(configuration.hostname, configuration.portNumber);
        connector.setName("OH-binding-" + getThing().getUID());
        connector.setDaemon(true);

        parser = Optional.of(new Ipx800MessageParser(connector, this));

        updateStatus(ThingStatus.ONLINE);
        connector.start();
    }

    @Override
    public void dispose() {
        if (connector != null) {
            connector.destroyAndExit();
        }

        portDatas.values().stream().forEach(portData -> {
            if (portData != null) {
                portData.destroy();
            }
        });

        super.dispose();
    }

    @Override
    public void errorOccurred(Exception e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
    }

    private @Nullable Channel getChannelForPort(String port) {
        String portKind = port.substring(0, 1);
        String portNum = port.replace(portKind, "");
        return thing.getChannel(portKind + "#" + portNum);
    }

    @Override
    public void dataReceived(String port, Double value) {
        Channel channel = getChannelForPort(port);
        if (channel != null) {
            PortData portData = portDatas.get(channel.getUID().getId());
            if (portData != null) {
                if (value.equals(portData.getValue())) {
                    return;
                }

                ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
                long sinceLastChange = Duration.between(portData.getTimestamp(), now).toMillis();
                Configuration configuration = channel.getConfiguration();
                State state = UnDefType.UNDEF;
                String groupId = channel.getUID().getGroupId();
                if (groupId != null) {
                    switch (groupId) {
                        case COUNTER:
                            state = new DecimalType(value);
                            break;
                        case ANALOG_INPUT:
                            AnalogInputConfiguration config = configuration.as(AnalogInputConfiguration.class);
                            long histeresis = config.histeresis / 2;
                            if (portData.isInitializing() || value > portData.getValue() + histeresis
                                    || value < portData.getValue() - histeresis) {
                                state = new DecimalType(value);
                            } else {
                                return;
                            }
                            break;
                        case DIGITAL_INPUT:
                            DigitalInputConfiguration config2 = configuration.as(DigitalInputConfiguration.class);
                            if (config2.debouncePeriod != 0 && now.isBefore(
                                    portData.getTimestamp().plus(config2.debouncePeriod, ChronoUnit.MILLIS))) {
                                return;
                            }
                            portData.cancelPulsing();
                            if (value == 1) {
                                state = OpenClosedType.CLOSED;
                                if (portData.getValue() != -1) {
                                    triggerPushButtonChannel(channel, EVENT_PRESSED);
                                }
                                if (config2.longPressTime != 0 && !portData.isInitializing()) {
                                    scheduler.schedule(new LongPressEvaluator(channel, port, portData),
                                            config2.longPressTime, TimeUnit.MILLISECONDS);
                                } else if (config2.pulsePeriod != 0) {
                                    portData.setPulsing(scheduler.scheduleWithFixedDelay(() -> {
                                        triggerPushButtonChannel(channel, EVENT_PULSE);
                                    }, config2.pulsePeriod, config2.pulsePeriod, TimeUnit.MILLISECONDS));
                                    if (config2.pulseTimeout != 0) {
                                        scheduler.schedule(portData::cancelPulsing, config2.pulseTimeout,
                                                TimeUnit.MILLISECONDS);
                                    }
                                }
                            } else {
                                state = OpenClosedType.OPEN;
                                if (!portData.isInitializing()) {
                                    triggerPushButtonChannel(channel, EVENT_RELEASED);
                                    if (config2.longPressTime != 0 && sinceLastChange < config2.longPressTime) {
                                        triggerPushButtonChannel(channel, EVENT_SHORT_PRESS);
                                    }
                                }
                            }
                            break;
                        case RELAY_OUTPUT:
                            state = value == 1 ? OnOffType.ON : OnOffType.OFF;
                            break;
                    }
                }
                updateState(channel.getUID().getId(), state);
                if (!portData.isInitializing()) {
                    updateState(channel.getUID().getId() + PROPERTY_SEPARATOR + LAST_STATE_DURATION_CHANNEL_NAME,
                            new QuantityType<>(sinceLastChange / 1000, SmartHomeUnits.SECOND));
                }
                portData.setData(value, now);
            } else {
                logger.debug("Received data '{}' for not configured port '{}'", value, port);
            }
        } else {
            logger.debug("Received data '{}' for not configured channel '{}'", value, port);
        }
    }

    protected void triggerPushButtonChannel(Channel channel, String event) {
        logger.debug("Triggering event '{}' on channel '{}'", event, channel.getUID());
        triggerChannel(channel.getUID().getId() + PROPERTY_SEPARATOR + CHANNEL_TYPE_PUSH_BUTTON_TRIGGER, event);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        Channel channel = thing.getChannel(channelUID.getId());
        if (isValidPortId(channelUID) && RELAY_OUTPUT.equalsIgnoreCase(channelUID.getGroupId())
                && command instanceof OnOffType && channel != null) {
            RelayOutputConfiguration config = channel.getConfiguration().as(RelayOutputConfiguration.class);
            parser.ifPresent(p -> p.setOutput(channelUID.getIdWithoutGroup(),
                    (OnOffType) command == OnOffType.ON ? 1 : 0, config.pulse));
            return;
        }
        logger.info("Can not handle command '{}' on channel '{}'", command, channelUID);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);
        final String channelId = channelUID.getId();
        if (isValidPortId(channelUID)) {
            Channel channel = thing.getChannel(channelUID);
            if (channel != null) {
                Configuration configuration = channel.getConfiguration();
                PortData data = new PortData();
                if (configuration.get("pullFrequency") != null) {
                    int pullFrequency = configuration.as(CounterConfiguration.class).pullFrequency;
                    data.setPullJob(scheduler.scheduleWithFixedDelay(() -> {
                        parser.ifPresent(p -> p.getValue(channelId));
                    }, pullFrequency, pullFrequency, TimeUnit.MILLISECONDS));
                }
                portDatas.put(channelId, data);
            }
        }
    }

    private boolean isValidPortId(ChannelUID channelUID) {
        return channelUID.getIdWithoutGroup().chars().allMatch(Character::isDigit);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        PortData portData = portDatas.remove(channelUID.getId());
        if (portData != null) {
            portData.destroy();
        }
    }

    public void resetCounter(int counter) {
        parser.ifPresent(p -> p.resetCounter(counter));
    }

    public void reset() {
        parser.ifPresent(Ipx800MessageParser::reset);
    }

}
