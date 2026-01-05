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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusColorChannel;
import org.openhab.binding.velbus.internal.config.VelbusSensorConfig;
import org.openhab.binding.velbus.internal.packets.VelbusNewDimmerRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetColorPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetDimPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetScenePacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMB4LEDHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB4LEDHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB4LEDPWM_20));
    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) VelbusSensorConfig sensorConfig;

    private VelbusColorChannel[] colorChannels;
    private byte[] fadeModeChannels;
    private String moduleMode = "";

    private static final StringType DIRECT = new StringType("DIRECT");
    private static final StringType FADE_RATE = new StringType("FADE_RATE");
    private static final StringType FADE_TIME = new StringType("FADE_TIME");

    private static final String DIM = "4DIM";
    private static final String RGB = "RGB";
    private static final String RGBW = "RGBW";

    public VelbusVMB4LEDHandler(Thing thing) {
        super(thing, 0);

        colorChannels = new VelbusColorChannel[4];
        fadeModeChannels = new byte[4];
    }

    @Override
    public void initialize() {
        this.sensorConfig = getConfigAs(VelbusSensorConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
        initializeColorChannel();
        initializeFadeMode();
        initializeMode();
        initializeChannelStates();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = this.sensorConfig.refresh;

        if (refreshInterval > 0) {
            startAutomaticRefresh(refreshInterval);
        }
    }

    private void initializeColorChannel() {
        for (int i = 0; i <= 3; i++) {
            colorChannels[i] = new VelbusColorChannel(CURVE_TYPE_EXPONENTIAL);
        }
    }

    private void initializeFadeMode() {
        for (int i = 0; i <= 3; i++) {
            fadeModeChannels[i] = 0x00;
        }
    }

    private void initializeMode() {
        if (getConfig().containsKey(MODULE_MODE)) {
            String configMode = getConfig().get(MODULE_MODE).toString().toUpperCase();
            if (configMode.length() > 0) {
                switch (configMode) {
                    case DIM:
                    case RGB:
                    case RGBW:
                        moduleMode = configMode;
                        break;
                    default:
                        moduleMode = DIM;
                        logger.warn(
                                "VMB4LEDPWM-20 on address {} : Mode '{}' is not supported by this module. Will use mode '{}'.",
                                getModuleAddress().getAddress(), configMode, moduleMode);
                }
            }
        } else {
            moduleMode = DIM;
        }
    }

    private void initializeChannelStates() {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        sendNewDimmerReadoutRequest(velbusBridgeHandler, ALL_CHANNELS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        this.refreshJob = null;
        super.dispose();
    }

    private void startAutomaticRefresh(int refreshInterval) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            sendNewDimmerReadoutRequest(velbusBridgeHandler, ALL_CHANNELS);
        }, 0, refreshInterval, TimeUnit.SECONDS);
    }

    protected void sendNewDimmerReadoutRequest(VelbusBridgeHandler velbusBridgeHandler, byte channel) {
        VelbusNewDimmerRequestPacket packet = new VelbusNewDimmerRequestPacket(getModuleAddress().getAddress(),
                channel);

        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        byte address = getModuleAddress().getChannelIdentifier(channelUID).getAddress();
        byte channel = Integer.valueOf(getModuleAddress().getChannelNumber(channelUID)).byteValue();

        if (command instanceof RefreshType) {
            if (isBrightnessGroupChannel(channelUID)) {
                sendNewDimmerReadoutRequest(velbusBridgeHandler, channel);
            }
        } else if (isSceneGroupChannel(channelUID)) {
            if (command instanceof DecimalType decimalCommand) {
                byte scene = decimalCommand.byteValue();

                VelbusSetScenePacket packet = new VelbusSetScenePacket(address, channel, scene);
                velbusBridgeHandler.sendPacket(packet.getBytes());
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
            }
        } else if (isFadeModeGroupChannel(channelUID)) {
            if (command instanceof StringType stringCommand) {
                byte fadeMode = 0x00;

                if (stringCommand.equals(DIRECT)) {
                    fadeMode = 0x00;
                } else if (stringCommand.equals(FADE_RATE)) {
                    fadeMode = 0x01;
                } else if (stringCommand.equals(FADE_TIME)) {
                    fadeMode = 0x02;
                }

                fadeModeChannels[Byte.toUnsignedInt(channel) - 1] = fadeMode;
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
            }
        } else if (isBrightnessGroupChannel(channelUID)) {
            VelbusColorChannel colorChannel = colorChannels[Byte.toUnsignedInt(channel) - 1];

            if (command instanceof PercentType percentCommand) {
                colorChannel.setBrightness(percentCommand);

                VelbusSetDimPacket packet = new VelbusSetDimPacket(address, channel);
                packet.setDim(colorChannel.getBrightnessVelbus());
                packet.setMode(fadeModeChannels[Byte.toUnsignedInt(channel) - 1]);
                velbusBridgeHandler.sendPacket(packet.getBytes());
            } else if (command instanceof OnOffType onOffCommand) {
                VelbusSetDimPacket packet = new VelbusSetDimPacket(address, channel);
                if (onOffCommand == OnOffType.ON) {
                    packet.setLastUsedDim();
                } else {
                    colorChannel.setBrightness(0);
                    packet.setDim(colorChannel.getBrightnessVelbus());
                }
                packet.setMode(fadeModeChannels[Byte.toUnsignedInt(channel) - 1]);
                velbusBridgeHandler.sendPacket(packet.getBytes());
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
            }
        } else if (isDimWhiteGroupChannel(channelUID)) {
            if (!moduleMode.equals(DIM)) {
                VelbusColorChannel virtualColorChannel = colorChannels[0];

                if (command instanceof PercentType percentCommand) {
                    if (moduleMode.equals(RGB)) {
                        virtualColorChannel.setBrightness(percentCommand);

                        VelbusSetDimPacket packet = new VelbusSetDimPacket(address, (byte) 0x04);
                        packet.setDim(virtualColorChannel.getBrightnessVelbus());
                        velbusBridgeHandler.sendPacket(packet.getBytes());
                    } else if (moduleMode.equals(RGBW)) {
                        virtualColorChannel.setWhite(percentCommand);

                        VelbusSetColorPacket packet = new VelbusSetColorPacket(address, (byte) 0x01);
                        packet.setWhite(virtualColorChannel.getWhiteVelbus());
                        velbusBridgeHandler.sendPacket(packet.getBytes());
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported when in mode '" + moduleMode + "'.");
            }
        } else if (isVirtualLightChannel(channelUID)) {
            if (!moduleMode.equals(DIM)) {
                VelbusColorChannel virtualColorChannel = colorChannels[0];

                if (command instanceof HSBType hsbCommand) {
                    virtualColorChannel.setColor(hsbCommand);

                    VelbusSetColorPacket packet = new VelbusSetColorPacket(address, (byte) 0x01);
                    packet.setColor(virtualColorChannel.getColorVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported when in mode '" + moduleMode + "'.");
            }
        }
    }

    private boolean isBrightnessGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_BRIGHTNESS.equals(channelUID.getGroupId());
    }

    private boolean isSceneGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_SCENE.equals(channelUID.getGroupId());
    }

    private boolean isFadeModeGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_FADE_MODE.equals(channelUID.getGroupId());
    }

    private boolean isVirtualLightChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_VIRTUAL_LIGHT.equals(channelUID.getGroupId());
    }

    private boolean isDimWhiteGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_DIM_WHITE.equals(channelUID.getGroupId());
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 7) {
            byte command = packet[4];
            int channel = Byte.toUnsignedInt(packet[5]);
            byte setting = packet[6];

            if (command == COMMAND_TEMP_SENSOR_SETTINGS_PART1 && setting == SETTING_ACTUAL_LEVEL) {
                if (moduleMode.equals(DIM)) {
                    if (channel >= 1 && channel <= 4) {
                        VelbusColorChannel colorChannel = colorChannels[channel - 1];

                        if (packet.length >= 8) {
                            ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS,
                                    CHANNEL + channel);

                            colorChannel.setBrightness(packet[7]);

                            updateState(brightness, colorChannel.getBrightnessPercent());
                        }
                    }
                } else if (moduleMode.equals(RGB)) {
                    VelbusColorChannel colorChannel = colorChannels[0];

                    switch (channel) {
                        case 1:
                            if (packet.length >= 10) {
                                ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VIRTUAL_LIGHT,
                                        VIRTUAL_LIGHT + "1");

                                byte[] rgb = new byte[] { packet[7], packet[8], packet[9] };
                                colorChannel.setColor(rgb);

                                updateState(color, colorChannel.getColorHSB());
                            }
                            break;
                        case 4:
                            if (packet.length >= 8) {
                                ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DIM_WHITE,
                                        DIM_WHITE_CHANNEL + "1");
                                colorChannel.setBrightness(packet[7]);
                                updateState(brightness, colorChannel.getBrightnessPercent());
                            }
                            break;
                    }
                } else if (moduleMode.equals(RGBW)) {
                    VelbusColorChannel colorChannel = colorChannels[0];

                    if (channel == 1 && packet.length >= 8) {
                        ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VIRTUAL_LIGHT,
                                VIRTUAL_LIGHT + "1");

                        byte[] rgb = new byte[] { packet[8], packet[9], packet[10] };
                        colorChannel.setColor(rgb);
                        updateState(color, colorChannel.getColorHSB());

                        if (packet.length >= 12) {
                            ChannelUID dimwhite = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DIM_WHITE,
                                    DIM_WHITE_CHANNEL + "1");

                            colorChannel.setWhite(packet[11]);
                            updateState(dimwhite, colorChannel.getWhitePercent());
                        }
                    }
                }
            } else if (command == COMMAND_DIMVALUE_STATUS) {
                if (moduleMode.equals(DIM)) {
                    if (channel >= 1 && channel <= 4 && packet.length >= 8) {
                        for (int i = 0; i < (packet.length - 8); i++) {
                            ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS,
                                    CHANNEL + Integer.toString(channel + i));
                            VelbusColorChannel colorChannel = colorChannels[channel + i - 1];

                            colorChannel.setBrightness(packet[6 + i]);

                            updateState(brightness, colorChannel.getBrightnessPercent());
                        }
                    }
                } else if (moduleMode.equals(RGB)) {
                    VelbusColorChannel colorChannel = colorChannels[0];

                    switch (channel) {
                        case 1:
                            if (packet.length >= 9) {
                                ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VIRTUAL_LIGHT,
                                        VIRTUAL_LIGHT + "1");

                                byte[] rgb = new byte[] { packet[6], packet[7], packet[8] };
                                colorChannel.setColor(rgb);
                                updateState(color, colorChannel.getColorHSB());
                            }
                            break;
                        case 4:
                            if (packet.length >= 7) {
                                ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DIM_WHITE,
                                        DIM_WHITE_CHANNEL + "1");
                                colorChannel.setBrightness(packet[6]);
                                updateState(brightness, colorChannel.getBrightnessPercent());
                            }
                            break;
                    }
                } else if (moduleMode.equals(RGBW)) {
                    VelbusColorChannel colorChannel = colorChannels[0];

                    if (channel == 1 && packet.length >= 9) {
                        ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VIRTUAL_LIGHT,
                                VIRTUAL_LIGHT + "1");

                        byte[] rgb = new byte[] { packet[6], packet[7], packet[8] };
                        colorChannel.setColor(rgb);
                        updateState(color, colorChannel.getColorHSB());
                        if (packet.length >= 10) {
                            ChannelUID dimwhite = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DIM_WHITE,
                                    DIM_WHITE_CHANNEL + "1");

                            colorChannel.setWhite(packet[9]);
                            updateState(dimwhite, colorChannel.getWhitePercent());
                        }
                    }
                }
            }
        }

        return true;
    }
}
