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
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.config.RFXComLighting4DeviceConfiguration.PULSE_LABEL;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComLighting4DeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for lighting4 message.
 *
 * a Lighting4 Base command is composed of 24 bit DATA plus PULSE information
 *
 * DATA:
 * Code = 014554
 * S1- S24 = <0000 0001 0100 0101 0101> <0100>
 * first 20 are DeviceID last 4 are for Command
 *
 * PULSE:
 * default 350
 *
 * Tested on a PT2262 remote PlugIn module
 *
 * Example:
 *
 * Switch TESTout "TestOut" (All) {rfxcom=">83205.350:LIGHTING4.PT2262:Command"}
 * (SendCommand DeviceID(int).Pulse(int):LIGHTING4.Subtype:Command )
 *
 * Switch TESTin "TestIn" (All) {rfxcom="<83205:Command"}
 * (ReceiveCommand ON/OFF Command )
 *
 * @author Alessandro Ballini (ITA) - Initial contribution
 * @author Pauli Anttila - Migrated to OH2
 * @author Martin van Wingerden - Extended support for more complex PT2262 devices
 * @author James Hewitt - Use the thing config to identify what incoming commandIds map to
 * @author James Hewitt - Deprecate using previously discovered commandIds because they are unreliable
 */
public class RFXComLighting4Message extends RFXComDeviceMessageImpl<RFXComLighting4Message.SubType> {
    public enum SubType implements ByteEnumWrapper {
        PT2262(0);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    // These are historical behaviour, are deprecated, and will be removed in a future openHAB release.
    @Deprecated
    private static final byte DEFAULT_OFF_COMMAND_ID = 4;
    @Deprecated
    private static final byte DEFAULT_ON_COMMAND_ID = 1;
    @Deprecated
    private Set<Integer> ON_COMMAND_IDS = Stream.of(1, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15)
            .collect(Collectors.toCollection(HashSet::new));

    private SubType subType;
    private int sensorId;
    private int pulse;
    private int commandId;

    private RFXComLighting4DeviceConfiguration config;

    public RFXComLighting4Message() {
        super(PacketType.LIGHTING4);
    }

    public RFXComLighting4Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command Id = " + commandId;
        str += ", Pulse = " + pulse;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = ByteEnumUtil.fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 12 | (data[5] & 0xFF) << 4 | (data[6] & 0xF0) >> 4;

        commandId = (data[6] & 0x0F);

        pulse = (data[7] & 0xFF) << 8 | (data[8] & 0xFF);

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = PacketType.LIGHTING4.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        // SENSOR_ID + COMMAND
        data[4] = (byte) ((sensorId >> 12) & 0xFF);
        data[5] = (byte) ((sensorId >> 4) & 0xFF);
        data[6] = (byte) ((sensorId << 4 & 0xF0) | (commandId & 0x0F));

        // PULSE
        data[7] = (byte) (pulse >> 8 & 0xFF);
        data[8] = (byte) (pulse & 0xFF);

        // SIGNAL
        data[9] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration configuration, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        RFXComLighting4DeviceConfiguration config = (RFXComLighting4DeviceConfiguration) configuration;

        switch (channelId) {
            case CHANNEL_COMMAND:
            case CHANNEL_MOTION:
                if (config.onCommandId != null && commandId == config.onCommandId) {
                    return OnOffType.ON;
                }
                if (config.offCommandId != null && commandId == config.offCommandId) {
                    return OnOffType.OFF;
                }
                // Deprecated if statement - to be removed in a future release
                if (config.onCommandId == null && config.offCommandId == null) {
                    return ON_COMMAND_IDS.contains(commandId) ? OnOffType.ON : OnOffType.OFF;
                }
                throw new RFXComInvalidStateException(channelId, Integer.toString(commandId),
                        "Device not configured for received commandId");

            case CHANNEL_CONTACT:
                if (config.openCommandId != null && commandId == config.openCommandId) {
                    return OpenClosedType.OPEN;
                }
                if (config.closedCommandId != null && commandId == config.closedCommandId) {
                    return OpenClosedType.CLOSED;
                }
                // Deprecated if statement - to be removed in a future release
                if (config.onCommandId == null && config.offCommandId == null) {
                    return ON_COMMAND_IDS.contains(commandId) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                }
                throw new RFXComInvalidStateException(channelId, Integer.toString(commandId),
                        "Device not configured for received commandId");

            case CHANNEL_COMMAND_ID:
                return new DecimalType(commandId);

            default:
                return super.convertToState(channelId, config, deviceState);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String deviceId) {
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(String channelId, Type type)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    if (type == OnOffType.ON) {
                        if (config.onCommandId != null) {
                            commandId = config.onCommandId;
                        } else {
                            // Deprecated - to throw RFXComInvalidStateException in a future release, see contact
                            // channel
                            commandId = DEFAULT_ON_COMMAND_ID;
                        }
                    }
                    if (type == OnOffType.OFF) {
                        if (config.offCommandId != null) {
                            commandId = config.offCommandId;
                        } else {
                            // Deprecated - to throw RFXComInvalidStateException in a future release, see contact
                            // channel
                            commandId = DEFAULT_OFF_COMMAND_ID;
                        }
                    }
                } else {
                    throw new RFXComInvalidStateException(channelId, type.toString(),
                            "Channel only supports OnOffType");
                }
                break;

            case CHANNEL_CONTACT:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.OPEN) {
                        if (config.openCommandId != null) {
                            commandId = config.openCommandId;
                        } else {
                            throw new RFXComInvalidStateException(channelId, type.toString(),
                                    "openCommandId not configured for this device");
                        }
                    }
                    if (type == OpenClosedType.CLOSED) {
                        if (config.closedCommandId != null) {
                            commandId = config.closedCommandId;
                        } else {
                            throw new RFXComInvalidStateException(channelId, type.toString(),
                                    "closedCommandId not configured for this device");
                        }
                    }
                } else {
                    throw new RFXComInvalidStateException(channelId, type.toString(),
                            "Channel only supports OpenClosedType");
                }
                break;

            case CHANNEL_COMMAND_ID:
                if (type instanceof DecimalType decimalCommand) {
                    commandId = (byte) decimalCommand.intValue();
                } else {
                    throw new RFXComInvalidStateException(channelId, type.toString(),
                            "Channel only supports DecimalType");
                }
                break;

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not supported by Lighting4");
        }
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }

    @Override
    public void addDevicePropertiesTo(DiscoveryResultBuilder discoveryResultBuilder) throws RFXComException {
        super.addDevicePropertiesTo(discoveryResultBuilder);
        discoveryResultBuilder.withProperty(PULSE_LABEL, pulse);
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration config) throws RFXComException {
        super.setConfig(config);
        this.config = (RFXComLighting4DeviceConfiguration) config;
        this.pulse = this.config.pulse != null ? this.config.pulse : 350;
    }
}
