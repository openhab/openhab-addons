/**
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.openhab.core.types.UnDefType;
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
    private final Map<String, PortData> portDatas = new HashMap<>();

    private @Nullable Ipx800DeviceConnector deviceConnector;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    private class LongPressEvaluator implements Runnable {
        private final Instant referenceTime;
        private final String port;
        private final String eventChannelId;

        public LongPressEvaluator(Channel channel, String port, PortData portData) {
            this.referenceTime = portData.getTimestamp();
            this.port = port;
            this.eventChannelId = "%s-%s".formatted(channel.getUID().getId(), TRIGGER_CONTACT);
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
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IPX800 handler for uid '{}'", getThing().getUID());

        Ipx800Configuration config = getConfigAs(Ipx800Configuration.class);

        deviceConnector = new Ipx800DeviceConnector(config.hostname, config.portNumber, getThing().getUID(), this);

        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::readStatusFile, 1500, config.pullInterval,
                TimeUnit.MILLISECONDS));
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

            if (status != null) {
                for (PortDefinition portDefinition : PortDefinition.values()) {
                    status.getMatchingNodes(portDefinition.nodeName).forEach(node -> {
                        String sPortNum = node.getNodeName().replace(portDefinition.nodeName, "");
                        try {
                            int portNum = Integer.parseInt(sPortNum) + 1;
                            double value = Double
                                    .parseDouble(node.getTextContent().replace("dn", "1").replace("up", "0"));
                            dataReceived("%s%d".formatted(portDefinition.portName, portNum), value);
                        } catch (NumberFormatException e) {
                            logger.warn(e.getMessage());
                        }
                    });
                }
            }

        }
    }

    private void updateChannels(@Nullable StatusFile status) {
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        PortDefinition.AS_SET.forEach(portDefinition -> {
            int nbElements = status != null ? status.getMaxNumberofNodeType(portDefinition) : portDefinition.quantity;
            for (int i = 0; i < nbElements; i++) {
                ChannelUID portChannelUID = createChannels(portDefinition, i, channels);
                portDatas.put(portChannelUID.getId(), new PortData());
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
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();

        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.dispose();
            connector = null;
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
            PortDefinition portDefinition, Instant now) {
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
                Instant now = Instant.now();
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
                        state = OnOffType.from(value == 1);
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
        if (command instanceof OnOffType onOffCommand && isValidPortId(channelUID)
                && PortDefinition.fromGroupId(groupId) == PortDefinition.RELAY
                && deviceConnector instanceof Ipx800DeviceConnector connector) {
            RelayOutputConfiguration config = channel.getConfiguration().as(RelayOutputConfiguration.class);
            String id = channelUID.getIdWithoutGroup();
            connector.getParser().setOutput(id, onOffCommand == OnOffType.ON ? 1 : 0, config.pulse);
            return;
        }
        logger.debug("Can not handle command '{}' on channel '{}'", command, channelUID);
    }

    private boolean isValidPortId(ChannelUID channelUID) {
        return channelUID.getIdWithoutGroup().chars().allMatch(Character::isDigit);
    }

    public void resetCounter(int counter) {
        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.getParser().resetCounter(counter);
        }
    }

    public void reset() {
        if (deviceConnector instanceof Ipx800DeviceConnector connector) {
            connector.getParser().resetPLC();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(Ipx800Actions.class);
    }
}
