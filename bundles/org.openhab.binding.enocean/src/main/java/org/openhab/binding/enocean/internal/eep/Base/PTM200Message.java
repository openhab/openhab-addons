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
import org.openhab.binding.enocean.internal.statemachine.BlindAction;
import org.openhab.binding.enocean.internal.statemachine.BlindState;
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
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine<?, ?> stm) {
    }

    // TODO: Architectural improvement - Feedback processing should be in Handler
    // This EEP processes feedback and updates state machine that was initiated by A5_3F_7F_EltakoFSB.
    // This creates coupling between send and receive EEP classes. Ideally, the Handler should
    // coordinate state machine updates based on both sent commands and received feedback.
    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config,
            @Nullable STMStateMachine<?, ?> stm) {
        switch (channelId) {
            case CHANNEL_GENERAL_SWITCHING:
                return OnOffType.from(bytes[0] == SWITCH_ON);
            case CHANNEL_ROLLERSHUTTER:
                switch (bytes[0]) {
                    case UP:
                        if (stm != null) {
                            @SuppressWarnings("unchecked")
                            STMStateMachine<BlindAction, BlindState> blindStm = (STMStateMachine<BlindAction, BlindState>) stm;
                            switch (blindStm.getState()) {
                                case INVALID:
                                    // command is coming from elsewhere (e.g. local switch) and not from stm
                                    // can be used for pushing stm into calibrated state
                                    blindStm.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    blindStm.apply(BlindAction.CALIBRATION_REQUEST_UP);
                                    break;
                                case MOVEMENT_POSITION_UP:
                                    // StopMoveType.MOVE is used as command for adjustment of slats
                                    blindStm.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    break;
                                default:
                                    break;
                            }
                            blindStm.apply(BlindAction.CALIBRATION_DONE);
                            blindStm.apply(BlindAction.POSITION_DONE);
                        }
                        return PercentType.ZERO;
                    case DOWN:
                        if (stm != null) {
                            @SuppressWarnings("unchecked")
                            STMStateMachine<BlindAction, BlindState> blindStm = (STMStateMachine<BlindAction, BlindState>) stm;
                            switch (blindStm.getState()) {
                                case INVALID:
                                    // command is coming from elsewhere (e.g. local switch) and not from stm
                                    // can be used for pushing stm into calibrated state
                                    blindStm.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    blindStm.apply(BlindAction.CALIBRATION_REQUEST_DOWN);
                                    break;
                                case MOVEMENT_POSITION_DOWN:
                                    // StopMoveType.MOVE is used as command for adjustment of slats
                                    blindStm.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    break;
                                default:
                                    break;
                            }
                            blindStm.apply(BlindAction.CALIBRATION_DONE);
                            blindStm.apply(BlindAction.POSITION_DONE);
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
