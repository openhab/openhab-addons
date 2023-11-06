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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.ZERO;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class A5_3F_7F_EltakoFSB extends _4BSMessage {

    static final byte STOP = 0x00;
    static final byte MOVE_UP = 0x01;
    static final byte MOVE_DOWN = 0x02;

    static final byte UP = 0x70;
    static final byte DOWN = 0x50;

    public A5_3F_7F_EltakoFSB() {
        super();
    }

    public A5_3F_7F_EltakoFSB(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        int shutTime = 0xFF;
        if (config == null) {
            logger.debug("No configuration, shutTime fallback to {}", shutTime);
        } else {
            shutTime = Math.min(255, config.as(EnOceanChannelRollershutterConfig.class).shutTime);
        }

        if (command instanceof PercentType percentCommand) {
            State channelState = getCurrentStateFunc.apply(channelId);
            if (percentCommand.intValue() == PercentType.ZERO.intValue()) {
                setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => move completely up
            } else if (percentCommand.intValue() == PercentType.HUNDRED.intValue()) {
                setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => move completely down
            } else if (channelState != null) {
                PercentType current = channelState.as(PercentType.class);
                if (current != null) {
                    if (current.intValue() != percentCommand.intValue()) {
                        byte direction = current.intValue() > percentCommand.intValue() ? MOVE_UP : MOVE_DOWN;
                        byte duration = (byte) Math.min(255,
                                (Math.abs(current.intValue() - percentCommand.intValue()) * shutTime)
                                        / PercentType.HUNDRED.intValue());

                        setData(ZERO, duration, direction, TEACHIN_BIT);
                    }
                }
            }

        } else if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
                setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => 0 percent
            } else if (upDownCommand == UpDownType.DOWN) {
                setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => 100 percent
            }
        } else if (command instanceof StopMoveType stopMoveCommand) {
            if (stopMoveCommand == StopMoveType.STOP) {
                setData(ZERO, (byte) 0xFF, STOP, TEACHIN_BIT);
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        State currentState = getCurrentStateFunc.apply(channelId);

        if (currentState != null) {
            int duration = ((getDB3Value() << 8) + getDB2Value()) / 10; // => Time in DB3 and DB2 is given
                                                                        // in ms
            EnOceanChannelRollershutterConfig c = config.as(EnOceanChannelRollershutterConfig.class);
            if (duration == c.shutTime) {
                return getDB1() == MOVE_UP ? PercentType.ZERO : PercentType.HUNDRED;
            } else {
                PercentType current = PercentType.ZERO;
                if (currentState instanceof PercentType) {
                    current = currentState.as(PercentType.class);
                }

                int direction = getDB1() == MOVE_UP ? -1 : 1;
                if (current != null && c.shutTime != -1 && c.shutTime != 0) {
                    return new PercentType(Math.min(100, (Math.max(0, current.intValue()
                            + direction * ((duration * PercentType.HUNDRED.intValue()) / c.shutTime)))));
                }
            }
        }

        return UnDefType.UNDEF;
    }
}
