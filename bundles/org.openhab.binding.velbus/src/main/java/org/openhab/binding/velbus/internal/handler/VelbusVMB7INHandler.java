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
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.config.VelbusVMB7INConfig;
import org.openhab.binding.velbus.internal.packets.VelbusCounterStatusRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMB7INHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB7INHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB7IN));

    private final ChannelUID counter1Channel = new ChannelUID(thing.getUID(), "counter", "counter1");
    private final ChannelUID counter1ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter1Current");
    private final ChannelUID counter2Channel = new ChannelUID(thing.getUID(), "counter", "counter2");
    private final ChannelUID counter2ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter2Current");
    private final ChannelUID counter3Channel = new ChannelUID(thing.getUID(), "counter", "counter3");
    private final ChannelUID counter3ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter3Current");
    private final ChannelUID counter4Channel = new ChannelUID(thing.getUID(), "counter", "counter4");
    private final ChannelUID counter4ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter4Current");

    public @NonNullByDefault({}) VelbusVMB7INConfig vmb7inConfig;

    private @Nullable ScheduledFuture<?> refreshJob;

    public VelbusVMB7INHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void initialize() {
        this.vmb7inConfig = getConfigAs(VelbusVMB7INConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = vmb7inConfig.refresh;

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
            sendCounterStatusRequest(velbusBridgeHandler, ALL_CHANNELS);
        }, 0, refreshInterval, TimeUnit.SECONDS);
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
            if (channelUID.equals(counter1Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x01);
            } else if (channelUID.equals(counter2Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x02);
            } else if (channelUID.equals(counter3Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x03);
            } else if (channelUID.equals(counter4Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x04);
            }
        }
    }

    protected void sendCounterStatusRequest(VelbusBridgeHandler velbusBridgeHandler, byte channel) {
        VelbusCounterStatusRequestPacket packet = new VelbusCounterStatusRequestPacket(
                new VelbusChannelIdentifier(getModuleAddress().getAddress(), channel));

        byte[] packetBytes = packet.getBytes();
        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_COUNTER_STATUS && packet.length >= 6) {
                int counterChannel = packet[5] & 0x03;
                int pulsesPerUnit = ((packet[5] & 0x7C) / 0x04) * 0x64;

                double counterValue = ((double) (((packet[6] & 0xff) << 24) | ((packet[7] & 0xff) << 16)
                        | ((packet[8] & 0xff) << 8) | (packet[9] & 0xff))) / pulsesPerUnit;
                double currentValue = (1000 * 3600)
                        / (((double) (((packet[10] & 0xff) << 8) | (packet[11] & 0xff))) * pulsesPerUnit);

                switch (counterChannel) {
                    case 0x00:
                        double counter1PulseMultiplier = vmb7inConfig.counter1PulseMultiplier;
                        updateState(counter1Channel, new DecimalType(counterValue / counter1PulseMultiplier));
                        updateState(counter1ChannelCurrent, new DecimalType(currentValue / counter1PulseMultiplier));
                        break;
                    case 0x01:
                        double counter2PulseMultiplier = vmb7inConfig.counter2PulseMultiplier;
                        updateState(counter2Channel, new DecimalType(counterValue / counter2PulseMultiplier));
                        updateState(counter2ChannelCurrent, new DecimalType(currentValue / counter2PulseMultiplier));
                        break;
                    case 0x02:
                        double counter3PulseMultiplier = vmb7inConfig.counter3PulseMultiplier;
                        updateState(counter3Channel, new DecimalType(counterValue / counter3PulseMultiplier));
                        updateState(counter3ChannelCurrent, new DecimalType(currentValue / counter3PulseMultiplier));
                        break;
                    case 0x03:
                        double counter4PulseMultiplier = vmb7inConfig.counter4PulseMultiplier;
                        updateState(counter4Channel, new DecimalType(counterValue / counter4PulseMultiplier));
                        updateState(counter4ChannelCurrent, new DecimalType(currentValue / counter4PulseMultiplier));
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "The given channel is not a counter channel: " + counterChannel);
                }
            }
        }

        return true;
    }
}
