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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT12Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
public class SoulissT12Handler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;

    byte t1nRawState;
    byte xSleepTime = 0;

    public SoulissT12Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // TODO : Initialize the thingGeneric. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thingGeneric does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thingGeneric.getConfiguration();
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
                    switch (t1nRawState) {
                        case SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL_AUTO:
                        case SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL:
                            this.setState(OnOffType.ON);
                            break;
                        case SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL_AUTO:
                        case SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL:
                            this.setState(OnOffType.OFF);
                            break;
                    }
                    break;
                case SoulissBindingConstants.AUTOMODE_CHANNEL:
                    switch (t1nRawState) {
                        case SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL_AUTO:
                        case SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL_AUTO:
                            this.setStateAutomode(OnOffType.ON);
                            break;
                        case SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL:
                        case SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL:
                            this.setStateAutomode(OnOffType.OFF);
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
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_ON_CMD);
                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_OFF_CMD);
                        }
                    }
                    break;
                case SoulissBindingConstants.AUTOMODE_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T1N_AUTO_CMD);
                        }
                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND((byte) (SoulissBindingProtocolConstants.SOULISS_T1N_TIMED + xSleepTime));
                            // set Off
                            updateState(channelUID, OnOffType.OFF);
                        }
                    }
                    break;

            }
        }
    }

    public void setState(PrimitiveType state) {
        if (state != null) {
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) state);
        }
    }

    public void setStateAutomode(PrimitiveType state) {
        if (state != null) {
            this.updateState(SoulissBindingConstants.AUTOMODE_CHANNEL, (OnOffType) state);
        }
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();

        // update item state only if it is different from previous
        if (t1nRawState != rawState) {
            if (rawState == SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL_AUTO) {
                this.setState(OnOffType.ON);
                this.setStateAutomode(OnOffType.ON);
            } else if (rawState == SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL_AUTO) {
                this.setState(OnOffType.OFF);
                this.setStateAutomode(OnOffType.ON);
            } else if (rawState == SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL) {
                this.setState(OnOffType.ON);
                this.setStateAutomode(OnOffType.OFF);
            } else if (rawState == SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL) {
                this.setState(OnOffType.OFF);
                this.setStateAutomode(OnOffType.OFF);
            }
        }
        t1nRawState = rawState;
    }

    @Override
    public byte getRawState() {
        return t1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        if (bSecureSend) {
            if (bCommand == SoulissBindingProtocolConstants.SOULISS_T1N_ON_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL;
            } else if (bCommand == SoulissBindingProtocolConstants.SOULISS_T1N_OFF_CMD) {
                return SoulissBindingProtocolConstants.SOULISS_T1N_OFF_COIL;
            } else if (bCommand >= SoulissBindingProtocolConstants.SOULISS_T1N_TIMED) {
                // SLEEP
                return SoulissBindingProtocolConstants.SOULISS_T1N_ON_COIL;
            }
        }
        return -1;
    }
}
