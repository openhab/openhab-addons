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
package org.openhab.binding.enocean.internal.eep.F6_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_10_00 extends _RPSMessage {

    public final byte Closed = (byte) 0xF0; // 1111xxxx
    public final byte Open1 = (byte) 0xE0; // 1110xxxx
    public final byte Open2 = (byte) 0xC0; // 1100xxxx
    public final byte Tilted = (byte) 0xD0; // 1101xxxx

    public F6_10_00() {
        super();
    }

    public F6_10_00(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {

        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        byte data = (byte) (bytes[0] & 0xF0);

        // todo localization
        switch (channelId) {
            case CHANNEL_WINDOWHANDLESTATE:
                if (data == Closed) {
                    return new StringType("CLOSED");
                } else if (data == Tilted) {
                    return new StringType("TILTED");
                } else if (data == Open1 || data == Open2) {
                    return new StringType("OPEN");
                }

            case CHANNEL_CONTACT:
                if (data == Closed) {
                    return OpenClosedType.CLOSED;
                } else if (data == Tilted) {
                    return OpenClosedType.OPEN;
                } else if (data == Open1 || data == Open2) {
                    return OpenClosedType.OPEN;
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && getBit(bytes[0], 7) && getBit(bytes[0], 6);
    }
}
