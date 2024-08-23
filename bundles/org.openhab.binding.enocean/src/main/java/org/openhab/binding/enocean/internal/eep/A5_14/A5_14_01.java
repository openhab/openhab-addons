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
package org.openhab.binding.enocean.internal.eep.A5_14;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

/**
 * Single Input Contact (Window/Door), Supply voltage monitor
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_14_01 extends A5_14 {

    public A5_14_01(ERP1Message packet) {
        super(packet);
    }

    private State getContact(boolean inverted) {
        boolean ct = getBit(getDB0(), 0);

        if (inverted) {
            return ct ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else {
            return ct ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                return getContact(c.inverted);
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}
