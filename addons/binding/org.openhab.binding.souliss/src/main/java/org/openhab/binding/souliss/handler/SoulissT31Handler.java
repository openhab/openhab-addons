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

package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT31Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT31Handler extends SoulissGenericTypical implements typicalCommonMethods {

    Configuration gwConfigurationMap;
    DecimalType _setPointValue = DecimalType.ZERO;
    StringType _fanStateValue = StringType.EMPTY;
    StringType _powerState = StringType.EMPTY;
    StringType _fireState = StringType.EMPTY;

    StringType _lastModeState = StringType.EMPTY;
    StringType _modeStateValue = StringType.EMPTY;
    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);
    DecimalType _setMeasuredValue = DecimalType.ZERO;

    public SoulissT31Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
        } else {
            switch (channelUID.getId()) {
                // FAN
                case SoulissBindingConstants.T31_SYSTEM_CHANNEL:
                    if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);
                    } else {
                        if (_modeStateValue.equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                        } else {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                        }
                    }
                    break;
                case SoulissBindingConstants.T31_MODE_CHANNEL:
                    if (command.equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                    } else {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                    }
                    break;
                case SoulissBindingConstants.T31_BUTTON_CHANNEL:
                    if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_AsMeasured);
                    }
                    break;
                case SoulissBindingConstants.T31_FAN_CHANNEL:
                    switch (command.toString()) {
                        case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanHigh);
                            _fanStateValue = StringType
                                    .valueOf(SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanMed);
                            _fanStateValue = StringType
                                    .valueOf(SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanLow);
                            _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanAuto);
                            _fanStateValue = StringType
                                    .valueOf(SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanOff);
                            _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL);
                            break;
                    }
                    break;
                case SoulissBindingConstants.T31_SETPOINT_CHANNEL:
                    if (command instanceof DecimalType) {
                        int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
                        byte B2 = (byte) (uu >> 8);
                        byte B1 = (byte) uu;
                        // setpoint command
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T31_Use_Of_Slot_SETPOINT_COMMAND, B1, B2);
                    }
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void setState(PrimitiveType _state) {

        this.updateState(SoulissBindingConstants.T31_BUTTON_CHANNEL, OnOffType.OFF);

        super.setLastStatusStored();
        if (_state != null) {
            if (_state instanceof StringType) {
                switch (_state.toString()) {
                    case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                        if (!_fanStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                            _fanStateValue = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL:
                    case SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL:
                        if (!_modeStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) _state);
                            _modeStateValue = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL:
                        if (!_powerState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.OFF);
                            _powerState = (StringType) _state;
                        }
                        break;
                    case SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL:
                        if (!_powerState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.ON);
                            _powerState = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL:
                        if (!_fireState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.ON);
                            _powerState = (StringType) _state;
                        }
                        break;
                    case SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL:
                        if (!_fireState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.OFF);
                            _powerState = (StringType) _state;
                        }
                        break;
                }

            }

        }
    }

    public void setMeasuredValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!_setMeasuredValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_VALUE_CHANNEL, valueOf);
                _setMeasuredValue = valueOf;
            }
        }

    }

    public void setSetpointValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!_setPointValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_SETPOINT_CHANNEL, valueOf);
                _setPointValue = valueOf;
            }
        }

    }

}
