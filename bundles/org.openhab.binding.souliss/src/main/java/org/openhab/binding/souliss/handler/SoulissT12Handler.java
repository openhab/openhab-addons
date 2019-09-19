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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT12Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT12Handler extends SoulissGenericTypical implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT12Handler.class);
    OnOffType T1nState = OnOffType.OFF;
    OnOffType T1nAutomodeState = OnOffType.OFF;
    short xSleepTime = 0;

    public SoulissT12Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    updateState(channelUID, T1nState);
                    break;
                case SoulissBindingConstants.AUTOMODE_CHANNEL:
                    updateState(channelUID, T1nAutomodeState);
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
                case SoulissBindingConstants.AUTOMODE_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_AutoCmd);
                        }
                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND((short) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
                            // set Off
                            updateState(channelUID, OnOffType.OFF);
                        }
                    }
                    break;

            }
        }
    }

    @Override
    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            if (((OnOffType) _state) != this.T1nState) {
                this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
                // this.updateThing(this.thing);
                this.T1nState = (OnOffType) _state;
            }
        }
    }

    public void setState_Automode(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            if (((OnOffType) _state) != this.T1nAutomodeState) {
                this.updateState(SoulissBindingConstants.AUTOMODE_CHANNEL, (OnOffType) _state);
                // this.updateThing(this.thing);
                this.T1nAutomodeState = (OnOffType) _state;
            }
        }
    }

}
