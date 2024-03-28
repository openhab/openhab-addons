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
package org.openhab.binding.emotiva.internal;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BAR;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT1;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME_DB;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_CHANNEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME_DB;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CONNECTION_RETRIES;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.NAME_SOURCES_MAP;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHandler.channelToControlRequest;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHandler.volumeDecibelToPercentage;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHandler.volumePercentageToDecibel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_am;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_fm;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.none;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.STRING;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.NOT_VALID;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.PROTOCOL_V3;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.protocolFromConfig;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_band;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_channel;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import javax.measure.quantity.Frequency;
import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.EmotivaAckDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaMenuNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaPropertyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaSubscriptionResponse;
import org.openhab.binding.emotiva.internal.dto.EmotivaUpdateResponse;
import org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaDataType;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.binding.emotiva.internal.protocol.EmotivaUdpResponse;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmotivaProcessorHandler} is responsible for handling OpenHAB commands, which are
 * sent to one of the channels.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaProcessorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EmotivaProcessorHandler.class);

    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    private final EmotivaTranslationProvider messages;
    private final EmotivaConfiguration config;

    private final EmotivaCommandHandler commandHandler;

    /**
     * Emotiva devices have trouble with too many subscriptions in same request, so subscriptions are dividing into
     * those general group channels, and the rest.
     */
    private final EmotivaSubscriptionTags[] generalSubscription = EmotivaSubscriptionTags.generalChannels();
    private final EmotivaSubscriptionTags[] nonGeneralSubscriptions = EmotivaSubscriptionTags.nonGeneralChannels();

    private final EnumMap<EmotivaControlCommands, String> sources;
    private final EnumMap<EmotivaControlCommands, String> channels;
    private final EnumMap<EmotivaControlCommands, String> bands;
    private final EnumMap<EmotivaSubscriptionTags, String> modes;
    private final Map<String, Map<EmotivaControlCommands, String>> commandMaps = new ConcurrentHashMap<>();

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> subscriptionJob;
    private @Nullable EmotivaUdpConnector udpConnector;

    private final EmotivaXmlUtils xmlUtils = new EmotivaXmlUtils();

    private boolean active = false;

    public EmotivaProcessorHandler(Thing thing, EmotivaTranslationProvider i18nProvider) throws JAXBException {
        super(thing);
        this.messages = i18nProvider;
        this.config = getConfigAs(EmotivaConfiguration.class);
        this.commandHandler = new EmotivaCommandHandler(config);
        sources = new EnumMap<>(EmotivaControlCommands.class);
        channels = new EnumMap<>(Map.ofEntries(Map.entry(EmotivaControlCommands.channel_1, channel_1.getLabel()),
                Map.entry(EmotivaControlCommands.channel_2, EmotivaControlCommands.channel_2.getLabel()),
                Map.entry(EmotivaControlCommands.channel_3, EmotivaControlCommands.channel_3.getLabel()),
                Map.entry(EmotivaControlCommands.channel_4, EmotivaControlCommands.channel_4.getLabel()),
                Map.entry(EmotivaControlCommands.channel_5, EmotivaControlCommands.channel_5.getLabel()),
                Map.entry(EmotivaControlCommands.channel_6, EmotivaControlCommands.channel_6.getLabel()),
                Map.entry(EmotivaControlCommands.channel_7, EmotivaControlCommands.channel_7.getLabel()),
                Map.entry(EmotivaControlCommands.channel_8, EmotivaControlCommands.channel_8.getLabel()),
                Map.entry(EmotivaControlCommands.channel_9, EmotivaControlCommands.channel_9.getLabel()),
                Map.entry(EmotivaControlCommands.channel_10, EmotivaControlCommands.channel_10.getLabel()),
                Map.entry(EmotivaControlCommands.channel_11, EmotivaControlCommands.channel_11.getLabel()),
                Map.entry(EmotivaControlCommands.channel_12, EmotivaControlCommands.channel_12.getLabel()),
                Map.entry(EmotivaControlCommands.channel_13, EmotivaControlCommands.channel_13.getLabel()),
                Map.entry(EmotivaControlCommands.channel_14, EmotivaControlCommands.channel_14.getLabel()),
                Map.entry(EmotivaControlCommands.channel_15, EmotivaControlCommands.channel_15.getLabel()),
                Map.entry(EmotivaControlCommands.channel_16, EmotivaControlCommands.channel_16.getLabel()),
                Map.entry(EmotivaControlCommands.channel_17, EmotivaControlCommands.channel_17.getLabel()),
                Map.entry(EmotivaControlCommands.channel_18, EmotivaControlCommands.channel_18.getLabel()),
                Map.entry(EmotivaControlCommands.channel_19, EmotivaControlCommands.channel_19.getLabel()),
                Map.entry(EmotivaControlCommands.channel_20, EmotivaControlCommands.channel_20.getLabel())));
        bands = new EnumMap<>(Map.of(band_am, band_am.getLabel(), band_fm, band_fm.getLabel()));
        modes = new EnumMap<>(EmotivaSubscriptionTags.class);
        commandMaps.put(NAME_SOURCES_MAP, sources);
        commandMaps.put(tuner_channel.getName(), channels);
        commandMaps.put(tuner_band.getName(), bands);
    }

    @Override
    public void initialize() {
        logger.info("initialize: {}", getThing().getUID());

        // background initialization
        scheduler.execute(this::initializeConnection);
    }

    private void initializeConnection() {
        final EmotivaConfiguration localConfig = config;
        try {
            final EmotivaUdpConnector newUdpConnector = new EmotivaUdpConnector(localConfig, scheduler);
            udpConnector = newUdpConnector;

            // establish connection and register listener
            newUdpConnector.connect(this::handleStatusUpdate, true);

            // request initial state, 3 attempts
            for (int attempt = 1; attempt <= CONNECTION_RETRIES && !active; attempt++) {
                try {
                    logger.info("Attempt {}", attempt);
                    newUdpConnector.sendSubscription(generalSubscription, config);
                    newUdpConnector.sendSubscription(nonGeneralSubscriptions, config);
                    // TODO Request bar_update for volume, will give user specified max volume

                } catch (IOException e) {
                    // network or socket failure, also wait 2 sec and try again
                }

                // answer expected within 50-600ms on a regular network; wait up to 2sec just to make sure
                for (int delay = 0; delay < 10 && !active; delay++) {
                    Thread.sleep(200); // wait 10 x 200ms = 2sec
                }
            }
            updateStatus(ThingStatus.ONLINE);

        } catch (InterruptedException e) {
            // OH shutdown - don't log anything, Framework will call dispose()
        } catch (Exception e) {
            logger.info("Connection to '{}' failed", localConfig, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "@text/message.processor.connection.failed");
            dispose();
        }
    }

    private void handleStatusUpdate(EmotivaUdpResponse emotivaUdpResponse) {
        active = true;
        logger.debug("Received data from {} with length {}", emotivaUdpResponse.ipAddress(),
                emotivaUdpResponse.answer().length());

        Object object;
        try {
            object = xmlUtils.unmarshallToEmotivaDTO(emotivaUdpResponse.answer());
        } catch (JAXBException e) {
            logger.info("Could not unmarshal answer from {} with length {} and content {}",
                    emotivaUdpResponse.ipAddress(), emotivaUdpResponse.answer().length(), emotivaUdpResponse.answer());
            return;
        }

        if (object instanceof EmotivaAckDTO answerDto) {
            logger.info("Processing received {} with {} ", EmotivaAckDTO.class.getSimpleName(),
                    emotivaUdpResponse.answer());
            // TODO: Maybe revert channel state change if answer is "nak"
        } else if (object instanceof EmotivaBarNotifyWrapper answerDto) {
            logger.info("Processing received {} with {} ", EmotivaBarNotifyWrapper.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            List<EmotivaBarNotifyDTO> emotivaBarNotifies = xmlUtils.unmarshallToBarNotify(answerDto.getTags());

            if (!emotivaBarNotifies.isEmpty()) {
                if (emotivaBarNotifies.get(0).getType() != null) {
                    findChannelDatatypeAndUpdateChannel(CHANNEL_BAR, emotivaBarNotifies.get(0).formattedMessage(),
                            STRING);
                }
            }

        } else if (object instanceof EmotivaUpdateResponse answerDto) {
            logger.info("Processing received {} with {} ", EmotivaUpdateResponse.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                handleChannel(property.getName(), property.getValue(), property.getVisible(), property.getStatus());
            }

        } else if (object instanceof EmotivaMenuNotifyDTO answerDto) {
            logger.info("Processing received {} with {} ", EmotivaMenuNotifyDTO.class.getSimpleName(),
                    emotivaUdpResponse.answer());
            // TODO: Add support for menuNotify
        } else if (object instanceof EmotivaSubscriptionResponse answerDto) {
            logger.info("Processing received {} with {} ", EmotivaSubscriptionResponse.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            // Populates static input sources, except input
            sources.putAll(EmotivaControlCommands.getCommandsFromType(EmotivaCommandType.SOURCE));
            sources.remove(EmotivaControlCommands.input);
            commandMaps.put(NAME_SOURCES_MAP, sources);

            if (answerDto.getProperties() == null) {
                for (EmotivaNotifyDTO dto : xmlUtils.unmarshallToNotification(answerDto.getTags())) {
                    handleChannel(dto.getName(), dto.getValue(), dto.getVisible(), dto.getAck());
                }
            } else {
                for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                    handleChannel(property.getName(), property.getValue(), property.getVisible(), property.getStatus());
                }
            }

        } else if (object instanceof EmotivaNotifyWrapper answerDto) {
            logger.debug("Processing received {} with {} ", EmotivaNotifyWrapper.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            if (answerDto.getProperties() == null) {
                for (EmotivaNotifyDTO tag : xmlUtils.unmarshallToNotification(answerDto.getTags())) {
                    try {
                        EmotivaSubscriptionTags tagName = EmotivaSubscriptionTags.valueOf(tag.getName());
                        if (EmotivaSubscriptionTags.hasChannel(tag.getName())) {
                            findChannelDatatypeAndUpdateChannel(tagName.getChannel(), tag.getValue(),
                                    tagName.getDataType());
                        }
                    } catch (IllegalArgumentException e) {
                        logger.debug("Subscription name {} could not be mapped to a channel", tag.getName());
                    }
                }
            } else {
                for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                    handleChannel(property.getName(), property.getValue(), property.getVisible(), property.getStatus());
                }
            }
        }
    }

    private void handleChannel(String name, String value, String visible, String status) {

        if (Objects.nonNull(status) && status.equals(NOT_VALID.name())) {
            logger.debug("Subscription property {} not present in device, skipping", name);
            return;
        }

        if (Objects.nonNull(value) && "None".equals(value)) {
            logger.debug("No value present for channel {}, usually means a speaker is not enabled", name);
            return;
        }

        try {
            EmotivaSubscriptionTags.hasChannel(name);
        } catch (IllegalArgumentException e) {
            logger.debug("Subscription property {} is not know to the binding, might need updating", name);
            return;
        }

        try {
            EmotivaSubscriptionTags subscriptionTag;
            try {
                subscriptionTag = EmotivaSubscriptionTags.valueOf(name);
            } catch (IllegalArgumentException e) {
                logger.info("Property {} could not be mapped subscription tag, skipping", name);
                return;
            }

            if (subscriptionTag.getChannel().isEmpty()) {
                logger.info("Subscription property {} does not have a corresponding channel, skipping", name);
                return;
            }

            // Add/Update user assigned name for inputs
            if (subscriptionTag.getChannel().startsWith(CHANNEL_INPUT1.substring(0, CHANNEL_INPUT1.indexOf("_") + 1))
                    && "true".equals(visible)) {
                logger.debug("Adding {} to dynamic source input list", value);
                sources.put(EmotivaControlCommands.matchToInput(subscriptionTag.name()), value);
                commandMaps.put(NAME_SOURCES_MAP, sources);
            }

            // Add/Update audio modes
            if (subscriptionTag.getChannel().startsWith(CHANNEL_MODE + "_") && "true".equals(visible)) {
                logger.debug("Adding {} to dynamic mode list", value);
                modes.put(EmotivaSubscriptionTags.fromChannelUID(subscriptionTag.getChannel()), value);
            }

            findChannelDatatypeAndUpdateChannel(subscriptionTag.getChannel(), value, subscriptionTag.getDataType());
        } catch (IllegalArgumentException e) {
            logger.info("Error updating subscription property {}", name, e);
        }
    }

    private void findChannelDatatypeAndUpdateChannel(String channelName, String value, EmotivaDataType dataType) {
        switch (dataType) {
            case ON_OFF -> {
                logger.info("Update channel {} to {}:{}", channelName, OnOffType.class.getSimpleName(), value);
                updateChannelState(channelName, OnOffType.from(value.trim().toUpperCase()));
            }
            case STRING -> {
                logger.debug("Update channel {} to {}:{}", channelName, StringType.class.getSimpleName(), value);
                updateChannelState(channelName, StringType.valueOf(value));
            }
            case DIMENSIONLESS_PERCENT -> {
                var trimmedString = value.replaceAll("[ +]", "");
                logger.debug("Update channel {} to {}:{}", channelName, PercentType.class.getSimpleName(), value);
                updateChannelState(channelName, PercentType.valueOf(trimmedString));
            }
            case DIMENSIONLESS_DECIBEL -> {
                var trimmedString = value.replaceAll("[ +]", "");
                logger.debug("Update channel {} to {}:{}", channelName, QuantityType.class.getSimpleName(),
                        trimmedString);
                if (channelName.equals(CHANNEL_MAIN_VOLUME)) {
                    updateVolumeChannels(trimmedString, CHANNEL_MUTE, channelName, CHANNEL_MAIN_VOLUME_DB);
                } else if (channelName.equals(CHANNEL_ZONE2_VOLUME)) {
                    updateVolumeChannels(trimmedString, CHANNEL_ZONE2_MUTE, channelName, CHANNEL_ZONE2_VOLUME_DB);
                } else {
                    if (trimmedString.equals("None")) {
                        updateChannelState(channelName, QuantityType.valueOf(0, Units.DECIBEL));
                    } else {
                        updateChannelState(channelName,
                                QuantityType.valueOf(Double.parseDouble(trimmedString), Units.DECIBEL));
                    }
                }
            }
            case FREQUENCY_HERTZ -> {
                logger.debug("Update channel {} to {}:{}", channelName, Units.HERTZ.getClass().getSimpleName(), value);
                if (!value.isEmpty()) {
                    // Getting rid of characters and empty space leaves us with the raw frequency
                    try {
                        String frequencyString = value.replaceAll("[a-zA-Z ]", "");
                        QuantityType<Frequency> hz = QuantityType.valueOf(0, Units.HERTZ);
                        if (value.contains("AM")) {
                            hz = QuantityType.valueOf(Double.parseDouble(frequencyString) * 1000, Units.HERTZ);
                        } else if (value.contains("FM")) {
                            hz = QuantityType.valueOf(Double.parseDouble(frequencyString) * 1000000, Units.HERTZ);
                        }
                        updateChannelState(CHANNEL_TUNER_CHANNEL, hz);
                    } catch (NumberFormatException e) {
                        logger.info("Could not extract radio tuner frequency from {}", value);
                    }
                }
            }
            case UNKNOWN -> // Do nothing, types not connect to channels
                logger.info("Channel {} with UNKNOWN type and value {} was not updated", channelName, value);
        }
    }

    private void updateChannelState(String channelID, State state) {
        stateMap.put(channelID, state);
        // TODO: Consider checking previous to new value
        updateState(channelID, state);
    }

    private void updateVolumeChannels(String value, String muteChannel, String volumeChannel, String volumeDbChannel) {
        if ("Mute".equals(value)) {
            updateChannelState(muteChannel, OnOffType.ON);
        } else {
            updateChannelState(volumeChannel, volumeDecibelToPercentage(value));
            updateChannelState(volumeDbChannel, QuantityType.valueOf(Double.parseDouble(value), Units.DECIBEL));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command ohCommand) {
        logger.info("Handling ohCommand {}:{} for {}", channelUID.getId(), ohCommand, channelUID.getThingUID());

        if (udpConnector != null) {
            EmotivaControlRequest emotivaRequest = channelToControlRequest(channelUID.getId(), commandMaps,
                    protocolFromConfig(config.protocolVersion));
            if (ohCommand instanceof RefreshType) {
                stateMap.remove(channelUID.getId());

                try {
                    if (emotivaRequest.getDefaultCommand().equals(none)) {
                        logger.info("Found controlCommand 'none' for request {} from channel {} with RefreshType",
                                emotivaRequest.getName(), channelUID);
                    } else {
                        if (config.protocolVersion.equals(PROTOCOL_V3.value())) {
                            logger.info("Sending update for {}", emotivaRequest);
                            udpConnector.sendUpdate(emotivaRequest.getDefaultCommand(), config);
                        } else {
                            udpConnector.sendSubscription(emotivaRequest.getDefaultCommand(), config);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to send Subscription message to device {}", this.getThing().getThingTypeUID(),
                            e);
                }
            } else {
                try {
                    EmotivaControlDTO dto = emotivaRequest.createDTO(ohCommand, stateMap.get(channelUID.getId()));
                    udpConnector.send(dto);

                    if (emotivaRequest.getName().equals(EmotivaControlCommands.volume.name())) {
                        if (ohCommand instanceof PercentType value) {
                            updateChannelState(CHANNEL_MAIN_VOLUME_DB,
                                    QuantityType.valueOf(volumePercentageToDecibel(value.intValue()), Units.DECIBEL));
                        } else if (ohCommand instanceof QuantityType<?> value) {
                            updateChannelState(CHANNEL_MAIN_VOLUME, volumeDecibelToPercentage(value.toString()));
                        }
                    } else if (emotivaRequest.getName().equals(EmotivaControlCommands.zone2_volume.name())) {
                        if (ohCommand instanceof PercentType value) {
                            updateChannelState(CHANNEL_ZONE2_VOLUME_DB,
                                    QuantityType.valueOf(volumePercentageToDecibel(value.intValue()), Units.DECIBEL));
                        } else if (ohCommand instanceof QuantityType<?> value) {
                            updateChannelState(CHANNEL_ZONE2_VOLUME, volumeDecibelToPercentage(value.toString()));
                        }
                    } else if (ohCommand instanceof OnOffType value) {
                        if (value.equals(OnOffType.ON) && emotivaRequest.getOnCommand().equals(power_on)) {
                            udpConnector.sendUpdate(EmotivaSubscriptionTags.speakerChannels(), config);
                        }
                    }

                } catch (IOException e) {
                    logger.error("Failed updating state for channel {}:{}:{}", channelUID.getId(),
                            emotivaRequest.getName(), emotivaRequest.getDataType(), e);
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.info("Disposing {}", getThing().getUID());
        super.dispose();
        active = false;

        final EmotivaUdpConnector connector = udpConnector;
        if (connector != null) {

            try {
                // Unsubscribe before disconnect
                udpConnector.sendUnsubscribe(generalSubscription);
                udpConnector.sendUnsubscribe(nonGeneralSubscriptions);
            } catch (IOException e) {
                logger.info("Failed to unsubscribe for: {}", config, e);
            }

            udpConnector = null;
            try {
                connector.disconnect();
            } catch (Exception e) {
                logger.debug("Failed to close socket connection for: {}", config, e);
            }
        }

        ScheduledFuture<?> localSubscriptionJob = this.subscriptionJob;
        if (localSubscriptionJob != null) {
            localSubscriptionJob.cancel(true);
            this.subscriptionJob = null;
        }
        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(InputStateOptionProvider.class);
    }

    public EnumMap<EmotivaControlCommands, String> getSources() {
        return sources;
    }

    public EnumMap<EmotivaSubscriptionTags, String> getModes() {
        return modes;
    }
}
