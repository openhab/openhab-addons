/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT41Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT41Handler extends SoulissGenericTypical implements typicalCommonMethods {

    Configuration gwConfigurationMap;

    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);

    public SoulissT41Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {

        } else if (channelUID.getAsString().split(":")[3].equals(SoulissBindingConstants.T41_ONOFFALARM_CHANNEL)) {
            if (command instanceof OnOffType) {

                switch (command.toFullString()) {
                    case "OFF":
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T4n_NotArmed);
                        break;
                    case "ON":
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T4n_Armed);
                        break;
                }

            }
        } else if (channelUID.getAsString().split(":")[3].equals(SoulissBindingConstants.T41_REARMALARM_CHANNEL)) {
            if (command instanceof OnOffType) {

                switch (command.toFullString()) {
                    case "ON":
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T4n_ReArm);
                        this.setState(StringType.valueOf(SoulissBindingConstants.T41_REARMOFF_MESSAGE_CHANNEL));
                        break;
                }

            }
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        updateStatus(ThingStatus.ONLINE);
        // gwConfigurationMap = thing.getConfiguration();

    }

    @Override
    public void setState(PrimitiveType _state) {
        if (_state != null) {
            if (_state instanceof OnOffType) {
                this.updateState(SoulissBindingConstants.T41_ONOFFALARM_CHANNEL, (OnOffType) _state);
            } else if (_state instanceof StringType) {
                switch (String.valueOf(_state)) {
                    case SoulissBindingConstants.T41_ALARMON_MESSAGE_CHANNEL:
                        this.updateState(SoulissBindingConstants.T41_STATUSALARM_CHANNEL, OnOffType.ON);
                        break;
                    case SoulissBindingConstants.T41_REARMOFF_MESSAGE_CHANNEL:
                        this.updateState(SoulissBindingConstants.T41_REARMALARM_CHANNEL, OnOffType.OFF);
                }
            }
            // // Resetto il tasto di rearm. Questo perchè se premuto non torna da solo in off
            this.updateState(SoulissBindingConstants.T41_REARMALARM_CHANNEL, OnOffType.OFF);

            super.setLastStatusStored();

            this.updateThing(this.thing);
        }
    }
}
