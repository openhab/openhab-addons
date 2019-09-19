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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;

/**
 * The {@link SoulissT12Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT12Handler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;

    byte T1nRawState;
    byte xSleepTime = 0;

    public SoulissT12Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thing.getConfiguration();
        if (gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL) != null) {
            xSleepTime = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).byteValue();
        }
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    switch (T1nRawState) {
                        case SoulissBindingProtocolConstants.Souliss_T1n_OnCoil_Auto:
                        case SoulissBindingProtocolConstants.Souliss_T1n_OnCoil:
                            this.setState(OnOffType.ON);
                            break;
                        case SoulissBindingProtocolConstants.Souliss_T1n_OffCoil_Auto:
                        case SoulissBindingProtocolConstants.Souliss_T1n_OffCoil:
                            this.setState(OnOffType.OFF);
                            break;
                    }
                    break;
                case SoulissBindingConstants.AUTOMODE_CHANNEL:
                    switch (T1nRawState) {
                        case SoulissBindingProtocolConstants.Souliss_T1n_OnCoil_Auto:
                        case SoulissBindingProtocolConstants.Souliss_T1n_OffCoil_Auto:
                            this.setState_Automode(OnOffType.ON);
                            break;
                        case SoulissBindingProtocolConstants.Souliss_T1n_OnCoil:
                        case SoulissBindingProtocolConstants.Souliss_T1n_OffCoil:
                            this.setState_Automode(OnOffType.OFF);
                            break;
                    }

                    break;
            }
        } else

        {
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
                            commandSEND((byte) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
                            // set Off
                            updateState(channelUID, OnOffType.OFF);
                        }
                    }
                    break;

            }
        }
    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        }
    }

    public void setState_Automode(PrimitiveType _state) {
        if (_state != null) {
            this.updateState(SoulissBindingConstants.AUTOMODE_CHANNEL, (OnOffType) _state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {

        // update Last Status stored time
        super.setLastStatusStored();

        // update item state only if it is different from previous
        if (T1nRawState != _rawState) {
            if (_rawState == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil_Auto) {
                this.setState(OnOffType.ON);
                this.setState_Automode(OnOffType.ON);
            } else if (_rawState == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil_Auto) {
                this.setState(OnOffType.OFF);
                this.setState_Automode(OnOffType.ON);
            } else if (_rawState == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
                this.setState(OnOffType.ON);
                this.setState_Automode(OnOffType.OFF);
            } else if (_rawState == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
                this.setState(OnOffType.OFF);
                this.setState_Automode(OnOffType.OFF);
            }
        }
        T1nRawState = _rawState;
    }

    @Override
    public byte getRawState() {
        return T1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        if (bSecureSend) {
            if (bCommand == SoulissBindingProtocolConstants.Souliss_T1n_OnCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OnCoil;
            } else if (bCommand == SoulissBindingProtocolConstants.Souliss_T1n_OffCmd) {
                return SoulissBindingProtocolConstants.Souliss_T1n_OffCoil;
            } else if (bCommand >= SoulissBindingProtocolConstants.Souliss_T1n_Timed) {
                // SLEEP
                return SoulissBindingProtocolConstants.Souliss_T1n_OnCoil;
            }
        }
        return -1;
    }

}
