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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRollershutterConfig;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

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
            Map<String, State> currentState, Configuration config) {

        int shutTime = 0xFF;
        if (config != null) {
            shutTime = Math.min(255, config.as(EnOceanChannelRollershutterConfig.class).shutTime);
        }

        if (command instanceof PercentType) {
            State channelState = currentState.get(channelId);

            PercentType target = (PercentType) command;
            if (target.intValue() == PercentType.ZERO.intValue()) {
                setData(Zero, (byte) shutTime, MoveUp, TeachInBit); // => move completely up
            } else if (target.intValue() == PercentType.HUNDRED.intValue()) {
                setData(Zero, (byte) shutTime, MoveDown, TeachInBit); // => move completely down
            } else if (channelState != null) {
                PercentType current = channelState.as(PercentType.class);
                if (config != null && current != null) {
                    if (current.intValue() != target.intValue()) {
                        byte direction = current.intValue() > target.intValue() ? MoveUp : MoveDown;
                        byte duration = (byte) Math.min(255,
                                (Math.abs(current.intValue() - target.intValue()) * shutTime)
                                        / PercentType.HUNDRED.intValue());

                        setData(Zero, duration, direction, TeachInBit);
                    }
                }
            }

        } else if (command instanceof UpDownType) {
            if ((UpDownType) command == UpDownType.UP) {
                setData(Zero, (byte) shutTime, MoveUp, TeachInBit); // => 0 percent
            } else if ((UpDownType) command == UpDownType.DOWN) {
                setData(Zero, (byte) shutTime, MoveDown, TeachInBit); // => 100 percent
            }
        } else if (command instanceof StopMoveType) {
            if ((StopMoveType) command == StopMoveType.STOP) {
                setData(Zero, (byte) 0xFF, Stop, TeachInBit);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId, State currentState,
            Configuration config) {

        if (currentState != null) {
            int direction = getDB_1() == MoveUp ? -1 : 1;
            int duration = ((getDB_3Value() << 8) + getDB_2Value()) / 10; // => Time in DB3 and DB2 is given
                                                                          // in ms

            PercentType current = currentState.as(PercentType.class);
            if (config != null && current != null) {
                EnOceanChannelRollershutterConfig c = config.as(EnOceanChannelRollershutterConfig.class);
                if (c.shutTime != -1 && c.shutTime != 0) {
                    return new PercentType(Math.min(100, (Math.max(0, current.intValue()
                            + direction * ((duration * PercentType.HUNDRED.intValue()) / c.shutTime)))));
                }
            }
        }

        return UnDefType.UNDEF;
    }
}
