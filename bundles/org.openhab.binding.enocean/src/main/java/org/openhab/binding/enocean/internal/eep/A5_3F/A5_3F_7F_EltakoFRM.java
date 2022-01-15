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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Andreas Hofinger
 */
public class A5_3F_7F_EltakoFRM extends _4BSMessage {

    static final byte Stop = 0x00;
    static final byte Move = 0x03;

    static final int Top = 0xC8;
    static final int Bottom = 0x00;

    public A5_3F_7F_EltakoFRM() {
        super();
    }

    public A5_3F_7F_EltakoFRM(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        if (command instanceof PercentType) {
            PercentType target = (PercentType) command;
            int rawPosition = Math.round(
                    (PercentType.HUNDRED.floatValue() - target.floatValue()) * Top / PercentType.HUNDRED.floatValue());
            int position = Math.min(Top, Math.max(Bottom, rawPosition));
            setData((byte) position, ZERO, Move, TeachInBit);
        } else if (command instanceof UpDownType) {
            if ((UpDownType) command == UpDownType.UP) {
                setData((byte) Top, ZERO, Move, TeachInBit); // => 0 percent
            } else if ((UpDownType) command == UpDownType.DOWN) {
                setData((byte) Bottom, ZERO, Move, TeachInBit); // => 100 percent
            }
        } else if (command instanceof StopMoveType) {
            if ((StopMoveType) command == StopMoveType.STOP) {
                setData(ZERO, ZERO, Stop, TeachInBit);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        // 0x0A.. Move was locked for switch
        // 0x0E.. Move was not locked
        if (getDB_2() == ZERO && getDB_1() == Move && (getDB_0() == 0x0A || getDB_0() == 0x0E)) {
            int position = getDB_3Value();
            float percentage = 100.0f * (Top - position) / (float) (Top - Bottom);
            return new PercentType(Math.round(Math.min(100, (Math.max(0, percentage)))));
        }
        return UnDefType.UNDEF;
    }
}
