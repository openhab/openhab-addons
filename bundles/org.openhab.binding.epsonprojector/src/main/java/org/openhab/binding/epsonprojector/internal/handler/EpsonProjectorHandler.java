/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.epsonprojector.internal.configuration.EpsonProjectorConfiguration;
import org.openhab.binding.epsonprojector.internal.enums.AspectRatio;
import org.openhab.binding.epsonprojector.internal.enums.Background;
import org.openhab.binding.epsonprojector.internal.enums.ColorMode;
import org.openhab.binding.epsonprojector.internal.enums.Gamma;
import org.openhab.binding.epsonprojector.internal.enums.Luminance;
import org.openhab.binding.epsonprojector.internal.enums.PowerStatus;
import org.openhab.binding.epsonprojector.internal.enums.Switch;
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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private @Nullable ScheduledFuture<?> pollingJob;
    private Optional<EpsonProjectorDevice> device = Optional.empty();

    private boolean isPowerOn = false;
    private int maxVolume = 20;
    private int curVolumeStep = -1;
    private int pollingInterval = DEFAULT_POLLING_INTERVAL_SEC;

    public EpsonProjectorHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            Channel channel = this.thing.getChannel(channelUID);
            if (channel != null) {
                updateChannelState(channel);
            }
        } else {
            EpsonProjectorCommandType epsonCommand = EpsonProjectorCommandType.getCommandType(channelId);
            sendDataToDevice(epsonCommand, command);
        }
    }

    @Override
    public void initialize() {
        EpsonProjectorConfiguration config = getConfigAs(EpsonProjectorConfiguration.class);
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PROJECTOR_SERIAL.equals(thingTypeUID)) {
            device = Optional.of(new EpsonProjectorDevice(serialPortManager, config));
        } else if (THING_TYPE_PROJECTOR_TCP.equals(thingTypeUID)) {
            device = Optional.of(new EpsonProjectorDevice(config));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

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
            List<Channel> channels = this.thing.getChannels();
            for (Channel channel : channels) {
                // only query power & lamp time when projector is off
                if (isPowerOn || (channel.getUID().getId().equals(CHANNEL_TYPE_POWER)
                        || channel.getUID().getId().equals(CHANNEL_TYPE_LAMPTIME))) {
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

            EpsonProjectorCommandType epsonCommand = EpsonProjectorCommandType.getCommandType(channel.getUID().getId());

            State state = queryDataFromDevice(epsonCommand);

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
        EpsonProjectorDevice remoteController = device.get();

        try {
            if (!remoteController.isConnected()) {
                remoteController.connect();
            }

            if (!remoteController.isReady()) {
                logger.debug("Refusing command {} while not ready", commandType.toString());
                return null;
            }

            switch (commandType) {
                case AKEYSTONE:
                    Switch autoKeystone = remoteController.getAutoKeystone();
                    return autoKeystone == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case ASPECT_RATIO:
                    AspectRatio aspectRatio = remoteController.getAspectRatio();
                    return new StringType(aspectRatio.toString());
                case BACKGROUND:
                    Background background = remoteController.getBackground();
                    return new StringType(background.toString());
                case BRIGHTNESS:
                    int brightness = remoteController.getBrightness();
                    return new DecimalType(brightness);
                case COLOR_MODE:
                    ColorMode colorMode = remoteController.getColorMode();
                    return new StringType(colorMode.toString());
                case COLOR_TEMP:
                    int ctemp = remoteController.getColorTemperature();
                    return new DecimalType(ctemp);
                case CONTRAST:
                    int contrast = remoteController.getContrast();
                    return new DecimalType(contrast);
                case DENSITY:
                    int density = remoteController.getDensity();
                    return new DecimalType(density);
                case ERR_CODE:
                    int err = remoteController.getError();
                    return new DecimalType(err);
                case ERR_MESSAGE:
                    String errString = remoteController.getErrorString();
                    return new StringType(errString);
                case FLESH_TEMP:
                    int fleshColor = remoteController.getFleshColor();
                    return new DecimalType(fleshColor);
                case FREEZE:
                    Switch freeze = remoteController.getFreeze();
                    return freeze == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case GAMMA:
                    Gamma gamma = remoteController.getGamma();
                    return new StringType(gamma.toString());
                case HKEYSTONE:
                    int hKeystone = remoteController.getHorizontalKeystone();
                    return new DecimalType(hKeystone);
                case HPOSITION:
                    int hPosition = remoteController.getHorizontalPosition();
                    return new DecimalType(hPosition);
                case HREVERSE:
                    Switch hReverse = remoteController.getHorizontalReverse();
                    return hReverse == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case KEY_CODE:
                    break;
                case LAMP_TIME:
                    int lampTime = remoteController.getLampTime();
                    return new DecimalType(lampTime);
                case LUMINANCE:
                    Luminance luminance = remoteController.getLuminance();
                    return new StringType(luminance.toString());
                case MUTE:
                    Switch mute = remoteController.getMute();
                    return mute == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                case POWER:
                    PowerStatus powerStatus = remoteController.getPowerStatus();
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
                case POWER_STATE:
                    return null;
                case SOURCE:
                    return new StringType(remoteController.getSource());
                case TINT:
                    int tint = remoteController.getTint();
                    return new DecimalType(tint);
                case VKEYSTONE:
                    int vKeystone = remoteController.getVerticalKeystone();
                    return new DecimalType(vKeystone);
                case VOLUME:
                    // Each volume step falls within several percentage values, only change the UI if the polled step is
                    // different than the step of the current percent. Without this logic the UI would snap back to the
                    // closest whole % value for that step. e.g., UI set to 51% would snap back to 50% on the next
                    // polling update.
                    int volumeStep = remoteController.getVolume(maxVolume);
                    if (curVolumeStep != volumeStep) {
                        curVolumeStep = volumeStep;
                        return new PercentType(
                                BigDecimal.valueOf(Math.round(curVolumeStep / (double) maxVolume * 100.0)));
                    }
                    return null;
                case VPOSITION:
                    int vPosition = remoteController.getVerticalPosition();
                    return new DecimalType(vPosition);
                case VREVERSE:
                    Switch vReverse = remoteController.getVerticalReverse();
                    return vReverse == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                default:
                    logger.warn("Unknown '{}' command!", commandType);
                    return UnDefType.UNDEF;
            }
        } catch (EpsonProjectorCommandException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
            return UnDefType.UNDEF;
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
                case AKEYSTONE:
                    remoteController.setAutoKeystone((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                case ASPECT_RATIO:
                    remoteController.setAspectRatio(AspectRatio.valueOf(command.toString()));
                    break;
                case BACKGROUND:
                    remoteController.setBackground(Background.valueOf(command.toString()));
                    break;
                case BRIGHTNESS:
                    remoteController.setBrightness(((DecimalType) command).intValue());
                    break;
                case COLOR_MODE:
                    remoteController.setColorMode(ColorMode.valueOf(command.toString()));
                    break;
                case COLOR_TEMP:
                    remoteController.setColorTemperature(((DecimalType) command).intValue());
                    break;
                case CONTRAST:
                    remoteController.setContrast(((DecimalType) command).intValue());
                    break;
                case DENSITY:
                    remoteController.setDensity(((DecimalType) command).intValue());
                    break;
                case ERR_CODE:
                    logger.warn("'{}' is read only parameter", commandType);
                    break;
                case ERR_MESSAGE:
                    logger.warn("'{}' is read only parameter", commandType);
                    break;
                case FLESH_TEMP:
                    remoteController.setFleshColor(((DecimalType) command).intValue());
                    break;
                case FREEZE:
                    remoteController.setFreeze(command == OnOffType.ON ? Switch.ON : Switch.OFF);
                    break;
                case GAMMA:
                    remoteController.setGamma(Gamma.valueOf(command.toString()));
                    break;
                case HKEYSTONE:
                    remoteController.setHorizontalKeystone(((DecimalType) command).intValue());
                    break;
                case HPOSITION:
                    remoteController.setHorizontalPosition(((DecimalType) command).intValue());
                    break;
                case HREVERSE:
                    remoteController.setHorizontalReverse((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                case KEY_CODE:
                    remoteController.sendKeyCode(command.toString());
                    break;
                case LAMP_TIME:
                    logger.warn("'{}' is read only parameter", commandType);
                    break;
                case LUMINANCE:
                    remoteController.setLuminance(Luminance.valueOf(command.toString()));
                    break;
                case MUTE:
                    remoteController.setMute((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                case POWER:
                    if (command == OnOffType.ON) {
                        remoteController.setPower(Switch.ON);
                        isPowerOn = true;
                    } else {
                        remoteController.setPower(Switch.OFF);
                        isPowerOn = false;
                    }
                    break;
                case POWER_STATE:
                    logger.warn("'{}' is read only parameter", commandType);
                    break;
                case SOURCE:
                    remoteController.setSource(command.toString());
                    break;
                case TINT:
                    remoteController.setTint(((DecimalType) command).intValue());
                    break;
                case VKEYSTONE:
                    remoteController.setVerticalKeystone(((DecimalType) command).intValue());
                    break;
                case VOLUME:
                    int newVolumeStep = (int) Math.round(((PercentType) command).doubleValue() / 100.0 * maxVolume);
                    if (curVolumeStep != newVolumeStep) {
                        curVolumeStep = newVolumeStep;
                        remoteController.setVolume(curVolumeStep, maxVolume);
                    }
                    break;
                case VPOSITION:
                    remoteController.setVerticalPosition(((DecimalType) command).intValue());
                    break;
                case VREVERSE:
                    remoteController.setVerticalReverse((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                default:
                    logger.warn("Unknown '{}' command!", commandType);
                    break;
            }
        } catch (EpsonProjectorCommandException e) {
            logger.debug("Error executing command '{}', {}", commandType, e.getMessage());
        } catch (EpsonProjectorException e) {
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
            } catch (EpsonProjectorException e) {
                logger.debug("Error occurred when closing connection to device '{}'", this.thing.getUID(), e);
            }
        }
    }
}
