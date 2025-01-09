/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gce.internal.action.Ipx800Actions;
import org.openhab.binding.gce.internal.config.AnalogInputConfiguration;
import org.openhab.binding.gce.internal.config.DigitalInputConfiguration;
import org.openhab.binding.gce.internal.config.Ipx800Configuration;
import org.openhab.binding.gce.internal.config.RelayOutputConfiguration;
import org.openhab.binding.gce.internal.model.PortData;
import org.openhab.binding.gce.internal.model.PortDefinition;
import org.openhab.binding.gce.internal.model.StatusFile;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
    private final Map<ChannelUID, PortData> portDatas = new HashMap<>();

    private @Nullable Ipx800DeviceConnector deviceConnector;
    private List<ScheduledFuture<?>> jobs = new ArrayList<>();

    public Ipx800v3Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IPX800 handler for uid '{}'", getThing().getUID());

        Ipx800Configuration config = getConfigAs(Ipx800Configuration.class);

        try {
            deviceConnector = new Ipx800DeviceConnector(config.hostname, config.portNumber, getThing().getUID(), this);
            updateStatus(ThingStatus.UNKNOWN);
            jobs.add(scheduler.scheduleWithFixedDelay(this::readStatusFile, 1500, config.pullInterval,
                    TimeUnit.MILLISECONDS));
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void readStatusFile() {
        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            StatusFile status = null;
            try {
                status = connector.readStatusFile();
            } catch (SAXException | IOException e) {
                logger.warn("Unable to read status file for {}", thing.getUID());
            }

            if (Thread.State.NEW.equals(connector.getState())) {
                setProperties(status);
                updateChannels(status);
                connector.start();
            }

            if (status instanceof StatusFile statusFile) {
                PortDefinition.AS_SET.forEach(portDefinition -> statusFile.getPorts(portDefinition).forEach(
                        (portNum, value) -> dataReceived("%s%d".formatted(portDefinition.portName, portNum), value)));
            }
        }
    }

    private void updateChannels(@Nullable StatusFile status) {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        PortDefinition.AS_SET.forEach(portDefinition -> {
            int nbElements = status != null ? status.getPorts(portDefinition).size() : portDefinition.quantity;
            for (int i = 0; i < nbElements; i++) {
                ChannelUID portChannelUID = createChannels(portDefinition, i, channels);
                portDatas.put(portChannelUID, new PortData());
            }
        });
        updateThing(editThing().withChannels(channels).build());
    }

    private void setProperties(@Nullable StatusFile status) {
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        properties.put(Thing.PROPERTY_VENDOR, "GCE Electronics");
        if (status != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, status.getVersion());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, status.getMac());
        }
        updateProperties(properties);
    }

    @Override
    public void dispose() {
        jobs.forEach(job -> job.cancel(true));
        jobs.clear();

        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.dispose();
            deviceConnector = null;
        }

        portDatas.values().stream().forEach(PortData::dispose);
        portDatas.clear();

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
            case ANALOG -> {
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.NUMBER)
                        .withLabel("Analog Input " + ndx).withType(channelType), channels);
                addIfChannelAbsent(
                        ChannelBuilder.create(new ChannelUID(groupUID, ndx + "-voltage"), "Number:ElectricPotential")
                                .withType(new ChannelTypeUID(BINDING_ID, CHANNEL_VOLTAGE)).withLabel("Voltage " + ndx),
                        channels);
            }
            case CONTACT -> {
                addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.CONTACT)
                        .withLabel("Contact " + ndx).withType(channelType), channels);
                addIfChannelAbsent(ChannelBuilder.create(new ChannelUID(groupUID, ndx + "-event"), null)
                        .withType(new ChannelTypeUID(BINDING_ID, TRIGGER_CONTACT + (portIndex < 8 ? "" : "Advanced")))
                        .withLabel("Contact " + ndx + " Event").withKind(ChannelKind.TRIGGER), channels);
            }
            case COUNTER -> addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.NUMBER)
                    .withLabel("Counter " + ndx).withType(channelType), channels);
            case RELAY -> addIfChannelAbsent(ChannelBuilder.create(mainChannelUID, CoreItemFactory.SWITCH)
                    .withLabel("Relay " + ndx).withType(channelType), channels);
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
            PortDefinition portDefinition, Instant now) {
        if (portData.isInitialized()) { // Always accept if portData is not initialized
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
        if (thing.getChannel(PortDefinition.asChannelId(port)) instanceof Channel channel) {
            ChannelUID channelUID = channel.getUID();
            String channelId = channelUID.getId();

            if (portDatas.get(channelUID) instanceof PortData portData
                    && channelUID.getGroupId() instanceof String groupId) {
                Instant now = Instant.now();
                Configuration configuration = channel.getConfiguration();
                PortDefinition portDefinition = PortDefinition.fromGroupId(groupId);
                if (ignoreCondition(value, portData, configuration, portDefinition, now)) {
                    logger.trace("Ignore condition met for port '{}' with data '{}'", port, value);
                    return;
                }
                logger.debug("About to update port '{}' with data '{}'", port, value);
                long sinceLastChange = Duration.between(portData.getTimestamp(), now).toMillis();
                State state = switch (portDefinition) {
                    case COUNTER -> new DecimalType(value);
                    case RELAY -> OnOffType.from(value == 1);
                    case ANALOG -> {
                        updateIfLinked(channelId + PROPERTY_SEPARATOR + CHANNEL_VOLTAGE,
                                new QuantityType<>(value * ANALOG_SAMPLING, Units.VOLT));
                        yield new DecimalType(value);
                    }
                    case CONTACT -> {
                        portData.cancelPulsing();
                        DigitalInputConfiguration config = configuration.as(DigitalInputConfiguration.class);

                        if (value == 1) { // CLOSED
                            if (config.longPressTime != 0 && portData.isInitialized()) {
                                jobs.add(scheduler.schedule(() -> {
                                    if (portData.getValue() == 1 && now.equals(portData.getTimestamp())) {
                                        String eventChannelId = "%s-%s".formatted(channelUID.getId(), TRIGGER_CONTACT);
                                        triggerChannel(eventChannelId, EVENT_LONG_PRESS);
                                    }
                                }, config.longPressTime, TimeUnit.MILLISECONDS));
                            } else if (config.pulsePeriod != 0) {
                                portData.setPulsing(scheduler.scheduleWithFixedDelay(() -> {
                                    triggerPushButtonChannel(channel, EVENT_PULSE);
                                }, config.pulsePeriod, config.pulsePeriod, TimeUnit.MILLISECONDS));
                                if (config.pulseTimeout != 0) {
                                    portData.setPulseCanceler(scheduler.schedule(portData::cancelPulsing,
                                            config.pulseTimeout, TimeUnit.MILLISECONDS));
                                }
                            }
                        } else if (portData.isInitialized() && sinceLastChange < config.longPressTime) {
                            triggerPushButtonChannel(channel, EVENT_SHORT_PRESS);
                        }
                        if (portData.isInitialized()) {
                            triggerPushButtonChannel(channel, value == 1 ? EVENT_PRESSED : EVENT_RELEASED);
                        }
                        yield value == 1 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                    }
                };

                updateIfLinked(channelId, state);
                if (portData.isInitialized()) {
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

        if (thing.getChannel(channelUID.getId()) instanceof Channel channel
                && channelUID.getGroupId() instanceof String groupId //
                && command instanceof OnOffType onOffCommand //
                && isValidPortId(channelUID) //
                && PortDefinition.RELAY.equals(PortDefinition.fromGroupId(groupId))
                && deviceConnector instanceof Ipx800DeviceConnector connector) {
            RelayOutputConfiguration config = channel.getConfiguration().as(RelayOutputConfiguration.class);
            connector.setOutput(channelUID.getIdWithoutGroup(), OnOffType.ON.equals(onOffCommand) ? 1 : 0,
                    config.pulse);
        } else {
            logger.debug("Can not handle command '{}' on channel '{}'", command, channelUID);
        }
    }

    private boolean isValidPortId(ChannelUID channelUID) {
        return channelUID.getIdWithoutGroup().chars().allMatch(Character::isDigit);
    }

    public void resetCounter(int counter) {
        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.resetCounter(counter);
        }
    }

    public void reset() {
        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.resetPLC();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(Ipx800Actions.class);
    }
}
