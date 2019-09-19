/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;

/**
 * Window/Door-Sensor with States Open/Closed/Tilt, Supply voltage monitor
 *
 * @author Dominik Krickl-Vorreiter - Initial contribution
 */
public class A5_14_09 extends A5_14 {
    public final byte CLOSED = (byte) 0x00;
    public final byte TILTED = (byte) 0x01;
    public final byte OPEN = (byte) 0x03;

    public A5_14_09(ERP1Message packet) {
        super(packet);
    }

    private State getWindowhandleState() {
        byte ct = (byte) ((getDB_0() & 0x06) >> 1);

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
        byte ct = (byte) ((getDB_0() & 0x06) >> 1);

        switch (ct) {
            case CLOSED:
                return inverted? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case OPEN:
            case TILTED:
                return inverted? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, Function<String, State> getCurrentStateFunc,
            Configuration config) {
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
