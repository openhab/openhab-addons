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
public class F6_10_01 extends _RPSMessage {

    public static final byte CLOSED = 0x0F; // xxxx1111
    public static final byte OPEN_1 = 0x0E; // xxxx1110
    public static final byte OPEN_2 = 0x0C; // xxxx1100
    public static final byte TILTED = 0x0D; // xxxx1101

    public F6_10_01() {
    }

    public F6_10_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        byte data = (byte) (bytes[0] & 0x0F);

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
        return super.validateData(bytes) && getBit(bytes[0], 6) && getBit(bytes[0], 3) && getBit(bytes[0], 2);
    }

    @Override
    public boolean isValidForTeachIn() {
        return !getBit(bytes[0], 7) && getBit(bytes[0], 6) && !getBit(bytes[0], 5) && !getBit(bytes[0], 4)
                && getBit(bytes[0], 3) && getBit(bytes[0], 2);
    }
}
