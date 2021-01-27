/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.core.config.core.Configuration;
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
public class SoulissT22Handler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;
    // private Logger logger = LoggerFactory.getLogger(SoulissT22Handler.class);
    byte T2nRawState;

    public SoulissT22Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        gwConfigurationMap = thing.getConfiguration();
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    // updateState(channelUID, T2nState);
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T2N_OPEN_CMD);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T2N_CLOSE_CMD);
                        }
                    } else if (command instanceof StopMoveType) {
                        if (command.equals(StopMoveType.STOP)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T2N_STOP_CMD);
                        }
                    }
                    break;
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T2N_OPEN_CMD_LOCAL);
                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T2N_CLOSE_CMD_LOCAL);
                        }
                    }
                    break;
            }
        }
    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            if (_state instanceof PercentType) {
                this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, (PercentType) _state);
            }
        }
    }

    public void setState_Message(String rollershutterMessage) {
        this.updateState(SoulissBindingConstants.ROLLERSHUTTER_STATE_CHANNEL_CHANNEL,
                StringType.valueOf(rollershutterMessage));
    }

    @Override
    public void setRawState(byte _rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T2nRawState != _rawState) {
            this.setState(getOHState_T22_FromSoulissVal(_rawState));

            if (_rawState == SoulissBindingProtocolConstants.SOULISS_T2N_OPEN_CMD) {
                this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
            } else if (_rawState == SoulissBindingProtocolConstants.SOULISS_T2N_CLOSE_CMD) {
                this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL);
            }
            switch (_rawState) {
                case SoulissBindingProtocolConstants.SOULISS_T2N_COIL_STOP:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STOP_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_COIL_OFF:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_LIMSWITCH_CLOSE:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_LIMSWITCH_OPEN:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_NOLIMSWITCH:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_TIMER_OFF:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_TIMER_OFF);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_STATE_OPEN:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.SOULISS_T2N_STATE_CLOSE:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL);
                    break;
            }
            T2nRawState = _rawState;
        }
    }

    private PercentType getOHState_T22_FromSoulissVal(short sVal) {
        int iState = 0;
        switch (sVal) {
            case SoulissBindingProtocolConstants.SOULISS_T2N_COIL_OPEN:
                iState = 0;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_COIL_CLOSE:
                iState = 100;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_COIL_STOP:
                iState = 50;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_LIMSWITCH_CLOSE:
                iState = 100;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_LIMSWITCH_OPEN:
                iState = 0;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_NOLIMSWITCH:
                iState = 50;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_TIMER_OFF:
                iState = 50;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_STATE_OPEN:
                iState = 0;
                break;
            case SoulissBindingProtocolConstants.SOULISS_T2N_STATE_CLOSE:
                iState = 100;
                break;
        }
        return PercentType.valueOf(String.valueOf(iState));
    }

    @Override
    public byte getRawState() {
        // TODO Auto-generated method stub
        return T2nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissBindingProtocolConstants.SOULISS_T2N_OPEN_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T2N_COIL_OPEN;
            } else if (bCmd == SoulissBindingProtocolConstants.SOULISS_T2N_CLOSE_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T2N_COIL_CLOSE;
            } else if (bCmd >= SoulissBindingProtocolConstants.SOULISS_T2N_STOP_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T2N_COIL_STOP;
            }
        }
        return -1;
    }
}
