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
import org.openhab.binding.velbus.internal.VelbusVirtualColorChannel;
import org.openhab.binding.velbus.internal.config.VelbusSensorConfig;
import org.openhab.binding.velbus.internal.packets.VelbusDaliRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetColorPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetDimPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetScenePacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMBDALIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBDALIHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBDALI, THING_TYPE_VMBDALI_20));
    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) VelbusSensorConfig sensorConfig;

    private VelbusColorChannel[] colorChannels;
    private VelbusVirtualColorChannel[] virtualColorChannels;

    public VelbusVMBDALIHandler(Thing thing) {
        super(thing, 9);

        colorChannels = new VelbusColorChannel[81];
        virtualColorChannels = new VelbusVirtualColorChannel[16];
    }

    @Override
    public void initialize() {
        this.sensorConfig = getConfigAs(VelbusSensorConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
        initializeColorChannel();
        initializeVirtualLight();
        initializeChannelStates();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = this.sensorConfig.refresh;

        if (refreshInterval > 0) {
            startAutomaticRefresh(refreshInterval);
        }
    }

    private void initializeColorChannel() {
        for (int i = 0; i <= 80; i++) {
            colorChannels[i] = new VelbusColorChannel(CURVE_TYPE_EXPONENTIAL);
        }
    }

    private void initializeVirtualLight() {
        String virtualLight;

        for (int i = 1; i <= 16; i++) {
            if (getConfig().containsKey(VIRTUAL_LIGHT + i)) {
                virtualLight = getConfig().get(VIRTUAL_LIGHT + i).toString();
                if (virtualLight.length() > 0) {
                    try {
                        virtualColorChannels[i - 1] = new VelbusVirtualColorChannel(virtualLight);
                    } catch (Exception e) {
                        logger.warn("VMBDALI on address {} : Virtual Light {} has wrong channel format '{}'. {}",
                                getModuleAddress().getAddress(), i, virtualLight, e.getMessage());
                    }
                }
            }
            if (virtualColorChannels[i - 1] == null) {
                virtualColorChannels[i - 1] = new VelbusVirtualColorChannel();
            }
        }
    }

    private void initializeChannelStates() {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        sendDaliReadoutRequest(velbusBridgeHandler, ALL_DALI_CHANNELS);
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
            sendDaliReadoutRequest(velbusBridgeHandler, ALL_DALI_CHANNELS);
        }, 0, refreshInterval, TimeUnit.SECONDS);
    }

    protected void sendDaliReadoutRequest(VelbusBridgeHandler velbusBridgeHandler, byte channel) {
        VelbusDaliRequestPacket packet = new VelbusDaliRequestPacket(getModuleAddress().getAddress(), channel);

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
            if (isColorGroupChannel(channelUID) || isBrightnessGroupChannel(channelUID)
                    || isWhiteGroupChannel(channelUID)) {
                sendDaliReadoutRequest(velbusBridgeHandler, channel);
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
        } else if (isColorGroupChannel(channelUID) || isBrightnessGroupChannel(channelUID)
                || isWhiteGroupChannel(channelUID) || isVirtualLightChannel(channelUID)) {
            VelbusColorChannel colorChannel = colorChannels[Byte.toUnsignedInt(channel) - 1];

            if (isBrightnessGroupChannel(channelUID)) {
                if (command instanceof PercentType percentCommand) {
                    colorChannel.setBrightness(percentCommand);

                    VelbusSetDimPacket packet = new VelbusSetDimPacket(address, channel);
                    packet.setDim(colorChannel.getBrightnessVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            } else if (isColorGroupChannel(channelUID)) {
                if (command instanceof HSBType hsbCommand) {
                    colorChannel.setBrightness(hsbCommand);
                    colorChannel.setColor(hsbCommand);

                    VelbusSetColorPacket packet = new VelbusSetColorPacket(address, channel);
                    packet.setBrightness(colorChannel.getBrightnessVelbus());
                    packet.setColor(colorChannel.getColorVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            } else if (isWhiteGroupChannel(channelUID)) {
                if (command instanceof PercentType percentCommand) {
                    int channelNumber = getModuleAddress().getChannelNumber(channelUID);
                    boolean isVirtualLight = false;

                    for (int i = 0; i < 16; i++) {
                        if (virtualColorChannels[i].isVirtualColorChannel(channelNumber)) {
                            virtualColorChannels[i].setWhite(percentCommand);

                            VelbusSetDimPacket packet = new VelbusSetDimPacket(address, channel);
                            packet.setDim(virtualColorChannels[i].getWhiteVelbus());
                            velbusBridgeHandler.sendPacket(packet.getBytes());
                            isVirtualLight = true;
                        }
                    }
                    if (!isVirtualLight) {
                        colorChannel.setWhite(percentCommand);

                        VelbusSetColorPacket packet = new VelbusSetColorPacket(address, channel);
                        packet.setWhite(colorChannel.getWhiteVelbus());
                        velbusBridgeHandler.sendPacket(packet.getBytes());
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            } else if (isVirtualLightChannel(channelUID)) {
                int virtualChannel = getModuleAddress().getChannelNumber(channelUID) - 1;
                VelbusVirtualColorChannel virtualColorChannel = virtualColorChannels[virtualChannel];

                if (command instanceof HSBType hsbCommand && virtualColorChannel.isRGBConfigured()) {
                    virtualColorChannel.setBrightness(hsbCommand);
                    virtualColorChannel.setColor(hsbCommand);

                    VelbusSetDimPacket packet = new VelbusSetDimPacket(address, virtualColorChannel.getRedChannel());
                    packet.setDim(virtualColorChannel.getRedColorVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());

                    packet = new VelbusSetDimPacket(address, virtualColorChannel.getGreenChannel());
                    packet.setDim(virtualColorChannel.getGreenColorVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());

                    packet = new VelbusSetDimPacket(address, virtualColorChannel.getBlueChannel());
                    packet.setDim(virtualColorChannel.getBlueColorVelbus());
                    velbusBridgeHandler.sendPacket(packet.getBytes());
                } else {
                    throw new UnsupportedOperationException(
                            "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
                }
            }
        }
    }

    private boolean isColorGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_COLOR.equals(channelUID.getGroupId());
    }

    private boolean isBrightnessGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_BRIGHTNESS.equals(channelUID.getGroupId());
    }

    private boolean isWhiteGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_WHITE.equals(channelUID.getGroupId());
    }

    private boolean isSceneGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_SCENE.equals(channelUID.getGroupId());
    }

    private boolean isVirtualLightChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_VIRTUAL_LIGHT.equals(channelUID.getGroupId());
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 7) {
            byte command = packet[4];
            byte setting = packet[6];

            if (command == COMMAND_TEMP_SENSOR_SETTINGS_PART1 && setting == SETTING_ACTUAL_LEVEL) {
                int channel = Byte.toUnsignedInt(packet[5]);

                if (channel >= 1 && channel <= 80) {
                    VelbusColorChannel colorChannel = colorChannels[channel - 1];

                    if (packet.length >= 8 && packet.length < 12) {
                        ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS,
                                CHANNEL + channel);
                        colorChannel.setBrightness(packet[7]);
                        updateState(brightness, colorChannel.getBrightnessPercent());
                        updateVirtualLightState(channel, packet[7]);
                    } else if (packet.length >= 12) {
                        ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS,
                                CHANNEL + channel);
                        colorChannel.setBrightness(packet[7]);
                        updateState(brightness, colorChannel.getBrightnessPercent());

                        ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_COLOR, CHANNEL + channel);
                        colorChannel.setColor(new byte[] { packet[8], packet[9], packet[10] });
                        updateState(color, colorChannel.getColorHSB());

                        ChannelUID white = new ChannelUID(thing.getUID(), CHANNEL_GROUP_WHITE, CHANNEL + channel);
                        colorChannel.setWhite(packet[11]);
                        updateState(white, colorChannel.getWhitePercent());
                    }
                } else if (channel == 81) { // Broadcast
                    if (packet.length >= 8 && packet.length < 12) {
                        VelbusColorChannel colorChannel;
                        ChannelUID brightness;

                        for (int i = 1; i <= 80; i++) {
                            colorChannel = colorChannels[i - 1];
                            brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS, CHANNEL + i);
                            colorChannel.setBrightness(packet[7]);
                            updateState(brightness, colorChannel.getBrightnessPercent());
                            updateVirtualLightState(i, packet[7]);
                        }
                    } else if (packet.length >= 12) {
                        VelbusColorChannel colorChannel;
                        ChannelUID brightness;
                        ChannelUID color;
                        ChannelUID white;
                        byte[] rgb = new byte[] { packet[8], packet[9], packet[10] };

                        for (int i = 1; i <= 80; i++) {
                            colorChannel = colorChannels[i - 1];

                            brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS, CHANNEL + i);
                            colorChannel.setBrightness(packet[7]);
                            updateState(brightness, colorChannel.getBrightnessPercent());

                            color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_COLOR, CHANNEL + i);
                            colorChannel.setColor(rgb);
                            updateState(color, colorChannel.getColorHSB());

                            white = new ChannelUID(thing.getUID(), CHANNEL_GROUP_WHITE, CHANNEL + i);
                            colorChannel.setWhite(packet[11]);
                            updateState(white, colorChannel.getWhitePercent());
                        }
                    }
                }
            } else if (command == COMMAND_DIMVALUE_STATUS && packet.length >= 8) {
                int channel = Byte.toUnsignedInt(packet[5]);

                if (channel >= 1 && channel <= 80) {
                    VelbusColorChannel colorChannel = colorChannels[channel - 1];

                    for (int i = 0; i < (packet.length - 8); i++) {
                        ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS,
                                CHANNEL + (channel + i));
                        colorChannel.setBrightness(packet[6 + i]);
                        updateState(brightness, colorChannel.getBrightnessPercent());
                        updateVirtualLightState(channel + i, packet[6 + i]);
                    }
                } else if (channel == 81) { // Broadcast
                    VelbusColorChannel colorChannel;
                    ChannelUID brightness;

                    for (int i = 1; i <= 80; i++) {
                        colorChannel = colorChannels[i - 1];
                        brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_BRIGHTNESS, CHANNEL + i);
                        colorChannel.setBrightness(packet[6]);
                        updateState(brightness, colorChannel.getBrightnessPercent());
                        updateVirtualLightState(i, packet[6]);
                    }
                }
            }
        }

        return true;
    }

    private void updateVirtualLightState(int channel, byte dimVal) {
        for (int i = 0; i < 16; i++) {
            if (virtualColorChannels[i].isVirtualColorChannel(channel)) {
                virtualColorChannels[i].setColor(dimVal, channel);

                if (virtualColorChannels[i].isColorChannel(channel)) {
                    ChannelUID virtualLight = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VIRTUAL_LIGHT,
                            VIRTUAL_LIGHT + (i + 1));
                    updateState(virtualLight, virtualColorChannels[i].getColorHSB());
                } else if (virtualColorChannels[i].isWhiteChannel(channel)) {
                    ChannelUID whiteChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_WHITE, CHANNEL + channel);
                    updateState(whiteChannel, virtualColorChannels[i].getWhitePercent());
                }
            }
        }
    }
}
