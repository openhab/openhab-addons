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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusCounterStatusRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;

/**
 * The {@link VelbusVMB7INHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB7INHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB7IN));

    private final ChannelUID counter1Channel = new ChannelUID(thing.getUID(), "counter#COUNTER1");
    private final ChannelUID counter1ChannelCurrent = new ChannelUID(thing.getUID(), "counter#COUNTER1_CURRENT");
    private final ChannelUID counter2Channel = new ChannelUID(thing.getUID(), "counter#COUNTER2");
    private final ChannelUID counter2ChannelCurrent = new ChannelUID(thing.getUID(), "counter#COUNTER2_CURRENT");
    private final ChannelUID counter3Channel = new ChannelUID(thing.getUID(), "counter#COUNTER3");
    private final ChannelUID counter3ChannelCurrent = new ChannelUID(thing.getUID(), "counter#COUNTER3_CURRENT");
    private final ChannelUID counter4Channel = new ChannelUID(thing.getUID(), "counter#COUNTER4");
    private final ChannelUID counter4ChannelCurrent = new ChannelUID(thing.getUID(), "counter#COUNTER4_CURRENT");

    private double counter1PulseMultiplier = 1;
    private double counter2PulseMultiplier = 1;
    private double counter3PulseMultiplier = 1;
    private double counter4PulseMultiplier = 1;

    private @Nullable ScheduledFuture<?> refreshJob;

    public VelbusVMB7INHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void initialize() {
        super.initialize();

        initializePulseCounterMultipliers();
        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        Object refreshIntervalObject = getConfig().get(REFRESH_INTERVAL);
        if (refreshIntervalObject != null) {
            int refreshInterval = ((BigDecimal) refreshIntervalObject).intValue();

            if (refreshInterval > 0) {
                startAutomaticRefresh(refreshInterval);
            }
        }
    }

    private void initializePulseCounterMultipliers() {
        Object counter1PulseMultiplierObject = getConfig().get(COUNTER1_PULSE_MULTIPLIER);
        if (counter1PulseMultiplierObject != null) {
            counter1PulseMultiplier = ((BigDecimal) counter1PulseMultiplierObject).doubleValue();
        }

        Object counter2PulseMultiplierObject = getConfig().get(COUNTER2_PULSE_MULTIPLIER);
        if (counter2PulseMultiplierObject != null) {
            counter2PulseMultiplier = ((BigDecimal) counter2PulseMultiplierObject).doubleValue();
        }

        Object counter3PulseMultiplierObject = getConfig().get(COUNTER3_PULSE_MULTIPLIER);
        if (counter3PulseMultiplierObject != null) {
            counter3PulseMultiplier = ((BigDecimal) counter3PulseMultiplierObject).doubleValue();
        }

        Object counter4PulseMultiplierObject = getConfig().get(COUNTER4_PULSE_MULTIPLIER);
        if (counter4PulseMultiplierObject != null) {
            counter4PulseMultiplier = ((BigDecimal) counter4PulseMultiplierObject).doubleValue();
        }
    }

    @Override
    public void dispose() {
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
    public void onPacketReceived(byte[] packet) {
        super.onPacketReceived(packet);

        logger.trace("onPacketReceived() was called");

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
                        updateState(counter1Channel, new DecimalType(counterValue / counter1PulseMultiplier));
                        updateState(counter1ChannelCurrent, new DecimalType(currentValue / counter1PulseMultiplier));
                        break;
                    case 0x01:
                        updateState(counter2Channel, new DecimalType(counterValue / counter2PulseMultiplier));
                        updateState(counter2ChannelCurrent, new DecimalType(currentValue / counter2PulseMultiplier));
                        break;
                    case 0x02:
                        updateState(counter3Channel, new DecimalType(counterValue / counter3PulseMultiplier));
                        updateState(counter3ChannelCurrent, new DecimalType(currentValue / counter3PulseMultiplier));
                        break;
                    case 0x03:
                        updateState(counter4Channel, new DecimalType(counterValue / counter4PulseMultiplier));
                        updateState(counter4ChannelCurrent, new DecimalType(currentValue / counter4PulseMultiplier));
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "The given channel is not a counter channel: " + counterChannel);
                }
            }
        }
    }
}
