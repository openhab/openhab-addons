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

import org.openhab.binding.enocean.internal.config.EnOceanChannelRollershutterConfig;
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
 * @author Daniel Weber - Initial contribution
 */
public class A5_3F_7F_EltakoFSB extends _4BSMessage {

    static final byte Stop = 0x00;
    static final byte MoveUp = 0x01;
    static final byte MoveDown = 0x02;

    static final byte Up = 0x70;
    static final byte Down = 0x50;

    public A5_3F_7F_EltakoFSB() {
        super();
    }

    public A5_3F_7F_EltakoFSB(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        int shutTime = 0xFF;
        if (config != null) {
            shutTime = Math.min(255, config.as(EnOceanChannelRollershutterConfig.class).shutTime);
        }

        if (command instanceof PercentType) {
            State channelState = getCurrentStateFunc.apply(channelId);

            PercentType target = (PercentType) command;
            if (target.intValue() == PercentType.ZERO.intValue()) {
                setData(ZERO, (byte) shutTime, MoveUp, TeachInBit); // => move completely up
            } else if (target.intValue() == PercentType.HUNDRED.intValue()) {
                setData(ZERO, (byte) shutTime, MoveDown, TeachInBit); // => move completely down
            } else if (channelState != null) {
                PercentType current = channelState.as(PercentType.class);
                if (config != null && current != null) {
                    if (current.intValue() != target.intValue()) {
                        byte direction = current.intValue() > target.intValue() ? MoveUp : MoveDown;
                        byte duration = (byte) Math.min(255,
                                (Math.abs(current.intValue() - target.intValue()) * shutTime)
                                        / PercentType.HUNDRED.intValue());

                        setData(ZERO, duration, direction, TeachInBit);
                    }
                }
            }

        } else if (command instanceof UpDownType) {
            if ((UpDownType) command == UpDownType.UP) {
                setData(ZERO, (byte) shutTime, MoveUp, TeachInBit); // => 0 percent
            } else if ((UpDownType) command == UpDownType.DOWN) {
                setData(ZERO, (byte) shutTime, MoveDown, TeachInBit); // => 100 percent
            }
        } else if (command instanceof StopMoveType) {
            if ((StopMoveType) command == StopMoveType.STOP) {
                setData(ZERO, (byte) 0xFF, Stop, TeachInBit);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        State currentState = getCurrentStateFunc.apply(channelId);

        if (currentState != null) {
            int duration = ((getDB_3Value() << 8) + getDB_2Value()) / 10; // => Time in DB3 and DB2 is given
                                                                          // in ms

            if (config != null) {
                EnOceanChannelRollershutterConfig c = config.as(EnOceanChannelRollershutterConfig.class);
                if (duration == c.shutTime) {
                    return getDB_1() == MoveUp ? PercentType.ZERO : PercentType.HUNDRED;
                } else {
                    PercentType current = PercentType.ZERO;
                    if (currentState instanceof PercentType) {
                        current = currentState.as(PercentType.class);
                    }

                    int direction = getDB_1() == MoveUp ? -1 : 1;
                    if (c.shutTime != -1 && c.shutTime != 0) {
                        return new PercentType(Math.min(100, (Math.max(0, current.intValue()
                                + direction * ((duration * PercentType.HUNDRED.intValue()) / c.shutTime)))));
                    }
                }
            }
        }

        return UnDefType.UNDEF;
    }
}
