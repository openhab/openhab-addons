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
package org.openhab.binding.enocean.internal.eep.D2_01;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.config.EnOceanChannelDimmerConfig;
import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.eep.EEPHelper;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class D2_01 extends _VLDMessage {

    protected final byte cmdMask = 0x0f;
    protected final byte outputValueMask = 0x7f;
    protected final byte outputChannelMask = 0x1f;

    protected final byte CMD_ACTUATOR_SET_STATUS = 0x01;
    protected final byte CMD_ACTUATOR_STATUS_QUERY = 0x03;
    protected final byte CMD_ACTUATOR_STATUS_RESPONE = 0x04;
    protected final byte CMD_ACTUATOR_MEASUREMENT_QUERY = 0x06;
    protected final byte CMD_ACTUATOR_MEASUREMENT_RESPONE = 0x07;

    protected final byte AllChannels_Mask = 0x1e;
    protected final byte ChannelA_Mask = 0x00;
    protected final byte ChannelB_Mask = 0x01;

    protected final byte STATUS_SWITCHING_ON = 0x01;
    protected final byte STATUS_SWITCHING_OFF = 0x00;
    protected final byte STATUS_DIMMING_100 = 0x64;

    public D2_01() {
        super();
    }

    public D2_01(ERP1Message packet) {
        super(packet);
    }

    protected byte getCMD() {
        return (byte) (bytes[0] & cmdMask);
    }

    protected void setSwitchingData(OnOffType command, byte outputChannel) {
        if (command == OnOffType.ON) {
            setData(CMD_ACTUATOR_SET_STATUS, outputChannel, STATUS_SWITCHING_ON);
        } else {
            setData(CMD_ACTUATOR_SET_STATUS, outputChannel, STATUS_SWITCHING_OFF);
        }
    }

    protected void setSwitchingQueryData(byte outputChannel) {
        setData(CMD_ACTUATOR_STATUS_QUERY, outputChannel);
    }

    protected State getSwitchingData() {
        if (getCMD() == CMD_ACTUATOR_STATUS_RESPONE) {
            return (bytes[bytes.length - 1] & outputValueMask) == STATUS_SWITCHING_OFF ? OnOffType.OFF : OnOffType.ON;
        }

        return UnDefType.UNDEF;
    }

    protected byte getChannel() {
        return (byte) (bytes[1] & outputChannelMask);
    }

    protected State getSwitchingData(byte channel) {
        if (getCMD() == CMD_ACTUATOR_STATUS_RESPONE && (getChannel() == channel || getChannel() == AllChannels_Mask)) {
            return (bytes[bytes.length - 1] & outputValueMask) == STATUS_SWITCHING_OFF ? OnOffType.OFF : OnOffType.ON;
        }

        return UnDefType.UNDEF;
    }

    protected void setDimmingData(Command command, byte outputChannel, Configuration config) {
        byte outputValue;

        if (command instanceof DecimalType) {
            if (((DecimalType) command).equals(DecimalType.ZERO)) {
                outputValue = STATUS_SWITCHING_OFF;
            } else {
                outputValue = ((DecimalType) command).byteValue();
            }
        } else if ((OnOffType) command == OnOffType.ON) {
            outputValue = STATUS_DIMMING_100;
        } else {
            outputValue = STATUS_SWITCHING_OFF;
        }

        EnOceanChannelDimmerConfig c = config.as(EnOceanChannelDimmerConfig.class);
        byte rampingTime = Integer.valueOf(c.rampingTime).byteValue();

        setData(CMD_ACTUATOR_SET_STATUS, (byte) ((rampingTime << 5) | outputChannel), outputValue);
    }

    protected State getDimmingData() {
        if (getCMD() == CMD_ACTUATOR_STATUS_RESPONE) {
            return new PercentType((bytes[bytes.length - 1] & outputValueMask));
        }

        return UnDefType.UNDEF;
    }

    protected void setEnergyMeasurementQueryData(byte outputChannel) {
        setData(CMD_ACTUATOR_MEASUREMENT_QUERY, outputChannel);
    }

    protected void setPowerMeasurementQueryData(byte outputChannel) {
        setData(CMD_ACTUATOR_MEASUREMENT_QUERY, (byte) (0x20 | outputChannel));
    }

    protected State getEnergyMeasurementData() {
        if (getCMD() == CMD_ACTUATOR_MEASUREMENT_RESPONE) {
            float factor = 1;

            switch (bytes[1] >>> 5) {
                case 0: // value is given as watt seconds, so divide it by 3600 to get watt hours, and 1000 to get
                        // kilowatt hours
                    factor /= (3600 * 1000);
                    break;
                case 1: // value is given as watt hours, so divide it by 1000 to get kilowatt hours
                    factor /= 1000;
                    break;
                case 2: // value is given as kilowatt hours
                    factor = 1;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            float energy = Long.parseLong(HexUtils.bytesToHex(new byte[] { bytes[2], bytes[3], bytes[4], bytes[5] }),
                    16) * factor;
            return new QuantityType<>(energy, Units.KILOWATT_HOUR);
        }

        return UnDefType.UNDEF;
    }

    protected State getPowerMeasurementData() {
        if (getCMD() == CMD_ACTUATOR_MEASUREMENT_RESPONE) {
            float factor = 1;

            switch (bytes[1] >>> 5) {
                case 3: // value is given as watt
                    factor = 1;
                    break;
                case 4: // value is given as kilowatt
                    factor /= 1000;
                    break;
                default:
                    return UnDefType.UNDEF;
            }

            float power = Long.parseLong(HexUtils.bytesToHex(new byte[] { bytes[2], bytes[3], bytes[4], bytes[5] }), 16)
                    * factor;

            return new QuantityType<>(power, Units.WATT);
        }

        return UnDefType.UNDEF;
    }

    @Override
    public void addConfigPropertiesTo(DiscoveryResultBuilder discoveredThingResultBuilder) {
        discoveredThingResultBuilder.withProperty(PARAMETER_SENDINGEEPID, getEEPType().getId())
                .withProperty(PARAMETER_RECEIVINGEEPID, getEEPType().getId());
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_GENERAL_SWITCHING)) {
            if (command == RefreshType.REFRESH) {
                setSwitchingQueryData(AllChannels_Mask);
            } else {
                setSwitchingData((OnOffType) command, AllChannels_Mask);
            }
        } else if (channelId.equals(CHANNEL_GENERAL_SWITCHINGA)) {
            if (command == RefreshType.REFRESH) {
                setSwitchingQueryData(ChannelA_Mask);
            } else {
                setSwitchingData((OnOffType) command, ChannelA_Mask);
            }
        } else if (channelId.equals(CHANNEL_GENERAL_SWITCHINGB)) {
            if (command == RefreshType.REFRESH) {
                setSwitchingQueryData(ChannelB_Mask);
            } else {
                setSwitchingData((OnOffType) command, ChannelB_Mask);
            }
        } else if (channelId.equals(CHANNEL_DIMMER)) {
            if (command == RefreshType.REFRESH) {
                setSwitchingQueryData(AllChannels_Mask);
            } else {
                setDimmingData(command, AllChannels_Mask, config);
            }
        } else if (channelId.equals(CHANNEL_INSTANTPOWER) && command == RefreshType.REFRESH) {
            setPowerMeasurementQueryData(AllChannels_Mask);
        } else if (channelId.equals(CHANNEL_TOTALUSAGE) && command == RefreshType.REFRESH) {
            setEnergyMeasurementQueryData(AllChannels_Mask);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_GENERAL_SWITCHING:
                return getSwitchingData();
            case CHANNEL_GENERAL_SWITCHINGA:
                return getSwitchingData(ChannelA_Mask);
            case CHANNEL_GENERAL_SWITCHINGB:
                return getSwitchingData(ChannelB_Mask);
            case CHANNEL_DIMMER:
                return getDimmingData();
            case CHANNEL_INSTANTPOWER:
                return getPowerMeasurementData();
            case CHANNEL_TOTALUSAGE:
                State value = getEnergyMeasurementData();
                State currentState = getCurrentStateFunc.apply(channelId);
                return EEPHelper.validateTotalUsage(value, currentState, config);
        }

        return UnDefType.UNDEF;
    }
}
