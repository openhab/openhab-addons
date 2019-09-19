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
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT16Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT16Handler extends SoulissGenericTypical implements typicalCommonMethods {
    Configuration gwConfigurationMap;
    private Logger logger = LoggerFactory.getLogger(SoulissT16Handler.class);
    OnOffType T1nState = OnOffType.OFF;
    HSBType hsbState = HSBType.WHITE;
    short xSleepTime = 0;

    public SoulissT16Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    updateState(channelUID, T1nState);
                    break;
                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    updateState(channelUID, hsbState);
                    break;
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(channelUID, PercentType.valueOf(hsbState.getBrightness().toString()));
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
                        hsbState = HSBType.fromRGB(255, 255, 255);
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set, (short) 255, (short) 255,
                                (short) 255);
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, hsbState);

                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((short) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
                        // set Off
                        updateState(channelUID, OnOffType.OFF);
                    }
                    break;

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        hsbState = new HSBType(hsbState.getHue(), hsbState.getSaturation(), (PercentType) command);
                        updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, hsbState);
                        // updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        /// PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set,
                                (short) (hsbState.getRed().shortValue() * (255.00 / 100)),
                                (short) (hsbState.getGreen().shortValue() * (255.00 / 100)),
                                (short) (hsbState.getBlue().shortValue() * (255.00 / 100)));

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
                        hsbState = (HSBType) command;

                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                                PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set,
                                (short) (hsbState.getRed().shortValue() * 255.00 / 100),
                                (short) (hsbState.getGreen().shortValue() * 255.00 / 100),
                                (short) (hsbState.getBlue().shortValue() * 255.00 / 100));
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
            xSleepTime = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).shortValue();
        }
    }

    @Override
    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
        if (_state != null) {
            if (((OnOffType) _state) != this.T1nState) {
                logger.debug("T16, setting state to {}", _state.toFullString());
                this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
                // this.updateThing(this.thing);
                this.T1nState = (OnOffType) _state;
            }
        }
    }

    public void setStateRGB(short _stateRED, short _stateGREEN, short _stateBLU) {
        HSBType _hsbState = HSBType.fromRGB(_stateRED, _stateGREEN, _stateBLU);
        logger.debug("T16, setting color to {},{},{}", _stateRED, _stateGREEN, _stateBLU);
        if (_hsbState != hsbState) {
            updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                    PercentType.valueOf(hsbState.getBrightness().toString()));

            updateState(SoulissBindingConstants.LED_COLOR_CHANNEL, hsbState);
        }

    }

}
