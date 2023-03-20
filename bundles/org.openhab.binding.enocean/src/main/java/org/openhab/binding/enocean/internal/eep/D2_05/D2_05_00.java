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
package org.openhab.binding.enocean.internal.eep.D2_05;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._VLDMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D2_05_00 extends _VLDMessage {

    protected final byte cmdMask = 0x0f;
    protected final byte outputValueMask = 0x7f;
    protected final byte outputChannelMask = 0x1f;

    protected final byte CMD_ACTUATOR_SET_POSITION = 0x01;
    protected final byte CMD_ACTUATOR_STOP = 0x02;
    protected final byte CMD_ACTUATOR_POSITION_QUERY = 0x03;
    protected final byte CMD_ACTUATOR_POSITION_RESPONE = 0x04;

    protected final byte AllChannels_Mask = 0x1e;
    protected final byte ChannelA_Mask = 0x00;

    protected final byte DOWN = 0x64; // 100%
    protected final byte UP = 0x00; // 0%

    public D2_05_00() {
        super();
    }

    public D2_05_00(ERP1Message packet) {
        super(packet);
    }

    protected byte getCMD() {
        return (byte) (bytes[bytes.length - 1] & cmdMask);
    }

    protected void setPositionData(Command command, byte outputChannel) {
        if (command instanceof UpDownType) {
            if (command == UpDownType.DOWN) {
                setData(DOWN, (byte) 0x00, (byte) 0x00, (byte) (outputChannel + CMD_ACTUATOR_SET_POSITION));
            } else {
                setData(UP, (byte) 0x00, (byte) 0x00, (byte) (outputChannel + CMD_ACTUATOR_SET_POSITION));
            }
        } else if (command instanceof StopMoveType) {
            if (command == StopMoveType.STOP) {
                setData((byte) (outputChannel + CMD_ACTUATOR_STOP));
            }
        } else if (command instanceof PercentType) {
            setData((byte) (((PercentType) command).intValue()), (byte) 0x00, (byte) 0x00,
                    (byte) (outputChannel + CMD_ACTUATOR_SET_POSITION));
        }
    }

    protected void setPositionQueryData(byte outputChannel) {
        setData((byte) (outputChannel + CMD_ACTUATOR_POSITION_QUERY));
    }

    protected State getPositionData() {
        if (getCMD() == CMD_ACTUATOR_POSITION_RESPONE) {
            int position = bytes[0] & 0x7f;
            if (position != 127) {
                return new PercentType(position);
            }
        }

        return UnDefType.UNDEF;
    }

    protected byte getChannel() {
        return (byte) (bytes[1] & outputChannelMask);
    }

    @Override
    public void addConfigPropertiesTo(DiscoveryResultBuilder discoveredThingResultBuilder) {
        discoveredThingResultBuilder.withProperty(PARAMETER_SENDINGEEPID, getEEPType().getId())
                .withProperty(PARAMETER_RECEIVINGEEPID, getEEPType().getId());
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_ROLLERSHUTTER)) {
            if (command == RefreshType.REFRESH) {
                setPositionQueryData(ChannelA_Mask);
            } else {
                setPositionData(command, ChannelA_Mask);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                return getPositionData();
        }

        return UnDefType.UNDEF;
    }
}
