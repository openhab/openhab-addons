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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;

/**
 * The {@link SoulissT42Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT42Handler extends SoulissGenericHandler {

    Configuration gwConfigurationMap;

    // private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);
    byte T4nRawState;

    public SoulissT42Handler(Thing _thing) {
        super(_thing);
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getAsString().split(":")[3].equals(SoulissBindingConstants.T4N_REARMALARM_CHANNEL)) {
            if (command instanceof OnOffType) {

                switch (command.toFullString()) {
                    case "ON":
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T4n_ReArm);
                        this.setState(StringType.valueOf(SoulissBindingConstants.T4N_REARMOFF_MESSAGE_CHANNEL));
                        break;
                }

            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thing.getConfiguration();
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }

    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            if (_state instanceof StringType) {
                switch (String.valueOf(_state)) {
                    case SoulissBindingConstants.T4N_ALARMON_MESSAGE_CHANNEL:
                        this.updateState(SoulissBindingConstants.T4N_STATUSALARM_CHANNEL, OnOffType.ON);
                        break;
                    case SoulissBindingConstants.T4N_ALARMOFF_MESSAGE_CHANNEL:
                        this.updateState(SoulissBindingConstants.T4N_STATUSALARM_CHANNEL, OnOffType.OFF);
                        break;
                }
            }
            // // Resetto il tasto di rearm. Questo perchè se premuto non torna da solo in off
            updateState(SoulissBindingConstants.T4N_REARMALARM_CHANNEL, OnOffType.OFF);

            super.setLastStatusStored();
        }
    }

    @Override
    public void setRawState(byte _rawState) {

        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T4nRawState != _rawState) {
            this.setState(getOHState_OnOff_FromSoulissVal(_rawState));
        }
        T4nRawState = _rawState;
    }

    @Override
    public byte getRawState() {
        return T4nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bSecureSend) {
            if (bCmd == SoulissBindingProtocolConstants.Souliss_T4n_ReArm) {
                return SoulissBindingProtocolConstants.Souliss_T4n_Antitheft;
            }
        }
        return -1;
    }
}
