/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelContactConfig;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.statemachine.STMAction;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 * @author Sven Schad - added state machine for blinds/rollershutter, see A5_3F_7F_EltakoFSB.class
 */
@NonNullByDefault
public class PTM200Message extends _RPSMessage {

    static final byte SWITCH_ON = 0x70;
    static final byte SWITCH_OFF = 0x50;
    static final byte UP = 0x70;
    static final byte DOWN = 0x50;
    static final byte OPEN = (byte) 0xE0;
    static final byte CLOSED = (byte) 0xF0;

    public PTM200Message() {
    }

    public PTM200Message(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(Thing thing, ChannelUID channelUID, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config,
            @Nullable STMStateMachine STM) {
        switch (channelId) {
            case CHANNEL_GENERAL_SWITCHING:
                return OnOffType.from(bytes[0] == SWITCH_ON);
            case CHANNEL_ROLLERSHUTTER:
                switch (bytes[0]) {
                    case UP:
                        if (STM != null) {
                            switch (STM.getState()) {
                                case INVALID:
                                    // command is coming from elsewhere (e.g. local switch) and not from stm
                                    // can be used for pushing stm into calibrated state
                                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    STM.apply(STMAction.CALIBRATION_REQUEST_UP);
                                case MOVEMENT_POSITION_UP:
                                    // StopMoveType.MOVE is used as command for adjustment of slats
                                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                default:
                            }
                            STM.apply(STMAction.CALIBRATION_DONE);
                            STM.apply(STMAction.POSITION_DONE);
                        }
                        return PercentType.ZERO;
                    case DOWN:
                        if (STM != null) {
                            switch (STM.getState()) {
                                case INVALID:
                                    // command is coming from elsewhere (e.g. local switch) and not from stm
                                    // can be used for pushing stm into calibrated state
                                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    STM.apply(STMAction.CALIBRATION_REQUEST_DOWN);
                                case MOVEMENT_POSITION_DOWN:
                                    // StopMoveType.MOVE is used as command for adjustment of slats
                                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                default:
                            }
                            STM.apply(STMAction.CALIBRATION_DONE);
                            STM.apply(STMAction.POSITION_DONE);
                        }
                        return PercentType.HUNDRED;
                    default:
                        return UnDefType.UNDEF;
                }

            case CHANNEL_CONTACT:
                EnOceanChannelContactConfig c = config.as(EnOceanChannelContactConfig.class);
                if (c.inverted) {
                    return bytes[0] == OPEN ? OpenClosedType.CLOSED
                            : (bytes[0] == CLOSED ? OpenClosedType.OPEN : UnDefType.UNDEF);
                } else {
                    return bytes[0] == OPEN ? OpenClosedType.OPEN
                            : (bytes[0] == CLOSED ? OpenClosedType.CLOSED : UnDefType.UNDEF);
                }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public boolean isValidForTeachIn() {
        return false;
    }
}
