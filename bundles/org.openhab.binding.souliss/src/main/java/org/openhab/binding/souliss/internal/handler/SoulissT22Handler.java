/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT22Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissT22Handler extends SoulissGenericHandler {
    byte t2nRawState = 0xF;

    public SoulissT22Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);

        var configurationMap = getThing().getConfiguration();
        if (configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    break;
                default:
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    if (command.equals(UpDownType.UP)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T2N_OPEN_CMD);
                    } else if (command.equals(UpDownType.DOWN)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T2N_CLOSE_CMD);
                    } else if (command.equals(StopMoveType.STOP)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T2N_STOP_CMD);
                    }
                    break;
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T2N_OPEN_CMD_LOCAL);
                    } else if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T2N_CLOSE_CMD_LOCAL);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void setState(PrimitiveType state) {
        if (state instanceof PercentType percentCommand) {
            this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, percentCommand);

        }
    }

    public void setStateMessage(String rollershutterMessage) {
        this.updateState(SoulissBindingConstants.ROLLERSHUTTER_STATE_CHANNEL_CHANNEL,
                StringType.valueOf(rollershutterMessage));
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (t2nRawState != rawState) {
            var val = getOhStateT22FromSoulissVal(rawState);
            this.setState(val);

            if (rawState == SoulissProtocolConstants.SOULISS_T2N_OPEN_CMD) {
                this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
            } else if (rawState == SoulissProtocolConstants.SOULISS_T2N_CLOSE_CMD) {
                this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL);
            }
            switch (rawState) {
                case SoulissProtocolConstants.SOULISS_T2N_COIL_STOP:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STOP_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_COIL_OFF:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_LIMSWITCH_CLOSE:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_LIMSWITCH_OPEN:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_NOLIMSWITCH:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_TIMER_OFF:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_TIMER_OFF);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_STATE_OPEN:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL);
                    break;
                case SoulissProtocolConstants.SOULISS_T2N_STATE_CLOSE:
                    this.setStateMessage(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL);
                    break;
                default:
                    break;
            }
            t2nRawState = rawState;
        }
    }

    private PercentType getOhStateT22FromSoulissVal(short sVal) {
        var iState = 0;
        switch (sVal) {
            case SoulissProtocolConstants.SOULISS_T2N_COIL_OPEN:
                iState = 0;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_COIL_CLOSE:
                iState = 100;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_COIL_STOP:
                iState = 50;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_LIMSWITCH_CLOSE:
                iState = 100;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_LIMSWITCH_OPEN:
                iState = 0;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_NOLIMSWITCH:
                iState = 50;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_TIMER_OFF:
                iState = 50;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_STATE_OPEN:
                iState = 0;
                break;
            case SoulissProtocolConstants.SOULISS_T2N_STATE_CLOSE:
                iState = 100;
                break;
            default:
                break;
        }
        return PercentType.valueOf(String.valueOf(iState));
    }

    @Override
    public byte getRawState() {
        return t2nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissProtocolConstants.SOULISS_T2N_OPEN_CMD) {
                return SoulissProtocolConstants.SOULISS_T2N_COIL_OPEN;
            } else if (bCmd == SoulissProtocolConstants.SOULISS_T2N_CLOSE_CMD) {
                return SoulissProtocolConstants.SOULISS_T2N_COIL_CLOSE;
            } else if (bCmd >= SoulissProtocolConstants.SOULISS_T2N_STOP_CMD) {
                return SoulissProtocolConstants.SOULISS_T2N_COIL_STOP;
            }
        }
        return -1;
    }
}
