/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

        } else if (command instanceof StringType) {
            switch ((command).toString()) {
                // FAN
                case "HIGH":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanHigh);
                    _fanStateValue = StringType.valueOf("HIGH");
                    break;
                case "MEDIUM":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanMed);
                    _fanStateValue = StringType.valueOf("MEDIUM");
                    break;
                case "LOW":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanLow);
                    _fanStateValue = StringType.valueOf("LOW");
                    break;
                case "AUTO":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanAuto);
                    _fanStateValue = StringType.valueOf("AUTO");
                    break;
                case "OFF":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanOff);
                    _fanStateValue = StringType.valueOf("FANOFF");
                    break;
                // MODE
                case "HEATING_MODE":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                    _lastModeState = StringType.valueOf("HEAT");
                    break;
                case "COOLING_MODE":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                    _lastModeState = StringType.valueOf("COOL");
                    break;
                case "POWEREDOFF_MODE":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);
                    _lastModeState = StringType.valueOf("POWER OFF");
                    break;
            }
        } else if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_AsMeasured);
            }

        } else if (command instanceof DecimalType) {

            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte B2 = (byte) (uu >> 8);
            byte B1 = (byte) uu;
            // setpoint command
            commandSEND(SoulissBindingProtocolConstants.Souliss_T31_Use_Of_Slot_SETPOINT_COMMAND, B1, B2);
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
                    case SoulissBindingConstants.T31_FANLOW_MESSAGE_CHANNEL:
                    case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_CHANNEL:
                    case SoulissBindingConstants.T31_FANHIGH_MESSAGE_CHANNEL:
                    case SoulissBindingConstants.T31_FANAUTO_MESSAGE_CHANNEL:
                    case SoulissBindingConstants.T31_FANOFF_MESSAGE_CHANNEL:
                        if (!_fanStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                            _fanStateValue = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_CHANNEL:
                    case SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_CHANNEL:
                        if (!_modeStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) _state);
                            _modeStateValue = (StringType) _state;
                        }
                        if (_powerState.equals(SoulissBindingConstants.T31_POWEREDOFF_MESSAGE_CHANNEL)) {
                            _powerState = (StringType) _state;
                            this.updateState(SoulissBindingConstants.T31_STATUS_CHANNEL, OnOffType.ON);

                        }

                        break;

                    case SoulissBindingConstants.T31_POWEREDOFF_MESSAGE_CHANNEL:
                        if (!_powerState.equals(SoulissBindingConstants.T31_POWEREDOFF_MESSAGE_CHANNEL)) {
                            _powerState = (StringType) _state;
                            this.updateState(SoulissBindingConstants.T31_STATUS_CHANNEL, OnOffType.OFF);
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
