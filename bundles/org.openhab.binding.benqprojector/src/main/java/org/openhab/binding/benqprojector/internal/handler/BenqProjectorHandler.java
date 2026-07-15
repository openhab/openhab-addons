/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.benqprojector.internal.handler;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.BenqProjectorCommandException;
import org.openhab.binding.benqprojector.internal.BenqProjectorCommandType;
import org.openhab.binding.benqprojector.internal.BenqProjectorDevice;
import org.openhab.binding.benqprojector.internal.BenqProjectorException;
import org.openhab.binding.benqprojector.internal.configuration.BenqProjectorConfiguration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BenqProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Based on 'epsonprojector' originally by Pauli Anttila and Yannick Schaus
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class BenqProjectorHandler extends BaseThingHandler {
    private static final int DEFAULT_POLLING_INTERVAL_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(BenqProjectorHandler.class);
    private final SerialPortManager serialPortManager;
    private final Object sequenceLock = new Object();

    private @Nullable ScheduledFuture<?> pollingJob;
    private Optional<BenqProjectorDevice> device = Optional.empty();

    private boolean isPowerOn = false;
    private int pollingInterval = DEFAULT_POLLING_INTERVAL_SEC;

    public BenqProjectorHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (sequenceLock) {
            if (command instanceof RefreshType) {
                final Channel channel = this.thing.getChannel(channelUID);
                if (channel != null && getThing().getStatus() == ThingStatus.ONLINE) {
                    updateChannelState(channel);
                }
            } else {
                sendDataToDevice(BenqProjectorCommandType.getCommandType(channelUID.getId()), command);
            }
        }
    }

    @Override
    public void initialize() {
        final BenqProjectorConfiguration config = getConfigAs(BenqProjectorConfiguration.class);

        if (THING_TYPE_PROJECTOR_SERIAL.equals(thing.getThingTypeUID())) {
            device = Optional.of(new BenqProjectorDevice(serialPortManager, config));
        } else if (THING_TYPE_PROJECTOR_TCP.equals(thing.getThingTypeUID())) {
            device = Optional.of(new BenqProjectorDevice(config));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        pollingInterval = config.pollingInterval;
        updateStatus(ThingStatus.UNKNOWN);
        schedulePollingJob();
    }

    /**
     * Schedule the polling job
     */
    private void schedulePollingJob() {
        cancelPollingJob();

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                for (Channel channel : this.thing.getChannels()) {
                    // only query power when projector is off
                    if (isPowerOn || CHANNEL_TYPE_POWER.equals(channel.getUID().getId())) {
                        updateChannelState(channel);
                    }
                }
            }
        }, 0, (pollingInterval > 0) ? pollingInterval : DEFAULT_POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    @Override
    public void dispose() {
        cancelPollingJob();
        closeConnection();
        super.dispose();
    }

    private void updateChannelState(Channel channel) {
        try {
            if (!isLinked(channel.getUID()) && !CHANNEL_TYPE_POWER.equals(channel.getUID().getId())) {
                return;
            }

            final State state = queryDataFromDevice(BenqProjectorCommandType.getCommandType(channel.getUID().getId()));

            if (state != null) {
                if (isLinked(channel.getUID())) {
                    updateState(channel.getUID(), state);
                }
                // the first valid response will cause the thing to go ONLINE
                if (state != UnDefType.UNDEF) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown channel {}, exception: {}", channel.getUID().getId(), e.getMessage());
        }
    }

    @Nullable
    private State queryDataFromDevice(BenqProjectorCommandType commandType) {
        final BenqProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            switch (commandType) {
                case POWER:
                    final OnOffType powerState = remoteController.getPower();
                    isPowerOn = powerState == OnOffType.ON;
                    return powerState;
                case SOURCE:
                    return new StringType(remoteController.getSource());
                case PICTUREMODE:
                    return new StringType(remoteController.getPictureMode());
                case ASPECTRATIO:
                    return new StringType(remoteController.getAspectRatio());
                case FREEZE:
                    return remoteController.getFreeze();
                case BLANK:
                    return remoteController.getBlank();
                case DIRECTCMD:
                    break;
                case LAMPTIME:
                    return new DecimalType(remoteController.getLampTime());
                default:
                    logger.warn("Unknown '{}' command!", commandType);
                    return UnDefType.UNDEF;
            }
        } catch (BenqProjectorCommandException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
            return UnDefType.UNDEF;
        } catch (BenqProjectorException e) {
            logger.debug("Couldn't execute command '{}', {}", commandType, e.getMessage());
            closeConnection();
            return null;
        }

        return UnDefType.UNDEF;
    }

    private void sendDataToDevice(BenqProjectorCommandType commandType, Command command) {
        final BenqProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            switch (commandType) {
                case POWER:
                    remoteController.setPower((OnOffType) command);
                    isPowerOn = (OnOffType) command == OnOffType.ON;
                    break;
                case SOURCE:
                    remoteController.setSource(command.toString());
                    break;
                case PICTUREMODE:
                    remoteController.setPictureMode(command.toString());
                    break;
                case ASPECTRATIO:
                    remoteController.setAspectRatio(command.toString());
                    break;
                case FREEZE:
                    remoteController.setFreeze((OnOffType) command);
                    break;
                case BLANK:
                    remoteController.setBlank((OnOffType) command);
                    break;
                case DIRECTCMD:
                    remoteController.sendDirectCommand(command.toString());
                    break;
                default:
                    logger.warn("Unknown channel: '{}'!", commandType);
                    break;
            }
        } catch (BenqProjectorCommandException | ClassCastException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
        } catch (BenqProjectorException e) {
            logger.warn("Couldn't execute command '{}', {}", commandType, e.getMessage());
            closeConnection();
        }
    }

    private void closeConnection() {
        if (device.isPresent()) {
            try {
                logger.debug("Closing connection to device '{}'", this.thing.getUID());
                device.get().disconnect();
                updateStatus(ThingStatus.OFFLINE);
            } catch (BenqProjectorException e) {
                logger.debug("Error occurred when closing connection to device '{}'", this.thing.getUID(), e);
            }
        }
    }
}
