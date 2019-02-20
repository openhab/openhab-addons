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
 * The {@link SoulissT19Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT19Handler extends SoulissGenericTypical implements typicalCommonMethods {
    Configuration gwConfigurationMap;
    private Logger logger = LoggerFactory.getLogger(SoulissT19Handler.class);
    OnOffType T1nState = OnOffType.OFF;
    int dimmerValue = 255;
    short xSleepTime = 0;

    public SoulissT19Handler(Thing _thing) {
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
                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                            PercentType.valueOf(String.valueOf((Math.round((dimmerValue / 255) * 100)))));
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

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        dimmerValue = ((PercentType) command).intValue();
                        updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL, (PercentType) command);
                        // updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        /// PercentType.valueOf(hsbState.getBrightness().toString()));
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_Set,
                                (short) (((PercentType) command).shortValue() * 255.00 / 100.00));
                        // Short.parseShort(String.valueOf(Math.round((dimmerValue / 255.00) * 100)))
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
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        commandSEND((short) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
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
        if (_state != null) {
            updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
            if (((OnOffType) _state) != this.T1nState) {
                logger.debug("T19, setting state to {}", _state.toFullString());
                this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
                // this.updateThing(this.thing);
                this.T1nState = (OnOffType) _state;
            }
        }
    }

    public void setDimmerValue(float _dimmerValue) {
        try {
            if (_dimmerValue != dimmerValue) {
                logger.debug("T19, setting dimmer to {}", _dimmerValue);
                updateState(SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL,
                        PercentType.valueOf(String.valueOf(Math.round((_dimmerValue / 255) * 100))));

            }
        } catch (IllegalStateException ex) {
            logger.debug("UUID: " + this.getThing().getUID().getAsString()
                    + " - Update state error (in setDimmerValue): " + ex.getMessage());
        }
    }
}
