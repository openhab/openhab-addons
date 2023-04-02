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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class PTM200Message extends _RPSMessage {

    static final byte SWITCH_ON = 0x70;
    static final byte SWITCH_OFF = 0x50;
    static final byte UP = 0x70;
    static final byte DOWN = 0x50;
    static final byte OPEN = (byte) 0xE0;
    static final byte CLOSED = (byte) 0xF0;

    public PTM200Message() {
        super();
    }

    public PTM200Message(ERP1Message packet) {
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
            case CHANNEL_GENERAL_SWITCHING:
                return bytes[0] == SWITCH_ON ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_ROLLERSHUTTER:
                return bytes[0] == UP ? PercentType.ZERO : (bytes[0] == DOWN ? PercentType.HUNDRED : UnDefType.UNDEF);
            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                if (c.inverted) {
                    return bytes[0] == OPEN ? OpenClosedType.CLOSED
                            : (bytes[0] == CLOSED ? OpenClosedType.OPEN : UnDefType.UNDEF);
                } else {
                    return bytes[0] == OPEN ? OpenClosedType.OPEN
                            : (bytes[0] == CLOSED ? OpenClosedType.CLOSED : UnDefType.UNDEF);
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public boolean isValidForTeachIn() {
        return false;
    }
}
