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
package org.openhab.binding.enocean.internal.eep.F6_00;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_ROLLERSHUTTER;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Stefan Pledl - Initial contribution
 */
@NonNullByDefault
public class F6_00_00_EltakoFJ62 extends _RPSMessage {

    static final byte UP = 0x01;
    static final byte DOWN = 0x02;
    static final byte OPEN = 0x70;
    static final byte CLOSED = 0x50;

    public F6_00_00_EltakoFJ62() {
    }

    public F6_00_00_EltakoFJ62(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                switch (bytes[0]) {
                    case OPEN:
                        return PercentType.ZERO;
                    case CLOSED:
                        return PercentType.HUNDRED;
                    default:
                        return UnDefType.UNDEF;
                }

            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                if (c.inverted) {
                    switch (bytes[0]) {
                        case OPEN:
                            return OpenClosedType.CLOSED;
                        case CLOSED:
                            return OpenClosedType.OPEN;
                        case UP:
                            return OpenClosedType.CLOSED;
                        default:
                            return UnDefType.UNDEF;
                    }
                } else {
                    switch (bytes[0]) {
                        case OPEN:
                            return OpenClosedType.OPEN;
                        case CLOSED:
                            return OpenClosedType.CLOSED;
                        case DOWN:
                            return OpenClosedType.CLOSED;
                        default:
                            return UnDefType.UNDEF;
                    }
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public boolean isValidForTeachIn() {
        return false;
    }
}
