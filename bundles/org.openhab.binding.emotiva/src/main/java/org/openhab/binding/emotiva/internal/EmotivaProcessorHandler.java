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

import static java.lang.String.format;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.channelToControlRequest;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.getMenuPanelColumnLabel;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.getMenuPanelRowLabel;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.updateProgress;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumeDecibelToPercentage;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumePercentageToDecibel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_am;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_fm;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_1;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.none;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.STRING;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.NOT_VALID;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.protocolFromConfig;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.noSubscriptionToChannel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_band;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_channel;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Frequency;
import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.AbstractNotificationDTO;
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
import org.openhab.core.common.NamedThreadFactory;
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
 * The EmotivaProcessorHandler is responsible for handling OpenHAB commands, which are
 * sent to one of the channels.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaProcessorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EmotivaProcessorHandler.class);

    private final Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    private final EmotivaConfiguration config;

    /**
     * Emotiva devices have trouble with too many subscriptions in same request, so subscriptions are dividing into
     * those general group channels, and the rest.
     */
    private final EmotivaSubscriptionTags[] generalSubscription = EmotivaSubscriptionTags.generalChannels();
    private final EmotivaSubscriptionTags[] nonGeneralSubscriptions = EmotivaSubscriptionTags.nonGeneralChannels();

    private final EnumMap<EmotivaControlCommands, String> sourcesMainZone;
    private final EnumMap<EmotivaControlCommands, String> sourcesZone2;
    private final EnumMap<EmotivaSubscriptionTags, String> modes;
    private final Map<String, Map<EmotivaControlCommands, String>> commandMaps = new ConcurrentHashMap<>();
    private final EmotivaTranslationProvider i18nProvider;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private @Nullable EmotivaUdpSendingService sendingService;
    private @Nullable EmotivaUdpReceivingService notifyListener;
    private @Nullable EmotivaUdpReceivingService menuNotifyListener;

    private final int retryConnectInMinutes;

    /**
     * Thread factory for menu progress bar
     */
    private final NamedThreadFactory listeningThreadFactory = new NamedThreadFactory(BINDING_ID, true);

    private final EmotivaXmlUtils xmlUtils = new EmotivaXmlUtils();

    private boolean udpSenderActive = false;

    public EmotivaProcessorHandler(Thing thing, EmotivaTranslationProvider i18nProvider) throws JAXBException {
        super(thing);
        this.i18nProvider = i18nProvider;
        this.config = getConfigAs(EmotivaConfiguration.class);
        this.retryConnectInMinutes = config.retryConnectInMinutes;

        sourcesMainZone = new EnumMap<>(EmotivaControlCommands.class);
        commandMaps.put(MAP_SOURCES_MAIN_ZONE, sourcesMainZone);

        sourcesZone2 = new EnumMap<>(EmotivaControlCommands.class);
        commandMaps.put(MAP_SOURCES_ZONE_2, sourcesZone2);

        EnumMap<EmotivaControlCommands, String> channels = new EnumMap<>(
                Map.ofEntries(Map.entry(channel_1, channel_1.getLabel()),
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
        commandMaps.put(tuner_channel.getEmotivaName(), channels);

        EnumMap<EmotivaControlCommands, String> bands = new EnumMap<>(
                Map.of(band_am, band_am.getLabel(), band_fm, band_fm.getLabel()));
        commandMaps.put(tuner_band.getEmotivaName(), bands);

        modes = new EnumMap<>(EmotivaSubscriptionTags.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize: '{}'", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/message.processor.connecting");
        if (config.controlPort < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/message.processor.connection.error.port");
            return;
        }
        if (config.ipAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/message.processor.connection.error.address-empty");
            return;
        } else {
            try {
                // noinspection ResultOfMethodCallIgnored
                InetAddress.getByName(config.ipAddress);
            } catch (UnknownHostException ignored) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/message.processor.connection.error.address-invalid");
                return;
            }
        }

        scheduler.execute(this::connect);
    }

    private synchronized void connect() {
        final EmotivaConfiguration localConfig = config;
        try {
            final EmotivaUdpReceivingService notifyListener = new EmotivaUdpReceivingService(localConfig.notifyPort,
                    localConfig, scheduler);
            this.notifyListener = notifyListener;
            notifyListener.connect(this::handleStatusUpdate, true);

            final EmotivaUdpSendingService sendConnector = new EmotivaUdpSendingService(localConfig, scheduler);
            sendingService = sendConnector;
            sendConnector.connect(this::handleStatusUpdate, true);

            // Simple retry mechanism to handle minor network issues, if this fails a retry job is created
            for (int attempt = 1; attempt <= DEFAULT_CONNECTION_RETRIES && !udpSenderActive; attempt++) {
                try {
                    logger.debug("Connection attempt '{}'", attempt);
                    sendConnector.sendSubscription(generalSubscription, config);
                    sendConnector.sendSubscription(nonGeneralSubscriptions, config);
                } catch (IOException e) {
                    // network or socket failure, also wait 2 sec and try again
                }

                for (int delay = 0; delay < 10 && !udpSenderActive; delay++) {
                    Thread.sleep(200); // wait 10 x 200ms = 2sec
                }
            }

            if (udpSenderActive) {
                updateStatus(ThingStatus.ONLINE);

                final EmotivaUdpReceivingService menuListenerConnector = new EmotivaUdpReceivingService(
                        localConfig.menuNotifyPort, localConfig, scheduler);
                this.menuNotifyListener = menuListenerConnector;
                menuListenerConnector.connect(this::handleStatusUpdate, true);

                startPollingKeepAlive();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "@text/message.processor.connection.failed");
                disconnect();
                scheduleConnectRetry(retryConnectInMinutes);
            }
        } catch (InterruptedException e) {
            // OH shutdown - don't log anything, Framework will call dispose()
        } catch (Exception e) {
            logger.error("Connection to '{}' failed", localConfig.ipAddress, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "@text/message.processor.connection.failed");
            disconnect();
            scheduleConnectRetry(retryConnectInMinutes);
        }
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in '{}' minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    /**
     * Starts a polling job for connection to th device, adds the
     * {@link EmotivaBindingConstants#DEFAULT_KEEP_ALIVE_IN_MILLISECONDS} as a time buffer for checking, to avoid
     * flapping state or minor network issues.
     */
    private void startPollingKeepAlive() {
        final ScheduledFuture<?> localRefreshJob = this.pollingJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            logger.debug("Start polling");

            int delay = stateMap.get(EmotivaSubscriptionTags.keepAlive.name()) != null
                    && stateMap.get(EmotivaSubscriptionTags.keepAlive.name()) instanceof Number keepAlive
                            ? keepAlive.intValue()
                            : config.keepAlive;
            pollingJob = scheduler.scheduleWithFixedDelay(this::checkKeepAliveTimestamp,
                    delay + DEFAULT_KEEP_ALIVE_IN_MILLISECONDS, delay + DEFAULT_KEEP_ALIVE_IN_MILLISECONDS,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void checkKeepAliveTimestamp() {
        if (ThingStatus.ONLINE.equals(getThing().getStatusInfo().getStatus())) {
            State state = stateMap.get(LAST_SEEN_STATE_NAME);
            if (state instanceof Number value) {
                Instant lastKeepAliveMessageTimestamp = Instant.ofEpochSecond(value.longValue());
                Instant deviceGoneGracePeriod = Instant.now().minus(config.keepAlive, ChronoUnit.MILLIS)
                        .minus(DEFAULT_KEEP_ALIVE_CONSIDERED_LOST_IN_MILLISECONDS, ChronoUnit.MILLIS);
                if (lastKeepAliveMessageTimestamp.isBefore(deviceGoneGracePeriod)) {
                    logger.debug(
                            "Last KeepAlive message received '{}', over grace-period by '{}', consider '{}' gone, setting OFFLINE and disposing",
                            lastKeepAliveMessageTimestamp,
                            Duration.between(lastKeepAliveMessageTimestamp, deviceGoneGracePeriod),
                            thing.getThingTypeUID());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/message.processor.connection.error.keep-alive");
                    // Connection lost, avoid sending unsubscription messages
                    udpSenderActive = false;
                    disconnect();
                    scheduleConnectRetry(retryConnectInMinutes);
                }
            }
        } else if (ThingStatus.OFFLINE.equals(getThing().getStatusInfo().getStatus())) {
            logger.debug("Keep alive pool job, '{}' is '{}'", getThing().getThingTypeUID(),
                    getThing().getStatusInfo().getStatus());
        }
    }

    private void handleStatusUpdate(EmotivaUdpResponse emotivaUdpResponse) {
        udpSenderActive = true;
        logger.debug("Received data from '{}' with length '{}'", emotivaUdpResponse.ipAddress(),
                emotivaUdpResponse.answer().length());

        Object object;
        try {
            object = xmlUtils.unmarshallToEmotivaDTO(emotivaUdpResponse.answer());
        } catch (JAXBException e) {
            logger.debug("Could not unmarshal answer from '{}' with length '{}' and content '{}'",
                    emotivaUdpResponse.ipAddress(), emotivaUdpResponse.answer().length(), emotivaUdpResponse.answer(),
                    e);
            return;
        }

        if (object instanceof EmotivaAckDTO answerDto) {
            // Currently not supported to revert a failed command update, just used for logging for now.
            logger.trace("Processing received '{}' with '{}'", EmotivaAckDTO.class.getSimpleName(), answerDto);

        } else if (object instanceof EmotivaBarNotifyWrapper answerDto) {
            logger.trace("Processing received '{}' with '{}'", EmotivaBarNotifyWrapper.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            List<EmotivaBarNotifyDTO> emotivaBarNotifies = xmlUtils.unmarshallToBarNotify(answerDto.getTags());

            if (!emotivaBarNotifies.isEmpty()) {
                if (emotivaBarNotifies.get(0).getType() != null) {
                    findChannelDatatypeAndUpdateChannel(CHANNEL_BAR, emotivaBarNotifies.get(0).formattedMessage(),
                            STRING);
                }
            }
        } else if (object instanceof EmotivaNotifyWrapper answerDto) {
            logger.trace("Processing received '{}' with '{}'", EmotivaNotifyWrapper.class.getSimpleName(),
                    emotivaUdpResponse.answer());
            handleNotificationUpdate(answerDto);
        } else if (object instanceof EmotivaUpdateResponse answerDto) {
            logger.trace("Processing received '{}' with '{}'", EmotivaUpdateResponse.class.getSimpleName(),
                    emotivaUdpResponse.answer());
            handleNotificationUpdate(answerDto);
        } else if (object instanceof EmotivaMenuNotifyDTO answerDto) {
            logger.trace("Processing received '{}' with '{}'", EmotivaMenuNotifyDTO.class.getSimpleName(),
                    emotivaUdpResponse.answer());

            if (answerDto.getRow() != null) {
                handleMenuNotify(answerDto);
            } else if (answerDto.getProgress() != null && answerDto.getProgress().getTime() != null) {
                logger.trace("Processing received '{}' with '{}'", EmotivaMenuNotifyDTO.class.getSimpleName(),
                        emotivaUdpResponse.answer());
                listeningThreadFactory
                        .newThread(() -> handleMenuNotifyProgressMessage(answerDto.getProgress().getTime())).start();
            }
        } else if (object instanceof EmotivaSubscriptionResponse answerDto) {
            logger.trace("Processing received '{}' with '{}'", EmotivaSubscriptionResponse.class.getSimpleName(),
                    emotivaUdpResponse.answer());
            // Populates static input sources, except input
            sourcesMainZone.putAll(EmotivaControlCommands.getCommandsFromType(EmotivaCommandType.SOURCE_MAIN_ZONE));
            sourcesMainZone.remove(EmotivaControlCommands.input);
            commandMaps.put(MAP_SOURCES_MAIN_ZONE, sourcesMainZone);

            sourcesZone2.putAll(EmotivaControlCommands.getCommandsFromType(EmotivaCommandType.SOURCE_ZONE2));
            sourcesZone2.remove(EmotivaControlCommands.zone2_input);
            commandMaps.put(MAP_SOURCES_ZONE_2, sourcesZone2);

            if (answerDto.getProperties() == null) {
                for (EmotivaNotifyDTO dto : xmlUtils.unmarshallToNotification(answerDto.getTags())) {
                    handleChannelUpdate(dto.getName(), dto.getValue(), dto.getVisible(), dto.getAck());
                }
            } else {
                for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                    handleChannelUpdate(property.getName(), property.getValue(), property.getVisible(),
                            property.getStatus());
                }
            }
        }
    }

    private void handleMenuNotify(EmotivaMenuNotifyDTO answerDto) {
        String highlightValue = "";

        for (var row = 4; row <= 6; row++) {
            var emotivaMenuRow = answerDto.getRow().get(row);
            logger.debug("Checking row '{}' with '{}' columns", row, emotivaMenuRow.getCol().size());
            for (var column = 0; column <= 2; column++) {
                var emotivaMenuCol = emotivaMenuRow.getCol().get(column);
                String cellValue = "";
                if (emotivaMenuCol.getValue() != null) {
                    cellValue = emotivaMenuCol.getValue();
                }

                if (emotivaMenuCol.getCheckbox() != null) {
                    cellValue = MENU_PANEL_CHECKBOX_ON.equalsIgnoreCase(emotivaMenuCol.getCheckbox().trim()) ? "☑"
                            : "☐";
                }

                if (emotivaMenuCol.getHighlight() != null
                        && MENU_PANEL_HIGHLIGHTED.equalsIgnoreCase(emotivaMenuCol.getHighlight().trim())) {
                    logger.debug("Highlight is at row '{}' column '{}' value '{}'", row, column, cellValue);
                    highlightValue = cellValue;
                }

                var channelName = format("%s-%s-%s", CHANNEL_MENU_DISPLAY_PREFIX, getMenuPanelRowLabel(row),
                        getMenuPanelColumnLabel(column));
                updateChannelState(channelName, new StringType(cellValue));
            }
        }
        updateChannelState(CHANNEL_MENU_DISPLAY_HIGHLIGHT, new StringType(highlightValue));
    }

    private void handleMenuNotifyProgressMessage(String progressBarTimeInSeconds) {
        try {
            var seconds = Integer.parseInt(progressBarTimeInSeconds);
            for (var count = 0; seconds >= count; count++) {
                updateChannelState(CHANNEL_MENU_DISPLAY_HIGHLIGHT,
                        new StringType(updateProgress(EmotivaCommandHelper.integerToPercentage(count))));
            }
        } catch (NumberFormatException e) {
            logger.debug("Menu progress bar time value '{}' is not a valid integer", progressBarTimeInSeconds);
        }
    }

    private void resetMenuPanelChannels() {
        logger.debug("Resetting Menu Panel Display");
        for (var row = 4; row <= 6; row++) {
            for (var column = 0; column <= 2; column++) {
                var channelName = format("%s-%s-%s", CHANNEL_MENU_DISPLAY_PREFIX, getMenuPanelRowLabel(row),
                        getMenuPanelColumnLabel(column));
                updateChannelState(channelName, new StringType(""));
            }
        }
        updateChannelState(CHANNEL_MENU_DISPLAY_HIGHLIGHT, new StringType(""));
    }

    private void sendEmotivaUpdate(EmotivaControlCommands tags) {
        EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            try {
                localSendingService.sendUpdate(tags, config);
            } catch (InterruptedIOException e) {
                logger.debug("Interrupted during sending of EmotivaUpdate message to device '{}'",
                        this.getThing().getThingTypeUID(), e);
            } catch (IOException e) {
                logger.error("Failed to send EmotivaUpdate message to device '{}'", this.getThing().getThingTypeUID(),
                        e);
            }
        }
    }

    private void handleNotificationUpdate(AbstractNotificationDTO answerDto) {
        if (answerDto.getProperties() == null) {
            for (EmotivaNotifyDTO tag : xmlUtils.unmarshallToNotification(answerDto.getTags())) {
                try {
                    EmotivaSubscriptionTags tagName = EmotivaSubscriptionTags.valueOf(tag.getName());
                    if (EmotivaSubscriptionTags.hasChannel(tag.getName())) {
                        findChannelDatatypeAndUpdateChannel(tagName.getChannel(), tag.getValue(),
                                tagName.getDataType());
                    }
                } catch (IllegalArgumentException e) {
                    logger.debug("Subscription name '{}' could not be mapped to a channel", tag.getName());
                }
            }
        } else {
            for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                handleChannelUpdate(property.getName(), property.getValue(), property.getVisible(),
                        property.getStatus());
            }
        }
    }

    private void handleChannelUpdate(String emotivaSubscriptionName, String value, String visible, String status) {
        logger.debug("Handling channel update for '{}' with value '{}'", emotivaSubscriptionName, value);

        if (status.equals(NOT_VALID.name())) {
            logger.debug("Subscription property '{}' not present in device, skipping", emotivaSubscriptionName);
            return;
        }

        if ("None".equals(value)) {
            logger.debug("No value present for channel {}, usually means a speaker is not enabled",
                    emotivaSubscriptionName);
            return;
        }

        try {
            EmotivaSubscriptionTags.hasChannel(emotivaSubscriptionName);
        } catch (IllegalArgumentException e) {
            logger.debug("Subscription property '{}' is not know to the binding, might need updating",
                    emotivaSubscriptionName);
            return;
        }

        if (noSubscriptionToChannel().contains(EmotivaSubscriptionTags.valueOf(emotivaSubscriptionName))) {
            logger.debug("Initial subscription status update for {}, skipping, only want notifications",
                    emotivaSubscriptionName);
            return;
        }

        try {
            EmotivaSubscriptionTags subscriptionTag;
            try {
                subscriptionTag = EmotivaSubscriptionTags.valueOf(emotivaSubscriptionName);
            } catch (IllegalArgumentException e) {
                logger.debug("Property '{}' could not be mapped subscription tag, skipping", emotivaSubscriptionName);
                return;
            }

            if (subscriptionTag.getChannel().isEmpty()) {
                logger.debug("Subscription property '{}' does not have a corresponding channel, skipping",
                        emotivaSubscriptionName);
                return;
            }

            String trimmedValue = value.trim();

            logger.debug("Found subscription '{}' for '{}' and value '{}'", subscriptionTag, emotivaSubscriptionName,
                    trimmedValue);

            // Add/Update user assigned name for inputs
            if (subscriptionTag.getChannel().startsWith(CHANNEL_INPUT1.substring(0, CHANNEL_INPUT1.indexOf("-") + 1))
                    && "true".equals(visible)) {
                logger.debug("Adding '{}' to dynamic source input list", trimmedValue);
                sourcesMainZone.put(EmotivaControlCommands.matchToInput(subscriptionTag.name()), trimmedValue);
                commandMaps.put(MAP_SOURCES_MAIN_ZONE, sourcesMainZone);

                logger.debug("sources list is now {}", sourcesMainZone.size());
            }

            // Add/Update audio modes
            if (subscriptionTag.getChannel().startsWith(CHANNEL_MODE + "-") && "true".equals(visible)) {
                String modeName = i18nProvider.getText("channel-type.emotiva.selected-mode.option."
                        + subscriptionTag.getChannel().substring(subscriptionTag.getChannel().indexOf("_") + 1));
                logger.debug("Adding '{} ({})' from channel '{}' to dynamic mode list", trimmedValue, modeName,
                        subscriptionTag.getChannel());
                modes.put(EmotivaSubscriptionTags.fromChannelUID(subscriptionTag.getChannel()), trimmedValue);
            }

            findChannelDatatypeAndUpdateChannel(subscriptionTag.getChannel(), trimmedValue,
                    subscriptionTag.getDataType());
        } catch (IllegalArgumentException e) {
            logger.debug("Error updating subscription property '{}'", emotivaSubscriptionName, e);
        }
    }

    private void findChannelDatatypeAndUpdateChannel(String channelName, String value, EmotivaDataType dataType) {
        switch (dataType) {
            case DIMENSIONLESS_DECIBEL -> {
                var trimmedString = value.replaceAll("[ +]", "");
                logger.debug("Update channel '{}' to '{}:{}'", channelName, QuantityType.class.getSimpleName(),
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
            case DIMENSIONLESS_PERCENT -> {
                var trimmedString = value.replaceAll("[ +]", "");
                logger.debug("Update channel '{}' to '{}:{}'", channelName, PercentType.class.getSimpleName(), value);
                updateChannelState(channelName, PercentType.valueOf(trimmedString));
            }
            case FREQUENCY_HERTZ -> {
                logger.debug("Update channel '{}' to '{}:{}'", channelName, Units.HERTZ.getClass().getSimpleName(),
                        value);
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
                        logger.debug("Could not extract radio tuner frequency from '{}'", value);
                    }
                }
            }
            case GOODBYE -> {
                logger.info(
                        "Received goodbye notification from '{}'; disconnecting and scheduling av connection retry in '{}' minutes",
                        getThing().getUID(), DEFAULT_RETRY_INTERVAL_MINUTES);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/message.processor.goodbye");

                // Device gone, sending unsubscription messages not needed
                udpSenderActive = false;
                disconnect();
                scheduleConnectRetry(retryConnectInMinutes);
            }
            case NUMBER_TIME -> {
                logger.debug("Update channel '{}' to '{}:{}'", channelName, Number.class.getSimpleName(), value);
                long nowEpochSecond = Instant.now().getEpochSecond();
                updateChannelState(channelName, new QuantityType<>(nowEpochSecond, Units.SECOND));
            }
            case ON_OFF -> {
                logger.debug("Update channel '{}' to '{}:{}'", channelName, OnOffType.class.getSimpleName(), value);
                OnOffType switchValue = OnOffType.from(value.trim().toUpperCase());
                updateChannelState(channelName, switchValue);
                if (switchValue.equals(OnOffType.OFF) && CHANNEL_MENU.equals(channelName)) {
                    resetMenuPanelChannels();
                }
            }
            case STRING -> {
                logger.debug("Update channel '{}' to '{}:{}'", channelName, StringType.class.getSimpleName(), value);
                updateChannelState(channelName, StringType.valueOf(value));
            }
            case UNKNOWN -> // Do nothing, types not connect to channels
                logger.debug("Channel '{}' with UNKNOWN type and value '{}' was not updated", channelName, value);
            default -> {
                // datatypes not connect to a channel, so do nothing
            }
        }
    }

    private void updateChannelState(String channelID, State state) {
        stateMap.put(channelID, state);
        logger.trace("Updating channel '{}' with '{}'", channelID, state);
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
        logger.debug("Handling ohCommand '{}:{}' for '{}'", channelUID.getId(), ohCommand, channelUID.getThingUID());
        EmotivaUdpSendingService localSendingService = sendingService;

        if (localSendingService != null) {
            EmotivaControlRequest emotivaRequest = channelToControlRequest(channelUID.getId(), commandMaps,
                    protocolFromConfig(config.protocolVersion));
            if (ohCommand instanceof RefreshType) {
                stateMap.remove(channelUID.getId());

                if (emotivaRequest.getDefaultCommand().equals(none)) {
                    logger.debug("Found controlCommand 'none' for request '{}' from channel '{}' with RefreshType",
                            emotivaRequest.getName(), channelUID);
                } else {
                    logger.debug("Sending EmotivaUpdate for '{}'", emotivaRequest);
                    sendEmotivaUpdate(emotivaRequest.getDefaultCommand());
                }
            } else {
                try {
                    EmotivaControlDTO dto = emotivaRequest.createDTO(ohCommand, stateMap.get(channelUID.getId()));
                    localSendingService.send(dto);

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
                            localSendingService.sendUpdate(EmotivaSubscriptionTags.speakerChannels(), config);
                        }
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted during updating state for channel: '{}:{}:{}'", channelUID.getId(),
                            emotivaRequest.getName(), emotivaRequest.getDataType(), e);
                } catch (IOException e) {
                    logger.error("Failed updating state for channel '{}:{}:{}'", channelUID.getId(),
                            emotivaRequest.getName(), emotivaRequest.getDataType(), e);
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing '{}'", getThing().getUID());

        disconnect();
        super.dispose();
    }

    private synchronized void disconnect() {
        final EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            logger.debug("Disposing active sender");
            if (udpSenderActive) {
                try {
                    // Unsubscribe before disconnect
                    localSendingService.sendUnsubscribe(generalSubscription);
                    localSendingService.sendUnsubscribe(nonGeneralSubscriptions);
                } catch (IOException e) {
                    logger.debug("Failed to unsubscribe for '{}'", config.ipAddress, e);
                }
            }

            sendingService = null;
            try {
                localSendingService.disconnect();
                logger.debug("Disconnected udp send connector");
            } catch (Exception e) {
                logger.debug("Failed to close socket connection for '{}'", config.ipAddress, e);
            }
        }
        udpSenderActive = false;

        final EmotivaUdpReceivingService notifyConnector = notifyListener;
        if (notifyConnector != null) {
            notifyListener = null;
            try {
                notifyConnector.disconnect();
                logger.debug("Disconnected notify connector");
            } catch (Exception e) {
                logger.error("Failed to close socket connection for: '{}:{}'", config.ipAddress, config.notifyPort, e);
            }
        }

        final EmotivaUdpReceivingService menuConnector = menuNotifyListener;
        if (menuConnector != null) {
            menuNotifyListener = null;
            try {
                menuConnector.disconnect();
                logger.debug("Disconnected menu notify connector");
            } catch (Exception e) {
                logger.error("Failed to close socket connection for: '{}:{}'", config.ipAddress, config.notifyPort, e);
            }
        }

        ScheduledFuture<?> localConnectRetryJob = this.connectRetryJob;
        if (localConnectRetryJob != null) {
            localConnectRetryJob.cancel(true);
            this.connectRetryJob = null;
        }

        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            this.pollingJob = null;
            logger.debug("Polling job canceled");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(InputStateOptionProvider.class);
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesMainZone() {
        return sourcesMainZone;
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesZone2() {
        return sourcesZone2;
    }

    public EnumMap<EmotivaSubscriptionTags, String> getModes() {
        return modes;
    }
}
