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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.config.VelbusSensorConfig;
import org.openhab.binding.velbus.internal.packets.VelbusDimmerPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusSensorReadoutRequestPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMB4ANHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB4ANHandler extends VelbusSensorWithAlarmClockHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB4AN));

    private static final String ALARM_GROUP = "alarm";
    private static final String ANALOG_INPUT_GROUP = "analogInput";
    private static final String ANALOG_OUTPUT_GROUP = "analogOutput";
    private static final String RAW_CHANNEL_SUFFIX = "Raw";

    private static final byte VOLTAGE_SENSOR_TYPE = 0x00;
    private static final byte CURRENT_SENSOR_TYPE = 0x01;
    private static final byte RESISTANCE_SENSOR_TYPE = 0x02;
    private static final byte PERIOD_MEASUREMENT_SENSOR_TYPE = 0x03;

    private String[] channelText = new String[] { "", "", "", "" };

    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) VelbusSensorConfig sensorConfig;

    public VelbusVMB4ANHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void initialize() {
        this.sensorConfig = getConfigAs(VelbusSensorConfig.class);

        super.initialize();

        initializeAutomaticRefresh();
    }

    private void initializeAutomaticRefresh() {
        int refreshInterval = sensorConfig.refresh;

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
        super.dispose();
    }

    private void startAutomaticRefresh(int refreshInterval) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            sendSensorReadoutRequest(velbusBridgeHandler, ALL_CHANNELS);
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

        byte channelByte = convertChannelUIDToChannelByte(channelUID);

        if (command instanceof RefreshType) {
            VelbusStatusRequestPacket packet = new VelbusStatusRequestPacket(channelByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof PercentType percentCommand && isAnalogOutputChannel(channelUID)) {
            VelbusDimmerPacket packet = new VelbusDimmerPacket(
                    new VelbusChannelIdentifier(this.getModuleAddress().getAddress(), channelByte), COMMAND_SET_VALUE,
                    percentCommand.byteValue(), 0x00, false);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        }
    }

    protected void sendSensorReadoutRequest(VelbusBridgeHandler velbusBridgeHandler, byte channel) {
        VelbusSensorReadoutRequestPacket packet = new VelbusSensorReadoutRequestPacket(getModuleAddress().getAddress(),
                channel);

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

            if (command == COMMAND_SENSOR_RAW_DATA && packet.length >= 10) {
                byte channel = packet[5];
                byte operatingMode = packet[6];
                byte upperByteSensorValue = packet[7];
                byte highByteSensorValue = packet[8];
                byte lowByteSensorValue = packet[9];

                double sensorValue = (((upperByteSensorValue & 0xff) << 16) + ((highByteSensorValue & 0xff) << 8)
                        + (lowByteSensorValue & 0xff));
                String channelUID = convertAnalogInputChannelByteToRawChannelUID(channel);

                switch (operatingMode) {
                    case VOLTAGE_SENSOR_TYPE:
                        double voltageResolution = 0.25;
                        double voltageSensorValueState = sensorValue * voltageResolution;
                        updateState(channelUID,
                                new QuantityType<>(voltageSensorValueState, MetricPrefix.MILLI(Units.VOLT)));
                        break;
                    case CURRENT_SENSOR_TYPE:
                        double currentResolution = 5;
                        double currentSensorValueState = sensorValue * currentResolution;
                        updateState(channelUID,
                                new QuantityType<>(currentSensorValueState, MetricPrefix.MICRO(Units.AMPERE)));
                        break;
                    case RESISTANCE_SENSOR_TYPE:
                        double resistanceResolution = 0.25;
                        double resistanceSensorValueState = sensorValue * resistanceResolution;
                        updateState(channelUID, new QuantityType<>(resistanceSensorValueState, Units.OHM));
                        break;
                    case PERIOD_MEASUREMENT_SENSOR_TYPE:
                        double periodResolution = 0.5;
                        double periodSensorValueState = sensorValue * periodResolution;
                        updateState(channelUID,
                                new QuantityType<>(periodSensorValueState, MetricPrefix.MICRO(Units.SECOND)));
                        break;
                }
            } else if (command == COMMAND_TEXT) {
                byte channel = packet[5];
                byte textStartPosition = packet[6];

                StringBuilder contents = new StringBuilder();
                for (int i = 7; i < packet.length - 2; i++) {
                    byte currentChar = packet[i];
                    if (currentChar == (byte) -0x50) {
                        contents.append("°");
                    } else if (currentChar != (byte) 0x00) {
                        contents.append((char) currentChar);
                    }
                }

                channelText[channel - 9] = channelText[channel - 9].substring(0, textStartPosition)
                        + contents.toString()
                        + (channelText[channel - 9].length() > textStartPosition + 5 ? channelText[channel - 9]
                                .substring(textStartPosition + 5, channelText[channel - 9].length()) : "");

                String channelUID = convertAnalogInputChannelByteToChannelUID(channel);
                updateState(channelUID, new StringType(channelText[channel - 9]));
            }
        }

        return true;
    }

    protected byte convertChannelUIDToChannelByte(ChannelUID channelUID) {
        if (isAlarmChannel(channelUID)) {
            return convertAlarmChannelUIDToChannelByte(channelUID);
        } else if (isTextAnalogInputChannel(channelUID)) {
            return convertTextAnalogInputChannelUIDToChannelByte(channelUID);
        } else if (isRawAnalogInputChannel(channelUID)) {
            return convertRawAnalogInputChannelUIDToChannelByte(channelUID);
        } else if (isAnalogOutputChannel(channelUID)) {
            return convertAnalogOutputChannelUIDToChannelByte(channelUID);
        } else {
            throw new UnsupportedOperationException(
                    "The channel '" + channelUID + "' is not supported on a VMB4AN module.");
        }
    }

    protected boolean isAlarmChannel(ChannelUID channelUID) {
        return ALARM_GROUP.equals(channelUID.getGroupId());
    }

    protected byte convertAlarmChannelUIDToChannelByte(ChannelUID channelUID) {
        return Byte.parseByte(channelUID.getIdWithoutGroup().replaceAll(CHANNEL, ""));
    }

    protected boolean isTextAnalogInputChannel(ChannelUID channelUID) {
        return ANALOG_INPUT_GROUP.equals(channelUID.getGroupId())
                && !channelUID.getIdWithoutGroup().endsWith(RAW_CHANNEL_SUFFIX);
    }

    protected boolean isRawAnalogInputChannel(ChannelUID channelUID) {
        return ANALOG_INPUT_GROUP.equals(channelUID.getGroupId())
                && channelUID.getIdWithoutGroup().endsWith(RAW_CHANNEL_SUFFIX);
    }

    protected byte convertRawAnalogInputChannelUIDToChannelByte(ChannelUID channelUID) {
        return Byte
                .parseByte(channelUID.getIdWithoutGroup().replaceAll(CHANNEL, "").replaceAll(RAW_CHANNEL_SUFFIX, ""));
    }

    protected byte convertTextAnalogInputChannelUIDToChannelByte(ChannelUID channelUID) {
        return Byte.parseByte(channelUID.getIdWithoutGroup().replaceAll(CHANNEL, ""));
    }

    protected String convertAnalogInputChannelByteToRawChannelUID(byte channelByte) {
        return convertAnalogInputChannelByteToChannelUID(channelByte) + RAW_CHANNEL_SUFFIX;
    }

    protected String convertAnalogInputChannelByteToChannelUID(byte channelByte) {
        return ANALOG_INPUT_GROUP + "#" + CHANNEL + channelByte;
    }

    protected boolean isAnalogOutputChannel(ChannelUID channelUID) {
        return ANALOG_OUTPUT_GROUP.equals(channelUID.getGroupId());
    }

    protected byte convertAnalogOutputChannelUIDToChannelByte(ChannelUID channelUID) {
        return Byte.parseByte(channelUID.getIdWithoutGroup().replaceAll(CHANNEL, ""));
    }

    @Override
    protected int getClockAlarmAndProgramSelectionIndexInModuleStatus() {
        return 8;
    }
}
