/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
public class PTM200Message extends _RPSMessage {

    static final byte On = 0x70;
    static final byte Off = 0x50;
    static final byte Up = 0x70;
    static final byte Down = 0x50;
    static final byte Open = (byte) 0xE0;
    static final byte Closed = (byte) 0xF0;

    public PTM200Message() {
        super();
    }

    public PTM200Message(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        switch (channelId) {
            case CHANNEL_GENERAL_SWITCHING:
                return bytes[0] == On ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_ROLLERSHUTTER:
                return bytes[0] == Up ? PercentType.ZERO : (bytes[0] == Down ? PercentType.HUNDRED : UnDefType.UNDEF);
            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                if (c.inverted) {
                    return bytes[0] == Open ? OpenClosedType.CLOSED
                            : (bytes[0] == Closed ? OpenClosedType.OPEN : UnDefType.UNDEF);
                } else {
                    return bytes[0] == Open ? OpenClosedType.OPEN
                            : (bytes[0] == Closed ? OpenClosedType.CLOSED : UnDefType.UNDEF);
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public boolean isValidForTeachIn() {
        return false;
    }
}
