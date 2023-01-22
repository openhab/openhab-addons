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
package org.openhab.binding.enigma2.internal.handler;

import static org.openhab.binding.enigma2.internal.Enigma2BindingConstants.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enigma2.internal.Enigma2Client;
import org.openhab.binding.enigma2.internal.Enigma2Configuration;
import org.openhab.binding.enigma2.internal.Enigma2RemoteKey;
import org.openhab.binding.enigma2.internal.actions.Enigma2Actions;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2Handler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(Enigma2Handler.class);
    private Enigma2Configuration configuration = new Enigma2Configuration();
    private Optional<Enigma2Client> enigma2Client = Optional.empty();
    private @Nullable ScheduledFuture<?> refreshJob;
    private LocalDateTime lastAnswerTime = LocalDateTime.now();

    public Enigma2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(Enigma2Configuration.class);
        if (configuration.host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host must not be empty");
        } else if (configuration.timeout <= 0 || configuration.timeout > 300) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "timeout must be between 0 and 300 seconds");
        } else if (configuration.refreshInterval <= 0 || configuration.refreshInterval > 3600) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "refreshInterval must be between 0 and 3600 seconds");
        }
        enigma2Client = Optional.of(new Enigma2Client(configuration.host, configuration.user, configuration.password,
                configuration.timeout));
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 2, configuration.refreshInterval,
                TimeUnit.SECONDS);
    }

    private void refresh() {
        getEnigma2Client().ifPresent(client -> {
            boolean online = client.refresh();
            if (online) {
                updateStatus(ThingStatus.ONLINE);
                updateState(CHANNEL_POWER, client.isPower() ? OnOffType.ON : OnOffType.OFF);
                updateState(CHANNEL_MUTE, client.isMute() ? OnOffType.ON : OnOffType.OFF);
                updateState(CHANNEL_VOLUME, new PercentType(client.getVolume()));
                updateState(CHANNEL_CHANNEL, new StringType(client.getChannel()));
                updateState(CHANNEL_TITLE, new StringType(client.getTitle()));
                updateState(CHANNEL_DESCRIPTION, new StringType(client.getDescription()));
                if (lastAnswerTime.isBefore(client.getLastAnswerTime())) {
                    lastAnswerTime = client.getLastAnswerTime();
                    updateState(CHANNEL_ANSWER, new StringType(client.getAnswer()));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        this.refreshJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({},{})", channelUID, command);
        getEnigma2Client().ifPresent(client -> {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    handlePower(channelUID, command, client);
                    break;
                case CHANNEL_CHANNEL:
                    handleChannel(channelUID, command, client);
                    break;
                case CHANNEL_MEDIA_PLAYER:
                    handleMediaPlayer(channelUID, command);
                    break;
                case CHANNEL_MEDIA_STOP:
                    handleMediaStop(channelUID, command);
                    break;
                case CHANNEL_MUTE:
                    handleMute(channelUID, command, client);
                    break;
                case CHANNEL_VOLUME:
                    handleVolume(channelUID, command, client);
                    break;
                case CHANNEL_TITLE:
                    handleTitle(channelUID, command, client);
                    break;
                case CHANNEL_DESCRIPTION:
                    handleDescription(channelUID, command, client);
                    break;
                case CHANNEL_ANSWER:
                    handleAnswer(channelUID, command, client);
                    break;
                default:
                    logger.debug("Channel {} is not supported", channelUID);
                    break;
            }
        });
    }

    private void handleVolume(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshVolume();
            updateState(channelUID, new PercentType(client.getVolume()));
        } else if (command instanceof PercentType) {
            client.setVolume(((PercentType) command).intValue());
        } else if (command instanceof DecimalType) {
            client.setVolume(((DecimalType) command).intValue());
        } else {
            logger.info("Channel {} only accepts PercentType, DecimalType, RefreshType. Type was {}.", channelUID,
                    command.getClass());
        }
    }

    private void handleMute(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshVolume();
            updateState(channelUID, client.isMute() ? OnOffType.ON : OnOffType.OFF);
        } else if (OnOffType.ON.equals(command)) {
            client.setMute(true);
        } else if (OnOffType.OFF.equals(command)) {
            client.setMute(false);
        } else {
            logger.info("Channel {} only accepts OnOffType, RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    private void handleAnswer(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshAnswer();
            if (lastAnswerTime.isBefore(client.getLastAnswerTime())) {
                lastAnswerTime = client.getLastAnswerTime();
                updateState(channelUID, new StringType(client.getAnswer()));
            }
        } else {
            logger.info("Channel {} only accepts RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    private void handleMediaStop(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } else if (command instanceof OnOffType) {
            sendRcCommand(Enigma2RemoteKey.STOP);
        } else {
            logger.info("Channel {} only accepts OnOffType, RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    private void handleMediaPlayer(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            return;
        } else if (PlayPauseType.PLAY == command) {
            sendRcCommand(Enigma2RemoteKey.PLAY);
        } else if (PlayPauseType.PAUSE == command) {
            sendRcCommand(Enigma2RemoteKey.PAUSE);
        } else if (NextPreviousType.NEXT == command) {
            sendRcCommand(Enigma2RemoteKey.FAST_FORWARD);
        } else if (NextPreviousType.PREVIOUS == command) {
            sendRcCommand(Enigma2RemoteKey.FAST_BACKWARD);
        } else {
            logger.info("Channel {} only accepts PlayPauseType, NextPreviousType, RefreshType. Type was {}.",
                    channelUID, command.getClass());
        }
    }

    private void handleChannel(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshChannel();
            updateState(channelUID, new StringType(client.getChannel()));
        } else if (command instanceof StringType) {
            client.setChannel(command.toString());
        } else {
            logger.info("Channel {} only accepts StringType, RefreshType. Type was {}.", channelUID,
                    command.getClass());
        }
    }

    private void handleTitle(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshEpg();
            updateState(channelUID, new StringType(client.getTitle()));
        } else {
            logger.info("Channel {} only accepts RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    private void handleDescription(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (command instanceof RefreshType) {
            client.refreshEpg();
            updateState(channelUID, new StringType(client.getDescription()));
        } else {
            logger.info("Channel {} only accepts RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    private void handlePower(ChannelUID channelUID, Command command, Enigma2Client client) {
        if (RefreshType.REFRESH == command) {
            client.refreshPower();
            updateState(channelUID, client.isPower() ? OnOffType.ON : OnOffType.OFF);
        } else if (OnOffType.ON == command) {
            client.setPower(true);
        } else if (OnOffType.OFF == command) {
            client.setPower(false);
        } else {
            logger.info("Channel {} only accepts OnOffType, RefreshType. Type was {}.", channelUID, command.getClass());
        }
    }

    public void sendRcCommand(String rcButton) {
        logger.debug("sendRcCommand({})", rcButton);
        try {
            Enigma2RemoteKey remoteKey = Enigma2RemoteKey.valueOf(rcButton);
            sendRcCommand(remoteKey);
        } catch (IllegalArgumentException ex) {
            logger.warn("{} is not a valid value for button - available are: {}", rcButton,
                    Stream.of(Enigma2RemoteKey.values()).map(b -> b.name()).collect(Collectors.joining(", ")));
        }
    }

    private void sendRcCommand(Enigma2RemoteKey remoteKey) {
        getEnigma2Client().ifPresent(client -> client.sendRcCommand(remoteKey.getValue()));
    }

    public void sendInfo(int timeout, String text) {
        getEnigma2Client().ifPresent(client -> client.sendInfo(timeout, text));
    }

    public void sendWarning(int timeout, String text) {
        getEnigma2Client().ifPresent(client -> client.sendWarning(timeout, text));
    }

    public void sendError(int timeout, String text) {
        getEnigma2Client().ifPresent(client -> client.sendError(timeout, text));
    }

    public void sendQuestion(int timeout, String text) {
        getEnigma2Client().ifPresent(client -> client.sendQuestion(timeout, text));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(Enigma2Actions.class);
    }

    /**
     * Getter for Test-Injection
     *
     * @return Enigma2Client.
     */
    Optional<Enigma2Client> getEnigma2Client() {
        return enigma2Client;
    }
}
