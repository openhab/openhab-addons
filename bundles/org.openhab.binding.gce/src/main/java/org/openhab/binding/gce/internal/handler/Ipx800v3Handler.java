/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gce.internal.action.Ipx800Actions;
import org.openhab.binding.gce.internal.config.AnalogInputConfiguration;
import org.openhab.binding.gce.internal.config.DigitalInputConfiguration;
import org.openhab.binding.gce.internal.config.Ipx800Configuration;
import org.openhab.binding.gce.internal.config.RelayOutputConfiguration;
import org.openhab.binding.gce.internal.model.M2MMessageParser;
import org.openhab.binding.gce.internal.model.PortData;
import org.openhab.binding.gce.internal.model.PortDefinition;
import org.openhab.binding.gce.internal.model.StatusFileInterpreter;
import org.openhab.binding.gce.internal.model.StatusFileInterpreter.StatusEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private static final double ANALOG_SAMPLING = 0.000050354;

    private final Logger logger = LoggerFactory.getLogger(Ipx800v3Handler.class);

    private Optional<Ipx800DeviceConnector> connector = Optional.empty();
    private Optional<M2MMessageParser> parser = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    private final Map<String, PortData> portDatas = new HashMap<>();

    private class LongPressEvaluator implements Runnable {
        private final ZonedDateTime referenceTime;
        private final String port;
        private final String eventChannelId;

        public LongPressEvaluator(Channel channel, String port, PortData portData) {
            this.referenceTime = portData.getTimestamp();
            this.port = port;
            this.eventChannelId = channel.getUID().getId() + PROPERTY_SEPARATOR + TRIGGER_CONTACT;
        }

        @Override
        public void run() {
            PortData currentData = portDatas.get(port);
            if (currentData != null && currentData.getValue() == 1
                    && referenceTime.equals(currentData.getTimestamp())) {
                triggerChannel(eventChannelId, EVENT_LONG_PRESS);
            }
        }
    }

    public Ipx800v3Handler(Thing thing) {
        super(thing);
        logger.debug("Create an IPX800 Handler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IPX800 handler for uid '{}'", getThing().getUID());

        Ipx800Configuration config = getConfigAs(Ipx800Configuration.class);
        StatusFileInterpreter statusFile = new StatusFileInterpreter(config.hostname, this);

        if (thing.getProperties().isEmpty()) {
            updateProperties(Map.of(Thing.PROPERTY_VENDOR, "GCE Electronics", Thing.PROPERTY_FIRMWARE_VERSION,
                    statusFile.getElement(StatusEntry.VERSION), Thing.PROPERTY_MAC_ADDRESS,
                    statusFile.getElement(StatusEntry.CONFIG_MAC)));
        }

        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        PortDefinition.asStream().forEach(portDefinition -> {
            int nbElements = statusFile.getMaxNumberofNodeType(portDefinition);
            for (int i = 0; i < nbElements; i++) {
                ChannelUID portChannelUID = createChannels(portDefinition, i, channels);
                portDatas.put(portChannelUID.getId(), new PortData());
            }
        });

        updateThing(editThing().withChannels(channels).build());

        connector = Optional.of(new Ipx800DeviceConnector(config.hostname, config.portNumber, getThing().getUID()));
        parser = Optional.of(new M2MMessageParser(connector.get(), this));

        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = Optional.of(
                scheduler.scheduleWithFixedDelay(statusFile::read, 3000, config.pullInterval, TimeUnit.MILLISECONDS));

        connector.get().start();
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();

        connector.ifPresent(Ipx800DeviceConnector::dispose);
        connector = Optional.empty();

        parser = Optional.empty();

        portDatas.values().stream().forEach(PortData::dispose);
        super.dispose();
    }

    private void addIfChannelAbsent(ChannelBuilder channelBuilder, List<Channel> channels) {
        Channel newChannel = channelBuilder.build();
        if (channels.stream().noneMatch(c -> c.getUID().equals(newChannel.getUID()))) {
            channels.add(newChannel);
        }
    }

    private ChannelUID createChannels(PortDefinition portDefinition, int portIndex, List<Channel> channels) {
        String ndx = Integer.toString(portIndex + 1);
        String advancedChannelTypeName = portDefinition.toString()
                + (portDefinition.isAdvanced(portIndex) ? "Advanced" : "");
        ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), portDefinition.toString());
        ChannelUID mainChannelUID = new ChannelUID(groupUID, ndx);
        ChannelTypeUID channelType = new ChannelTypeUID(BINDING_ID, advancedChannelTypeName);
        switch (portDefinition) {
            case ANALOG:
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.NUMBER)
                        .withLabel("Analog Input " + ndx).withType(channelType), channels);
                addIfChannelAbsent(
                        ChannelBuilder.create(new ChannelUID(groupUID, ndx + "-voltage"), "Number:ElectricPotential")
                                .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_VOLTAGE)).withLabel("Voltage " + ndx),
                        channels);
                break;
            case CONTACT:
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.CONTACT)
                        .withLabel("Contact " + ndx).withType(channelType), channels);
                addIfChannelAbsent(ChannelBuilder.create(new ChannelUID(groupUID, ndx + "-event"), null)
                        .withType(new ChannelTypeUID(BINDING_ID, TRIGGER_CONTACT + (portIndex < 8 ? "" : "Advanced")))
                        .withLabel("Contact " + ndx + " Event").withKind(ChannelKind.TRIGGER), channels);
                break;
            case COUNTER:
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.NUMBER)
                        .withLabel("Counter " + ndx).withType(channelType), channels);
                break;
            case RELAY:
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.SWITCH)
                        .withLabel("Relay " + ndx).withType(channelType), channels);
                break;
        }

        addIfChannelAbsent(ChannelBuilder.create(new ChannelUID(groupUID, ndx + "-duration"), "Number:Time")
                .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_LAST_STATE_DURATION))
                .withLabel("Previous state duration " + ndx), channels);

        return mainChannelUID;
    }

    @Override
    public void errorOccurred(Exception e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
    }

    private boolean ignoreCondition(double newValue, PortData portData, Configuration configuration,
            PortDefinition portDefinition, ZonedDateTime now) {
        if (!portData.isInitializing()) { // Always accept if portData is not initialized
            double prevValue = portData.getValue();
            if (newValue == prevValue) { // Always reject if the value did not change
                return true;
            }
            if (portDefinition == PortDefinition.ANALOG) { // For analog values, check histeresis
                AnalogInputConfiguration config = configuration.as(AnalogInputConfiguration.class);
                long hysteresis = config.hysteresis / 2;
                return (newValue <= prevValue + hysteresis && newValue >= prevValue - hysteresis);
            } else if (portDefinition == PortDefinition.CONTACT) { // For contact values, check debounce
                DigitalInputConfiguration config = configuration.as(DigitalInputConfiguration.class);
                return (config.debouncePeriod != 0
                        && now.isBefore(portData.getTimestamp().plus(config.debouncePeriod, ChronoUnit.MILLIS)));
            }
        }
        return false;
    }

    @Override
    public void dataReceived(String port, double value) {
        updateStatus(ThingStatus.ONLINE);
        Channel channel = thing.getChannel(PortDefinition.asChannelId(port));
        if (channel != null) {
            String channelId = channel.getUID().getId();
            String groupId = channel.getUID().getGroupId();
            PortData portData = portDatas.get(channelId);
            if (portData != null && groupId != null) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
                long sinceLastChange = Duration.between(portData.getTimestamp(), now).toMillis();
                Configuration configuration = channel.getConfiguration();
                PortDefinition portDefinition = PortDefinition.fromGroupId(groupId);
                if (ignoreCondition(value, portData, configuration, portDefinition, now)) {
                    logger.debug("Ignore condition met for port '{}' with data '{}'", port, value);
                    return;
                }
                logger.debug("About to update port '{}' with data '{}'", port, value);
                State state = UnDefType.NULL;
                switch (portDefinition) {
                    case COUNTER:
                        state = new DecimalType(value);
                        break;
                    case RELAY:
                        state = value == 1 ? OnOffType.ON : OnOffType.OFF;
                        break;
                    case ANALOG:
                        state = new DecimalType(value);
                        updateIfLinked(channelId + PROPERTY_SEPARATOR + CHANNEL_VOLTAGE,
                                new QuantityType<>(value * ANALOG_SAMPLING, Units.VOLT));
                        break;
                    case CONTACT:
                        DigitalInputConfiguration config = configuration.as(DigitalInputConfiguration.class);
                        portData.cancelPulsing();
                        state = value == 1 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                        switch ((OpenClosedType) state) {
                            case CLOSED:
                                if (config.longPressTime != 0 && !portData.isInitializing()) {
                                    scheduler.schedule(new LongPressEvaluator(channel, port, portData),
                                            config.longPressTime, TimeUnit.MILLISECONDS);
                                } else if (config.pulsePeriod != 0) {
                                    portData.setPulsing(scheduler.scheduleWithFixedDelay(() -> {
                                        triggerPushButtonChannel(channel, EVENT_PULSE);
                                    }, config.pulsePeriod, config.pulsePeriod, TimeUnit.MILLISECONDS));
                                    if (config.pulseTimeout != 0) {
                                        scheduler.schedule(portData::cancelPulsing, config.pulseTimeout,
                                                TimeUnit.MILLISECONDS);
                                    }
                                }
                                break;
                            case OPEN:
                                if (!portData.isInitializing() && config.longPressTime != 0
                                        && sinceLastChange < config.longPressTime) {
                                    triggerPushButtonChannel(channel, EVENT_SHORT_PRESS);
                                }
                                break;
                        }
                        if (!portData.isInitializing()) {
                            triggerPushButtonChannel(channel, value == 1 ? EVENT_PRESSED : EVENT_RELEASED);
                        }
                        break;
                }

                updateIfLinked(channelId, state);
                if (!portData.isInitializing()) {
                    updateIfLinked(channelId + PROPERTY_SEPARATOR + CHANNEL_LAST_STATE_DURATION,
                            new QuantityType<>(sinceLastChange / 1000, Units.SECOND));
                }
                portData.setData(value, now);
            } else {
                logger.debug("Received data '{}' for not configured port '{}'", value, port);
            }
        } else {
            logger.debug("Received data '{}' for not configured channel '{}'", value, port);
        }
    }

    private void updateIfLinked(String channelId, State state) {
        if (isLinked(channelId)) {
            updateState(channelId, state);
        }
    }

    protected void triggerPushButtonChannel(Channel channel, String event) {
        logger.debug("Triggering event '{}' on channel '{}'", event, channel.getUID());
        triggerChannel(channel.getUID().getId() + PROPERTY_SEPARATOR + TRIGGER_CONTACT, event);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        Channel channel = thing.getChannel(channelUID.getId());
        String groupId = channelUID.getGroupId();

        if (channel == null || groupId == null) {
            return;
        }
        if (command instanceof OnOffType && isValidPortId(channelUID)
                && PortDefinition.fromGroupId(groupId) == PortDefinition.RELAY) {
            RelayOutputConfiguration config = channel.getConfiguration().as(RelayOutputConfiguration.class);
            String id = channelUID.getIdWithoutGroup();
            parser.ifPresent(p -> p.setOutput(id, (OnOffType) command == OnOffType.ON ? 1 : 0, config.pulse));
            return;
        }
        logger.debug("Can not handle command '{}' on channel '{}'", command, channelUID);
    }

    private boolean isValidPortId(ChannelUID channelUID) {
        return channelUID.getIdWithoutGroup().chars().allMatch(Character::isDigit);
    }

    public void resetCounter(int counter) {
        parser.ifPresent(p -> p.resetCounter(counter));
    }

    public void reset() {
        parser.ifPresent(M2MMessageParser::resetPLC);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(Ipx800Actions.class);
    }
}
