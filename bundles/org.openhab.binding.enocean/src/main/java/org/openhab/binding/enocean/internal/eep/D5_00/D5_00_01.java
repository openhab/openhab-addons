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
package org.openhab.binding.enocean.internal.eep.D5_00;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.eep.Base._1BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class D5_00_01 extends _1BSMessage {

    final byte OPEN = 0 | TeachInBit;
    final byte CLOSED = 1 | TeachInBit;

    public D5_00_01() {
        super();
    }

    public D5_00_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
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
}
