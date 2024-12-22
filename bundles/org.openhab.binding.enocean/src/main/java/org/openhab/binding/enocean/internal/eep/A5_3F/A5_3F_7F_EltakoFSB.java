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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelRollershutterConfig;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.handler.EnOceanBaseActuatorHandler;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.statemachine.STMAction;
import org.openhab.binding.enocean.internal.statemachine.STMState;
import org.openhab.binding.enocean.internal.statemachine.STMStateMachine;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 * @author Sven Schad - added state machine for blinds/rollershutter
 *
 */

@NonNullByDefault
public class A5_3F_7F_EltakoFSB extends _4BSMessage {

    static final byte STOP = 0x00;
    static final byte MOVE_UP = 0x01;
    static final byte MOVE_DOWN = 0x02;

    static final byte UP = 0x70;
    static final byte DOWN = 0x50;

    static final byte CMD_100MSEC = 0x0A;

    public A5_3F_7F_EltakoFSB() {
    }

    public A5_3F_7F_EltakoFSB(ERP1Message packet) {
        super(packet);
    }

    private void convertDimmerImpl(int newPos, int actPos, Configuration config, STMStateMachine STM) {

        int swapTime = config.as(EnOceanChannelRollershutterConfig.class).swapTime;

        byte direction = actPos > newPos ? MOVE_UP : MOVE_DOWN;
        int duration = Math.abs(newPos - actPos) * swapTime / 100;
        byte duration_lsb = (byte) (duration & 0xFF); // lsb
        byte duration_msb = (byte) ((duration & 0xFF00) >> 8);

        if (duration != 0) {
            setData(duration_msb, duration_lsb, direction, CMD_100MSEC);
            STM.apply(STMAction.SLATS_POS_REQUEST);
            return;
        } else {
            STM.apply(STMAction.SLATS_POS_DONE); // safe way to return to IDLE
        }
    }

    private void convertDimmer(PercentType decimalCommand, Configuration config,
            Function<String, State> getCurrentStateFunc, STMStateMachine STM) {

        State channelDimmer = getCurrentStateFunc.apply(CHANNEL_DIMMER);
        PercentType currentDimmer = channelDimmer.as(PercentType.class);
        if (currentDimmer != null) {
            int newPos = decimalCommand.intValue();
            int actPos = currentDimmer.intValue();
            convertDimmerImpl(newPos, actPos, config, STM);
        } else {
            STM.apply(STMAction.SLATS_POS_DONE);//
        }
    }

    private void convertPosition(DecimalType percentCommand, Configuration config,
            Function<String, State> getCurrentStateFunc, STMStateMachine STM) {

        State channelRollershutter = getCurrentStateFunc.apply(CHANNEL_ROLLERSHUTTER);
        State channelDimmer = getCurrentStateFunc.apply(CHANNEL_DIMMER);
        PercentType currentRollershutter = channelRollershutter.as(PercentType.class);
        PercentType currentDimmer = channelDimmer.as(PercentType.class);

        int shutTime = config.as(EnOceanChannelRollershutterConfig.class).shutTime;
        int swapTime = config.as(EnOceanChannelRollershutterConfig.class).swapTime;

        if (currentRollershutter != null) {
            int actPos_rs = currentRollershutter.intValue();
            int newPos_rs = percentCommand.intValue();
            boolean direction = actPos_rs > newPos_rs;
            byte direction_b = direction ? MOVE_UP : MOVE_DOWN;
            int duration = Math.abs(newPos_rs - actPos_rs) * shutTime * 10 / 100;

            if (currentDimmer != null) {
                int actPos_dimmer = currentDimmer.intValue();
                duration = direction ? duration + actPos_dimmer / 100 * swapTime
                        : duration + (100 - actPos_dimmer) / 100 * swapTime; // Correction for blinds
            }

            byte duration_lsb = (byte) (duration & 0xFF); // lsb
            byte duration_msb = (byte) ((duration & 0xFF00) >> 8);

            if (duration != 0) {
                setData(duration_msb, duration_lsb, direction_b, (CMD_100MSEC));

                if (direction) {
                    STM.apply(STMAction.POSITION_REQUEST_UP);
                } else {
                    STM.apply(STMAction.POSITION_REQUEST_DOWN);
                }
            }

        }
    }

    void processCmdAsPercentType(Thing thing, PercentType percentCommand, Configuration config,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        int shutTime = config.as(EnOceanChannelRollershutterConfig.class).shutTime;

        if (STM != null) {
            switch (STM.getState()) {
                case INVALID:
                    // Can't process command immediately -> store it.
                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, percentCommand);
                    setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => move completely up
                    STM.apply(STMAction.CALIBRATION_REQUEST_UP);
                    break;
                case IDLE:
                    if (percentCommand.intValue() == PercentType.ZERO.intValue()) {
                        setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => move completely up
                        STM.apply(STMAction.POSITION_REQUEST_UP);
                    } else if (percentCommand.intValue() == PercentType.HUNDRED.intValue()) {
                        setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => move completely down
                        STM.apply(STMAction.POSITION_REQUEST_DOWN);
                    } else {
                        convertPosition(percentCommand, config, getCurrentStateFunc, STM);
                    }
                    break;
                default:
            }
        } else { // Legacy Code
            State channelState = getCurrentStateFunc.apply(CHANNEL_ROLLERSHUTTER);
            if (percentCommand.intValue() == PercentType.ZERO.intValue()) {
                setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => move completely up
            } else if (percentCommand.intValue() == PercentType.HUNDRED.intValue()) {
                setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => move completely down
            } else {
                PercentType current = channelState.as(PercentType.class);
                if (current != null) {
                    if (current.intValue() != percentCommand.intValue()) {
                        byte direction = current.intValue() > percentCommand.intValue() ? MOVE_UP : MOVE_DOWN;
                        byte duration = (byte) Math.min(255,
                                (Math.abs(current.intValue() - percentCommand.intValue()) * shutTime)
                                        / PercentType.HUNDRED.intValue());

                        if (duration == 0) {
                            setData(ZERO, (byte) 0xFF, STOP, TEACHIN_BIT);
                        } else {
                            setData(ZERO, duration, direction, TEACHIN_BIT);
                        }
                    }
                }
            }

        }

    }

    void processCmdAsUpDownType(Thing thing, UpDownType upDownCommand, Configuration config,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        int shutTime = config.as(EnOceanChannelRollershutterConfig.class).shutTime;

        if (STM != null) {
            if ((STM.getState() == STMState.IDLE) || (STM.getState() == STMState.INVALID)) {
                if (upDownCommand == UpDownType.UP) {
                    setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => 0 percent
                    if (STM.getState() == STMState.IDLE) {
                        STM.apply(STMAction.POSITION_REQUEST_UP);
                    } else {
                        STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                        STM.apply(STMAction.CALIBRATION_REQUEST_UP);
                    }

                } else if (upDownCommand == UpDownType.DOWN) {
                    setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => 100 percent
                    if (STM.getState() == STMState.IDLE) {
                        STM.apply(STMAction.POSITION_REQUEST_DOWN);
                    } else {
                        STM.apply(STMAction.CALIBRATION_REQUEST_DOWN);
                    }

                }
            }
        } else { // Legacy code
            if (upDownCommand == UpDownType.UP) {
                setData(ZERO, (byte) shutTime, MOVE_UP, TEACHIN_BIT); // => 0 percent
            } else if (upDownCommand == UpDownType.DOWN) {
                setData(ZERO, (byte) shutTime, MOVE_DOWN, TEACHIN_BIT); // => 100 percent
            }
        }
    }

    void processCmdAsStopMoveType(Thing thing, StopMoveType stopMoveCommand, Configuration config,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        if (STM != null) {
            if (stopMoveCommand == StopMoveType.STOP) {
                setData(ZERO, (byte) 0xFF, STOP, TEACHIN_BIT);
                STM.apply(STMAction.INVALID_REQUEST);
            } else if (stopMoveCommand == StopMoveType.MOVE) {
                // if (strCmd.toString() == "CALLBACK") {
                if (STM.getState() == STMState.POSITION_REACHED) {
                    // Position has been reached, slat now needs to be adjusted
                    // Since the actual position is not persisted elsewhere, the slat position is determined based on
                    // the direction of travel
                    State channelDimmer = getCurrentStateFunc.apply(CHANNEL_DIMMER);
                    PercentType currentDimmer = channelDimmer.as(PercentType.class);
                    if (currentDimmer != null) {
                        if (STM.getPrevState() == STMState.MOVEMENT_POSITION_UP) {
                            convertDimmerImpl(currentDimmer.intValue(), PercentType.ZERO.intValue(), config, STM);
                        } else {
                            convertDimmerImpl(currentDimmer.intValue(), PercentType.HUNDRED.intValue(), config, STM);
                        }
                    }
                }
            }
        } else { // Legacy code
            if (stopMoveCommand == StopMoveType.STOP) {
                setData(ZERO, (byte) 0xFF, STOP, TEACHIN_BIT);
            }

        }
    }

    void processChannelRollershutter(Thing thing, Command command, Configuration config,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        if (command instanceof PercentType percentCommand) {
            processCmdAsPercentType(thing, percentCommand, config, getCurrentStateFunc, STM);
        } else if (command instanceof UpDownType upDownCommand) {
            processCmdAsUpDownType(thing, upDownCommand, config, getCurrentStateFunc, STM);
        } else if (command instanceof StopMoveType stopMoveCommand) {
            processCmdAsStopMoveType(thing, stopMoveCommand, config, getCurrentStateFunc, STM);
        }

    }

    void processChannelDimmer(Thing thing, Command command, Configuration config,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        Channel channel = null;
        if (command instanceof PercentType decimalCommand) {
            if (STM != null) {
                switch (STM.getState()) {
                    case IDLE:
                        convertDimmer(decimalCommand, config, getCurrentStateFunc, STM);
                        if (thing.getHandler() instanceof EnOceanBaseActuatorHandler myHandler) {
                            channel = thing.getChannel(CHANNEL_DIMMER);
                            if (channel != null) {
                                ChannelUID channelUID = channel.getUID();
                                myHandler.setState(channelUID, decimalCommand);
                            }
                        }
                        break;
                    case MOVEMENT_POSITION_UP, MOVEMENT_POSITION_DOWN, MOVEMENT_CALIBRATION_UP,
                            MOVEMENT_CALIBRATION_DOWN, POSITION_REACHED, INVALID:
                        // we avoid update during MOVEMENT_SLATS, all other cases are allowed.
                        if (thing.getHandler() instanceof EnOceanBaseActuatorHandler myHandler) {
                            channel = thing.getChannel(CHANNEL_DIMMER);
                            if (channel != null) {
                                ChannelUID channelUID = channel.getUID();
                                myHandler.setState(channelUID, decimalCommand);
                            }
                        }
                        break;
                    default:
                }
            }
        }
    }

    @Override
    protected void convertFromCommandImpl(Thing thing, ChannelUID channelUID, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable STMStateMachine STM) {

        // always take the config from channel rollershutter, concept: global config for all channels
        Channel channel = thing.getChannel(CHANNEL_ROLLERSHUTTER);
        if (channel != null) {
            Configuration config = channel.getConfiguration();
            String channelId = channelUID.getId();
            switch (channelId) {
                case CHANNEL_ROLLERSHUTTER:
                    processChannelRollershutter(thing, command, config, getCurrentStateFunc, STM);
                    break;
                case CHANNEL_DIMMER:
                    processChannelDimmer(thing, command, config, getCurrentStateFunc, STM);
                    break;
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config,
            @Nullable STMStateMachine STM) {

        if (CHANNEL_ROLLERSHUTTER.equals(channelId)) { //
            State currentState = getCurrentStateFunc.apply(channelId);
            if (currentState != null) {
                int duration = ((getDB3Value() << 8) + getDB2Value()) / 10; // => Time in DB3 and DB2 is given
                                                                            // in ms
                EnOceanChannelRollershutterConfig c = config.as(EnOceanChannelRollershutterConfig.class);
                boolean move_up = getDB1() == MOVE_UP;
                if (duration == c.shutTime) {
                    if (STM != null) {
                        switch (STM.getState()) {
                            case MOVEMENT_POSITION_UP, MOVEMENT_POSITION_DOWN:
                                // StopMoveType.MOVE is used as command for adjustment of slats
                                STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                            default:
                        }
                        STM.apply(STMAction.POSITION_DONE);// will launch new command for adjustment of slats
                        STM.apply(STMAction.CALIBRATION_DONE); // will launch new command for setting the position
                    }
                    return move_up ? PercentType.ZERO : PercentType.HUNDRED;
                } else {
                    PercentType current = PercentType.ZERO;
                    if (currentState instanceof PercentType) {
                        current = currentState.as(PercentType.class);
                    }

                    int direction = getDB1() == MOVE_UP ? -1 : 1;
                    if (current != null && c.shutTime != -1 && c.shutTime != 0) {
                        if (STM != null) {
                            switch (STM.getState()) {
                                case MOVEMENT_POSITION_UP, MOVEMENT_POSITION_DOWN:
                                    STM.storeCommand(CHANNEL_ROLLERSHUTTER, StopMoveType.MOVE);
                                    STM.apply(STMAction.POSITION_DONE); // will execute MOVE command, when we use blinds
                                    return new PercentType(Math.min(100, (Math.max(0, current.intValue() + direction
                                            * ((duration * PercentType.HUNDRED.intValue()) / c.shutTime)))));
                                case MOVEMENT_SLATS:
                                    STM.apply(STMAction.SLATS_POS_DONE);
                                    return new PercentType(current.intValue()); // do not change the position
                                default:
                            }
                        } else { // Legacy
                            return new PercentType(Math.min(100, (Math.max(0, current.intValue()
                                    + direction * ((duration * PercentType.HUNDRED.intValue()) / c.shutTime)))));
                        }
                    }
                }
            }
            return UnDefType.UNDEF;
        }
        return UnDefType.UNDEF;
    }
}
