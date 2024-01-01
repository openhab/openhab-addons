/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT41Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissT41Handler extends SoulissGenericHandler {

    byte t4nRawState = 0xF;

    public SoulissT41Handler(Thing thing) {
        super(thing);
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            if (channelUID.getId().equals(SoulissBindingConstants.T4N_ONOFFALARM_CHANNEL)) {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T4N_NOT_ARMED);
                    } else if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissProtocolConstants.SOULISS_T4N_ARMED);
                    }
                }
            } else if ((channelUID.getAsString().split(":")[3].equals(SoulissBindingConstants.T4N_REARMALARM_CHANNEL))
                    && (command instanceof OnOffType) && (command.equals(OnOffType.OFF))) {
                commandSEND(SoulissProtocolConstants.SOULISS_T4N_REARM);
                this.setState(StringType.valueOf(SoulissBindingConstants.T4N_REARMOFF_MESSAGE_CHANNEL));
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);

        var configurationMap = getThing().getConfiguration();
        if (configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) configurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    public void setState(PrimitiveType state) {
        if (state instanceof OnOffType onOffState) {
            this.updateState(SoulissBindingConstants.T4N_ONOFFALARM_CHANNEL, onOffState);
        } else if (state instanceof StringType) {
            switch (String.valueOf(state)) {
                case SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL:
                    this.updateState(SoulissBindingConstants.T4N_STATUSALARM_CHANNEL, OnOffType.ON);
                    break;
                case SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL:
                    this.updateState(SoulissBindingConstants.T4N_STATUSALARM_CHANNEL, OnOffType.OFF);
                    break;
                default:
                    break;
            }
        }
        // // Reset the rearm button. This is because if pressed, it does not turn off by itself
        updateState(SoulissBindingConstants.T4N_REARMALARM_CHANNEL, OnOffType.OFF);
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (t4nRawState != rawState) {
            switch (rawState) {
                case SoulissProtocolConstants.SOULISS_T4N_NO_ANTITHEFT:
                    this.setState(OnOffType.OFF);
                    this.setState(StringType.valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                    break;
                case SoulissProtocolConstants.SOULISS_T4N_ANTITHEFT:
                    this.setState(OnOffType.ON);
                    this.setState(StringType.valueOf(SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL));
                    break;
                case SoulissProtocolConstants.SOULISS_T4N_IN_ALARM:
                    this.setState(StringType.valueOf(SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL));
                    break;
                case SoulissProtocolConstants.SOULISS_T4N_ARMED:
                    this.setState(StringType.valueOf(SoulissBindingConstants.T4N_ARMED_MESSAGE_CHANNEL));
                    break;
                default:
                    break;
            }
        }
        t4nRawState = rawState;
    }

    @Override
    public byte getRawState() {
        return t4nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissProtocolConstants.SOULISS_T4N_ARMED) {
                return SoulissProtocolConstants.SOULISS_T4N_ANTITHEFT;
            } else if (bCmd == SoulissProtocolConstants.SOULISS_T4N_NOT_ARMED) {
                return SoulissProtocolConstants.SOULISS_T4N_NO_ANTITHEFT;
            } else if (bCmd >= SoulissProtocolConstants.SOULISS_T4N_REARM) {
                return SoulissProtocolConstants.SOULISS_T4N_ANTITHEFT;
            }
        }
        return -1;
    }
}
