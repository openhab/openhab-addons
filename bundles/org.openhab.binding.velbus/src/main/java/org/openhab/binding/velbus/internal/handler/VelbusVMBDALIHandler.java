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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.VelbusDALIConverter;
import org.openhab.binding.velbus.internal.config.VelbusSensorConfig;
import org.openhab.binding.velbus.internal.packets.VelbusDaliRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetColorPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetScenePacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMBDALI));
    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) VelbusSensorConfig sensorConfig;

    public VelbusVMBDALIHandler(Thing thing) {
        super(thing, 9);
    }

    @Override
    public void initialize() {
        this.sensorConfig = getConfigAs(VelbusSensorConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = this.sensorConfig.refresh;

        if (refreshInterval > 0) {
            startAutomaticRefresh(refreshInterval);
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void startAutomaticRefresh(int refreshInterval) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            sendDaliReadoutRequest(velbusBridgeHandler, ALL_CHANNELS);
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

        if (command instanceof RefreshType) {
            if (isColorGroupChannel(channelUID)) {
                sendDaliReadoutRequest(velbusBridgeHandler, getColorChannel(channelUID));
            }
        }

        if (isSceneChannel(channelUID) && command instanceof DecimalType) {
            byte scene = ((DecimalType) command).byteValue();

            VelbusSetScenePacket packet = new VelbusSetScenePacket(getModuleAddress().getChannelIdentifier(channelUID),
                    scene);
            velbusBridgeHandler.sendPacket(packet.getBytes());
        }

        if (isColorGroupChannel(channelUID)) {
            String channel = channelUID.getIdWithoutGroup().substring(4);
            VelbusSetColorPacket packet = new VelbusSetColorPacket(getModuleAddress().getChannelIdentifier(channelUID));
            VelbusDALIConverter converter = new VelbusDALIConverter();

            byte brightness = VALUE_UNCHANGED;
            byte temperature = VALUE_UNCHANGED;
            byte[] rgb = new byte[] { VALUE_UNCHANGED, VALUE_UNCHANGED, VALUE_UNCHANGED };

            switch (channel) {
                case CHANNEL_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        brightness = converter.getBrightness((PercentType) command);
                    } else if (command instanceof OnOffType) {
                        brightness = converter.getBrightness((OnOffType) command);
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    if (command instanceof PercentType) {
                        temperature = converter.getTemperature((PercentType) command);
                    } else if (command instanceof OnOffType) {
                        temperature = converter.getTemperature((OnOffType) command);
                    }
                    break;
                case CHANNEL_COLOR:
                    if (command instanceof HSBType) {
                        rgb = converter.getRgb((HSBType) command);
                        brightness = converter.getBrightness((HSBType) command);
                    }
                    break;
                default:
                    logger.debug("Command sent to an unknown channel id: {}:{}", getThing().getUID(), channel);
                    break;
            }

            packet.setBrightness(brightness);
            packet.setColor(rgb[0], rgb[1], rgb[2]);
            packet.setWhite(temperature);

            velbusBridgeHandler.sendPacket(packet.getBytes());
        }
    }

    private boolean isSceneChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_SCENE.equals(channelUID.getGroupId());
    }

    private boolean isColorGroupChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_COLOR.equals(channelUID.getGroupId());
    }

    private byte getColorChannel(ChannelUID channelUID) {
        String channel = channelUID.getIdWithoutGroup();
        Integer channelNumber = Integer.parseInt(channel.substring(2, channel.indexOf('-')));
        return channelNumber.byteValue();
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        super.onPacketReceived(packet);

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte address = packet[2];
            byte command = packet[4];

            if (command == COMMAND_TEMP_SENSOR_SETTINGS_PART1 && packet.length >= 8) {
                byte channel = packet[5];

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                VelbusDALIConverter converter = new VelbusDALIConverter();

                ChannelUID brightness = new ChannelUID(thing.getUID(), CHANNEL_GROUP_COLOR,
                        getModuleAddress().getChannelId(velbusChannelIdentifier) + "-" + CHANNEL_BRIGHTNESS);
                ChannelUID color = new ChannelUID(thing.getUID(), CHANNEL_GROUP_COLOR,
                        getModuleAddress().getChannelId(velbusChannelIdentifier) + "-" + CHANNEL_COLOR);
                ChannelUID temperature = new ChannelUID(thing.getUID(), CHANNEL_GROUP_COLOR,
                        getModuleAddress().getChannelId(velbusChannelIdentifier) + "-" + CHANNEL_TEMPERATURE);

                updateState(brightness, converter.getBrightness(packet[7]));
                updateState(color, converter.getHsb(new byte[] { packet[8], packet[9], packet[10] }));
                updateState(temperature, converter.getTemperature(packet[11]));
            }
        }
    }
}
