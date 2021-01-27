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

import java.math.BigDecimal;

import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT19Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
public class SoulissT19Handler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;
    private final Logger logger = LoggerFactory.getLogger(SoulissT19Handler.class);
    byte T1nRawState_byte0;
    byte T1nRawStateBrigthness_byte1;

    byte xSleepTime = 0;

    public SoulissT19Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    updateState(channelUID, getOHState_OnOff_FromSoulissVal(T1nRawState_byte0));
                    break;
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                            PercentType.valueOf(String.valueOf((T1nRawStateBrigthness_byte1 / 255) * 100)));
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_ON_CMD);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_OFF_CMD);
                        }
                    }
                    break;

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL, (PercentType) command);
                        // updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        /// PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_SET,
                                (byte) (((PercentType) command).shortValue() * 255.00 / 100.00));
                        // Short.parseShort(String.valueOf(Math.round((dimmerValue / 255.00) * 100)))
                    } else if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_ON_CMD);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_OFF_CMD);
                        }
                    }
                    break;

                case SoulissBindingConstants.ROLLER_BRIGHTNESS_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_BRIGHT_UP);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_BRIGHT_DOWN);
                        }
                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((byte) (SoulissBindingProtocolConstants.SOULISS_T1N_TIMED + xSleepTime));
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

    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
            logger.debug("T19, setting state to {}", _state.toFullString());
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        }
    }

    public void setRawStateDimmerValue(byte _dimmerValue) {
        try {
            if (_dimmerValue != T1nRawState_byte0) {
                logger.debug("T19, setting dimmer to {}", _dimmerValue);
                updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        PercentType.valueOf(String.valueOf(Math.round((T1nRawState_byte0 / 255) * 100))));
            }
        } catch (IllegalStateException ex) {
            logger.debug("UUID: {}", this.getThing().getUID().getAsString());
            logger.debug(" - Update state error (in setDimmerValue): {}", ex.getMessage());
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T1nRawState_byte0 != _rawState) {
            this.setState(getOHState_OnOff_FromSoulissVal(_rawState));
        }
        T1nRawState_byte0 = _rawState;
    }

    @Override
    public byte getRawState() {
        return T1nRawState_byte0;
    }

    public byte getRawStateDimmerValue() {
        return T1nRawStateBrigthness_byte1;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissBindingProtocolConstants.SOULISS_T1N_ON_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL;
            } else if (bCmd == SoulissBindingProtocolConstants.SOULISS_T1N_OFF_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL;
            } else if (bCmd >= SoulissBindingProtocolConstants.SOULISS_T1N_TIMED) {
                // SLEEP
                return SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL;
            }
        }
        return -1;
    }
}
