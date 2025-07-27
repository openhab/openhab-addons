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
package org.openhab.binding.emotiva.internal;

import static java.lang.String.format;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.channelToControlRequest;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.getMenuPanelColumnLabel;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.getMenuPanelRowLabel;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.updateProgress;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumeDecibelToPercentage;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumePercentageToDecibel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.none;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.power_on;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.STRING;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaPropertyStatus.NOT_VALID;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaProtocolVersion.protocolFromConfig;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.SOURCES;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.keepAlive;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.measure.quantity.Frequency;
import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.AbstractNotificationDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaAckDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaBarNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaMenuCol;
import org.openhab.binding.emotiva.internal.dto.EmotivaMenuNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaMenuRow;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaNotifyWrapper;
import org.openhab.binding.emotiva.internal.dto.EmotivaPropertyDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaSubscriptionResponse;
import org.openhab.binding.emotiva.internal.dto.EmotivaUpdateResponse;
import org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaDataType;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.binding.emotiva.internal.protocol.EmotivaUdpResponse;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.library.types.DecimalType;
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
import org.openhab.core.types.UnDefType;
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

    private final EmotivaConfiguration config;

    /**
     * Emotiva devices have trouble with too many subscriptions in same request, so subscriptions are dividing into
     * groups.
     */
    private final List<EmotivaSubscriptionTags> generalSubscription = EmotivaSubscriptionTags.channels("general");
    private final List<EmotivaSubscriptionTags> mainZoneSubscriptions = EmotivaSubscriptionTags.channels("main-zone");
    private final List<EmotivaSubscriptionTags> zone2Subscriptions = EmotivaSubscriptionTags.channels("zone2");

    private final EmotivaProcessorState state = new EmotivaProcessorState();
    private final EmotivaSubscriptionTagGroupHandler subscriptionHandler;
    private final EmotivaTranslationProvider i18nProvider;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private @Nullable ScheduledFuture<?> sourceLabelRefreshJob;
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
        this.subscriptionHandler = new EmotivaSubscriptionTagGroupHandler(config, state);
        this.retryConnectInMinutes = config.retryConnectInMinutes;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize: '{}'", thing.getUID());
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/message.processor.connecting");
        if (config.controlPort < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/message.processor.connection.error.port");
            return;
        }
        if (config.ipAddress.isBlank()) {
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
            final var notifyListener = new EmotivaUdpReceivingService(localConfig.notifyPort, localConfig, scheduler);
            this.notifyListener = notifyListener;
            notifyListener.connect(this::handleStatusUpdate, true);

            final var sendConnector = new EmotivaUdpSendingService(localConfig, scheduler);
            sendingService = sendConnector;
            sendConnector.connect(this::handleStatusUpdate, true);

            // Simple retry mechanism to handle minor network issues, if this fails a retry job is created
            for (int attempt = 1; attempt <= DEFAULT_CONNECTION_RETRIES && !udpSenderActive; attempt++) {
                try {
                    logger.debug("Connection attempt '{}'", attempt);
                    sendConnector.sendSubscription(subscriptionHandler.init(), config);
                } catch (IOException e) {
                    // network or socket failure, also wait 2 sec and try again
                }

                for (int delay = 0; delay < 10 && !udpSenderActive; delay++) {
                    Thread.sleep(200); // wait 10 x 200ms = 2sec
                }
            }

            if (udpSenderActive) {
                updateStatus(ThingStatus.ONLINE);
                state.updateLastSeen(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
                setInitialSourceLabels();
                starthSourceLabelsRefresJob();
                startPollingKeepAlive();

                final var menuListenerConnector = new EmotivaUdpReceivingService(localConfig.menuNotifyPort,
                        localConfig, scheduler);
                this.menuNotifyListener = menuListenerConnector;
                menuListenerConnector.connect(this::handleStatusUpdate, true);
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
        final ScheduledFuture<?> localScheduleConnectRetryJob = connectRetryJob;
        if (localScheduleConnectRetryJob == null || localScheduleConnectRetryJob.isCancelled()) {
            connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
        }
    }

    /**
     * Starts a polling job for connection to the device, adds the
     * {@link EmotivaBindingConstants#DEFAULT_KEEP_ALIVE_IN_MILLISECONDS} as a time buffer for checking, to avoid
     * flapping state or minor network issues.
     */
    private void startPollingKeepAlive() {
        final ScheduledFuture<?> localRefreshJob = this.pollingJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            Number keepAliveConfig = state.getChannel(EmotivaSubscriptionTags.keepAlive)
                    .filter(channel -> channel instanceof Number).map(keepAlive -> (Number) keepAlive)
                    .orElse(new DecimalType(config.keepAlive));

            // noinspection ConstantConditions
            long delay = keepAliveConfig == null
                    ? DEFAULT_KEEP_ALIVE_IN_MILLISECONDS + DEFAULT_KEEP_ALIVE_IN_MILLISECONDS
                    : keepAliveConfig.longValue() + DEFAULT_KEEP_ALIVE_IN_MILLISECONDS;
            pollingJob = scheduler.scheduleWithFixedDelay(this::checkKeepAliveTimestamp, delay, delay,
                    TimeUnit.MILLISECONDS);
            logger.debug("Started scheduled job to check connection to device, with an {}ms internal", delay);
        }
    }

    /**
     * Starts a polling job for refreshing source labels
     */
    private void starthSourceLabelsRefresJob() {
        final ScheduledFuture<?> localSourceLabelRefreshJob = sourceLabelRefreshJob;
        if (localSourceLabelRefreshJob == null || localSourceLabelRefreshJob.isCancelled()) {
            sourceLabelRefreshJob = scheduler.scheduleWithFixedDelay(this::refreshSourceLabels, 0,
                    DEFAULT_REFRESH_SOURCE_LABEL_JOB_IN_MINUTES, TimeUnit.MINUTES);
            logger.debug("Started scheduled job to update source labels now and every {}min",
                    DEFAULT_REFRESH_SOURCE_LABEL_JOB_IN_MINUTES);
        }
    }

    private void refreshSourceLabels() {
        EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            try {
                localSendingService
                        .sendSubscription(EmotivaSubscriptionTags.getBySubscriptionTagGroups(Set.of(SOURCES)), config);
                scheduler.schedule(this::unsubscribeSourceLabels, DEFAULT_REFRESH_SOURCE_UNSUBSCRIBE_DELAY_IN_SECONDS,
                        TimeUnit.SECONDS);
            } catch (InterruptedIOException e) {
                logger.debug("Interrupted during sending of EmotivaSubscription message to device '{}'", thing.getUID(),
                        e);
            } catch (IOException e) {
                logger.warn("Failed to send EmotivaSubscription message to device '{}'", thing.getUID(), e);
            }
        }
    }

    private void unsubscribeSourceLabels() {
        EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            try {
                localSendingService
                        .sendUnsubscribe(EmotivaSubscriptionTags.getBySubscriptionTagGroups(Set.of(SOURCES)));
            } catch (InterruptedIOException e) {
                logger.debug("Interrupted during sending of EmotivaUnsubscribe message to device '{}'", thing.getUID(),
                        e);
            } catch (IOException e) {
                logger.warn("Failed to send EmotivaUnsubscribe message to device '{}'", thing.getUID(), e);
            }
        }
    }

    private void subscribeTagGroups(Set<EmotivaSubscriptionTagGroup> groups) {
        EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            try {
                localSendingService.sendSubscription(EmotivaSubscriptionTags.getBySubscriptionTagGroups(groups),
                        config);
                state.updateSubscribedTagGroups(groups);
            } catch (InterruptedIOException e) {
                logger.debug("Interrupted during sending of EmotivaSubscription message to device '{}'", thing.getUID(),
                        e);
            } catch (IOException e) {
                logger.warn("Failed to send EmotivaSubscription message to device '{}'", thing.getUID(), e);
            }
        }
    }

    private void unsubscribeTagGroups(Set<EmotivaSubscriptionTagGroup> groups) {
        EmotivaUdpSendingService localSendingService = sendingService;
        if (localSendingService != null) {
            try {
                localSendingService.sendUnsubscribe(EmotivaSubscriptionTags.getBySubscriptionTagGroups(groups));
                state.updateUnsubscribedTagGroups(groups);
            } catch (InterruptedIOException e) {
                logger.debug("Interrupted during sending of EmotivaUnsubscribe message to device '{}'", thing.getUID(),
                        e);
            } catch (IOException e) {
                logger.warn("Failed to send EmotivaUnsubscribe message to device '{}'", thing.getUID(), e);
            }
        }
    }

    private void checkKeepAliveTimestamp() {
        Instant lastKeepAliveTimestamp = state.getLastSeen();

        Instant deviceGoneGracePeriod = Instant.now().minus(config.keepAlive, ChronoUnit.MILLIS)
                .minus(DEFAULT_KEEP_ALIVE_CONSIDERED_LOST_IN_MILLISECONDS, ChronoUnit.MILLIS);
        boolean isPastGradePeriod = lastKeepAliveTimestamp.isBefore(deviceGoneGracePeriod);

        if (ThingStatus.ONLINE.equals(thing.getStatusInfo().getStatus())) {
            if (isPastGradePeriod) {
                logger.debug(
                        "Keep-alive job for '{}': status={}, last-seen was '{}', past grace-period by '{}', scheduling connection retry",
                        thing.getUID(), thing.getStatusInfo().getStatus(), lastKeepAliveTimestamp,
                        Duration.between(lastKeepAliveTimestamp, deviceGoneGracePeriod));
                setOfflineAndScheduleConnectRetry();
            }
        } else {
            try {
                ScheduledFuture<?> localConnectRetryJob = connectRetryJob;
                if (localConnectRetryJob == null || localConnectRetryJob.isCancelled()) {
                    logger.debug(
                            "Keep-alive job for '{}': status={}, no active retry job, scheduling new connection retry",
                            thing.getUID(), thing.getStatusInfo().getStatus());
                    setOfflineAndScheduleConnectRetry();
                } else {
                    Duration currentJobDelay = Duration.of(localConnectRetryJob.getDelay(TimeUnit.SECONDS),
                            ChronoUnit.SECONDS);
                    Duration defaultJobDelay = Duration.of(DEFAULT_RETRY_INTERVAL_MINUTES, ChronoUnit.MINUTES);
                    if (defaultJobDelay.minus(currentJobDelay).isNegative()) {
                        logger.debug(
                                "Keep-alive job for '{}': status={}, retry job not working, canceling and scheduling new connection retry",
                                thing.getUID(), thing.getStatusInfo().getStatus());

                        // Kill current connection retry job and schedule a new one
                        localConnectRetryJob.cancel(true);
                        setOfflineAndScheduleConnectRetry();
                    } else {
                        logger.debug("Keep-alive job for '{}': status={}, retry job still scheduled, nothing to do",
                                thing.getUID(), thing.getStatusInfo().getStatus());
                    }
                }
            } catch (Exception e) {
                logger.debug("Keep-alive job for '{}': status={}, failed checking connection retry job with '{}'",
                        thing.getUID(), thing.getStatusInfo().getStatus(), e.getMessage());
            }
        }
    }

    private void setOfflineAndScheduleConnectRetry() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/message.processor.connection.error.keep-alive");
        // Connection lost, avoid sending unsubscription messages
        udpSenderActive = false;
        disconnect();
        scheduleConnectRetry(retryConnectInMinutes);
    }

    private void handleStatusUpdate(EmotivaUdpResponse emotivaUdpResponse) {
        udpSenderActive = true;
        logger.trace("Received data from '{}' with length '{}'", emotivaUdpResponse.ipAddress(),
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

        if (object instanceof EmotivaAckDTO) {
            // Currently not supported to revert a failed command update, just used for logging for now.
            logger.trace("Processing received '{}' with '{}'", EmotivaAckDTO.class.getSimpleName(),
                    emotivaUdpResponse.answer());
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

    /**
     * Sets initial source labels based on command list.
     */
    private void setInitialSourceLabels() {
        EnumMap<EmotivaControlCommands, String> sourceMainZone = EmotivaControlCommands
                .getByCommandType(EmotivaCommandType.SOURCE_MAIN_ZONE);
        sourceMainZone.remove(EmotivaControlCommands.input);
        state.setSourcesMainZone(sourceMainZone);

        EnumMap<EmotivaControlCommands, String> sourcesZone2 = EmotivaControlCommands
                .getByCommandType(EmotivaCommandType.SOURCE_ZONE2);
        sourcesZone2.remove(EmotivaControlCommands.input);
        state.setSourcesZone2(sourcesZone2);
    }

    private void handleMenuNotify(EmotivaMenuNotifyDTO answerDto) {
        String highlightValue = "";

        for (var row = 4; row <= 6; row++) {
            EmotivaMenuRow emotivaMenuRow = answerDto.getRow().get(row);
            logger.debug("Checking row '{}' with '{}' columns", row, emotivaMenuRow.getCol().size());
            for (var column = 0; column <= 2; column++) {
                EmotivaMenuCol emotivaMenuCol = emotivaMenuRow.getCol().get(column);
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

                String channelName = format("%s-%s-%s", CHANNEL_MENU_DISPLAY_PREFIX, getMenuPanelRowLabel(row),
                        getMenuPanelColumnLabel(column));
                updateChannelState(channelName, new StringType(cellValue));
            }
        }
        updateChannelState(CHANNEL_MENU_DISPLAY_HIGHLIGHT, new StringType(highlightValue));
    }

    private void handleMenuNotifyProgressMessage(String progressBarTimeInSeconds) {
        try {
            int seconds = Integer.parseInt(progressBarTimeInSeconds);
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
                String channelName = format("%s-%s-%s", CHANNEL_MENU_DISPLAY_PREFIX, getMenuPanelRowLabel(row),
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
                logger.debug("Interrupted during sending of EmotivaUpdate message to device '{}'", thing.getUID(), e);
            } catch (IOException e) {
                logger.error("Failed to send EmotivaUpdate message to device '{}'", thing.getUID(), e);
            }
        }
    }

    private void handleNotificationUpdate(AbstractNotificationDTO answerDto) {
        if (answerDto.getProperties() == null) {
            for (EmotivaNotifyDTO tag : xmlUtils.unmarshallToNotification(answerDto.getTags())) {
                try {
                    EmotivaSubscriptionTags tagName = EmotivaSubscriptionTags.valueOf(tag.getName());
                    findChannelDatatypeAndUpdateChannel(tagName.getChannel(), tag.getValue(), tagName.getDataType());
                } catch (IllegalArgumentException e) {
                    logger.debug("Subscription name '{}' could not be mapped to Emotiva property tag, skipping",
                            tag.getName());
                }
            }
        } else {
            for (EmotivaPropertyDTO property : answerDto.getProperties()) {
                handleChannelUpdate(property.getName(), property.getValue(), property.getVisible(),
                        property.getStatus());
            }
        }
    }

    private void handleChannelUpdate(String emotivaSubscriptionName, @Nullable String rawValue, String visible,
            String status) {
        logger.trace("Subscription property '{}' with raw value '{}' received, start processing",
                emotivaSubscriptionName, rawValue);

        if (status.equals(NOT_VALID.name())) {
            logger.debug("Subscription property '{}' not present in device, skipping", emotivaSubscriptionName);
            return;
        }

        if ("None".equals(rawValue)) {
            logger.debug(
                    "Subscription property '{}' has no value, no update needed, usually means a speaker is not enabled",
                    emotivaSubscriptionName);
            return;
        }

        if (keepAlive.name().equals(emotivaSubscriptionName)) {
            updateKeepAliveState(rawValue);
            return;
        }

        try {
            EmotivaSubscriptionTags.hasChannel(emotivaSubscriptionName);
        } catch (IllegalArgumentException e) {
            logger.debug("Subscription property '{}' is not know to the binding, might need updating",
                    emotivaSubscriptionName);
            return;
        }

        try {
            EmotivaSubscriptionTags subscriptionTag;
            try {
                subscriptionTag = EmotivaSubscriptionTags.valueOf(emotivaSubscriptionName);
            } catch (IllegalArgumentException e) {
                logger.debug("Subscription property '{}' could not be mapped to Emotiva property tag, skipping",
                        emotivaSubscriptionName);
                return;
            }

            if (subscriptionTag.getChannel().isEmpty()) {
                logger.debug(
                        "Subscription property '{}' with value '{}' does not have a corresponding OH channel, skipping",
                        emotivaSubscriptionName, rawValue);
                return;
            }

            String trimmedValue = rawValue == null ? "" : rawValue.trim();
            logger.trace("Subscription property '{}' with value '{}' mapped to OH channel '{}'", subscriptionTag,
                    trimmedValue, subscriptionTag.getChannel());

            // Add/Update user assigned name for sources
            if (subscriptionTag.getChannel().startsWith(CHANNEL_INPUT1.substring(0, CHANNEL_INPUT1.indexOf("-") + 1))
                    && "true".equals(visible)) {
                state.updateSourcesMainZone(EmotivaControlCommands.matchFromSourceInput(subscriptionTag), trimmedValue);
                logger.trace("Adding/Updating '{}' to OH channel '{}' state options, all options are now {}",
                        trimmedValue, CHANNEL_SOURCE, state.getSourcesMainZone());
            }

            // Add/Update audio modes
            if (subscriptionTag.getChannel().startsWith(CHANNEL_MODE + "-") && "true".equals(visible)) {
                String modeName = i18nProvider.getText("channel-type.emotiva.selected-mode.option."
                        + subscriptionTag.getChannel().substring(subscriptionTag.getChannel().indexOf("_") + 1));
                logger.trace("Adding/Updating '{} ({})' from property '{}' to OH channel '{}' state options",
                        trimmedValue, modeName, subscriptionTag.getName(), CHANNEL_MODE);
                state.updateModes(EmotivaSubscriptionTags.fromChannelUID(subscriptionTag.getChannel()), trimmedValue);
            }

            findChannelDatatypeAndUpdateChannel(subscriptionTag.getChannel(), trimmedValue,
                    subscriptionTag.getDataType());
        } catch (IllegalArgumentException e) {
            logger.debug("Error updating subscription property '{}'", emotivaSubscriptionName, e);
        }
    }

    private void findChannelDatatypeAndUpdateChannel(String channelName, @Nullable String rawValue,
            EmotivaDataType dataType) {
        switch (dataType) {
            case DIMENSIONLESS_DECIBEL -> {
                if (rawValue == null) {
                    logger.debug("Channel '{}' with DIMENSIONLESS_DECIBEL type has value 'null', not updated",
                            channelName);
                } else {
                    String trimmedString = rawValue.replaceAll("[ +]", "");
                    logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName,
                            trimmedString, QuantityType.class.getSimpleName());
                    if (channelName.equals(CHANNEL_MAIN_VOLUME)) {
                        updateVolumeChannels(trimmedString, CHANNEL_MUTE, channelName, CHANNEL_MAIN_VOLUME_DB);
                    } else if (channelName.equals(CHANNEL_ZONE2_VOLUME)) {
                        updateVolumeChannels(trimmedString, CHANNEL_ZONE2_MUTE, channelName, CHANNEL_ZONE2_VOLUME_DB);
                    } else {
                        if ("None".equals(trimmedString)) {
                            updateChannelState(channelName, QuantityType.valueOf(0, Units.DECIBEL));
                        } else {
                            updateChannelState(channelName,
                                    QuantityType.valueOf(Double.parseDouble(trimmedString), Units.DECIBEL));
                        }
                    }
                }
            }
            case DIMENSIONLESS_PERCENT -> {
                if (rawValue == null) {
                    logger.debug("Channel '{}' with DIMENSIONLESS_PERCENT type has value 'null', not updated",
                            channelName);
                } else {
                    String trimmedString = rawValue.replaceAll("[ +]", "");
                    logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName, rawValue,
                            PercentType.class.getSimpleName());
                    updateChannelState(channelName, PercentType.valueOf(trimmedString));
                }
            }
            case KEEP_ALIVE -> {
                updateKeepAliveState(rawValue);
            }
            case FREQUENCY_HERTZ -> {
                logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName, rawValue,
                        Units.HERTZ.getClass().getSimpleName());
                if (rawValue == null || rawValue.isBlank()) {
                    logger.debug("Channel '{}' with FREQUENCY_HERTZ type has value '{}', not updated", channelName,
                            rawValue);
                } else {
                    // Getting rid of characters and empty space leaves us with the raw frequency
                    try {
                        String frequencyString = rawValue.replaceAll("[a-zA-Z ]", "");
                        QuantityType<Frequency> hz = QuantityType.valueOf(0, Units.HERTZ);
                        if (rawValue.contains("AM")) {
                            hz = QuantityType.valueOf(Double.parseDouble(frequencyString) * 1000, Units.HERTZ);
                        } else if (rawValue.contains("FM")) {
                            hz = QuantityType.valueOf(Double.parseDouble(frequencyString) * 1000000, Units.HERTZ);
                        }
                        updateChannelState(CHANNEL_TUNER_CHANNEL, hz);
                    } catch (NumberFormatException e) {
                        logger.debug("Could not extract radio tuner frequency from '{}'", rawValue);
                    }
                }
            }
            case GOODBYE -> {
                logger.info(
                        "Received goodbye notification from '{}'; disconnecting and scheduling av connection retry in '{}' minutes",
                        thing.getUID(), DEFAULT_RETRY_INTERVAL_MINUTES);
                setOfflineAndScheduleConnectRetry();
            }
            case NUMBER_TIME -> {
                logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName, rawValue,
                        Number.class.getSimpleName());
                updateChannelState(channelName,
                        new QuantityType<>(ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond(), Units.SECOND));
            }
            case ON_OFF -> {
                logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName, rawValue,
                        OnOffType.class.getSimpleName());
                if (rawValue == null) {
                    logger.debug("Channel '{}' with ON_OFF type has value 'null', not updated", channelName);
                } else {
                    OnOffType switchValue = OnOffType.from(rawValue.trim().toUpperCase());
                    updateChannelState(channelName, switchValue);
                    if (switchValue.equals(OnOffType.OFF) && CHANNEL_MENU.equals(channelName)) {
                        resetMenuPanelChannels();
                    }
                }
            }
            case STRING -> {
                logger.debug("Preparing to update OH channel '{}' with value:type '{}:{}'", channelName, rawValue,
                        StringType.class.getSimpleName());
                if (rawValue == null) {
                    logger.trace("Channel '{}' with STRING type has value 'null', not updated", channelName);
                } else {
                    updateChannelState(channelName, StringType.valueOf(rawValue));
                    if (channelName.equals(CHANNEL_SOURCE)) {
                        EmotivaControlCommands matchedSource = matchCommandFromSourceAndLabels(rawValue);
                        if (matchedSource.equals(none)) {
                            logger.error(
                                    "Error trying to get source command from OhCommand '{}:{}', not able to update subscriptions",
                                    channelName, rawValue);
                        } else {
                            BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups = (
                                    subscribeSet, unsubscribeSet) -> {
                                updateTagGroupChangesForSource(subscribeSet, unsubscribeSet, rawValue, matchedSource);
                            };
                            subscriptionHandler.tagGroupsFromSource(matchedSource, tagGroups);
                            logger.debug("Currently subscribed tag groups: '{}'", state.getSubscriptionsTagGroups());
                        }
                    }
                }
            }
            case UNKNOWN -> // Do nothing, types not connect to channels
                logger.debug("Channel '{}' with UNKNOWN type and value '{}' was not updated", channelName, rawValue);
            default -> {
                // datatypes not connect to a channel, so do nothing
            }
        }
    }

    private void updateKeepAliveState(@Nullable String rawValue) {
        String trimmedValue = rawValue == null ? "" : rawValue.trim();
        if (trimmedValue.isBlank()) {
            logger.trace("Subscription property '{}' has invalid value '{}' for device '{}', not updated",
                    keepAlive.name(), trimmedValue, thing.getUID());
        } else {
            state.updateLastSeen(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
            logger.trace(
                    "Subscription property '{}' with value '{}' mapped to last-seen for device '{}', value updated",
                    keepAlive.name(), trimmedValue, thing.getUID());
        }
    }

    /**
     * Updates tags subscriptions by the binding based on a source command and tag groups. The subscribe and unsubscribe
     * sets are matched against the currently subscribed tag groups to reduce number of needed subscription calls and
     * avoid recursive updates.
     *
     * @param subscribeSet Tag groups to subscribe to
     * @param unsubscribeSet Tag groups to unsubscribe from
     * @param value Input value from channel er device
     * @param matchedSource Source command matched from input value
     */
    private void updateTagGroupChangesForSource(Set<EmotivaSubscriptionTagGroup> subscribeSet,
            Set<EmotivaSubscriptionTagGroup> unsubscribeSet, String value, EmotivaControlCommands matchedSource) {
        Set<EmotivaSubscriptionTagGroup> currentSubscriptionTagGroups = state.getSubscriptionsTagGroups();
        var unsubscribe = unsubscribeSet.stream().filter(currentSubscriptionTagGroups::contains)
                .collect(Collectors.toCollection(HashSet::new));
        if (!unsubscribe.isEmpty()) {
            unsubscribeTagGroups(unsubscribeSet);
        }
        var subscribe = subscribeSet.stream().filter(group -> !currentSubscriptionTagGroups.contains(group))
                .collect(Collectors.toCollection(HashSet::new));
        if (!subscribe.isEmpty()) {
            subscribeTagGroups(subscribe);
        }
        logger.debug("Input '{}' matched to source '{}' subscribing to '{}' and unsubscribing from '{}'", value,
                matchedSource.name(), subscribe, unsubscribe);
    }

    public EmotivaControlCommands matchCommandFromSourceAndLabels(String value) {
        Map<EmotivaControlCommands, String> map = state.getCommandMap(MAP_SOURCES_MAIN_ZONE);
        EmotivaControlCommands command = EmotivaControlCommands.matchFromSourceInput(value, map);
        if (command.equals(none)) {
            logger.debug("Could not match OH command {} with value '{}' to values in map '{}', no matching command",
                    CHANNEL_SOURCE, value, map);
        }
        return command;
    }

    private void updateChannelState(String channelID, State channelState) {
        state.updateChannel(channelID, channelState);
        logger.trace("Updating OH channel '{}' with value '{}'", channelID, channelState);
        updateState(channelID, channelState);
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
            EmotivaControlRequest emotivaRequest = channelToControlRequest(channelUID.getId(), state,
                    protocolFromConfig(config.protocolVersion));
            if (ohCommand instanceof RefreshType) {
                state.removeChannel(channelUID.getId());

                if (emotivaRequest.getDefaultCommand().equals(none)) {
                    logger.debug("Found controlCommand 'none' for request '{}' from channel '{}' with RefreshType",
                            emotivaRequest.getName(), channelUID);
                } else {
                    logger.debug("Sending EmotivaUpdate for '{}'", emotivaRequest);
                    sendEmotivaUpdate(emotivaRequest.getDefaultCommand());
                }
            } else {
                try {
                    Optional<EmotivaControlDTO> dto = state.getChannel(channelUID.getId())
                            .map(previousState -> emotivaRequest.createDTO(ohCommand, previousState))
                            .or(() -> Optional.of(emotivaRequest.createDTO(ohCommand, UnDefType.UNDEF)));
                    localSendingService.send(dto.get());

                    if (emotivaRequest.getDefaultCommand().equals(EmotivaControlCommands.input)
                            && ohCommand instanceof StringType sourceString) {
                        try {
                            EmotivaControlCommands source = EmotivaControlCommands.valueOf(sourceString.toString());
                            BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups = (
                                    subscribeSet, unsubscribeSet) -> {
                                logger.debug(
                                        "OhCommand '{}:{}' is a source command with subscribeSet '{}' and unsubscribeSet '{}'",
                                        channelUID.getId(), ohCommand, subscribeSet, unsubscribeSet);
                                updateTagGroupChangesForSource(subscribeSet, unsubscribeSet, sourceString.toString(),
                                        source);
                            };
                            subscriptionHandler.tagGroupsFromSource(source, tagGroups);
                            logger.debug("Currently subscribed tag groups: '{}'", state.getSubscriptionsTagGroups());
                        } catch (IllegalArgumentException e) {
                            logger.error(
                                    "Error trying to get source command from OhCommand '{}:{}', not able to update subscriptions",
                                    channelUID.getId(), ohCommand);
                        }
                    } else if (emotivaRequest.getName().equals(EmotivaControlCommands.volume.name())
                            || emotivaRequest.getName().equals(EmotivaControlCommands.set_volume.name())) {
                        logger.debug("OhCommand '{}:{}' is of type main zone volume", channelUID.getId(), ohCommand);
                        if (ohCommand instanceof PercentType value) {
                            updateChannelState(CHANNEL_MAIN_VOLUME_DB,
                                    QuantityType.valueOf(volumePercentageToDecibel(value.intValue()), Units.DECIBEL));
                        } else if (ohCommand instanceof QuantityType<?> value) {
                            updateChannelState(CHANNEL_MAIN_VOLUME, volumeDecibelToPercentage(value.toString()));
                        }
                    } else if (emotivaRequest.getName().equals(EmotivaControlCommands.zone2_volume.name())
                            || emotivaRequest.getName().equals(EmotivaControlCommands.zone2_set_volume.name())) {
                        logger.debug("OhCommand '{}:{}' is of type zone 2 volume", channelUID.getId(), ohCommand);
                        if (ohCommand instanceof PercentType value) {
                            updateChannelState(CHANNEL_ZONE2_VOLUME_DB,
                                    QuantityType.valueOf(volumePercentageToDecibel(value.intValue()), Units.DECIBEL));
                        } else if (ohCommand instanceof QuantityType<?> value) {
                            updateChannelState(CHANNEL_ZONE2_VOLUME, volumeDecibelToPercentage(value.toString()));
                        }
                    } else if (ohCommand instanceof OnOffType value) {
                        logger.debug("OhCommand '{}:{}' is of type switch", channelUID.getId(), ohCommand);
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
        logger.debug("Disposing '{}'", thing.getUID());

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
                    localSendingService.sendUnsubscribe(mainZoneSubscriptions);
                    localSendingService.sendUnsubscribe(zone2Subscriptions);
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
        return Set.of(EmotivaInputStateOptionProvider.class);
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesMainZone() {
        return state.getSourcesMainZone();
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesZone2() {
        return state.getSourcesZone2();
    }

    public EnumMap<EmotivaSubscriptionTags, String> getModes() {
        return state.getModes();
    }
}
