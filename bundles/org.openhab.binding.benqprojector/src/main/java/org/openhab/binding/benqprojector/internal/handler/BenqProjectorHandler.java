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
package org.openhab.binding.benqprojector.internal.handler;

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.util.List;
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
import org.openhab.binding.benqprojector.internal.enums.Switch;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
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
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            Channel channel = this.thing.getChannel(channelUID);
            if (channel != null && getThing().getStatus() == ThingStatus.ONLINE) {
                updateChannelState(channel);
            }
        } else {
            BenqProjectorCommandType benqCommand = BenqProjectorCommandType.getCommandType(channelId);
            sendDataToDevice(benqCommand, command);
        }
    }

    @Override
    public void initialize() {
        BenqProjectorConfiguration config = getConfigAs(BenqProjectorConfiguration.class);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PROJECTOR_SERIAL.equals(thingTypeUID)) {
            device = Optional.of(new BenqProjectorDevice(serialPortManager, config));
        } else if (THING_TYPE_PROJECTOR_TCP.equals(thingTypeUID)) {
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
            List<Channel> channels = this.thing.getChannels();
            for (Channel channel : channels) {
                // only query power when projector is off
                if (isPowerOn || channel.getUID().getId().equals(CHANNEL_TYPE_POWER)) {
                    updateChannelState(channel);
                }
            }
        }, 0, (pollingInterval > 0) ? pollingInterval : DEFAULT_POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
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
            if (!isLinked(channel.getUID()) && !channel.getUID().getId().equals(CHANNEL_TYPE_POWER)) {
                return;
            }

            BenqProjectorCommandType benqCommand = BenqProjectorCommandType.getCommandType(channel.getUID().getId());

            State state = queryDataFromDevice(benqCommand);

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
        BenqProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            switch (commandType) {
                case POWER:
                    Switch powerStatus = remoteController.getPowerStatus();
                    if (powerStatus == Switch.ON) {
                        isPowerOn = true;
                        return OnOffType.ON;
                    } else {
                        isPowerOn = false;
                        return OnOffType.OFF;
                    }
                case SOURCE:
                    String source = remoteController.getSource();
                    if (source != null) {
                        return new StringType(source);
                    } else {
                        return UnDefType.UNDEF;
                    }
                case PICTURE_MODE:
                    String picturemode = remoteController.getPictureMode();
                    if (picturemode != null) {
                        return new StringType(picturemode);
                    } else {
                        return UnDefType.UNDEF;
                    }
                case ASPECT_RATIO:
                    String aspectratio = remoteController.getAspectRatio();
                    if (aspectratio != null) {
                        return new StringType(aspectratio);
                    } else {
                        return UnDefType.UNDEF;
                    }
                case FREEZE:
                    Switch freeze = remoteController.getFreeze();
                    return freeze == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case BLANK:
                    Switch blank = remoteController.getBlank();
                    return blank == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case DIRECTCMD:
                    break;
                case LAMP_TIME:
                    int lampTime = remoteController.getLampTime();
                    return new DecimalType(lampTime);
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
        BenqProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            switch (commandType) {
                case POWER:
                    if (command == OnOffType.ON) {
                        remoteController.setPower(Switch.ON);
                        isPowerOn = true;
                    } else {
                        remoteController.setPower(Switch.OFF);
                        isPowerOn = false;
                    }
                    break;
                case SOURCE:
                    remoteController.setSource(command.toString());
                    break;
                case PICTURE_MODE:
                    remoteController.setPictureMode(command.toString());
                    break;
                case ASPECT_RATIO:
                    remoteController.setAspectRatio(command.toString());
                    break;
                case FREEZE:
                    remoteController.setFreeze(command == OnOffType.ON ? Switch.ON : Switch.OFF);
                    break;
                case BLANK:
                    remoteController.setBlank(command == OnOffType.ON ? Switch.ON : Switch.OFF);
                    break;
                case DIRECTCMD:
                    remoteController.sendDirectCommand(command.toString());
                    break;
                default:
                    logger.warn("Unknown '{}' command!", commandType);
                    break;
            }
        } catch (BenqProjectorCommandException e) {
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
