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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Window/Door-Sensor with States Open/Closed/Tilt, Supply voltage monitor
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
@NonNullByDefault
public class A5_14_09 extends A5_14 {
    public static final byte CLOSED = (byte) 0x00;
    public static final byte TILTED = (byte) 0x01;
    public static final byte OPEN = (byte) 0x03;

    public A5_14_09(ERP1Message packet) {
        super(packet);
    }

    private State getWindowhandleState() {
        byte ct = (byte) ((getDB0() & 0x06) >> 1);

        switch (ct) {
            case CLOSED:
                return new StringType("CLOSED");
            case OPEN:
                return new StringType("OPEN");
            case TILTED:
                return new StringType("TILTED");
        }

        return UnDefType.UNDEF;
    }

    private State getContact(boolean inverted) {
        byte ct = (byte) ((getDB0() & 0x06) >> 1);

        switch (ct) {
            case CLOSED:
                return inverted ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case OPEN:
            case TILTED:
                return inverted ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_WINDOWHANDLESTATE:
                return getWindowhandleState();
            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                return getContact(c.inverted);
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}
