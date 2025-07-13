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

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.config.VelbusVMB8INConfig;
import org.openhab.binding.velbus.internal.packets.VelbusCounterStatusRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMB8INHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB8INHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB8IN_20));

    private final ChannelUID counter1Channel = new ChannelUID(thing.getUID(), "counter", "counter1");
    private final ChannelUID counter1ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter1Current");
    private final ChannelUID counter2Channel = new ChannelUID(thing.getUID(), "counter", "counter2");
    private final ChannelUID counter2ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter2Current");
    private final ChannelUID counter3Channel = new ChannelUID(thing.getUID(), "counter", "counter3");
    private final ChannelUID counter3ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter3Current");
    private final ChannelUID counter4Channel = new ChannelUID(thing.getUID(), "counter", "counter4");
    private final ChannelUID counter4ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter4Current");
    private final ChannelUID counter5Channel = new ChannelUID(thing.getUID(), "counter", "counter5");
    private final ChannelUID counter5ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter5Current");
    private final ChannelUID counter6Channel = new ChannelUID(thing.getUID(), "counter", "counter6");
    private final ChannelUID counter6ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter6Current");
    private final ChannelUID counter7Channel = new ChannelUID(thing.getUID(), "counter", "counter7");
    private final ChannelUID counter7ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter7Current");
    private final ChannelUID counter8Channel = new ChannelUID(thing.getUID(), "counter", "counter8");
    private final ChannelUID counter8ChannelCurrent = new ChannelUID(thing.getUID(), "counter", "counter8Current");

    private static final String COUNTER_UNIT_KWH = "kWh";
    private static final String COUNTER_UNIT_M3 = "m³";
    private static final String COUNTER_UNIT_LITERS = "liters";

    public @NonNullByDefault({}) VelbusVMB8INConfig vmb8inConfig;

    private @Nullable ScheduledFuture<?> refreshJob;

    public VelbusVMB8INHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void initialize() {
        this.vmb8inConfig = getConfigAs(VelbusVMB8INConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = vmb8inConfig.refresh;

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
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x04);
            } else if (channelUID.equals(counter4Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x08);
            } else if (channelUID.equals(counter5Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x10);
            } else if (channelUID.equals(counter6Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x20);
            } else if (channelUID.equals(counter7Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x40);
            } else if (channelUID.equals(counter8Channel)) {
                sendCounterStatusRequest(velbusBridgeHandler, (byte) 0x80);
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

            if (command == COMMAND_COUNTER_VALUE && packet.length >= 12) {
                int counterChannel = (packet[5] >> 4) & 0x07;

                double counterValue = ((double) (((packet[8] & 0xff) << 24) | ((packet[9] & 0xff) << 16)
                        | ((packet[10] & 0xff) << 8) | (packet[11] & 0xff)));
                double currentValue = ((double) (((packet[6] & 0xff) << 8) | (packet[7] & 0xff)));

                switch (counterChannel) {
                    case 0x00:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter1Unit)) {
                            updateState(counter1Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter1ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter1Unit)) {
                            updateState(counter1Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter1ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter1Unit)) {
                            updateState(counter1Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter1ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x01:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter2Unit)) {
                            updateState(counter2Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter2ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter2Unit)) {
                            updateState(counter2Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter2ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter2Unit)) {
                            updateState(counter2Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter2ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x02:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter3Unit)) {
                            updateState(counter3Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter3ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter3Unit)) {
                            updateState(counter3Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter3ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter3Unit)) {
                            updateState(counter3Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter3ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x03:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter4Unit)) {
                            updateState(counter4Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter4ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter4Unit)) {
                            updateState(counter4Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter4ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter4Unit)) {
                            updateState(counter4Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter4ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x04:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter5Unit)) {
                            updateState(counter5Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter5ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter5Unit)) {
                            updateState(counter5Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter5ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter5Unit)) {
                            updateState(counter5Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter5ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x05:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter6Unit)) {
                            updateState(counter6Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter6ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter6Unit)) {
                            updateState(counter6Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter6ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter6Unit)) {
                            updateState(counter6Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter6ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x06:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter7Unit)) {
                            updateState(counter7Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter7ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter7Unit)) {
                            updateState(counter7Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter7ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter7Unit)) {
                            updateState(counter7Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter7ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
                        break;
                    case 0x07:
                        if (COUNTER_UNIT_KWH.equals(vmb8inConfig.counter8Unit)) {
                            updateState(counter8Channel,
                                    new QuantityType<Energy>(counterValue / 1000, Units.KILOWATT_HOUR));
                            updateState(counter8ChannelCurrent, new QuantityType<Power>(currentValue, Units.WATT));
                        } else if (COUNTER_UNIT_M3.equals(vmb8inConfig.counter8Unit)) {
                            updateState(counter8Channel, new QuantityType<Volume>(counterValue, Units.LITRE));
                            updateState(counter8ChannelCurrent, new QuantityType<Volume>(currentValue, Units.LITRE));
                        } else if (COUNTER_UNIT_LITERS.equals(vmb8inConfig.counter8Unit)) {
                            updateState(counter8Channel, new QuantityType<Volume>(counterValue / 1000, Units.LITRE));
                            updateState(counter8ChannelCurrent,
                                    new QuantityType<Volume>(currentValue / 1000, Units.LITRE));
                        }
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
