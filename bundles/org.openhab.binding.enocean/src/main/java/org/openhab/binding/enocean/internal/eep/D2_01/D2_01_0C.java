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
package org.openhab.binding.enocean.internal.eep.D2_01;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class D2_01_0C extends D2_01 {

    protected static final byte CMD_ACTUATOR_SET_PILOT_WIRE = 0x08;
    protected static final byte CMD_ACTUATOR_PILOT_WIRE_QUERY = 0x09;
    protected static final byte CMD_ACTUATOR_PILOT_WIRE_RESPONSE = 0x0A;

    public D2_01_0C() {
    }

    public D2_01_0C(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (channelId.equals(CHANNEL_PILOT_WIRE)) {
            if (command == RefreshType.REFRESH) {
                setData(CMD_ACTUATOR_PILOT_WIRE_QUERY, ALL_CHANNELS_MASK);
            } else if (command instanceof DecimalType decimalCommand) {
                setData(CMD_ACTUATOR_SET_PILOT_WIRE, decimalCommand.byteValue());
            }
        } else {
            super.convertFromCommandImpl(channelId, channelTypeId, command, getCurrentStateFunc, config);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_PILOT_WIRE)) {
            if (getCMD() == CMD_ACTUATOR_PILOT_WIRE_RESPONSE) {
                return new DecimalType(bytes[1] & 0b111);
            }

            return UnDefType.UNDEF;
        } else {
            return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
        }
    }
}
