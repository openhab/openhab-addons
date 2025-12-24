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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Andreas Hofinger - Initial contribution
 */
@NonNullByDefault
public class A5_3F_7F_EltakoFRM extends _4BSMessage {

    static final byte STOP = 0x00;
    static final byte MOVE = 0x03;

    static final int TOP = 0xC8;
    static final int BOTTOM = 0x00;

    public A5_3F_7F_EltakoFRM() {
    }

    public A5_3F_7F_EltakoFRM(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (command instanceof PercentType percentCommand) {
            int rawPosition = Math.round((PercentType.HUNDRED.floatValue() - percentCommand.floatValue()) * TOP
                    / PercentType.HUNDRED.floatValue());
            int position = Math.min(TOP, Math.max(BOTTOM, rawPosition));
            setData((byte) position, ZERO, MOVE, TEACHIN_BIT);
        } else if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
                setData((byte) TOP, ZERO, MOVE, TEACHIN_BIT); // => 0 percent
            } else if (upDownCommand == UpDownType.DOWN) {
                setData((byte) BOTTOM, ZERO, MOVE, TEACHIN_BIT); // => 100 percent
            }
        } else if (command instanceof StopMoveType stopMoveCommand) {
            if (stopMoveCommand == StopMoveType.STOP) {
                setData(ZERO, ZERO, STOP, TEACHIN_BIT);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        // 0x0A.. Move was locked for switch
        // 0x0E.. Move was not locked
        if (getDB2() == ZERO && getDB1() == MOVE && (getDB0() == 0x0A || getDB0() == 0x0E)) {
            int position = getDB3Value();
            float percentage = 100.0f * (TOP - position) / (float) (TOP - BOTTOM);
            return new PercentType(Math.round(Math.min(100, (Math.max(0, percentage)))));
        }
        return UnDefType.UNDEF;
    }
}
