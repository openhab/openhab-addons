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

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The {@link SoulissT16Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT16Handler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;
    private Logger logger = LoggerFactory.getLogger(SoulissT16Handler.class);
    byte T1nRawState_byte0;
    byte T1nRawStateRED_byte1;
    byte T1nRawStateGREEN_byte2;
    byte T1nRawStateBLU_byte3;

    // HSBType hsbState = HSBType.WHITE;
    byte xSleepTime = 0;

    public SoulissT16Handler(Thing _thing) {
        super(_thing);
    }

    HSBType _hsbState;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    updateState(channelUID, getOHState_OnOff_FromSoulissVal(T1nRawState_byte0));
                    break;
                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    updateState(channelUID, gethsb(T1nRawStateRED_byte1, T1nRawStateGREEN_byte2, T1nRawStateBLU_byte3));
                    break;
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(channelUID,
                            PercentType
                                    .valueOf(gethsb(T1nRawStateRED_byte1, T1nRawStateGREEN_byte2, T1nRawStateBLU_byte3)
                                            .getBrightness().toString()));
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OnCmd);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OffCmd);
                        }
                    }
                    break;
                case SoulissBindingConstants.WHITE_MODE_CHANNEL:
                    if (command instanceof OnOffType) {
                        _hsbState = HSBType.fromRGB(255, 255, 255);
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set, (byte) 255, (byte) 255,
                                (byte) 255);
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, _hsbState);

                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((byte) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
                        // set Off
                        updateState(channelUID, OnOffType.OFF);
                    }
                    break;

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL,
                                gethsb(T1nRawStateRED_byte1, T1nRawStateGREEN_byte2, T1nRawStateBLU_byte3));
                        // updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        /// PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set,
                                (byte) (_hsbState.getRed().shortValue() * (255.00 / 100)),
                                (byte) (_hsbState.getGreen().shortValue() * (255.00 / 100)),
                                (byte) (_hsbState.getBlue().shortValue() * (255.00 / 100)));

                    } else if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OnCmd);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OffCmd);
                        }
                    }
                    break;

                case SoulissBindingConstants.ROLLER_BRIGHTNESS_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_BrightUp);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_BrightDown);
                        }
                    }
                    break;

                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    if (command instanceof HSBType) {
                        HSBType _hsbState = (HSBType) command;

                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                                PercentType.valueOf(_hsbState.getBrightness().toString()));
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set,
                                (byte) (_hsbState.getRed().shortValue() * 255.00 / 100),
                                (byte) (_hsbState.getGreen().shortValue() * 255.00 / 100),
                                (byte) (_hsbState.getBlue().shortValue() * 255.00 / 100));
                    }
                    break;

            }
        }

    }

    @Override
    public void initialize() {

        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thing.getConfiguration();

        if (gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL) != null) {
            xSleepTime = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).byteValue();
        }
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
        if (_state != null) {
            logger.debug("T16, setting state to {}", _state.toFullString());
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }

    public void setRawState_command(byte _rawState_byte0) {
        super.setLastStatusStored();
        if (_rawState_byte0 != T1nRawState_byte0) {
            this.setState(getOHState_OnOff_FromSoulissVal(_rawState_byte0));
        }
    }

    public void setRawState_RGB(byte _rawStateRED_byte1, byte _rawStateGREEN_byte2, byte _rawStateBLU_byte3) {
        super.setLastStatusStored();

        if (_rawStateRED_byte1 != T1nRawStateRED_byte1 || _rawStateGREEN_byte2 != T1nRawStateGREEN_byte2
                || _rawStateBLU_byte3 != T1nRawStateBLU_byte3) {
            HSBType _hsbState = gethsb(_rawStateRED_byte1, _rawStateGREEN_byte2, _rawStateBLU_byte3);
            logger.debug("T16, setting color to {},{},{}", _rawStateRED_byte1, _rawStateGREEN_byte2,
                    _rawStateBLU_byte3);

            updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                    PercentType.valueOf(_hsbState.getBrightness().toString()));

            updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, _hsbState);
        }

        T1nRawStateRED_byte1 = _rawStateRED_byte1;
        T1nRawStateGREEN_byte2 = _rawStateGREEN_byte2;
        T1nRawStateBLU_byte3 = _rawStateBLU_byte3;
    }

    @Override
    public byte getRawState() {
        throw new NotImplementedException();
    }

    public byte getRawState_command() {
        return T1nRawState_byte0;
    }

    public byte[] getRawState_values() {
        return new byte[] { T1nRawStateRED_byte1, T1nRawStateGREEN_byte2, T1nRawStateBLU_byte3 };
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissBindingProtocolConstants.Souliss_T1n_OnCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OnCoil;
            } else if (bCmd == SoulissBindingProtocolConstants.Souliss_T1n_OffCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OffCoil;
            } else if (bCmd >= SoulissBindingProtocolConstants.Souliss_T1n_Timed) {
                // SLEEP
                return SoulissBindingProtocolConstants.Souliss_T1n_OnCoil;
            }
        }
        return -1;
    }

    HSBType gethsb(byte _rawStateRED_byte1, byte _rawStateGREEN_byte2, byte _rawStateBLU_byte3) {
        return HSBType.fromRGB(_rawStateRED_byte1, _rawStateGREEN_byte2, _rawStateBLU_byte3);
    }
}
