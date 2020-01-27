/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.AspectRatio;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Background;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Color;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.ColorMode;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Gamma;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Luminance;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.PowerStatus;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Source;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorDevice.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpsonProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila, Yannick Schaus - Initial contribution
 */
@NonNullByDefault
public class EpsonProjectorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorHandler.class);

    private @Nullable EpsonProjectorConfiguration config;

    private @Nullable EpsonProjectorDevice device;
    private @Nullable ScheduledFuture<?> pollingJob;

    public EpsonProjectorHandler(Thing thing) {
        super(thing);
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
        config = getConfigAs(EpsonProjectorConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            if (StringUtils.isNotEmpty(config.serialPort)) {
                device = new EpsonProjectorDevice(config.serialPort);
            } else if (StringUtils.isNotEmpty(config.host) && config.port > 0) {
                device = new EpsonProjectorDevice(config.host, config.port);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
            }

            try {
                device.connect();
                updateStatus(ThingStatus.ONLINE);

                int pollingInterval = config.pollingInterval;
                if (pollingInterval == 0) {
                    pollingInterval = 10000;
                }

                List<Channel> channels = this.thing.getChannels();

                pollingJob = scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        for (Channel channel : channels) {
                            updateChannelState(channel);
                        }
                    }
                }, 0, pollingInterval, TimeUnit.MILLISECONDS);

            } catch (EpsonProjectorException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        closeConnection();
        super.dispose();
    }

    private void updateChannelState(Channel channel) {
        try {
            if (!isLinked(channel.getUID())) {
                return;
            }

            EpsonProjectorCommandType epsonCommand = EpsonProjectorCommandType.getCommandType(channel.getUID().getId());

            State state = queryDataFromDevice(epsonCommand);

            updateState(channel.getUID(), state);
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal command {}", channel.getUID().getId());
        }
    }

    private State queryDataFromDevice(EpsonProjectorCommandType commmandType) {

        EpsonProjectorDevice remoteController = device;

        if (remoteController == null) {
            logger.error("Device is not initialized: '{}'", this.thing.getUID());
            return UnDefType.UNDEF;
        }

        try {
            if (remoteController.isConnected() == false) {
                remoteController.connect();
            }

            switch (commmandType) {
                case AKEYSTONE:
                    int autoKeystone = remoteController.getAutoKeystone();
                    return new DecimalType(autoKeystone);
                case ASPECT_RATIO:
                    AspectRatio aspectRatio = remoteController.getAspectRatio();
                    return new StringType(aspectRatio.toString());
                case BACKGROUND:
                    Background background = remoteController.getBackground();
                    return new StringType(background.toString());
                case BRIGHTNESS:
                    int brightness = remoteController.getBrightness();
                    return new DecimalType(brightness);
                case COLOR:
                    Color color = remoteController.getColor();
                    return new StringType(color.toString());
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
                case DIRECT_SOURCE:
                    int directSource = remoteController.getDirectSource();
                    return new DecimalType(directSource);
                case ERR_CODE:
                    int err = remoteController.getError();
                    return new DecimalType(err);
                case ERR_MESSAGE:
                    String errString = remoteController.getErrorString();
                    return new StringType(errString);
                case FLESH_TEMP:
                    int fleshColor = remoteController.getFleshColor();
                    return new DecimalType(fleshColor);
                case GAIN_BLUE:
                    int gainBlue = remoteController.getGainBlue();
                    return new DecimalType(gainBlue);
                case GAIN_GREEN:
                    int gainGreen = remoteController.getGainGreen();
                    return new DecimalType(gainGreen);
                case GAIN_RED:
                    int gainRed = remoteController.getGainRed();
                    return new DecimalType(gainRed);
                case GAMMA:
                    Gamma gamma = remoteController.getGamma();
                    return new StringType(gamma.toString());
                case GAMMA_STEP:
                    logger.warn("Get '{}' not implemented!", commmandType.toString());
                    return UnDefType.UNDEF;
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
                case OFFSET_BLUE:
                    int offsetBlue = remoteController.getOffsetBlue();
                    return new DecimalType(offsetBlue);
                case OFFSET_GREEN:
                    int offsetGreen = remoteController.getOffsetGreen();
                    return new DecimalType(offsetGreen);
                case OFFSET_RED:
                    int offsetRed = remoteController.getOffsetRed();
                    return new DecimalType(offsetRed);
                case POWER:
                    PowerStatus powerStatus = remoteController.getPowerStatus();

                    if (powerStatus == PowerStatus.ON) {
                        return OnOffType.ON;
                    } else {
                        return OnOffType.OFF;
                    }
                case POWER_STATE:
                    PowerStatus powerStatus1 = remoteController.getPowerStatus();
                    return new StringType(powerStatus1.toString());
                case SHARP:
                    logger.warn("Get '{}' not implemented!", commmandType.toString());
                    return UnDefType.UNDEF;
                case SOURCE:
                    Source source = remoteController.getSource();
                    return new StringType(source.toString());
                case SYNC:
                    int sync = remoteController.getSync();
                    return new DecimalType(sync);
                case TINT:
                    int tint = remoteController.getTint();
                    return new DecimalType(tint);
                case TRACKING:
                    int tracking = remoteController.getTracking();
                    return new DecimalType(tracking);
                case VKEYSTONE:
                    int vKeystone = remoteController.getVerticalKeystone();
                    return new DecimalType(vKeystone);
                case VPOSITION:
                    int vPosition = remoteController.getVerticalPosition();
                    return new DecimalType(vPosition);
                case VREVERSE:
                    Switch vReverse = remoteController.getVerticalReverse();
                    return vReverse == Switch.ON ? OnOffType.ON : OnOffType.OFF;
                default:
                    logger.warn("Unknown '{}' command!", commmandType);
                    return UnDefType.UNDEF;
            }

        } catch (EpsonProjectorException e) {
            logger.debug("Couldn't execute command '{}', {}", commmandType.toString(), e.getMessage());
            // closeConnection();
        } catch (Exception e) {
            logger.warn("Couldn't retrieve state for command '{}', {}", commmandType.toString(), e.getMessage());
            return UnDefType.UNDEF;
        }

        return UnDefType.UNDEF;
    }

    private void sendDataToDevice(EpsonProjectorCommandType commmandType, Command command) {

        EpsonProjectorDevice remoteController = device;

        if (remoteController == null) {
            logger.error("Device is not initialized: '{}'", this.thing.getUID());
            return;
        }

        try {

            if (remoteController.isConnected() == false) {
                remoteController.connect();
            }

            switch (commmandType) {
                case AKEYSTONE:
                    remoteController.setAutoKeystone(((DecimalType) command).intValue());
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
                case COLOR:
                    remoteController.setColor(Color.valueOf(command.toString()));
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
                case DIRECT_SOURCE:
                    remoteController.setDirectSource(((DecimalType) command).intValue());
                    break;
                case ERR_CODE:
                    logger.error("'{}' is read only parameter", commmandType);
                    break;
                case ERR_MESSAGE:
                    logger.error("'{}' is read only parameter", commmandType);
                    break;
                case FLESH_TEMP:
                    remoteController.setFleshColor(((DecimalType) command).intValue());
                    break;
                case GAIN_BLUE:
                    remoteController.setGainBlue(((DecimalType) command).intValue());
                    break;
                case GAIN_GREEN:
                    remoteController.setGainGreen(((DecimalType) command).intValue());
                    break;
                case GAIN_RED:
                    remoteController.setGainRed(((DecimalType) command).intValue());
                    break;
                case GAMMA:
                    remoteController.setGamma(Gamma.valueOf(command.toString()));
                    break;
                case GAMMA_STEP:
                    logger.warn("Set '{}' not implemented!", commmandType.toString());
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
                    remoteController.sendKeyCode(((DecimalType) command).intValue());
                    break;
                case LAMP_TIME:
                    logger.error("'{}' is read only parameter", commmandType);
                    break;
                case LUMINANCE:
                    remoteController.setLuminance(Luminance.valueOf(command.toString()));
                    break;
                case MUTE:
                    remoteController.setMute((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                case OFFSET_BLUE:
                    remoteController.setOffsetBlue(((DecimalType) command).intValue());
                    break;
                case OFFSET_GREEN:
                    remoteController.setOffsetGreen(((DecimalType) command).intValue());
                    break;
                case OFFSET_RED:
                    remoteController.setOffsetRed(((DecimalType) command).intValue());
                    break;
                case POWER:
                    remoteController.setPower((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                case POWER_STATE:
                    logger.error("'{}' is read only parameter", commmandType);
                    break;
                case SHARP:
                    logger.warn("Set '{}' not implemented!", commmandType.toString());
                    break;
                case SOURCE:
                    remoteController.setSource(Source.valueOf(command.toString()));
                    break;
                case SYNC:
                    remoteController.setSync(((DecimalType) command).intValue());
                    break;
                case TINT:
                    remoteController.setTint(((DecimalType) command).intValue());
                    break;
                case TRACKING:
                    remoteController.setTracking(((DecimalType) command).intValue());
                    break;
                case VKEYSTONE:
                    remoteController.setVerticalKeystone(((DecimalType) command).intValue());
                    break;
                case VPOSITION:
                    remoteController.setVerticalPosition(((DecimalType) command).intValue());
                    break;
                case VREVERSE:
                    remoteController.setVerticalReverse((command == OnOffType.ON ? Switch.ON : Switch.OFF));
                    break;
                default:
                    logger.warn("Unknown '{}' command!", commmandType);
                    break;
            }

        } catch (EpsonProjectorException e) {
            logger.warn("Couldn't execute command '{}', {}", commmandType, e.getMessage());
            // closeConnection();
        }
    }

    private void closeConnection() {
        EpsonProjectorDevice remoteController = device;

        if (remoteController != null) {
            try {
                logger.debug("Closing connection to device '{}'", this.thing.getUID());
                remoteController.disconnect();
            } catch (EpsonProjectorException e) {
                logger.debug("Error occurred when closing connection to device '{}'", this.thing.getUID());
            }
        }
    }

}
