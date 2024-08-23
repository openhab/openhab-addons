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
package org.openhab.binding.enocean.internal.eep.F6_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class F6_10_00 extends _RPSMessage {

    public static final byte CLOSED = (byte) 0xF0; // 1111xxxx
    public static final byte OPEN_1 = (byte) 0xE0; // 1110xxxx
    public static final byte OPEN_2 = (byte) 0xC0; // 1100xxxx
    public static final byte TILTED = (byte) 0xD0; // 1101xxxx

    public F6_10_00() {
        super();
    }

    public F6_10_00(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        byte data = (byte) (bytes[0] & 0xF0);

        // todo localization
        switch (channelId) {
            case CHANNEL_WINDOWHANDLESTATE:
                if (data == CLOSED) {
                    return new StringType("CLOSED");
                } else if (data == TILTED) {
                    return new StringType("TILTED");
                } else if (data == OPEN_1 || data == OPEN_2) {
                    return new StringType("OPEN");
                }

            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                if (data == CLOSED) {
                    return c.inverted ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                } else if (data == TILTED) {
                    return c.inverted ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                } else if (data == OPEN_1 || data == OPEN_2) {
                    return c.inverted ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && getBit(bytes[0], 7) && getBit(bytes[0], 6);
    }

    @Override
    public boolean isValidForTeachIn() {
        return t21 && !nu && getBit(bytes[0], 7) && getBit(bytes[0], 6);
    }
}
