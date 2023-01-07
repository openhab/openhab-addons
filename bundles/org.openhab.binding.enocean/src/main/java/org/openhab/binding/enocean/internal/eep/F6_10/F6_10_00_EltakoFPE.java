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
package org.openhab.binding.enocean.internal.eep.F6_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Holger Englert - Initial contribution
 */
@NonNullByDefault
public class F6_10_00_EltakoFPE extends _RPSMessage {

    protected static final byte OPEN = 0x00;
    protected static final byte CLOSED = 0x10;

    public F6_10_00_EltakoFPE() {
        super();
    }

    public F6_10_00_EltakoFPE(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        if (channelId.equals(CHANNEL_CONTACT)) {
            EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
            if (c.inverted) {
                return bytes[0] == CLOSED ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            } else {
                return bytes[0] == CLOSED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        // FPE just sends 0b00010000 or 0b00000000 value, so we apply mask 0b11101111
        return super.validateData(bytes) && ((bytes[0] & (byte) 0xEF) == (byte) 0x00);
    }

    @Override
    public boolean isValidForTeachIn() {
        // just treat CLOSED as teach in
        return bytes[0] == CLOSED;
    }
}
