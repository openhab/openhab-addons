/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusClockAlarm;
import org.openhab.binding.velbus.internal.VelbusClockAlarmConfiguration;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSetLocalClockAlarmPacket;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusSensorWithAlarmClockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 * @author Daniel Rosengarten - Add new module support, removes global alarm configuration from module (moved on
 *         bridge), reduces bus flooding on alarm value update
 */
@NonNullByDefault
public class VelbusSensorWithAlarmClockHandler extends VelbusSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB2PBN,
            THING_TYPE_VMB6PBN, THING_TYPE_VMB8PBU, THING_TYPE_VMBPIRC, THING_TYPE_VMBPIRM, THING_TYPE_VMBRFR8S,
            THING_TYPE_VMBVP1, THING_TYPE_VMBKP, THING_TYPE_VMBIN, THING_TYPE_VMB4PB, THING_TYPE_VMB6PB_20));
    private static final HashMap<ThingTypeUID, Integer> ALARM_CONFIGURATION_MEMORY_ADDRESSES = new HashMap<>();

    static {
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB2PBN, 0x0093);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB4AN, 0x0046);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB6PBN, 0x0093);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB7IN, 0x0093);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB8PBU, 0x0093);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL1, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL2, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL4, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBELO, 0x05A3);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBELPIR, 0x030F);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBPIRC, 0x0031);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBPIRM, 0x0031);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBPIRO, 0x0031);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBMETEO, 0x0083);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP1, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP1_2, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP2, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP2_2, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4_2, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4PIR, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4PIR_2, 0x00A4);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGPO, 0x0284);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGPOD, 0x0284);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGPOD_2, 0x0284);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBRFR8S, 0x0093);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBVP1, 0x002B);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBKP, 0x00A7);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBIN, 0x00A7);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB4PB, 0x00A7);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBDALI, 0x0513);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMB6PB_20, 0x00A7);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL1_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL2_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL4_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBELO_20, 0x05A3);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP1_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP2_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4_20, 0x0357);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGPO_20, 0x05A3);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBDALI_20, 0x0513);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBEL4PIR_20, 0x032B);
        ALARM_CONFIGURATION_MEMORY_ADDRESSES.put(THING_TYPE_VMBGP4PIR_20, 0x032B);
    }

    private static final byte ALARM_CONFIGURATION_MEMORY_SIZE = 0x09;
    private static final byte ALARM_1_ENABLED_MASK = 0x01;
    private static final byte ALARM_1_TYPE_MASK = 0x02;
    private static final byte ALARM_2_ENABLED_MASK = 0x04;
    private static final byte ALARM_2_TYPE_MASK = 0x08;

    private static final StringType ALARM_TYPE_LOCAL = new StringType("LOCAL");
    private static final StringType ALARM_TYPE_GLOBAL = new StringType("GLOBAL");

    private final ChannelUID clockAlarm1Enabled = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_ENABLED);
    private final ChannelUID clockAlarm1Type = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_TYPE);
    private final ChannelUID clockAlarm1WakeupHour = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_WAKEUP_HOUR);
    private final ChannelUID clockAlarm1WakeupMinute = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_WAKEUP_MINUTE);
    private final ChannelUID clockAlarm1BedtimeHour = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_BEDTIME_HOUR);
    private final ChannelUID clockAlarm1BedtimeMinute = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM1_BEDTIME_MINUTE);
    private final ChannelUID clockAlarm2Enabled = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_ENABLED);
    private final ChannelUID clockAlarm2Type = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_TYPE);
    private final ChannelUID clockAlarm2WakeupHour = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_WAKEUP_HOUR);
    private final ChannelUID clockAlarm2WakeupMinute = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_WAKEUP_MINUTE);
    private final ChannelUID clockAlarm2BedtimeHour = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_BEDTIME_HOUR);
    private final ChannelUID clockAlarm2BedtimeMinute = new ChannelUID(thing.getUID(), CHANNEL_GROUP_MODULE_CLOCK_ALARM,
            CHANNEL_CLOCK_ALARM2_BEDTIME_MINUTE);

    private int clockAlarmConfigurationMemoryAddress;
    private VelbusClockAlarmConfiguration alarmClockConfiguration = new VelbusClockAlarmConfiguration();

    private long lastUpdateAlarm1TimeMillis;
    private long lastUpdateAlarm2TimeMillis;

    public VelbusSensorWithAlarmClockHandler(Thing thing) {
        this(thing, 0);
    }

    public VelbusSensorWithAlarmClockHandler(Thing thing, int numberOfSubAddresses) {
        super(thing, numberOfSubAddresses);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (ALARM_CONFIGURATION_MEMORY_ADDRESSES.containsKey(thing.getThingTypeUID())) {
            this.clockAlarmConfigurationMemoryAddress = ALARM_CONFIGURATION_MEMORY_ADDRESSES
                    .get(thing.getThingTypeUID());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (isAlarmClockChannel(channelUID) && command instanceof RefreshType) {
            sendReadMemoryBlockPacket(velbusBridgeHandler, this.clockAlarmConfigurationMemoryAddress + 0);
            sendReadMemoryBlockPacket(velbusBridgeHandler, this.clockAlarmConfigurationMemoryAddress + 4);
            sendReadMemoryPacket(velbusBridgeHandler, this.clockAlarmConfigurationMemoryAddress + 8);
        } else if (isAlarmClockChannel(channelUID)) {
            byte alarmNumber = determineAlarmNumber(channelUID);
            VelbusClockAlarm alarmClock = alarmClockConfiguration.getAlarmClock(alarmNumber);

            alarmClock.setLocal(true);

            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_CLOCK_ALARM1_TYPE:
                case CHANNEL_CLOCK_ALARM2_TYPE: {
                    if (command instanceof OnOffType) {
                        // If AlarmType is not read only, it's an old implementation of the module, warn user and
                        // discard the command
                        logger.warn(
                                "Old implementation of thing '{}'. Only local alarm on module, global alarm only on bridge. To avoid problem, remove and recreate the thing.",
                                getThing().getUID());
                    }
                    return;
                }
                case CHANNEL_CLOCK_ALARM1_ENABLED:
                case CHANNEL_CLOCK_ALARM2_ENABLED: {
                    if (command instanceof OnOffType) {
                        boolean enabled = command == OnOffType.ON;
                        alarmClock.setEnabled(enabled);
                    }
                    break;
                }
                case CHANNEL_CLOCK_ALARM1_WAKEUP_HOUR:
                case CHANNEL_CLOCK_ALARM2_WAKEUP_HOUR: {
                    if (command instanceof DecimalType decimalCommand) {
                        byte wakeupHour = decimalCommand.byteValue();
                        alarmClock.setWakeupHour(wakeupHour);
                    }
                    break;
                }
                case CHANNEL_CLOCK_ALARM1_WAKEUP_MINUTE:
                case CHANNEL_CLOCK_ALARM2_WAKEUP_MINUTE: {
                    if (command instanceof DecimalType decimalCommand) {
                        byte wakeupMinute = decimalCommand.byteValue();
                        alarmClock.setWakeupMinute(wakeupMinute);
                    }
                    break;
                }
                case CHANNEL_CLOCK_ALARM1_BEDTIME_HOUR:
                case CHANNEL_CLOCK_ALARM2_BEDTIME_HOUR: {
                    if (command instanceof DecimalType decimalCommand) {
                        byte bedTimeHour = decimalCommand.byteValue();
                        alarmClock.setBedtimeHour(bedTimeHour);
                    }
                    break;
                }
                case CHANNEL_CLOCK_ALARM1_BEDTIME_MINUTE:
                case CHANNEL_CLOCK_ALARM2_BEDTIME_MINUTE: {
                    if (command instanceof DecimalType decimalCommand) {
                        byte bedTimeMinute = decimalCommand.byteValue();
                        alarmClock.setBedtimeMinute(bedTimeMinute);
                    }
                    break;
                }
            }

            if (alarmNumber == 1) {
                lastUpdateAlarm1TimeMillis = System.currentTimeMillis();
            } else {
                lastUpdateAlarm2TimeMillis = System.currentTimeMillis();
            }

            VelbusSetLocalClockAlarmPacket packet = new VelbusSetLocalClockAlarmPacket(getModuleAddress().getAddress(),
                    alarmNumber, alarmClock);
            byte[] packetBytes = packet.getBytes();

            // Schedule the send of the packet to see if there is another update in less than 10 secondes (reduce
            // flooding of the bus)
            scheduler.schedule(() -> {
                sendAlarmPacket(alarmNumber, packetBytes);
            }, DELAY_SEND_CLOCK_ALARM_UPDATE, TimeUnit.MILLISECONDS);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    public synchronized void sendAlarmPacket(int alarmNumber, byte[] packetBytes) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        long timeSinceLastUpdate;

        if (alarmNumber == 1) {
            timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateAlarm1TimeMillis;
        } else {
            timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateAlarm2TimeMillis;
        }

        // If a value of the alarm has been updated, discard this old update
        if (timeSinceLastUpdate < DELAY_SEND_CLOCK_ALARM_UPDATE) {
            return;
        }

        velbusBridgeHandler.sendPacket(packetBytes);
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if ((command == COMMAND_MEMORY_DATA_BLOCK && packet.length >= 11)
                    || (command == COMMAND_MEMORY_DATA && packet.length >= 8)) {
                byte highMemoryAddress = packet[5];
                byte lowMemoryAddress = packet[6];
                int memoryAddress = ((highMemoryAddress & 0xff) << 8) | (lowMemoryAddress & 0xff);
                byte[] data = (command == COMMAND_MEMORY_DATA_BLOCK)
                        ? new byte[] { packet[7], packet[8], packet[9], packet[10] }
                        : new byte[] { packet[7] };

                for (int i = 0; i < data.length; i++) {

                    if (isClockAlarmConfigurationByte(memoryAddress + i)) {
                        setClockAlarmConfigurationByte(memoryAddress + i, data[i]);
                    }
                }
            } else if (command == COMMAND_MODULE_STATUS) {
                int clockAlarmAndProgramSelectionIndexInModuleStatus = this
                        .getClockAlarmAndProgramSelectionIndexInModuleStatus();
                if (packet.length >= clockAlarmAndProgramSelectionIndexInModuleStatus + 1) {
                    byte alarmAndProgramSelection = packet[clockAlarmAndProgramSelectionIndexInModuleStatus];

                    boolean alarmClock1Enabled = (alarmAndProgramSelection & 0x04) > 0;
                    boolean alarmClock1IsLocal = (alarmAndProgramSelection & 0x08) == 0;
                    VelbusClockAlarm alarmClock1 = this.alarmClockConfiguration.getAlarmClock1();
                    alarmClock1.setEnabled(alarmClock1Enabled);
                    alarmClock1.setLocal(alarmClock1IsLocal);
                    updateState(clockAlarm1Enabled, OnOffType.from(alarmClock1.isEnabled()));
                    updateState(clockAlarm1Type, alarmClock1.isLocal() ? ALARM_TYPE_LOCAL : ALARM_TYPE_GLOBAL);

                    boolean alarmClock2Enabled = (alarmAndProgramSelection & 0x10) > 0;
                    boolean alarmClock2IsLocal = (alarmAndProgramSelection & 0x20) == 0;
                    VelbusClockAlarm alarmClock2 = this.alarmClockConfiguration.getAlarmClock2();
                    alarmClock2.setEnabled(alarmClock2Enabled);
                    alarmClock2.setLocal(alarmClock2IsLocal);
                    updateState(clockAlarm2Enabled, OnOffType.from(alarmClock2.isEnabled()));
                    updateState(clockAlarm2Type, alarmClock2.isLocal() ? ALARM_TYPE_LOCAL : ALARM_TYPE_GLOBAL);
                }
            }
        }

        return true;
    }

    public Boolean isClockAlarmConfigurationByte(int memoryAddress) {
        return memoryAddress >= this.clockAlarmConfigurationMemoryAddress
                && memoryAddress < (this.clockAlarmConfigurationMemoryAddress + ALARM_CONFIGURATION_MEMORY_SIZE);
    }

    public void setClockAlarmConfigurationByte(int memoryAddress, byte data) {
        VelbusClockAlarm alarmClock1 = this.alarmClockConfiguration.getAlarmClock1();
        VelbusClockAlarm alarmClock2 = this.alarmClockConfiguration.getAlarmClock2();

        switch (memoryAddress - this.clockAlarmConfigurationMemoryAddress) {
            case 0:
                alarmClock1.setEnabled((data & ALARM_1_ENABLED_MASK) > 0);
                alarmClock1.setLocal((data & ALARM_1_TYPE_MASK) > 0);

                updateState(clockAlarm1Enabled, OnOffType.from(alarmClock1.isEnabled()));
                updateState(clockAlarm1Type, alarmClock1.isLocal() ? ALARM_TYPE_LOCAL : ALARM_TYPE_GLOBAL);

                alarmClock2.setEnabled((data & ALARM_2_ENABLED_MASK) > 0);
                alarmClock2.setLocal((data & ALARM_2_TYPE_MASK) > 0);

                updateState(clockAlarm2Enabled, OnOffType.from(alarmClock2.isEnabled()));
                updateState(clockAlarm2Type, alarmClock2.isLocal() ? ALARM_TYPE_LOCAL : ALARM_TYPE_GLOBAL);
                break;
            case 1:
                alarmClock1.setWakeupHour(data);
                updateState(clockAlarm1WakeupHour, new DecimalType(alarmClock1.getWakeupHour()));
                break;
            case 2:
                alarmClock1.setWakeupMinute(data);
                updateState(clockAlarm1WakeupMinute, new DecimalType(alarmClock1.getWakeupMinute()));
                break;
            case 3:
                alarmClock1.setBedtimeHour(data);
                updateState(clockAlarm1BedtimeHour, new DecimalType(alarmClock1.getBedtimeHour()));
                break;
            case 4:
                alarmClock1.setBedtimeMinute(data);
                updateState(clockAlarm1BedtimeMinute, new DecimalType(alarmClock1.getBedtimeMinute()));
                break;
            case 5:
                alarmClock2.setWakeupHour(data);
                updateState(clockAlarm2WakeupHour, new DecimalType(alarmClock2.getWakeupHour()));
                break;
            case 6:
                alarmClock2.setWakeupMinute(data);
                updateState(clockAlarm2WakeupMinute, new DecimalType(alarmClock2.getWakeupMinute()));
                break;
            case 7:
                alarmClock2.setBedtimeHour(data);
                updateState(clockAlarm2BedtimeHour, new DecimalType(alarmClock2.getBedtimeHour()));
                break;
            case 8:
                alarmClock2.setBedtimeMinute(data);
                updateState(clockAlarm2BedtimeMinute, new DecimalType(alarmClock2.getBedtimeMinute()));
                break;
            default:
                throw new IllegalArgumentException("The memory address '" + memoryAddress
                        + "' does not represent a clock alarm configuration for the thing '" + this.thing.getUID()
                        + "'.");
        }
    }

    protected boolean isAlarmClockChannel(ChannelUID channelUID) {
        return CHANNEL_GROUP_MODULE_CLOCK_ALARM.equals(channelUID.getGroupId());
    }

    protected byte determineAlarmNumber(ChannelUID channelUID) {
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_CLOCK_ALARM1_ENABLED:
            case CHANNEL_CLOCK_ALARM1_TYPE:
            case CHANNEL_CLOCK_ALARM1_WAKEUP_HOUR:
            case CHANNEL_CLOCK_ALARM1_WAKEUP_MINUTE:
            case CHANNEL_CLOCK_ALARM1_BEDTIME_HOUR:
            case CHANNEL_CLOCK_ALARM1_BEDTIME_MINUTE:
                return 1;
            case CHANNEL_CLOCK_ALARM2_ENABLED:
            case CHANNEL_CLOCK_ALARM2_TYPE:
            case CHANNEL_CLOCK_ALARM2_WAKEUP_HOUR:
            case CHANNEL_CLOCK_ALARM2_WAKEUP_MINUTE:
            case CHANNEL_CLOCK_ALARM2_BEDTIME_HOUR:
            case CHANNEL_CLOCK_ALARM2_BEDTIME_MINUTE:
                return 2;
        }

        throw new IllegalArgumentException("The given channelUID is not a module alarm clock channel: " + channelUID);
    }

    protected int getClockAlarmAndProgramSelectionIndexInModuleStatus() {
        return 10;
    }
}
