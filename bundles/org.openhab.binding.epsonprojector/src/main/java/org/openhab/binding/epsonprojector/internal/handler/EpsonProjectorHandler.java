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
package org.openhab.binding.epsonprojector.internal.handler;

import static org.openhab.binding.epsonprojector.internal.EpsonProjectorBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorCommandException;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorCommandType;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorException;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorPasswordException;
import org.openhab.binding.epsonprojector.internal.EpsonStateDescriptionOptionProvider;
import org.openhab.binding.epsonprojector.internal.configuration.EpsonProjectorConfiguration;
import org.openhab.binding.epsonprojector.internal.enums.AspectRatio;
import org.openhab.binding.epsonprojector.internal.enums.Background;
import org.openhab.binding.epsonprojector.internal.enums.ColorMode;
import org.openhab.binding.epsonprojector.internal.enums.Gamma;
import org.openhab.binding.epsonprojector.internal.enums.Luminance;
import org.openhab.binding.epsonprojector.internal.enums.PowerStatus;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
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
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpsonProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila, Yannick Schaus - Initial contribution
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public class EpsonProjectorHandler extends BaseThingHandler {
    private static final int DEFAULT_POLLING_INTERVAL_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorHandler.class);
    private final SerialPortManager serialPortManager;
    private final EpsonStateDescriptionOptionProvider stateDescriptionProvider;
    private final Object sequenceLock = new Object();

    private @Nullable ScheduledFuture<?> pollingJob;
    private Optional<EpsonProjectorDevice> device = Optional.empty();

    private boolean loadSourceList = false;
    private boolean isSourceListLoaded = false;
    private boolean isMetadataLoaded = false;
    private boolean isPowerOn = false;
    private int maxVolume = 20;
    private int curVolumeStep = -1;
    private int pollingInterval = DEFAULT_POLLING_INTERVAL_SEC;

    public EpsonProjectorHandler(Thing thing, SerialPortManager serialPortManager,
            EpsonStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.serialPortManager = serialPortManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (sequenceLock) {
            if (command instanceof RefreshType) {
                final Channel channel = this.thing.getChannel(channelUID);
                if (channel != null) {
                    updateChannelState(channel);
                }
            } else {
                sendDataToDevice(EpsonProjectorCommandType.getCommandType(channelUID.getId()), command);
            }
        }
    }

    @Override
    public void initialize() {
        final EpsonProjectorConfiguration config = getConfigAs(EpsonProjectorConfiguration.class);

        if (THING_TYPE_PROJECTOR_SERIAL.equals(thing.getThingTypeUID())) {
            device = Optional.of(new EpsonProjectorDevice(serialPortManager, config));
        } else if (THING_TYPE_PROJECTOR_TCP.equals(thing.getThingTypeUID())) {
            device = Optional.of(new EpsonProjectorDevice(config));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        loadSourceList = config.loadSourceList;
        maxVolume = config.maxVolume;
        pollingInterval = config.pollingInterval;
        device.ifPresent(dev -> dev.setScheduler(scheduler));
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
                for (Channel channel : thing.getChannels()) {
                    // only query power & lamp time when projector is off
                    if (isPowerOn || (CHANNEL_TYPE_POWER.equals(channel.getUID().getId())
                            || CHANNEL_TYPE_LAMPTIME.equals(channel.getUID().getId()))) {
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

            final State state = queryDataFromDevice(EpsonProjectorCommandType.getCommandType(channel.getUID().getId()));

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
    private State queryDataFromDevice(EpsonProjectorCommandType commandType) {
        final EpsonProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            if (!remoteController.isReady()) {
                logger.debug("Refusing command {} while not ready", commandType.toString());
                return null;
            }

            // When polling for PWR status, also try to get SOURCELIST when enabled until successful
            if (EpsonProjectorCommandType.POWER == commandType && loadSourceList && !isSourceListLoaded) {
                final List<StateOption> sourceListOptions = remoteController.getSourceList();

                // If a SOURCELIST was retrieved, load it in the source channel state options
                if (!sourceListOptions.isEmpty()) {
                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_TYPE_SOURCE),
                            sourceListOptions);
                    isSourceListLoaded = true;
                }
            }

            // When polling for PWR status, also try to get projector model and serial number
            if (EpsonProjectorCommandType.POWER == commandType && !isMetadataLoaded) {
                try {
                    thing.setProperty(Thing.PROPERTY_MODEL_ID, remoteController.getModel());
                } catch (EpsonProjectorCommandException e) {
                    logger.debug("PJINFO? command is not supported");
                }

                try {
                    thing.setProperty(Thing.PROPERTY_SERIAL_NUMBER, remoteController.getSerialNumber());
                } catch (EpsonProjectorCommandException e) {
                    logger.debug("SNO? command is not supported");
                }
                isMetadataLoaded = true;
            }

            switch (commandType) {
                case AUTOKEYSTONE:
                    return remoteController.getAutoKeystone();
                case ASPECTRATIO:
                    return new StringType(remoteController.getAspectRatio().toString());
                case BACKGROUND:
                    return new StringType(remoteController.getBackground().toString());
                case BRIGHTNESS:
                    return new DecimalType(remoteController.getBrightness());
                case COLORMODE:
                    return new StringType(remoteController.getColorMode().toString());
                case COLORTEMPERATURE:
                    return new DecimalType(remoteController.getColorTemperature());
                case CONTRAST:
                    return new DecimalType(remoteController.getContrast());
                case DENSITY:
                    return new DecimalType(remoteController.getDensity());
                case ERRCODE:
                    return new DecimalType(remoteController.getError());
                case ERRMESSAGE:
                    return new StringType(remoteController.getErrorString());
                case FREEZE:
                    return remoteController.getFreeze();
                case FLESHTEMPERATURE:
                    return new DecimalType(remoteController.getFleshColor());
                case GAMMA:
                    return new StringType(remoteController.getGamma().toString());
                case HORIZONTALKEYSTONE:
                    return new DecimalType(remoteController.getHorizontalKeystone());
                case HORIZONTALPOSITION:
                    return new DecimalType(remoteController.getHorizontalPosition());
                case HORIZONTALREVERSE:
                    return remoteController.getHorizontalReverse();
                case KEYCODE:
                    break;
                case LAMPTIME:
                    return new DecimalType(remoteController.getLampTime());
                case LUMINANCE:
                    return new StringType(remoteController.getLuminance().toString());
                case MUTE:
                    return remoteController.getMute();
                case POWER:
                    final PowerStatus powerStatus = remoteController.getPowerStatus();
                    if (isLinked(CHANNEL_TYPE_POWERSTATE)) {
                        updateState(CHANNEL_TYPE_POWERSTATE, new StringType(powerStatus.toString()));
                    }

                    if (powerStatus == PowerStatus.ON || powerStatus == PowerStatus.WARMUP) {
                        isPowerOn = true;
                        return OnOffType.ON;
                    } else {
                        isPowerOn = false;
                        return OnOffType.OFF;
                    }
                case POWERSTATE:
                    return null;
                case SOURCE:
                    return new StringType(remoteController.getSource());
                case TINT:
                    return new DecimalType(remoteController.getTint());
                case VERTICALKEYSTONE:
                    return new DecimalType(remoteController.getVerticalKeystone());
                case VERTICALPOSITION:
                    return new DecimalType(remoteController.getVerticalPosition());
                case VERTICALREVERSE:
                    return remoteController.getVerticalReverse();
                case VOLUME:
                    // Each volume step falls within several percentage values, only change the UI if the polled step is
                    // different than the step of the current percent. Without this logic the UI would snap back to the
                    // closest whole % value for that step. e.g., UI set to 51% would snap back to 50% on the next
                    // polling update.
                    final int volumeStep = remoteController.getVolume();
                    if (curVolumeStep != volumeStep) {
                        curVolumeStep = volumeStep;
                        return new PercentType(
                                BigDecimal.valueOf(Math.round(curVolumeStep / (double) maxVolume * 100.0)));
                    }
                    return null;
                default:
                    logger.warn("Unknown '{}' command!", commandType);
                    return UnDefType.UNDEF;
            }
        } catch (EpsonProjectorCommandException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
            return UnDefType.UNDEF;
        } catch (EpsonProjectorPasswordException e) {
            logger.debug("Password error: {}", e.getMessage());
            closeConnection(e.getMessage());
        } catch (EpsonProjectorException e) {
            logger.debug("Couldn't execute command '{}', {}", commandType, e.getMessage());
            closeConnection();
            return null;
        }

        return UnDefType.UNDEF;
    }

    private void sendDataToDevice(EpsonProjectorCommandType commandType, Command command) {
        EpsonProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            if (!remoteController.isReady()) {
                logger.debug("Refusing command '{}' while not ready", commandType.toString());
                return;
            }

            switch (commandType) {
                case AUTOKEYSTONE:
                    remoteController.setAutoKeystone((OnOffType) command);
                    break;
                case ASPECTRATIO:
                    remoteController.setAspectRatio(AspectRatio.valueOf(command.toString()));
                    break;
                case BACKGROUND:
                    remoteController.setBackground(Background.valueOf(command.toString()));
                    break;
                case BRIGHTNESS:
                    remoteController.setBrightness(((DecimalType) command).intValue());
                    break;
                case COLORMODE:
                    remoteController.setColorMode(ColorMode.valueOf(command.toString()));
                    break;
                case COLORTEMPERATURE:
                    remoteController.setColorTemperature(((DecimalType) command).intValue());
                    break;
                case CONTRAST:
                    remoteController.setContrast(((DecimalType) command).intValue());
                    break;
                case DENSITY:
                    remoteController.setDensity(((DecimalType) command).intValue());
                    break;
                case FLESHTEMPERATURE:
                    remoteController.setFleshColor(((DecimalType) command).intValue());
                    break;
                case FREEZE:
                    remoteController.setFreeze((OnOffType) command);
                    break;
                case GAMMA:
                    remoteController.setGamma(Gamma.valueOf(command.toString()));
                    break;
                case HORIZONTALKEYSTONE:
                    remoteController.setHorizontalKeystone(((DecimalType) command).intValue());
                    break;
                case HORIZONTALPOSITION:
                    remoteController.setHorizontalPosition(((DecimalType) command).intValue());
                    break;
                case HORIZONTALREVERSE:
                    remoteController.setHorizontalReverse((OnOffType) command);
                    break;
                case KEYCODE:
                    remoteController.sendKeyCode(command.toString());
                    break;
                case LUMINANCE:
                    remoteController.setLuminance(Luminance.valueOf(command.toString()));
                    break;
                case MUTE:
                    remoteController.setMute((OnOffType) command);
                    break;
                case POWER:
                    remoteController.setPower((OnOffType) command);
                    isPowerOn = (OnOffType) command == OnOffType.ON;
                    break;
                case SOURCE:
                    remoteController.setSource(command.toString());
                    break;
                case TINT:
                    remoteController.setTint(((DecimalType) command).intValue());
                    break;
                case VERTICALKEYSTONE:
                    remoteController.setVerticalKeystone(((DecimalType) command).intValue());
                    break;
                case VERTICALPOSITION:
                    remoteController.setVerticalPosition(((DecimalType) command).intValue());
                    break;
                case VERTICALREVERSE:
                    remoteController.setVerticalReverse((OnOffType) command);
                    break;
                case VOLUME:
                    final int newVolumeStep = (int) Math
                            .round(((PercentType) command).doubleValue() / 100.0 * maxVolume);
                    if (curVolumeStep != newVolumeStep) {
                        curVolumeStep = newVolumeStep;
                        remoteController.setVolume(curVolumeStep);
                    }
                    break;
                case ERRCODE:
                case ERRMESSAGE:
                case LAMPTIME:
                case POWERSTATE:
                    logger.warn("'{}' is a read-only channel", commandType);
                    break;
                default:
                    logger.warn("Unknown channel: '{}'!", commandType);
                    break;
            }
        } catch (EpsonProjectorCommandException | ClassCastException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
        } catch (EpsonProjectorPasswordException e) {
            logger.debug("Password error: {}", e.getMessage());
            closeConnection(e.getMessage());
        } catch (EpsonProjectorException e) {
            logger.warn("Couldn't execute command '{}', {}", commandType, e.getMessage());
            closeConnection();
        }
    }

    /**
     * Method to handle connection closing and updating ThingStatus without passing a passwordError
     */
    private void closeConnection() {
        closeConnection(null);
    }

    /**
     * Method to handle connection closing and updating ThingStatus
     *
     * @param passwordError sends password error info to the ThingStatusDetail and stops the polling
     */
    private void closeConnection(@Nullable String passwordError) {
        if (device.isPresent()) {
            try {
                logger.debug("Closing connection to device '{}'", this.thing.getUID());
                device.get().disconnect();
                isSourceListLoaded = false;
                isMetadataLoaded = false;
                isPowerOn = false;

                if (passwordError != null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, passwordError);
                    cancelPollingJob();
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
                if (isLinked(CHANNEL_TYPE_POWERSTATE)) {
                    updateState(CHANNEL_TYPE_POWERSTATE, new StringType(PowerStatus.OFFLINE.toString()));
                }
            } catch (EpsonProjectorException e) {
                logger.debug("Error occurred when closing connection to device '{}'", this.thing.getUID(), e);
            }
        }
    }
}
