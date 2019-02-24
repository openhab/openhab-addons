/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nadreceiver.internal;

import static org.openhab.binding.nadreceiver.internal.NadReceiverBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nadreceiver.internal.protocol.NadReceiverConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NadReceiverHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marc Chételat - Initial contribution
 */
public class NadReceiverHandler extends BaseThingHandler implements NadReceiverEventListener {

    private final Logger logger = LoggerFactory.getLogger(NadReceiverHandler.class);

    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;
    private static final int DEFAULT_TELNET_PORT = 23;
    private static final int DEFAULT_MAX_SOURCES = 9;

    /* Global configuration for NAD Receiver Thing */
    private NadReceiverConfiguration configuration;

    private NadReceiverConnection connection = null;
    private ScheduledFuture<?> connectionCheckerFuture = null;

    private String model = null;
    private Boolean currentPowerStatus = null;
    private String currentSource = null;
    private Boolean currentMute = null;
    private Integer currentVolume = null; // Percentage

    private NadSourcesOptionProvider sourcesOptionProvider = null;
    private NadReceiverSourcesConfiguration sourcesConfiguration = null;

    /**
     *
     * @param thing
     * @param sourcesOptionProvider
     */
    public NadReceiverHandler(Thing thing, NadSourcesOptionProvider sourcesOptionProvider) {
        super(thing);

        this.sourcesOptionProvider = sourcesOptionProvider;

        logger.debug("Create a NAD Receiver Handler for thing '{}'", getThing().getUID());
    }

    @Override
    /**
     *
     */
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (connection == null) {
            logger.debug("Thing not yet initialized!");
            return;
        }

        String id = channelUID.getId();

        if (getThing().getStatusInfo().getStatus() != ThingStatus.ONLINE) {
            switch (id) {
                case CHANNEL_POWER:
                    logger.debug("Device powered off sending {} {}", channelUID, command);
                    break;
                default:
                    logger.debug("Device powered off ignore command {} {}", channelUID, command);
                    return;
            }
        }

        try {
            switch (id) {
                case CHANNEL_POWER:
                    handlePowerCommand(command, channelUID);
                    break;
                case CHANNEL_SOURCE:
                    handleSourceCommand(command, channelUID);
                    break;
                case CHANNEL_VOLUME:
                    handleVolumeCommand(command, channelUID);
                    break;
                case CHANNEL_MUTE:
                    handleMuteCommand(command, channelUID);
                    break;
                case CHANNEL_MODEL:
                    handleModelCommand(command, channelUID);
                    break;
                default:
                    logger.error("Channel {} not supported!", id);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     *
     * @param command
     * @param channelUID
     * @throws IOException
     */
    private void handleMuteCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("handleMuteCommand RefreshType {}", command);
                updateState(channelUID, currentMute ? OnOffType.ON : OnOffType.OFF);
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof OnOffType) {
            logger.debug("handleMuteCommand set {}", command);
            connection.setMute(((OnOffType) command) == OnOffType.ON);
        }
    }

    /**
     *
     * @param command
     * @param channelUID
     * @throws IOException
     */
    private void handleVolumeCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("handleVolumeCommand RefreshType {}", command);
                updateState(channelUID, new PercentType(currentVolume.intValue()));
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof PercentType) {
            logger.debug("handleVolumeCommand PercentType set {} {}", command);
            connection.setVolume(((PercentType) command).intValue());
        }
        if (command instanceof IncreaseDecreaseType) {
            logger.debug("handleVolumeCommand IncreaseDecreaseType set {} {}", command);
            int change = command == IncreaseDecreaseType.INCREASE ? 1 : -1;
            connection.setVolume(currentVolume + change);
        }
        if (command instanceof OnOffType) {
            logger.debug("handleVolumeCommand OnOffType set {} {}", command);
            connection.setMute(((OnOffType) command) == OnOffType.ON);
        }

    }

    private void handleSourceCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            logger.debug("handleSourceCommand RefreshType {}", command);
            try {
                refreshSourceChannel();
                updateState(channelUID, new StringType(currentSource));
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof StringType) {
            logger.debug("handleSourceCommand set {}", command);
            connection.setSource(command.toString());
        }

    }

    private void handleModelCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            logger.debug("handleSourceCommand RefreshType {}", command);
            try {
                logger.debug("handleModelCommand RefreshType");
                updateState(channelUID, new StringType(model));
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof StringType) {
            logger.debug("handleSourceCommand set {}", command);
            connection.setSource(command.toString());
        }

    }

    public void handlePowerCommand(Command command, ChannelUID channelUID) throws IOException {
        if (currentPowerStatus != null) {
            if (command instanceof RefreshType) {
                try {
                    logger.debug("handlePowerCommand RefreshType");
                    updateState(channelUID, OnOffType.from(currentPowerStatus));
                } catch (CompletionException ex) {
                    throw new IOException(ex.getCause());
                }
            }
            if (command instanceof OnOffType) {
                logger.debug("handlePowerCommand set {}", command);
                connection.setPower(((OnOffType) command) == OnOffType.ON);
            }
        } else {
            logger.warn("currentPowerStatus not initialized");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NAD Receiver handler for uid '{}'", getThing().getUID());

        updateStatus(ThingStatus.UNKNOWN);

        configuration = getConfigAs(NadReceiverConfiguration.class);

        String hostname = configuration.hostname;
        Object portObject = configuration.port;
        Object heartbeatIntervalObject = configuration.heartbeatInterval;
        Object reconnectIntervalObject = configuration.reconnectInterval;
        Object maxSourcesObject = configuration.maxSources;

        int port = DEFAULT_TELNET_PORT;
        if (portObject instanceof BigDecimal) {
            port = ((BigDecimal) portObject).intValue();
        } else if (portObject instanceof Integer) {
            port = (int) portObject;
        }

        int heartbeatInterval = DEFAULT_HEARTBEAT_MINUTES;
        if (heartbeatIntervalObject instanceof BigDecimal) {
            heartbeatInterval = ((BigDecimal) heartbeatIntervalObject).intValue();
        } else if (heartbeatIntervalObject instanceof Integer) {
            heartbeatInterval = (int) heartbeatIntervalObject;
        }

        int reconnectInterval = DEFAULT_RECONNECT_MINUTES;
        if (reconnectIntervalObject instanceof BigDecimal) {
            reconnectInterval = ((BigDecimal) reconnectIntervalObject).intValue();
        } else if (reconnectIntervalObject instanceof Integer) {
            reconnectInterval = (int) reconnectIntervalObject;
        }

        int maxSources = DEFAULT_MAX_SOURCES;
        if (maxSourcesObject instanceof BigDecimal) {
            maxSources = ((BigDecimal) maxSourcesObject).intValue();
        } else if (reconnectIntervalObject instanceof Integer) {
            maxSources = (int) maxSourcesObject;
        }

        sourcesConfiguration = new NadReceiverSourcesConfiguration(maxSources);

        try {
            connection = new NadReceiverConnection(hostname, port, heartbeatInterval, reconnectInterval, maxSources,
                    this, this.scheduler);

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        logger.debug("Finished initializing!");

    }

    @Override
    public void dispose() {
        logger.debug("Disposing NadReceiverHandler");
        super.dispose();
        if (connectionCheckerFuture != null) {
            connectionCheckerFuture.cancel(true);
            connectionCheckerFuture = null;
        }
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    @Override
    public void updateSource(int source) {
        this.currentSource = String.valueOf(source);
        updateState(NadReceiverBindingConstants.CHANNEL_SOURCE, new StringType(currentSource));
    }

    /**
     * When message is received and we need to update info related to volume
     */
    @Override
    public void updateVolume(int volume) {
        this.currentVolume = volume;
        updateState(NadReceiverBindingConstants.CHANNEL_VOLUME, new PercentType(currentVolume));

    }

    @Override
    public void updateMute(boolean mute) {
        this.currentMute = mute;
        updateState(NadReceiverBindingConstants.CHANNEL_MUTE, OnOffType.from(mute));
    }

    /**
     * Theoretically the model name won't change too often ;-)
     */
    @Override
    public void updateModel(String model) {
        this.model = model;
        updateState(NadReceiverBindingConstants.CHANNEL_MODEL, new StringType(model));

    }

    @Override
    public void updatePowerStatus(boolean power) {
        this.currentPowerStatus = power;
        updateState(NadReceiverBindingConstants.CHANNEL_POWER, OnOffType.from(power));
    }

    @Override
    public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }

    @Override
    public void addOrUpdateSourceName(String number, String sourceName) {
        sourcesConfiguration.addOrUpdateSourceName(number, sourceName);
        if (sourcesConfiguration.isComplete()) {
            refreshSourceChannel();
        }
    }

    @Override
    public void addOrUpdateSourceState(String number, boolean enabled) {
        sourcesConfiguration.addOrUpdateSourceState(number, enabled);
        refreshSourceChannel();
    }

    public void refreshSourceChannel() {
        sourcesOptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SOURCE),
                sourcesConfiguration.getStateOptions());
    }

}
